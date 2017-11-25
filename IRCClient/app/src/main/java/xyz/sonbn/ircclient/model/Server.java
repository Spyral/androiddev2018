package xyz.sonbn.ircclient.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;


public class Server extends RealmObject {
    private int id, port;
    private String title, host, nickname;
    private User user;
    private RealmList<String> channels;
    private boolean isConnected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RealmList<String> getChannels() {
        return channels;
    }

    public void setChannels(RealmList<String> channels) {
        this.channels = channels;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

}
