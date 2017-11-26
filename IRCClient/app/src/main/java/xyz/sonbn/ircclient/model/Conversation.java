package xyz.sonbn.ircclient.model;

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

    private ArrayList<Message> mMessages;

    public Conversation() {
        mMessages = new ArrayList<>();
    }

    public Conversation(int serverId, String channel) {
        mServerId = serverId;
        mChannel = channel;
        mMessages = new ArrayList<>();
    }

    public void addMessage(Message message){
        mMessages.add(message);
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
