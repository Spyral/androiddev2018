package xyz.sonbn.ircclient.irc;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.User;
import xyz.sonbn.ircclient.util.AppManager;

/**
 * Created by sonbn on 11/25/2017.
 */

public class IRCConnection extends IRCProtocol {
    private final String TAG = "IRCConnection";

    private IRCService mIRCService;
    private final Server mServer;
    private final int mServerId;

    public IRCConnection(IRCService service, int serverId){
        mIRCService = service;
        mServerId = serverId;
        mServer = Realm.getDefaultInstance().where(Server.class).equalTo("id", mServerId).findFirst();
    }

    public void setNickname(String nickname){
        setName(nickname);
    }

    public void setRealName(String realName){
        setVersion(realName);
    }

    @Override
    public void onConnect(){
        mIRCService.sendBroadcast(Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, mServerId));

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServerId,
                mServer.getChannels().first()
        );
        mIRCService.sendBroadcast(intent);
    }

    @Override
    public void onRegister() {
        super.onRegister();

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            // do nothing
        }

        Log.d("IRCConnection", "Join channel " + mServer.getChannels().first());
        joinChannel(mServer.getChannels().first());

        Message infoMessage = new Message("System",mIRCService.getString(R.string.message_login_done));
        AppManager.getInstance().getConversation(mServer.getId(),mServer.getChannels().first()).addMessage(infoMessage);

        Intent intent = Broadcast.createConversationIntent(
                Broadcast.CONVERSATION_MESSAGE,
                mServer.getId(),
                mServer.getChannels().first()
        );

        mIRCService.sendBroadcast(intent);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String text) {
        Message message = new Message(sender, text);
        Conversation conversation = AppManager.getInstance().getConversation(mServer.getId(), channel);

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
        Message message = new Message(sender, text);
        String queryNick = sender;

        Conversation conversation = AppManager.getInstance().getConversation(mServer.getId(), target);
        conversation.addMessage(message);
        Intent intent = Broadcast.createConversationIntent(Broadcast.CONVERSATION_NEW, mServer.getId(), target);
        mIRCService.sendBroadcast(intent);
    }

    public ArrayList<String> getUsersAsStringArray(String channel)
    {
        return getUsers(channel);
    }

    public String getUser(String channel, String nickname)
    {
        ArrayList<String> users = getUsers(channel);
        int mLength = users.size();

        for (int i = 0; i < mLength; i++) {
            if (nickname.equals(users.get(i))) {
                return users.get(i);
            }
        }

        return null;
    }

    @Override
    protected void onServerResponse(int code, String response) {
        if (code == 4){
            onRegister();
        }
    }
}
