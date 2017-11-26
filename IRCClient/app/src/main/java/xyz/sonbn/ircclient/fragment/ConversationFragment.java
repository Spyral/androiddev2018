package xyz.sonbn.ircclient.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Server;

public class ConversationFragment extends Fragment implements ChannelListener {
    private Server mServer;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onConversationMessage(String target) {
    }

    @Override
    public void onNewConversation(String target) {

    }

    @Override
    public void onRemoveConversation(String target) {

    }

    @Override
    public void onTopicChanged(String target) {

    }
}
