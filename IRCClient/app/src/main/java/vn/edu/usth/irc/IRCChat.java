package vn.edu.usth.irc;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.irc.Model.Server;

/**
 * Created by Minu'aHYHY on 28-Nov-17.
 */

public class IRCChat {
    private static IRCChat instance;

    private SparseArray<Server> servers;
    private boolean serversLoaded = false;

    /**
     * Private constructor, you may want to use static getInstance()
     */
    private IRCChat()
    {
        servers = new SparseArray<Server>();
    }

    /**
     * Load servers from database
     *
     * @param context
     */
//    public void loadServers(Context context)
//    {
//        if (!serversLoaded) {
//            Database db = new Database(context);
//            servers = db.getServers();
//            db.close();
//
//            serversLoaded = true;
//        }
//    }

    /**
     * Get global Yaaic instance
     *
     * @return the global Yaaic instance
     */
    public static IRCChat getInstance()
    {
        if (instance == null) {
            instance = new IRCChat();
        }

        return instance;
    }

    /**
     * Get server by id
     *
     * @return Server object with given unique id
     */
    public Server getServerById(int serverId)
    {
        return servers.get(serverId);
    }

    /**
     * Remove server with given unique id from list
     *
     * @param serverId
     */
    public void removeServerById(int serverId)
    {
        servers.remove(serverId);
    }

    /**
     * Add server to list
     */
    public void addServer(Server server)
    {
        servers.put(server.getId(), server);
    }

    /**
     * Update a server in list
     */
    public void updateServer(Server server)
    {
        servers.put(server.getId(), server);
    }

    /**
     * Get list of servers
     *
     * @return list of servers
     */
    public List<Server> getServers()
    {
        List<Server> servers = new ArrayList<>(this.servers.size());

        for (int i = 0; i < this.servers.size(); i++) {
            servers.add(this.servers.valueAt(i));
        }

        return servers;
    }
}
