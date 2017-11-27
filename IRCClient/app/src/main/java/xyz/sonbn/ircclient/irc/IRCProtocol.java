package xyz.sonbn.ircclient.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import xyz.sonbn.ircclient.model.User;

/**
 * Created by sonbn on 11/24/2017.
 */

public abstract class IRCProtocol {
    public static final int RPL_LIST = 322;
    public static final int RPL_TOPIC = 332;
    public static final int RPL_TOPICINFO = 333;;
    public static final int RPL_NAMREPLY = 353;
    public static final int RPL_ENDOFNAMES = 366;
    public static final String VERSION = "1.4.6";

    private final String _channelPrefixes = "#&+!";

    private final int DEFAULT_PORT = 6667;

    private String mHostname, mPassword;
    private int mPort;
    private Socket mSocket;
    private InetAddress mInetAddress;
    private long mMessageDelay = 1000;
    private String mName, mVersion;
    private InputThread mInputThread = null;
    private final Queue _outQueue = new Queue();
    private boolean _autoNickChange = false;
    private int _autoNickTries = 1;
    private String _name = "PircBot";
    private String _login = "PircBot";
    private final List<String> _aliases = new ArrayList<String>();
    private String _nick = _name;
    private final Hashtable<String, String> _topics = new Hashtable<String, String>();

    private String _version = "PircBot " + VERSION + " Java IRC Bot - www.jibble.org";
    private String _finger = "You ought to be arrested for fingering a bot!";

    private boolean isRegisterd;

    private Hashtable<String, Hashtable<User, User>> _channels = new Hashtable<String, Hashtable<User, User>>();

    public IRCProtocol() {
    }

    public long getMessageDelay() {
        return mMessageDelay;
    }

    public void setMessageDelay(long messageDelay) {
        if (messageDelay < 0){
            throw new IllegalArgumentException("Time cannot negative");
        }
        mMessageDelay = messageDelay;
    }

