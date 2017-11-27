package vn.edu.usth.irc;

/**
 * Created by Local Boy on 11/27/2017.
 */

public class Chat {
    private String user, content;

    public Chat(String user, String content) {
        this.user = user;
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
