package xyz.sonbn.ircclient.model;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
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

    @Required
    private String nickname;

    @Required
    private RealmList<String> channels;

    @Required
    private String realname;

    // To do: Authenication


    public Server() {
        port = 6667;
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

    public RealmList<String> getChannels() {
        return channels;
    }

    public ArrayList<String> getChannelsInArrayList(){
        ArrayList<String> arrayListChannels = new ArrayList<>();
        for (String channel: channels) {
            arrayListChannels.add(channel);
        }
        return arrayListChannels;
    }

    public void setChannels(RealmList<String> channels) {
        this.channels = channels;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }
}
