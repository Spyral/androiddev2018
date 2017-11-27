package xyz.sonbn.ircclient.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import xyz.sonbn.ircclient.irc.IRCBinder;
import xyz.sonbn.ircclient.irc.IRCService;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.listener.ServerListener;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.receiver.ChannelReceiver;
import xyz.sonbn.ircclient.receiver.ServerReceiver;
import xyz.sonbn.ircclient.util.AppManager;

public class ConversationFragment extends Fragment implements ChannelListener, ServerListener, ServiceConnection {
    private Conversation mConversation;
    private String nickname;
    private int mServerId;

    private EditText input;
    private ImageButton sendButton;

    private ConversationFragment.OnListFragmentInteractionListener mListener;
    private ConversationAdapter mConversationAdapter;

    private ChannelReceiver mChannelReceiver;
    private ServerReceiver mServerReceiver;

    private IRCBinder mBinder;
    private String joinChannelBuffer;

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServerId = getArguments().getInt(Extra.SERVER_ID);
        mConversation = AppManager.getInstance().getConversation(mServerId, getArguments().getString(Extra.CHANNELS));
        nickname = AppManager.getInstance().getServerById(mServerId).getNickname();
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
    public void onResume() {
        mChannelReceiver = new ChannelReceiver(mServerId, this);
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_MESSAGE));
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_NEW));
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_REMOVE));
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_TOPIC));

        mServerReceiver = new ServerReceiver(this);
        getActivity().registerReceiver(mServerReceiver, new IntentFilter(Broadcast.SERVER_UPDATE));
        super.onResume();

        //Start service
        Log.d("IRCService", "start service");
        Intent intent = new Intent(getActivity(), IRCService.class);
        intent.setAction(IRCService.ACTION_FOREGROUND);
        getActivity().startService(intent);
        getActivity().bindService(intent, this, 0);

        if (joinChannelBuffer != null) {
            new Thread() {
                @Override
                public void run() {
                    mBinder.getService().getConnection(mServerId).joinChannel(joinChannelBuffer);
                    joinChannelBuffer = null;
                }
            }.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unbindService(this);
        getActivity().unregisterReceiver(mChannelReceiver);
        getActivity().unregisterReceiver(mServerReceiver);
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
        mBinder.getService().getConnection(mServerId).sendMessage(mConversation.getChannel(), text);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (IRCBinder) service;
        mBinder.connect(AppManager.getInstance().getServerById(mServerId));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
    }

}
