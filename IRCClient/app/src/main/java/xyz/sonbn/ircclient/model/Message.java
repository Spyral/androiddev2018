package xyz.sonbn.ircclient.model;

import java.util.Date;

public class Message {
    private String nickname;

    private String content;

    private Date date;

    public Message() {
        date = getDate();
    }

    public Message(String nickname, String content) {
        this.nickname = nickname;
        this.content = content;
        date = getDate();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
