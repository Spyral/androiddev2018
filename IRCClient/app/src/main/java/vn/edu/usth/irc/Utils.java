package vn.edu.usth.irc;

/**
 * Created by Local Boy on 12/3/2017.
 */

public final class Utils {
    public static User user;

    public static void setupUserInfo(String username) {
        if (user == null) {
            user = new User(username);
        } else {
            user.setUsername(username);
        }
    }
}