    protected void handleCommand(String rawCommand) throws IOException{
        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";

        StringTokenizer tokenizer = new StringTokenizer(rawCommand);
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;

        int exclamation = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (senderInfo.startsWith(":")) {
            if (exclamation > 0 && at > 0 && exclamation < at) {
                sourceNick = senderInfo.substring(1, exclamation);
                sourceLogin = senderInfo.substring(exclamation + 1, at);
                sourceHostname = senderInfo.substring(at + 1);
            }
            else {

                if (tokenizer.hasMoreTokens()) {
                    String token = command;

                    int code = -1;
                    try {
                        code = Integer.parseInt(token);
                    }
                    catch (NumberFormatException e) {
                        // Keep the existing value.
                    }

                    if (code != -1) {
                        String errorStr = token;
                        String response = rawCommand.substring(rawCommand.indexOf(errorStr, senderInfo.length()) + 4, rawCommand.length());

                        this.processServerResponse(code, response);

                        if (code == 433 && !isRegisterd) {
                            if (_autoNickChange) {
                                String oldNick = _nick;

                                List<String> aliases = getAliases();
                                _autoNickTries++;

                                if (_autoNickTries - 1 <= aliases.size()) {
                                    // Try next alias
                                    _nick = aliases.get(_autoNickTries - 2);
                                } else {
                                    // Append a number to the nickname
                                    _nick = getName() + (_autoNickTries - aliases.size());
                                }

                                // Notify ourself about the change
                                this.onNickChange(oldNick, getLogin(), "", _nick);

                                this.sendRawLineViaQueue("NICK " + _nick);
                            }
                            else {
                                mSocket.close();
                                mInputThread = null;
                            }
                        }

                        return;
                    }
                    else {
                        // This is not a server response.
                        // It must be a nick without login and hostname.
                        // (or maybe a NOTICE or suchlike from the server)
                        sourceNick = senderInfo;
                        target = token;

                        // XXX: PircBot Patch - Sometimes there are senderinfos with an ident but no host
                        if (sourceNick.contains("!") && !sourceNick.contains("@")) {
                            String[] chunks = sourceNick.split("!");
                            sourceNick = chunks[0]; // Use the part before the exclamation mark
                        }

                        // XXX: PircBot Patch - (Needed for BIP IRC Proxy)
                        //      If this is a NICK command, use next token as target
                        if (command.equalsIgnoreCase("nick")) {
                            target = tokenizer.nextToken();
                        }
                    }
                }
//                else {
//                    // We don't know what this line means.
//                    this.onUnknown(line);
//                    // Return from the method;
//                    return;
//                }

            }
        }

        command = command.toUpperCase();
        if (sourceNick.startsWith(":")) {
            sourceNick = sourceNick.substring(1);
        }
        if (target == null) {
            target = tokenizer.nextToken();
        }
        if (target.startsWith(":")) {
            target = target.substring(1);
        }

        // Check for CTCP requests.
        if (command.equals("PRIVMSG") && rawCommand.indexOf(":\u0001") > 0 && rawCommand.endsWith("\u0001")) {
            String request = rawCommand.substring(rawCommand.indexOf(":\u0001") + 2, rawCommand.length() - 1);
//            if (request.equals("VERSION")) {
//                // VERSION request
//                this.onVersion(sourceNick, sourceLogin, sourceHostname, target);
//            }
//            else if (request.startsWith("ACTION ")) {
//                // ACTION request
//                this.onAction(sourceNick, sourceLogin, sourceHostname, target, request.substring(7));
//            }
//            else if (request.startsWith("PING ")) {
//                // PING request
//                this.onPing(sourceNick, sourceLogin, sourceHostname, target, request.substring(5));
//            }
//            else if (request.equals("TIME")) {
//                // TIME request
//                this.onTime(sourceNick, sourceLogin, sourceHostname, target);
//            }
//            else if (request.equals("FINGER")) {
//                // FINGER request
//                this.onFinger(sourceNick, sourceLogin, sourceHostname, target);
//            }
//            else {
//                // An unknown CTCP message - ignore it.
//                this.onUnknown(line);
//            }
        }
        else if (command.equals("PRIVMSG") && _channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            // This is a normal message to a channel.
            this.onMessage(target, sourceNick, sourceLogin, sourceHostname, rawCommand.substring(rawCommand.indexOf(" :") + 2));
        }
        else if (command.equals("PRIVMSG")) {
            // This is a private message to us.
            // XXX PircBot patch to pass target info to privmsg callback
            this.onPrivateMessage(sourceNick, sourceLogin, sourceHostname, target, rawCommand.substring(rawCommand.indexOf(" :") + 2));
        }
        else if (command.equals("JOIN")) {
            // Someone is joining a channel.
            String channel = target;
            this.addUser(channel, new User("", sourceNick));
            this.onJoin(channel, sourceNick, sourceLogin, sourceHostname);
        }
        else if (command.equals("PART")) {
            // Someone is parting from a channel.
            this.removeUser(target, sourceNick);
            if (sourceNick.equals((getNick()))) {
                this.removeChannel(target);
            }
            this.onPart(target, sourceNick, sourceLogin, sourceHostname);
        }
        else if (command.equals("NICK")) {
            // Somebody is changing their nick.
            String newNick = target;
            this.renameUser(sourceNick, newNick);
            if (sourceNick.equals(this.getNick())) {
                // Update our nick if it was us that changed nick.
                this.setNick(newNick);
            }
            this.onNickChange(sourceNick, sourceLogin, sourceHostname, newNick);
        }
        else if (command.equals("NOTICE")) {
            // Someone is sending a notice.
            this.onNotice(sourceNick, sourceLogin, sourceHostname, target, rawCommand.substring(rawCommand.indexOf(" :") + 2));
        }
        else if (command.equals("QUIT")) {
            // Someone has quit from the IRC server.

            // XXX: Pircbot Patch - Call onQuit before removing the user. This way we
            //                        are able to know which channels the user was on.
            this.onQuit(sourceNick, sourceLogin, sourceHostname, rawCommand.substring(rawCommand.indexOf(" :") + 2));

            if (sourceNick.equals(this.getNick())) {
                this.removeAllChannels();
            }
            else {
                this.removeUser(sourceNick);
            }
        }
        else if (command.equals("KICK")) {
            // Somebody has been kicked from a channel.
            String recipient = tokenizer.nextToken();
            if (recipient.equals(this.getNick())) {
                this.removeChannel(target);
            }
            this.removeUser(target, recipient);
            this.onKick(target, sourceNick, sourceLogin, sourceHostname, recipient, rawCommand.substring(rawCommand.indexOf(" :") + 2));
        }
        else if (command.equals("MODE")) {
            // Somebody is changing the mode on a channel or user.
            String mode = rawCommand.substring(rawCommand.indexOf(target, 2) + target.length() + 1);
            if (mode.startsWith(":")) {
                mode = mode.substring(1);
            }
//            this.processMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        }
        else if (command.equals("TOPIC")) {
            // Someone is changing the topic.
            this.onTopic(target, rawCommand.substring(rawCommand.indexOf(" :") + 2), sourceNick, System.currentTimeMillis(), true);
        }
        else if (command.equals("INVITE")) {
            // Somebody is inviting somebody else into a channel.
            this.onInvite(target, sourceNick, sourceLogin, sourceHostname, rawCommand.substring(rawCommand.indexOf(" :") + 2));
        }
        else {
            // If we reach this point, then we've found something that the PircBot
            // Doesn't currently deal with.
            this.onUnknown(rawCommand);
        }
    }

    private final void processServerResponse(int code, String response) {

        if (code == RPL_LIST) {
            // This is a bit of information about a channel.
            int firstSpace = response.indexOf(' ');
            int secondSpace = response.indexOf(' ', firstSpace + 1);
            int thirdSpace = response.indexOf(' ', secondSpace + 1);
            int colon = response.indexOf(':');
            String channel = response.substring(firstSpace + 1, secondSpace);
            int userCount = 0;
            try {
                userCount = Integer.parseInt(response.substring(secondSpace + 1, thirdSpace));
            }
            catch (NumberFormatException e) {
                // Stick with the value of zero.
            }
            String topic = response.substring(colon + 1);
            this.onChannelInfo(channel, userCount, topic);
        }
        else if (code == RPL_TOPIC) {
            // This is topic information about a channel we've just joined.
            int firstSpace = response.indexOf(' ');
            int secondSpace = response.indexOf(' ', firstSpace + 1);
            int colon = response.indexOf(':');
            String channel = response.substring(firstSpace + 1, secondSpace);
            String topic = response.substring(colon + 1);

            _topics.put(channel, topic);

            // For backwards compatibility only - this onTopic method is deprecated.
            this.onTopic(channel, topic);
        }
        else if (code == RPL_TOPICINFO) {
            StringTokenizer tokenizer = new StringTokenizer(response);
            tokenizer.nextToken();
            String channel = tokenizer.nextToken();
            String setBy = tokenizer.nextToken();
            long date = 0;
            try {
                date = Long.parseLong(tokenizer.nextToken()) * 1000;
            }
            catch (NumberFormatException e) {
                // Stick with the default value of zero.
            }

            String topic = _topics.get(channel);
            _topics.remove(channel);

            this.onTopic(channel, topic, setBy, date, false);
        }
        else if (code == RPL_NAMREPLY) {
            // This is a list of nicks in a channel that we've just joined.
            int channelEndIndex = response.indexOf(" :");
            String channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);

            StringTokenizer tokenizer = new StringTokenizer(response.substring(response.indexOf(" :") + 2));
            while (tokenizer.hasMoreTokens()) {
                String nick = tokenizer.nextToken();
                String prefix = "";
                if (nick.startsWith("@")) {
                    // User is an operator in this channel.
                    prefix = "@";
                }
                else if (nick.startsWith("+")) {
                    // User is voiced in this channel.
                    prefix = "+";
                }
                else if (nick.startsWith(".")) {
                    // Some wibbly status I've never seen before...
                    prefix = ".";
                }
                // XXX: PircBot Patch - Recognize % as prefix - Often used as "half-operator" prefix
                else if (nick.startsWith("%")) {
                    prefix = "%";
                }
                nick = nick.substring(prefix.length());
                this.addUser(channel, new User(prefix, nick));
            }
        }
        else if (code == RPL_ENDOFNAMES) {
            // This is the end of a NAMES list, so we know that we've got
            // the full list of users in the channel that we just joined.
            String channel = response.substring(response.indexOf(' ') + 1, response.indexOf(" :"));
            User[] users = this.getUsersArray(channel);
            this.onUserList(channel, users);
        }

        this.onServerResponse(code, response);
    }

