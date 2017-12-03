package vn.edu.usth.irc;

/**
 * Created by Local Boy on 12/3/2017.
 */

public final class Ultils {
    public static User user;

    public static void setupUserInfo(String username, String realname) {
        if (user == null) {
            user = new User(username, realname);
        } else {
            user.setUsername(username);
            user.setRealname(realname);
        }
    }
}
