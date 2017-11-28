package xyz.sonbn.ircclient.irc;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.command.CommandParser;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Channel;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Query;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.ServerInfo;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.model.User;
import xyz.sonbn.ircclient.util.AppManager;

import static android.content.ContentValues.TAG;

public class IRCConnection extends IRCProtocol {
    private static final String TAG = "IRCConnection";

    private IRCService mIRCService;
    private final Server mServer;
    private ArrayList<String> autojoinChannels;
    private Realm mRealm;

    private boolean isQuitting = false;
    private boolean disposeRequested = false;
    private final Object isQuittingLock = new Object();

    public IRCConnection(IRCService service, int serverId){
        mIRCService = service;
        mRealm = Realm.getDefaultInstance();
        mServer = mRealm.where(Server.class).equalTo("id", serverId).findFirst();
    }

    public void setNickname(String nickname){
        setName(nickname);
    }

    public void setRealName(String realName){
        setVersion(realName);
    }

    public void setAutojoinChannels(ArrayList<String> channels)
    {
        autojoinChannels = channels;
    }

    @Override
    protected void onVersion(String sourceNick, String sourceLogin,    String sourceHostname, String target)
    {
        this.sendRawLine(
                "NOTICE " + sourceNick + " :\u0001VERSION " +
                        "IRCClient" +
                        "\u0001"
        );
    }

