package xyz.sonbn.ircclient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Extra;

public class ChannelReceiver extends BroadcastReceiver {
    private ChannelListener mChannelListener;
    private final int mServerId;

    public ChannelReceiver(int serverId, ChannelListener channelListener) {
        mChannelListener = channelListener;
        mServerId = serverId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int serverId = intent.getExtras().getInt(Extra.SERVER_ID);

        if (serverId != mServerId){
            return;
        }

        String action = intent.getAction();
        switch (action){
            case Broadcast.CONVERSATION_MESSAGE:
                mChannelListener.onConversationMessage(intent.getExtras().getString(Extra.CONVERSATION));
                break;
            case Broadcast.CONVERSATION_NEW:
                break;
            case Broadcast.CONVERSATION_REMOVE:
                break;
            case Broadcast.CONVERSATION_TOPIC:
                break;
        }
    }
}
