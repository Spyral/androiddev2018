package vn.edu.usth.irc;

/**
 * Created by Local Boy on 11/28/2017.
 */

public class User {
    private String username, channel = "general", message;
    private int id, user_count;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId(){return id;}

    public void setId(int new_id){this.id = new_id;}

    public int getUserCount(){return user_count;}

    public void setUserCount(int new_user_count){this.user_count = new_user_count;}
}