    @Override
    public void onConnect(){
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mServer.setStatus(Status.CONNECTED);
            }
        });

        mIRCService.sendBroadcast(Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, mServer.getId()));

        Message message = new Message(mIRCService.getString(R.string.message_connected, mServer.getTitle()));
        message.setColor(Message.COLOR_GREEN);
        mServer.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

        Message infoMessage = new Message(mIRCService.getString(R.string.message_now_login));
        infoMessage.setColor(Message.COLOR_GREY);
        mServer.getConversation(ServerInfo.DEFAULT_NAME).addMessage(infoMessage);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                ServerInfo.DEFAULT_NAME
        );

        mIRCService.sendBroadcast(intent);
    }

    @Override
    public void onRegister() {
        super.onRegister();

//        try {
//            Thread.sleep(1000);
//        } catch(InterruptedException e) {
//            // do nothing
//        }

        // join channels
        joinChannel("#straifur");
        if (autojoinChannels != null) {
            for (String channel : autojoinChannels) {
                // Add support for channel keys
                joinChannel(channel);
            }
        } else {
            for (String channel : mServer.getAutoJoinChannels()) {
                Log.d(TAG, "onRegister  : " + channel);

                joinChannel(channel);
            }
        }

        Message infoMessage = new Message(mIRCService.getString(R.string.message_login_done));
        infoMessage.setColor(Message.COLOR_GREY);
        mServer.getConversation(ServerInfo.DEFAULT_NAME).addMessage(infoMessage);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                ServerInfo.DEFAULT_NAME
        );

        mIRCService.sendBroadcast(intent);
    }

    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action)
    {
        Conversation conversation;

        Message message = new Message(sender + " " + action);
        message.setIcon(R.drawable.action);

        String queryNick = target;
        if (queryNick.equals(this.getNick())) {
            // We are the target - this is an action in a query
            queryNick = sender;
        }
        conversation = mServer.getConversation(queryNick);

        if (conversation == null) {
            // Open a query if there's none yet
            conversation = new Query(queryNick);
            mServer.addConversation(conversation);
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_NEW,
                    mServer.getId(),
                    queryNick
            );
            mIRCService.sendBroadcast(intent);
        } else {
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    mServer.getId(),
                    queryNick
            );
            mIRCService.sendBroadcast(intent);
        }

        if (sender.equals(this.getNick())) {
            // Don't notify for something sent in our name
            return;
        }
    }

    @Override
    protected void onDeop(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(mIRCService.getString(R.string.message_deop, sourceNick, recipient));
        message.setIcon(R.drawable.op);
        message.setColor(Message.COLOR_BLUE);
        mServer.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                target
        );

        mIRCService.sendBroadcast(intent);
    }

    @Override
    protected void onDeVoice(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(mIRCService.getString(R.string.message_devoice, sourceNick, recipient));
        message.setColor(Message.COLOR_BLUE);
        message.setIcon(R.drawable.voice);
        mServer.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                target
        );

        mIRCService.sendBroadcast(intent);
    }

    @Override
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String target)
    {
        if (targetNick.equals(this.getNick())) {
            // We are invited
            Message message = new Message(mIRCService.getString(R.string.message_invite_you, sourceNick, target));
            mServer.getConversation(mServer.getSelectedConversation()).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    mServer.getId(),
                    mServer.getSelectedConversation()
            );
            mIRCService.sendBroadcast(intent);
        } else {
            // Someone is invited
            Message message = new Message(mIRCService.getString(R.string.message_invite_someone, sourceNick, targetNick, target));
            mServer.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    mServer.getId(),
                    target
            );
            mIRCService.sendBroadcast(intent);
        }
    }

    @Override
    protected void onJoin(String target, String sender, String login, String hostname)
    {
        if (sender.equalsIgnoreCase(getNick()) && mServer.getConversation(target) == null) {
            // We joined a new channel
            Conversation conversation = new Channel(target);
            mServer.addConversation(conversation);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_NEW,
                    mServer.getId(),
                    target
            );
            mIRCService.sendBroadcast(intent);
        }
    }

    @Override
    protected void onKick(String target, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
    {
        if (recipientNick.equals(getNick())) {
            // We are kicked
            mServer.removeConversation(target);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_REMOVE,
                    mServer.getId(),
                    target
            );
            mIRCService.sendBroadcast(intent);
        } else {
            Message message = new Message(mIRCService.getString(R.string.message_kick, kickerNick, recipientNick));
            message.setColor(Message.COLOR_GREEN);
            mServer.getConversation(target).addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    mServer.getId(),
                    target
            );
            mIRCService.sendBroadcast(intent);
        }
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String text) {
        Message message = new Message(text, sender);
        Conversation conversation = mServer.getConversation(channel);

        Log.d(TAG, "On Message" + text + " " + sender);

        conversation.addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                channel
        );
        mIRCService.sendBroadcast(intent);
    }

    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String target, String text) {
        Message message = new Message("<" + sender + "> " + text);
        String queryNick = sender;

        if (queryNick.equals(this.getNick())) {
            queryNick = target;
        }
        Conversation conversation = mServer.getConversation(queryNick);

        if (conversation == null) {
            // Open a query if there's none yet
            conversation = new Query(queryNick);
            conversation.addMessage(message);
            mServer.addConversation(conversation);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_NEW,
                    mServer.getId(),
                    queryNick
            );
            mIRCService.sendBroadcast(intent);
        } else {
            conversation.addMessage(message);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_MESSAGE,
                    mServer.getId(),
                    queryNick
            );
            mIRCService.sendBroadcast(intent);
        }

        if (sender.equals(this.getNick())) {
            // Don't notify for something sent in our name
            return;
        }
    }

    @Override
    protected void onOp(String target, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        Message message = new Message(mIRCService.getString(R.string.message_op, sourceNick, recipient));
        message.setColor(Message.COLOR_BLUE);
        message.setIcon(R.drawable.op);
        mServer.getConversation(target).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                target
        );
        mIRCService.sendBroadcast(intent);
    }

    /**
     * On Part
     */
    @Override
    protected void onPart(String target, String sender, String login, String hostname)
    {
        if (sender.equals(getNick())) {
            // We parted a channel
            mServer.removeConversation(target);

            Intent intent = Broadcast.createConversationIntent(
                    Broadcast.CONVERSATION_REMOVE,
                    mServer.getId(),
                    target
            );
            mIRCService.sendBroadcast(intent);
        }
    }

    @Override
    protected void onServerResponse(int code, String response)
    {
        Log.d(TAG, "onServerResponse" + code + " " + response);
        if (code == 4) {
            // User has registered with the server
            onRegister();
            return;
        }
        if (code == 372 || code == 375) {
            return;
        }
        if (code == 376) {
            Message motdMessage = new Message(mIRCService.getString(R.string.message_motd_suppressed));
            motdMessage.setColor(Message.COLOR_GREY);
            mServer.getConversation(ServerInfo.DEFAULT_NAME).addMessage(motdMessage);
            return;
        }

        if (code >= 200 && code < 300) {
            // Skip 2XX responses
            return;
        }

        if (code == 353 || code == 366 || code == 332 || code == 333) {
            return;
        }

        if (code < 10) {
            // Skip server info
            return;
        }

        // Currently disabled... to much text
        Message message = new Message(response);
        message.setColor(Message.COLOR_GREY);
        mServer.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                ServerInfo.DEFAULT_NAME
        );
        mIRCService.sendBroadcast(intent);
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
    {
        if (sourceNick.equals(this.getNick())) {
            return;
        }
    }

    @Override
    public void onTopic(String target, String topic, String setBy, long date, boolean changed)
    {
        if (changed) {
            Message message = new Message(mIRCService.getString(R.string.message_topic_set, setBy, topic));
            message.setColor(Message.COLOR_YELLOW);
            mServer.getConversation(target).addMessage(message);
        } else {
            Message message = new Message(mIRCService.getString(R.string.message_topic, topic));
            message.setColor(Message.COLOR_YELLOW);
            mServer.getConversation(target).addMessage(message);
        }

        // remember channel's topic
        ((Channel) mServer.getConversation(target)).setTopic(topic);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                target
        );
        mIRCService.sendBroadcast(intent);

        // update the displayed conversation title if necessary
        intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_TOPIC,
                mServer.getId(),
                target
        );
        mIRCService.sendBroadcast(intent);
    }

    @Override
    public void dispose()
    {
        synchronized(isQuittingLock) {
            if (isQuitting) {
                disposeRequested = true;
            } else {
                super.dispose();
            }
        }
    }

    private Vector<String> getChannelsByNickname(String nickname)
    {
        Vector<String> channels = new Vector<String>();
        String[] channelArray = getChannels();

        for (String channel : channelArray) {
            User[] userArray = getUsers(channel);
            for (User user : userArray) {
                if (user.getNick().equals(nickname)) {
                    channels.add(channel);
                    break;
                }
            }
        }

        return channels;
    }

    @Override
    public void quitServer(final String message)
    {
        synchronized(isQuittingLock) {
            isQuitting = true;
        }

        new Thread() {
            @Override
            public void run() {
                superClassQuitServer(message);
            }
        }.start();
    }

    private final void superClassQuitServer(String message)
    {
        super.quitServer(message);
    }
}
