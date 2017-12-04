package vn.edu.usth.irc;

/**
 * Created by Local Boy on 12/3/2017.
 */

public final class Utils {
    public static String ipAddress = "192.168.208.109";
    private static int newestMessIdLocal = 63;
    private static int newestMessIdServer;
    public static User user;

    public static void setupUserInfo(String username) {
        if (user == null) {
            user = new User(username);
        } else {
            user.setUsername(username);
        }
    }

    public static int getNewestMessIdLocal() {
        return newestMessIdLocal;
    }

    public static void setNewestMessIdLocal(int newestMessIdLocal) {
        Utils.newestMessIdLocal = newestMessIdLocal;
    }

    public static int getNewestMessIdServer() {
        return newestMessIdServer;
    }

    public static void setNewestMessIdServer(int newestMessIdServer) {
        Utils.newestMessIdServer = newestMessIdServer;
    }
}