    //Overloading connect
    public final synchronized void connect(String hostname) throws IOException{
        connect(hostname, DEFAULT_PORT, null);
    }

    public final synchronized void connect(String hostname, int port) throws IOException{
        connect(hostname, port, null);
    }

    public final synchronized void connect(String hostname, int port, String password) throws IOException{
        mHostname = hostname;
        mPort = port;
        mPassword = password;

        mSocket = new Socket(mHostname, mPort);

        mInetAddress = mSocket.getLocalAddress();

        InputStreamReader inputStreamReader = new InputStreamReader(mSocket.getInputStream());
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        mInputThread = new InputThread(this, mSocket, bufferedReader, bufferedWriter);

        onConnect();
    }

    public final void joinChannel(String channel) {
        this.sendRawLine("JOIN " + channel);
    }

    public final void joinChannel(String channel, String key) {
        this.joinChannel(channel + " " + key);
    }

    public final synchronized void sendRawLine(String line){
        mInputThread.sendRawLine(line);
    }

    public final synchronized void sendRawLineViaQueue(String line) {
        if (line == null) {
            throw new NullPointerException("Cannot send null messages to server");
        }
        _outQueue.add(line);
    }

    public final int getMaxLineLength() {
        return InputThread.MAX_LINE_LENGTH;
    }

