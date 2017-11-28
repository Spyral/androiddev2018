package xyz.sonbn.ircclient.irc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.ServerInfo;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.util.AppManager;

public class IRCService extends Service {

    private final IRCBinder mBinder;
    private HashMap<Integer, IRCConnection> mConnections;
    private final ArrayList<String> connectedServerTitles;
    private Realm mRealm;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public IRCService() {
        super();
        mConnections = new HashMap<Integer, IRCConnection>( );
        mBinder = new IRCBinder(this);
        this.connectedServerTitles = new ArrayList<String>();
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void connect(final Server server){
        final int serverId = server.getId();
        final String nickname = server.getNickname();
        final String real_name = server.getRealname();
        final String host = server.getHost();
        final int port = server.getPort();

        Log.d("IRCService", "connect to " + host + ":" + port);

        new Thread("Connect thread to" + server.getTitle()){
            @Override
            public void run() {
                try {
                    IRCConnection connection = getConnection(serverId);
                    connection.setNickname(nickname);
                    connection.setRealName(real_name);

                    connection.connect(host, port);
                } catch (Exception e){
//                    mRealm.executeTransaction(new Realm.Transaction() {
//                        @Override
//                        public void execute(Realm realm) {
//                            server.setStatus(Status.DISCONNECTED);
//                        }
//                    });
                    Intent sIntent = Broadcast.createServerIntent(Broadcast.SERVER_UPDATE, serverId);
                    sendBroadcast(sIntent);

                    IRCConnection connection = getConnection(serverId);

                    Message message;
                    message = new Message(getString(R.string.could_not_connect, server.getHost(), server.getPort()));
                    message.setColor(Message.COLOR_RED);
                    message.setIcon(R.drawable.error);
                    server.getConversation(ServerInfo.DEFAULT_NAME).addMessage(message);

                    Intent cIntent = Broadcast.createConversationIntent(
                            Broadcast.CONVERSATION_MESSAGE,
                            serverId,
                            ServerInfo.DEFAULT_NAME
                    );
                    sendBroadcast(cIntent);
                }
            }
        }.start();
    }

    /**
     * Get connect for given serverId
     * @param serverId
     * @return
     */
    public synchronized IRCConnection getConnection(int serverId){
        IRCConnection connection = mConnections.get(serverId);

        if (connection == null){
            connection = new IRCConnection(this,serverId);
            mConnections.put(serverId, connection);
        }

        return connection;
    }

    public void checkServiceStatus(){
        boolean shutDown = true;
        List<Server> servers = AppManager.getInstance().getServers();

        for (final Server server : servers) {
            if (server.isDisconnected()) {
                int serverId = server.getId();
                synchronized(this) {
                    IRCConnection connection = mConnections.get(serverId);
                    if (connection != null) {
                        connection.dispose();
                    }
                    mConnections.remove(serverId);
                }
            } else {
                shutDown = false;
            }
        }

        if (shutDown) {
            stopSelf();
        }
    }
}
