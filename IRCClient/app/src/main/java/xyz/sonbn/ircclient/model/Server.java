package xyz.sonbn.ircclient.model;

import android.content.pm.ServiceInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class Server extends RealmObject {
    @PrimaryKey
    private int id;

    @Required
    private String title;

    @Required
    private String host;

    private int port;

    //For identity
    @Required
    private String nickname;

    @Required
    private String realname;

    @Required
    private RealmList<String> autoJoinChannels;

    private int status = Status.DISCONNECTED;

    @Ignore
    private String selected = "";

    @Ignore
    private LinkedHashMap<String, Conversation> conversations = new LinkedHashMap<>();


    public Server() {
        conversations.put(ServerInfo.DEFAULT_NAME, new ServerInfo());
        selected = ServerInfo.DEFAULT_NAME;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public ArrayList<String> getAutoJoinChannels() {
        ArrayList<String> result = new ArrayList<>();
        for (String channel: this.autoJoinChannels){
            result.add(channel);
        }
        return result;
    }

    public void setAutoJoinChannels(ArrayList<String> autoJoinChannels) {
        this.autoJoinChannels = new RealmList<>();
        for (String channel: autoJoinChannels){
            this.autoJoinChannels.add(channel);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDisconnected()
    {
        return status == Status.DISCONNECTED;
    }

    public boolean isConnected()
    {
        return status == Status.CONNECTED;
    }

    public String getSelectedConversation()
    {
        return selected;
    }

    public void setSelectedConversation(String selected)
    {
        this.selected = selected;
    }

    public Collection<Conversation> getConversations() {
        return conversations.values();
    }

    public Conversation getConversation(String name){
        return conversations.get(name.toLowerCase());
    }

    public void addConversation(Conversation conversation)
    {
        conversations.put(conversation.getName().toLowerCase(), conversation);
    }

    public void removeConversation(String name)
    {
        conversations.remove(name.toLowerCase());
    }

    public void clearConversations()
    {
        conversations.clear();

        // reset defaults
        conversations.put(ServerInfo.DEFAULT_NAME, new ServerInfo());
        this.selected = ServerInfo.DEFAULT_NAME;
    }

    public ArrayList<String> getCurrentChannelNames()
    {
        ArrayList<String> channels = new ArrayList<String>();
        Collection<Conversation> mConversations = conversations.values();

        for (Conversation conversation : mConversations) {
            if (conversation.getType() == Conversation.TYPE_CHANNEL) {
                channels.add(conversation.getName());
            }
        }

        return channels;
    }
}