    protected final void setName(String name){
        mName = name;
    }

    protected final void setVersion(String version){
        mVersion = version;
    }

    private final void setNick(String nick) {
        _nick = nick;
    }

    protected void onConnect() {}

    protected void onRegister()
    {
        isRegisterd = true;
    }

    public final ArrayList<String> getUsers(String channel) {
        channel = channel.toLowerCase();
        ArrayList<String> userArray = new ArrayList<>();
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users != null) {
                Enumeration<User> enumeration = users.elements();
                for (int i = 0; i < users.size(); i++) {
                    User user = enumeration.nextElement();
                    userArray.add(user.getNick());
                }
            }
        }
        return userArray;
    }

    protected void onServerResponse(int code, String response) {}


    /**
     * This method is called when we receive a user list from the server
     * after joining a channel.
     *  <p>
     * Shortly after joining a channel, the IRC server sends a list of all
     * users in that channel. The PircBot collects this information and
     * calls this method as soon as it has the full list.
     *  <p>
     * To obtain the nick of each user in the channel, call the getNick()
     * method on each User object in the array.
     *  <p>
     * At a later time, you may call the getUsers method to obtain an
     * up to date list of the users in the channel.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 1.0.0
     *
     * @param channel The name of the channel.
     * @param users An array of User objects belonging to this channel.
     *
     * @see User
     */
    protected void onUserList(String channel, User[] users) {}


    /**
     * This method is called whenever a message is sent to a channel.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel to which the message was sent.
     * @param sender The nick of the person who sent the message.
     * @param login The login of the person who sent the message.
     * @param hostname The hostname of the person who sent the message.
     * @param message The actual message sent to the channel.
     */
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {}


    /**
     * This method is called whenever an ACTION is sent from a user.  E.g.
     * such events generated by typing "/me goes shopping" in most IRC clients.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sender The nick of the user that sent the action.
     * @param login The login of the user that sent the action.
     * @param hostname The hostname of the user that sent the action.
     * @param target The target of the action, be it a channel or our nick.
     * @param action The action carried out by the user.
     */
    protected void onAction(String sender, String login, String hostname, String target, String action) {}


    /**
     * This method is called whenever we receive a notice.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick The nick of the user that sent the notice.
     * @param sourceLogin The login of the user that sent the notice.
     * @param sourceHostname The hostname of the user that sent the notice.
     * @param target The target of the notice, be it our nick or a channel name.
     * @param notice The notice message.
     */
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {}


    /**
     * This method is called whenever someone (possibly us) joins a channel
     * which we are on.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel which somebody joined.
     * @param sender The nick of the user who joined the channel.
     * @param login The login of the user who joined the channel.
     * @param hostname The hostname of the user who joined the channel.
     */
    protected void onJoin(String channel, String sender, String login, String hostname) {}


    /**
     * This method is called whenever someone (possibly us) parts a channel
     * which we are on.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel which somebody parted from.
     * @param sender The nick of the user who parted from the channel.
     * @param login The login of the user who parted from the channel.
     * @param hostname The hostname of the user who parted from the channel.
     */
    protected void onPart(String channel, String sender, String login, String hostname) {}

    /**
     * This method is called whenever someone (possibly us) is kicked from
     * any of the channels that we are in.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel from which the recipient was kicked.
     * @param kickerNick The nick of the user who performed the kick.
     * @param kickerLogin The login of the user who performed the kick.
     * @param kickerHostname The hostname of the user who performed the kick.
     * @param recipientNick The unfortunate recipient of the kick.
     * @param reason The reason given by the user who performed the kick.
     */
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {}


    /**
     * This method is called whenever someone (possibly us) quits from the
     * server.  We will only observe this if the user was in one of the
     * channels to which we are connected.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick The nick of the user that quit from the server.
     * @param sourceLogin The login of the user that quit from the server.
     * @param sourceHostname The hostname of the user that quit from the server.
     * @param reason The reason given for quitting the server.
     */
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {}


    /**
     * This method is called whenever a user sets the topic, or when
     * PircBot joins a new channel and discovers its topic.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel that the topic belongs to.
     * @param topic The topic for the channel.
     *
     * @deprecated As of 1.2.0, replaced by {@link #onTopic(String,String,String,long,boolean)}
     */
    @Deprecated
    protected void onTopic(String channel, String topic) {}


    /**
     * This method is called whenever a user sets the topic, or when
     * PircBot joins a new channel and discovers its topic.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel that the topic belongs to.
     * @param topic The topic for the channel.
     * @param setBy The nick of the user that set the topic.
     * @param date When the topic was set (milliseconds since the epoch).
     * @param changed True if the topic has just been changed, false if
     *                the topic was already there.
     *
     */
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {}


    /**
     * After calling the listChannels() method in PircBot, the server
     * will start to send us information about each channel on the
     * server.  You may override this method in order to receive the
     * information about each channel as soon as it is received.
     *  <p>
     * Note that certain channels, such as those marked as hidden,
     * may not appear in channel listings.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The name of the channel.
     * @param userCount The number of users visible in this channel.
     * @param topic The topic for this channel.
     */
    protected void onChannelInfo(String channel, int userCount, String topic) {}

    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}


    /**
     * Called when the mode of a user is set.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 1.2.0
     *
     * @param targetNick The nick that the mode operation applies to.
     * @param sourceNick The nick of the user that set the mode.
     * @param sourceLogin The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode The mode that has been set.
     *
     */
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}



    /**
     * Called when a user (possibly us) gets granted operator status for a channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient The nick of the user that got 'opped'.
     */
    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}


    /**
     * Called when a user (possibly us) gets operator status taken away.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient The nick of the user that got 'deopped'.
     */
    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}


    /**
     * Called when a user (possibly us) gets voice status granted in a channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient The nick of the user that got 'voiced'.
     */
    protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}


    /**
     * Called when a user (possibly us) gets voice status removed.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient The nick of the user that got 'devoiced'.
     */
    protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}


    /**
     * Called when a channel key is set.  When the channel key has been set,
     * other users may only join that channel if they know the key.  Channel keys
     * are sometimes referred to as passwords.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param key The new key for the channel.
     */
    protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}


    /**
     * Called when a channel key is removed.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param key The key that was in use before the channel key was removed.
     */
    protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}


    /**
     * Called when a user limit is set for a channel.  The number of users in
     * the channel cannot exceed this limit.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param limit The maximum number of users that may be in this channel at the same time.
     */
    protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {}


    /**
     * Called when the user limit is removed for a channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a user (possibly us) gets banned from a channel.  Being
     * banned from a channel prevents any user with a matching hostmask from
     * joining the channel.  For this reason, most bans are usually directly
     * followed by the user being kicked :-)
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param hostmask The hostmask of the user that has been banned.
     */
    protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}


    /**
     * Called when a hostmask ban is removed from a channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param hostmask
     */
    protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}


    /**
     * Called when topic protection is enabled for a channel.  Topic protection
     * means that only operators in a channel may change the topic.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when topic protection is removed for a channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is set to only allow messages from users that
     * are in the channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is set to allow messages from any user, even
     * if they are not actually in the channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is set to 'invite only' mode.  A user may only
     * join the channel if they are invited by someone who is already in the
     * channel.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel has 'invite only' removed.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is set to 'moderated' mode.  If a channel is
     * moderated, then only users who have been 'voiced' or 'opped' may speak
     * or change their nicks.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel has moderated mode removed.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is marked as being in private mode.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is marked as not being in private mode.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel is set to be in 'secret' mode.  Such channels
     * typically do not appear on a server's channel listing.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when a channel has 'secret' mode removed.
     *  <p>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param channel The channel in which the mode change took place.
     * @param sourceNick The nick of the user that performed the mode change.
     * @param sourceLogin The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     */
    protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}


    /**
     * Called when we are invited to a channel by a user.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.5
     *
     * @param targetNick The nick of the user being invited - should be us!
     * @param sourceNick The nick of the user that sent the invitation.
     * @param sourceLogin The login of the user that sent the invitation.
     * @param sourceHostname The hostname of the user that sent the invitation.
     * @param channel The channel that we're being invited to.
     */
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel)  {}

    /**
     * This method is called whenever we receive a VERSION request.
     * This abstract implementation responds with the PircBot's _version string,
     * so if you override this method, be sure to either mimic its functionality
     * or to call super.onVersion(...);
     *
     * @param sourceNick The nick of the user that sent the VERSION request.
     * @param sourceLogin The login of the user that sent the VERSION request.
     * @param sourceHostname The hostname of the user that sent the VERSION request.
     * @param target The target of the VERSION request, be it our nick or a channel name.
     */
    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + _version + "\u0001");
    }


    /**
     * This method is called whenever we receive a PING request from another
     * user.
     *  <p>
     * This abstract implementation responds correctly, so if you override this
     * method, be sure to either mimic its functionality or to call
     * super.onPing(...);
     *
     * @param sourceNick The nick of the user that sent the PING request.
     * @param sourceLogin The login of the user that sent the PING request.
     * @param sourceHostname The hostname of the user that sent the PING request.
     * @param target The target of the PING request, be it our nick or a channel name.
     * @param pingValue The value that was supplied as an argument to the PING command.
     */
    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + pingValue + "\u0001");
    }


    /**
     * The actions to perform when a PING request comes from the server.
     *  <p>
     * This sends back a correct response, so if you override this method,
     * be sure to either mimic its functionality or to call
     * super.onServerPing(response);
     *
     * @param response The response that should be given back in your PONG.
     */
    protected void onServerPing(String response) {
        this.sendRawLine("PONG " + response);
    }


    /**
     * This method is called whenever we receive a TIME request.
     *  <p>
     * This abstract implementation responds correctly, so if you override this
     * method, be sure to either mimic its functionality or to call
     * super.onTime(...);
     *
     * @param sourceNick The nick of the user that sent the TIME request.
     * @param sourceLogin The login of the user that sent the TIME request.
     * @param sourceHostname The hostname of the user that sent the TIME request.
     * @param target The target of the TIME request, be it our nick or a channel name.
     */
    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
    }


    /**
     * This method is called whenever we receive a FINGER request.
     *  <p>
     * This abstract implementation responds correctly, so if you override this
     * method, be sure to either mimic its functionality or to call
     * super.onFinger(...);
     *
     * @param sourceNick The nick of the user that sent the FINGER request.
     * @param sourceLogin The login of the user that sent the FINGER request.
     * @param sourceHostname The hostname of the user that sent the FINGER request.
     * @param target The target of the FINGER request, be it our nick or a channel name.
     */
    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + _finger + "\u0001");
    }


    /**
     * This method is called whenever we receive a line from the server that
     * the PircBot has not been programmed to recognise.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param line The raw line that was received from the server.
     */
    protected void onUnknown(String line) {
        // And then there were none :)
    }

    public final User[] getUsersArray(String channel) {
        channel = channel.toLowerCase();
        User[] userArray = new User[0];
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users != null) {
                userArray = new User[users.size()];
                Enumeration<User> enumeration = users.elements();
                for (int i = 0; i < userArray.length; i++) {
                    User user = enumeration.nextElement();
                    userArray[i] = user;
                }
            }
        }
        return userArray;
    }

    public final void sendMessage(String target, String message) {
        _outQueue.add("PRIVMSG " + target + " :" + message);
    }



    public final List<String> getAliases() {
        return Collections.unmodifiableList(_aliases);
    }

    public final String getName() {
        return _name;
    }

    public final String getLogin() {
        return _login;
    }

    public String getNick() {
        return _nick;
    }

    private final void addUser(String channel, User user) {
        channel = channel.toLowerCase();
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users == null) {
                users = new Hashtable<User, User>();
                _channels.put(channel, users);
            }
            users.put(user, user);
        }
    }


    /**
     * Remove a user from the specified channel in our memory.
     */
    private final User removeUser(String channel, String nick) {
        channel = channel.toLowerCase();
        User user = new User("", nick);
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users != null) {
                return users.remove(user);
            }
        }
        return null;
    }


    /**
     * Remove a user from all channels in our memory.
     */
    private final void removeUser(String nick) {
        synchronized (_channels) {
            Enumeration<String> enumeration = _channels.keys();
            while (enumeration.hasMoreElements()) {
                String channel = enumeration.nextElement();
                this.removeUser(channel, nick);
            }
        }
    }


    /**
     * Rename a user if they appear in any of the channels we know about.
     */
    private final void renameUser(String oldNick, String newNick) {
        synchronized (_channels) {
            Enumeration<String> enumeration = _channels.keys();
            while (enumeration.hasMoreElements()) {
                String channel = enumeration.nextElement();
                User user = this.removeUser(channel, oldNick);
                if (user != null) {
                    user = new User(user.getPrefix(), newNick);
                    this.addUser(channel, user);
                }
            }
        }
    }


    /**
     * Removes an entire channel from our memory of users.
     */
    private final void removeChannel(String channel) {
        channel = channel.toLowerCase();
        synchronized (_channels) {
            _channels.remove(channel);
        }
    }


    /**
     * Removes all channels from our memory of users.
     */
    private final void removeAllChannels() {
        synchronized(_channels) {
            _channels = new Hashtable<String, Hashtable<User, User>>();
        }
    }

    protected void onPrivateMessage(String sender, String login, String hostname, String target, String message) {}

    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {}
}
