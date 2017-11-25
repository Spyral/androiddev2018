package xyz.sonbn.ircclient.irc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

/**
 * Created by sonbn on 11/23/2017.
 */

public class IRCService extends Service {
    private final IRCBinder mBinder;
    private HashMap<Integer, IRCConnection> mConnections;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public IRCService() {
        super();
        mConnections = new HashMap<Integer, IRCConnection>( );
        mBinder = new IRCBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void connect(final Server server){
        final int serverId = server.getId();
        final IRCService service = this;

        new Thread("Connect thread to" + server.getTitle()){
            @Override
            public void run() {
                try {
                    IRCConnection connection = getConnection(serverId);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public synchronized IRCConnection getConnection(int serverId){
        IRCConnection connection = mConnections.get(serverId);
        if (connection == null){
            connection = new IRCConnection(this,serverId);
            mConnections.put(serverId, connection);
        }
        return connection;
    }
}
