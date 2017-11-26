package xyz.sonbn.ircclient.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

public class ConversationFragment extends Fragment implements ChannelListener {
    private Conversation mConversation;

    private EditText input;
    private ImageButton sendButton;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversation = AppManager.getInstance().getConversation(getArguments().getInt(Extra.SERVER_ID), getArguments().getString(Extra.CHANNELS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        input = (EditText) view.findViewById(R.id.input);
        sendButton = (ImageButton) view.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getText().length() > 0){
                    sendCurrentMessage();
                }
            }
        });

        return view;
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

    private void sendCurrentMessage() {
        sendMessage(input.getText().toString());
    }

    private void sendMessage(String text) {
        if (text.equals("")) {
            // ignore empty messages
            return;
        }

//        mConversation.addMessage(new Message(mServer.getNickname(), text));
//        mBinder.getService().getConnection(mServerId).sendMessage(mConversation.getChannel(), text);
    }
}
