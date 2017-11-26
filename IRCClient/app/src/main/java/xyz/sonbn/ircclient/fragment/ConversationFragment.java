package xyz.sonbn.ircclient.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.adapter.ConversationAdapter;
import xyz.sonbn.ircclient.fragment.dummy.DummyContent;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

public class ConversationFragment extends Fragment implements ChannelListener {
    private Conversation mConversation;
    private String nickname;

    private EditText input;
    private ImageButton sendButton;

    private ConversationFragment.OnListFragmentInteractionListener mListener;
    private ConversationAdapter mConversationAdapter;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int serverId = getArguments().getInt(Extra.SERVER_ID);
        mConversation = AppManager.getInstance().getConversation(serverId, getArguments().getString(Extra.CHANNELS));
        nickname = AppManager.getInstance().getServerById(serverId).getNickname();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        RecyclerView conversationView = view.findViewById(R.id.conversation);
        conversationView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mConversationAdapter = new ConversationAdapter(mConversation.getMessages(), mListener);
        conversationView.setAdapter(mConversationAdapter);

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

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Message item);
    }

    private void sendCurrentMessage() {
        sendMessage(input.getText().toString());
    }

    private void sendMessage(String text) {
        if (text.equals("")) {
            // ignore empty messages
            return;
        }

        mConversation.addMessage(new Message(nickname, text));
        mConversationAdapter.swap(mConversation.getMessages());
//        mBinder.getService().getConnection(mServerId).sendMessage(mConversation.getChannel(), text);
    }

}
