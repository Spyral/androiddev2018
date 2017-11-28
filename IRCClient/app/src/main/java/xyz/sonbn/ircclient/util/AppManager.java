package xyz.sonbn.ircclient.util;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Server;

/**
 * Created by sonbn on 11/25/2017.
 */

public class AppManager {
    private static AppManager instance;

    private SparseArray<Server> mServers;
    private boolean isServerLoaded = false;
    private Realm mRealm;
    private ArrayList<Conversation> mConversations;

    private AppManager(){
        mServers = new SparseArray<Server>();
        mConversations = new ArrayList<>();
        mRealm = Realm.getDefaultInstance();
    }

    public void loadServer(Context context){
        if (!isServerLoaded){
            RealmResults<Server> results = mRealm.where(Server.class).findAll();
            mServers = toSparseArray(results);
            isServerLoaded = true;
        }
    }

    private SparseArray<Server> toSparseArray(RealmResults<Server> results){
        SparseArray<Server> servers = new SparseArray<>();
        for (Server server: results) {
            servers.put(server.getId(), server);
        }
        return servers;
    }

    //Singleton
    public static AppManager getInstance(){
        if (instance == null){
            instance = new AppManager();
        }
        return instance;
    }

    public Server getServerById(int id){
        return mServers.get(id);
    }

    public void removeServerById(int id){
        mServers.remove(id);
    }

    public void addServer(Server server){
        mServers.put(server.getId(), server);
    }

    public void updateServer(Server server){
        mServers.put(server.getId(), server);
    }

    public List<Server> getServers(){
        List<Server> servers = new ArrayList<>(mServers.size());
        for (int i = 0; i < mServers.size(); i++){
            servers.add(mServers.valueAt(i));
        }
        return servers;
    }

    public ArrayList<String> getChannelsByServerId(int serverId)
    {
        ArrayList<String> channels = new ArrayList<String>();

        return channels;
    }
}
