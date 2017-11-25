package xyz.sonbn.ircclient.irc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import xyz.sonbn.ircclient.model.Server;

/**
 * Created by sonbn on 11/23/2017.
 */

public class IRCService extends Service {
    private final IRCBinder mBinder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public IRCService() {
        super();
        mBinder = new IRCBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void connect(final Server server){
        final int serverId = server.getId();
        final IRCService service = this;

        new Thread("Connect thread to"){
            @Override
            public void run() {
                super.run();
            }
        };
    }
}
