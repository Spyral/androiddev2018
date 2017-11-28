package xyz.sonbn.ircclient.irc;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import xyz.sonbn.ircclient.model.User;

import static android.content.ContentValues.TAG;

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

    private static final int OP_ADD = 1;
    private static final int OP_REMOVE = 2;
    private static final int VOICE_ADD = 3;
    private static final int VOICE_REMOVE = 4;

    // Connection stuff.
    private InputThread _inputThread = null;
    private OutputThread _outputThread = null;
    private String _charset = null;
    private InetAddress _inetAddress = null;
    private Socket _socket = null;

    // Details about the last server that we connected to.
    private String _server = null;
    private int _port = -1;
    private String _password = null;

    // Outgoing message stuff.
    private final Queue _outQueue = new Queue();
    private long _messageDelay = 1000;

    // SASL
    private String saslUsername;
    private String saslPassword;

    // A Hashtable of channels that points to a selfreferential Hashtable of
    // User objects (used to remember which users are in which channels).
    private Hashtable<String, Hashtable<User, User>> _channels = new Hashtable<String, Hashtable<User, User>>();

    // A Hashtable to temporarily store channel topics when we join them
    // until we find out who set that topic.
    private final Hashtable<String, String> _topics = new Hashtable<String, String>();

    // DccManager to process and handle all DCC events.
    private int[] _dccPorts = null;
    private InetAddress _dccInetAddress = null;

    // Default settings for the PircBot.
    private boolean _autoNickChange = false;
    private int _autoNickTries = 1;
    private boolean _useSSL = false;
    private boolean _registered = false;

    private String _name = "PircBot";
    private final List<String> _aliases = new ArrayList<String>();
    private String _nick = _name;
    private String _login = "PircBot";
    private String _version = "PircBot " + VERSION + " Java IRC Bot - www.jibble.org";
    private String _finger = "You ought to be arrested for fingering a bot!";

    private final String _channelPrefixes = "#&+!";

    public IRCProtocol() {}

    public final synchronized void connect(String hostname) throws IOException, IrcException, NickAlreadyInUseException {
        this.connect(hostname, 6667, null);
    }

    public final synchronized void connect(String hostname, int port) throws IOException, IrcException, NickAlreadyInUseException {
        this.connect(hostname, port, null);
    }

    public final synchronized void connect(String hostname, int port, String password) throws IOException, IrcException, NickAlreadyInUseException {
        _registered = false;

        _server = hostname;
        _port = port;
        _password = password;

        if (isConnected()) {
            throw new IOException("The PircBot is already connected to an IRC server.  Disconnect first.");
        }
        _autoNickTries = 1;

        // Don't clear the outqueue - there might be something important in it!

        // Clear everything we may have know about channels.
        this.removeAllChannels();

        // Connect to the server.

        // XXX: PircBot Patch for SSL
        if (_useSSL) {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[] { new NaiveTrustManager() }, null);
                SSLSocketFactory factory = context.getSocketFactory();
                SSLSocket ssocket = (SSLSocket) factory.createSocket(hostname, port);
                setSNIHost(factory, ssocket, hostname);
                ssocket.startHandshake();
                _socket = ssocket;
            }
            catch(Exception e)
            {
                // XXX: It's not really an IOException :)
                throw new IOException("Cannot open SSL socket");
            }
        } else {
            _socket =  new Socket(hostname, port);
        }

        _inetAddress = _socket.getLocalAddress();

        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;

        if (getEncoding() != null) {
            // Assume the specified encoding is valid for this JVM.
            inputStreamReader = new InputStreamReader(_socket.getInputStream(), getEncoding());
            outputStreamWriter = new OutputStreamWriter(_socket.getOutputStream(), getEncoding());
        }
        else {
            // Otherwise, just use the JVM's default encoding.
            inputStreamReader = new InputStreamReader(_socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(_socket.getOutputStream());
        }

        BufferedReader breader = new BufferedReader(inputStreamReader);
        BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);

        // Attempt to join the server.
        if (password != null && !password.equals("")) {
            OutputThread.sendRawLine(this, bwriter, "PASS " + password);
        }
        String nick = this.getName();


        if (saslUsername != null) {
            OutputThread.sendRawLine(this, bwriter, "CAP LS");
            OutputThread.sendRawLine(this, bwriter, "CAP REQ :sasl");
            OutputThread.sendRawLine(this, bwriter, "CAP END");

            OutputThread.sendRawLine(this, bwriter, "AUTHENTICATE PLAIN");

            String authString = saslUsername + "\0" + saslUsername + "\0" + saslPassword;

            String authStringEncoded = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

            while (authStringEncoded.length() >= 400) {
                String toSend = authStringEncoded.substring(0, 400);
                authString = authStringEncoded.substring(400);

                OutputThread.sendRawLine(this, bwriter, "AUTHENTICATE " + toSend);
            }

            if (authStringEncoded.length() > 0) {
                OutputThread.sendRawLine(this, bwriter, "AUTHENTICATE " + authStringEncoded);
            } else {
                OutputThread.sendRawLine(this, bwriter, "AUTHENTICATE +");
            }
        }

        OutputThread.sendRawLine(this, bwriter, "NICK " + nick);
        OutputThread.sendRawLine(this, bwriter, "USER " + this.getLogin() + " 8 * :" + this.getVersion());

        _inputThread = new InputThread(this, _socket, breader, bwriter);

        // XXX: PircBot Patch - Set nick before loop. otherwise we overwrite it in the loop again and again
        //                      But maybe we got a new nickname from the server (bouncers!)
        this.setNick(nick);

        // Read stuff back from the server to see if we connected.
        String line = null;
        line = breader.readLine();

        Log.d("Test", line);

        // XXX: PircBot patch - We are not connected to server if nothing received
        if (line == null) {
            throw new IOException("Could not connect to server");
        }

        // Send the first line to handleLine before the InputThread is started.
        this.handleLine(line);

        // This makes the socket timeout on read operations after 5 minutes.
        // Maybe in some future version I will let the user change this at runtime.
        _socket.setSoTimeout(5 * 60 * 1000);

        // Now start the InputThread to read all other lines from the server.
        _inputThread.start();

        // Now start the outputThread that will be used to send all messages.
        if (_outputThread == null) {
            _outputThread = new OutputThread(this, _outQueue);
            _outputThread.start();
        }

        this.onConnect();
    }

    private void setSNIHost(final SSLSocketFactory factory, final SSLSocket socket, final String hostname) {
        if (factory instanceof android.net.SSLCertificateSocketFactory && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ((android.net.SSLCertificateSocketFactory)factory).setHostname(socket, hostname);
        } else {
            try {
                socket.getClass().getMethod("setHostname", String.class).invoke(socket, hostname);
            } catch (Throwable e) {
                // ignore any error, we just can't set the hostname...
            }
        }
    }

    public void setUseSSL(boolean useSSL)
    {
        _useSSL = useSSL;
    }

    public void setSaslCredentials(String username, String password)
    {
        this.saslUsername = username;
        this.saslPassword = password;
    }

    public void setAutoNickChange(boolean autoNickChange) {
        _autoNickChange = autoNickChange;
    }

    public final void joinChannel(String channel) {
        this.sendRawLine("JOIN " + channel);
    }

    public final void joinChannel(String channel, String key) {
        this.joinChannel(channel + " " + key);
    }

    public final void partChannel(String channel) {
        this.sendRawLine("PART " + channel);
    }

    public void quitServer() {
        this.quitServer("");
    }

    public void quitServer(String reason) {
        this.sendRawLine("QUIT :" + reason);
    }

    public final synchronized void sendRawLine(String line) {
        if (isConnected()) {
            _inputThread.sendRawLine(line);
        }
    }

    public final synchronized void sendRawLineViaQueue(String line) {
        if (line == null) {
            throw new NullPointerException("Cannot send null messages to server");
        }
        if (isConnected()) {
            _outQueue.add(line);
        }
    }

    public final void sendMessage(String target, String message) {
        _outQueue.add("PRIVMSG " + target + " :" + message);
    }

    public final void sendAction(String target, String action) {
        sendCTCPCommand(target, "ACTION " + action);
    }

    public final void sendNotice(String target, String notice) {
        _outQueue.add("NOTICE " + target + " :" + notice);
    }

    public final void sendCTCPCommand(String target, String command) {
        _outQueue.add("PRIVMSG " + target + " :\u0001" + command + "\u0001");
    }

    public final void changeNick(String newNick) {
        this.sendRawLine("NICK " + newNick);
    }

    public final void identify(String password) {
        this.sendRawLine("NICKSERV IDENTIFY " + password);
    }

    public final void setMode(String channel, String mode) {
        this.sendRawLine("MODE " + channel + " " + mode);
    }

    public final void ban(String channel, String hostmask) {
        this.sendRawLine("MODE " + channel + " +b " + hostmask);
    }

    public final void unBan(String channel, String hostmask) {
        this.sendRawLine("MODE " + channel + " -b " + hostmask);
    }

    public final void op(String channel, String nick) {
        this.setMode(channel, "+o " + nick);
    }

    public final void deOp(String channel, String nick) {
        this.setMode(channel, "-o " + nick);
    }

    public final void voice(String channel, String nick) {
        this.setMode(channel, "+v " + nick);
    }

    public final void deVoice(String channel, String nick) {
        this.setMode(channel, "-v " + nick);
    }

    public final void setTopic(String channel, String topic) {
        this.sendRawLine("TOPIC " + channel + " :" + topic);
    }

    public final void kick(String channel, String nick) {
        this.kick(channel, nick, "");
    }

    public final void kick(String channel, String nick, String reason) {
        this.sendRawLine("KICK " + channel + " " + nick + " :" + reason);
    }

    public final void listChannels(String parameters) {
        if (parameters == null) {
            this.sendRawLine("LIST");
        }
        else {
            this.sendRawLine("LIST " + parameters);
        }
    }

    protected void handleLine(String line) throws NickAlreadyInUseException, IOException {
        // Check for server pings.
        if (line.startsWith("PING ")) {
            // Respond to the ping and return immediately.
            this.onServerPing(line.substring(5));
            return;
        }

        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";

        StringTokenizer tokenizer = new StringTokenizer(line);
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
                        String response = line.substring(line.indexOf(errorStr, senderInfo.length()) + 4, line.length());

                        this.processServerResponse(code, response);

                        if (code == 433 && !_registered) {
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
                                _socket.close();
                                _inputThread = null;
                                throw new NickAlreadyInUseException(line);
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
                else {
                    // We don't know what this line means.
                    this.onUnknown(line);
                    // Return from the method;
                    return;
                }

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
        if (command.equals("PRIVMSG") && line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
            String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
            if (request.equals("VERSION")) {
                // VERSION request
                this.onVersion(sourceNick, sourceLogin, sourceHostname, target);
            }
            else if (request.startsWith("ACTION ")) {
                // ACTION request
                this.onAction(sourceNick, sourceLogin, sourceHostname, target, request.substring(7));
            }
            else if (request.startsWith("PING ")) {
                // PING request
                this.onPing(sourceNick, sourceLogin, sourceHostname, target, request.substring(5));
            }
            else if (request.equals("TIME")) {
                // TIME request
                this.onTime(sourceNick, sourceLogin, sourceHostname, target);
            }
            else if (request.equals("FINGER")) {
                // FINGER request
                this.onFinger(sourceNick, sourceLogin, sourceHostname, target);
            }
            else {
                // An unknown CTCP message - ignore it.
                this.onUnknown(line);
            }
        }
        else if (command.equals("PRIVMSG") && _channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            // This is a normal message to a channel.
            this.onMessage(target, sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
        }
        else if (command.equals("PRIVMSG")) {
            // This is a private message to us.
            // XXX PircBot patch to pass target info to privmsg callback
            this.onPrivateMessage(sourceNick, sourceLogin, sourceHostname, target, line.substring(line.indexOf(" :") + 2));
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
            if (sourceNick.equals(this.getNick())) {
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
            this.onNotice(sourceNick, sourceLogin, sourceHostname, target, line.substring(line.indexOf(" :") + 2));
        }
        else if (command.equals("QUIT")) {
            // Someone has quit from the IRC server.

            // XXX: Pircbot Patch - Call onQuit before removing the user. This way we
            //                        are able to know which channels the user was on.
            this.onQuit(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));

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
            this.onKick(target, sourceNick, sourceLogin, sourceHostname, recipient, line.substring(line.indexOf(" :") + 2));
        }
        else if (command.equals("MODE")) {
            // Somebody is changing the mode on a channel or user.
            String mode = line.substring(line.indexOf(target, 2) + target.length() + 1);
            if (mode.startsWith(":")) {
                mode = mode.substring(1);
            }
            this.processMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        }
        else if (command.equals("TOPIC")) {
            // Someone is changing the topic.
            this.onTopic(target, line.substring(line.indexOf(" :") + 2), sourceNick, System.currentTimeMillis(), true);
        }
        else if (command.equals("INVITE")) {
            // Somebody is inviting somebody else into a channel.
            this.onInvite(target, sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
        }
        else {
            // If we reach this point, then we've found something that the PircBot
            // Doesn't currently deal with.
            this.onUnknown(line);
        }

    }


    /**
     * This method is called once the PircBot has successfully connected to
     * the IRC server.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.6
     */
    protected void onConnect() {}

    /**
     * This method is called once the client is registered with the server -
     * meaning the client recieved server code 004.
     *
     * @since Yaaic
     */
    protected void onRegister()
    {
        _registered = true;
    }


    /**
     * This method carries out the actions to be performed when the PircBot
     * gets disconnected.  This may happen if the PircBot quits from the
     * server, or if the connection is unexpectedly lost.
     *  <p>
     * Disconnection from the IRC server is detected immediately if either
     * we or the server close the connection normally. If the connection to
     * the server is lost, but neither we nor the server have explicitly closed
     * the connection, then it may take a few minutes to detect (this is
     * commonly referred to as a "ping timeout").
     *  <p>
     * If you wish to get your IRC bot to automatically rejoin a server after
     * the connection has been lost, then this is probably the ideal method to
     * override to implement such functionality.
     *  <p>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     */
    protected void onDisconnect()
    {
        _registered = false;
    }


    /**
     * This method is called by the PircBot when a numeric response
     * is received from the IRC server.  We use this method to
     * allow PircBot to process various responses from the server
     * before then passing them on to the onServerResponse method.
     *  <p>
     * Note that this method is private and should not appear in any
     * of the javadoc generated documenation.
     *
     * @param code The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     */
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
            User[] users = this.getUsers(channel);
            this.onUserList(channel, users);
        }

        this.onServerResponse(code, response);
    }

    protected void onServerResponse(int code, String response) {}

    protected void onUserList(String channel, User[] users) {}

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {}

    protected void onPrivateMessage(String sender, String login, String hostname, String target, String message) {}

    protected void onAction(String sender, String login, String hostname, String target, String action) {}

    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {}

    protected void onJoin(String channel, String sender, String login, String hostname) {}

    protected void onPart(String channel, String sender, String login, String hostname) {}

    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {}

    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {}

    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {}

    @Deprecated
    protected void onTopic(String channel, String topic) {}

    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {}

    protected void onChannelInfo(String channel, int userCount, String topic) {}

    private final void processMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode) {

        if (_channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            // The mode of a channel is being changed.
            String channel = target;
            StringTokenizer tok = new StringTokenizer(mode);
            String[] params = new String[tok.countTokens()];

            int t = 0;
            while (tok.hasMoreTokens()) {
                params[t] = tok.nextToken();
                t++;
            }

            char pn = ' ';
            int p = 1;

            // All of this is very large and ugly, but it's the only way of providing
            // what the users want :-/
            for (int i = 0; i < params[0].length(); i++) {
                char atPos = params[0].charAt(i);

                if (atPos == '+' || atPos == '-') {
                    pn = atPos;
                }
                else if (atPos == 'o') {
                    if (pn == '+') {
                        this.updateUser(channel, OP_ADD, params[p]);
                        onOp(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    else {
                        this.updateUser(channel, OP_REMOVE, params[p]);
                        onDeop(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 'v') {
                    if (pn == '+') {
                        this.updateUser(channel, VOICE_ADD, params[p]);
                        onVoice(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    else {
                        this.updateUser(channel, VOICE_REMOVE, params[p]);
                        onDeVoice(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 'k') {
                    if (pn == '+') {
                        onSetChannelKey(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    else {
                        onRemoveChannelKey(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 'l') {
                    if (pn == '+') {
                        onSetChannelLimit(channel, sourceNick, sourceLogin, sourceHostname, Integer.parseInt(params[p]));
                        p++;
                    }
                    else {
                        onRemoveChannelLimit(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'b') {
                    if (pn == '+') {
                        onSetChannelBan(channel, sourceNick, sourceLogin, sourceHostname,params[p]);
                    }
                    else {
                        onRemoveChannelBan(channel, sourceNick, sourceLogin, sourceHostname, params[p]);
                    }
                    p++;
                }
                else if (atPos == 't') {
                    if (pn == '+') {
                        onSetTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                    else {
                        onRemoveTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'n') {
                    if (pn == '+') {
                        onSetNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                    else {
                        onRemoveNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'i') {
                    if (pn == '+') {
                        onSetInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                    else {
                        onRemoveInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'm') {
                    if (pn == '+') {
                        onSetModerated(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                    else {
                        onRemoveModerated(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 'p') {
                    if (pn == '+') {
                        onSetPrivate(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                    else {
                        onRemovePrivate(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
                else if (atPos == 's') {
                    if (pn == '+') {
                        onSetSecret(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                    else {
                        onRemoveSecret(channel, sourceNick, sourceLogin, sourceHostname);
                    }
                }
            }

            this.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);
        }
        else {
            // The mode of a user is being changed.
            String nick = target;
            this.onUserMode(nick, sourceNick, sourceLogin, sourceHostname, mode);
        }
    }

    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {}

    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {}

    protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}

    protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {}

    protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {}

    protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}

    protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {}

    protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {}

    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel)  {}

    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + _version + "\u0001");
    }

    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + pingValue + "\u0001");
    }

    protected void onServerPing(String response) {
        this.sendRawLine("PONG " + response);
    }

    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
    }

    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + _finger + "\u0001");
    }

    protected void onUnknown(String line) {
        // And then there were none :)
    }

    protected final void setName(String name) {
        _name = name;
    }

    public final void setAliases(Collection<String> aliases) {
        _aliases.clear();
        _aliases.addAll(aliases);
    }

    private final void setNick(String nick) {
        _nick = nick;
    }


    protected final void setLogin(String login) {
        _login = login;
    }

    protected final void setVersion(String version) {
        _version = version;
    }

    protected final void setFinger(String finger) {
        _finger = finger;
    }

    public final String getName() {
        return _name;
    }

    public final List<String> getAliases() {
        return Collections.unmodifiableList(_aliases);
    }

    public String getNick() {
        return _nick;
    }

    public final String getLogin() {
        return _login;
    }

    public final String getVersion() {
        return _version;
    }

    public final String getFinger() {
        return _finger;
    }

    public final synchronized boolean isConnected() {
        return _inputThread != null && _inputThread.isConnected();
    }

    public final synchronized boolean isRegistered()
    {
        return _registered;
    }

    public final void setMessageDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Cannot have a negative time.");
        }
        _messageDelay = delay;
    }

    public final long getMessageDelay() {
        return _messageDelay;
    }

    public final int getMaxLineLength() {
        return InputThread.MAX_LINE_LENGTH;
    }

    public final int getOutgoingQueueSize() {
        return _outQueue.size();
    }

    public final String getServer() {
        return _server;
    }

    public final int getPort() {
        return _port;
    }

    public final String getPassword() {
        return _password;
    }

    public int[] longToIp(long address) {
        int[] ip = new int[4];
        for (int i = 3; i >= 0; i--) {
            ip[i] = (int) (address % 256);
            address = address / 256;
        }
        return ip;
    }

    public long ipToLong(byte[] address) {
        if (address.length != 4) {
            throw new IllegalArgumentException("byte array must be of length 4");
        }
        long ipNum = 0;
        long multiplier = 1;
        for (int i = 3; i >= 0; i--) {
            int byteVal = (address[i] + 256) % 256;
            ipNum += byteVal*multiplier;
            multiplier *= 256;
        }
        return ipNum;
    }

    public void setEncoding(String charset) throws UnsupportedEncodingException {
        // Just try to see if the charset is supported first...
        "".getBytes(charset);

        _charset = charset;
    }

    public String getEncoding() {
        return _charset;
    }

    public InetAddress getInetAddress() {
        return _inetAddress;
    }

    public void setDccInetAddress(InetAddress dccInetAddress) {
        _dccInetAddress = dccInetAddress;
    }

    public InetAddress getDccInetAddress() {
        return _dccInetAddress;
    }

    public int[] getDccPorts() {
        if (_dccPorts == null || _dccPorts.length == 0) {
            return null;
        }
        // Clone the array to prevent external modification.
        return _dccPorts.clone();
    }

    public void setDccPorts(int[] ports) {
        if (ports == null || ports.length == 0) {
            _dccPorts = null;
        }
        else {
            // Clone the array to prevent external modification.
            _dccPorts = ports.clone();
        }
    }

    @Override
    public boolean equals(Object o) {
        // This probably has the same effect as Object.equals, but that may change...
        if (o instanceof IRCProtocol) {
            IRCProtocol other = (IRCProtocol) o;
            return other == this;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Version{" + _version + "}" +
                " Connected{" + isConnected() + "}" +
                " Server{" + _server + "}" +
                " Port{" + _port + "}" +
                " Password{" + _password + "}";
    }

    public final User[] getUsers(String channel) {
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

    public final String[] getChannels() {
        String[] channels = new String[0];
        synchronized (_channels) {
            channels = new String[_channels.size()];
            Enumeration<String> enumeration = _channels.keys();
            for (int i = 0; i < channels.length; i++) {
                channels[i] = enumeration.nextElement();
            }
        }
        return channels;
    }

    public synchronized void dispose() {
        //System.out.println("disposing...");
        if (_outputThread != null) {
            _outputThread.interrupt();
        }
        if (_inputThread != null) {
            _inputThread.dispose();
        }
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

    private final void removeChannel(String channel) {
        channel = channel.toLowerCase();
        synchronized (_channels) {
            _channels.remove(channel);
        }
    }

    private final void removeAllChannels() {
        synchronized(_channels) {
            _channels = new Hashtable<String, Hashtable<User, User>>();
        }
    }


    private final void updateUser(String channel, int userMode, String nick) {
        channel = channel.toLowerCase();
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            User newUser = null;
            if (users != null) {
                Enumeration<User> enumeration = users.elements();
                while(enumeration.hasMoreElements()) {
                    User userObj = enumeration.nextElement();
                    if (userObj.getNick().equalsIgnoreCase(nick)) {
                        if (userMode == OP_ADD) {
                            if (userObj.hasVoice()) {
                                newUser = new User("@+", nick);
                            }
                            else {
                                newUser = new User("@", nick);
                            }
                        }
                        else if (userMode == OP_REMOVE) {
                            if(userObj.hasVoice()) {
                                newUser = new User("+", nick);
                            }
                            else {
                                newUser = new User("", nick);
                            }
                        }
                        else if (userMode == VOICE_ADD) {
                            if(userObj.isOp()) {
                                newUser = new User("@+", nick);
                            }
                            else {
                                newUser = new User("+", nick);
                            }
                        }
                        else if (userMode == VOICE_REMOVE) {
                            if(userObj.isOp()) {
                                newUser = new User("@", nick);
                            }
                            else {
                                newUser = new User("", nick);
                            }
                        }
                    }
                }
            }
            if (newUser != null) {
                users.put(newUser, newUser);
            }
            else {
                // just in case ...
                newUser = new User("", nick);
                users.put(newUser, newUser);
            }
        }
    }
}
