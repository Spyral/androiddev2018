package xyz.sonbn.ircclient.model;

import android.util.Log;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by sonbn on 11/22/2017.
 */

public class Conversation {
    private int mServerId;

    private String mChannel;

    private final ArrayList<Message> mMessages;

    public Conversation() {
        mMessages = new ArrayList<>();
    }

    public Conversation(int serverId, String channel) {
        mServerId = serverId;
        mChannel = channel;
        mMessages = new ArrayList<>();
        mMessages.add(new Message("sonbn", "Hi"));
    }

    public void addMessage(Message message){
        mMessages.add(message);
        Log.d("Conversation", mMessages.hashCode() + "");
    }

    public ArrayList<Message> getMessages(){
        Log.d("Conversation-2", mMessages.hashCode() + "");
        return mMessages;
    }

    public int getServerId() {
        return mServerId;
    }

    public void setServerId(int serverId) {
        mServerId = serverId;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setChannel(String channel) {
        mChannel = channel;
    }
}
