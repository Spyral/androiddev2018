package xyz.sonbn.ircclient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import xyz.sonbn.ircclient.listener.ServerListener;

/**
 * Created by sonbn on 11/26/2017.
 */

public class ServerReceiver extends BroadcastReceiver {
    private final ServerListener mServerListener;

    public ServerReceiver(ServerListener serverListener) {
        mServerListener = serverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
