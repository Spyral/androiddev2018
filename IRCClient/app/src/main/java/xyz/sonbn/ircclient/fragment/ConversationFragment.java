package xyz.sonbn.ircclient.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Collection;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.adapter.ConversationManager;
import xyz.sonbn.ircclient.adapter.MessageListAdapter;
import xyz.sonbn.ircclient.command.CommandParser;
import xyz.sonbn.ircclient.irc.IRCBinder;
import xyz.sonbn.ircclient.irc.IRCService;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.listener.ServerListener;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Scrollback;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.ServerInfo;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.receiver.ChannelReceiver;
import xyz.sonbn.ircclient.receiver.ServerReceiver;

public class ConversationFragment extends Fragment implements ChannelListener, ServerListener, ServiceConnection {
    private final String TAG = "ConversationFragment";

    private int mServerId;
    private Server mServer;

    private EditText input;
    private RecyclerView messageRecyvlerView;
    private ImageButton sendButton;

    private ConversationManager mConversationManager;

    private MessageListAdapter mMessageListAdapter;

    private ChannelReceiver mChannelReceiver;
    private ServerReceiver mServerReceiver;

    private IRCBinder mBinder;


    private Scrollback mScrollback;

    private String joinChannelBuffer;
    private Realm mRealm;

    private final View.OnKeyListener inputKeyListener = new View.OnKeyListener() {
        /**
         * On key pressed (input line)
         */
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            EditText input = (EditText) view;

            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                String message = mScrollback.goBack();
                if (message != null) {
                    input.setText(message);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                String message = mScrollback.goForward();
                if (message != null) {
                    input.setText(message);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                sendCurrentMessage();

                return true;
            }

            return false;
        }
    };

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServerId = getArguments().getInt(Extra.SERVER_ID);
        mRealm = Realm.getDefaultInstance();
        mServer = mRealm.where(Server.class).equalTo("id", mServerId).findFirst();

        mScrollback = new Scrollback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        input = (EditText) view.findViewById(R.id.input);
        input.setOnKeyListener(inputKeyListener);

        messageRecyvlerView = (RecyclerView) view.findViewById(R.id.message_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        messageRecyvlerView.setLayoutManager(llm);

        mConversationManager = new ConversationManager(getActivity(), mServer);
        mMessageListAdapter = mConversationManager.getItemAdapter(ServerInfo.DEFAULT_NAME);
        messageRecyvlerView.setAdapter(mMessageListAdapter);

        if (mServer.getStatus() == Status.PRE_CONNECTING){
            mServer.clearConversations();
            mConversationManager.clearConversations();
        }

        Collection<Conversation> mConversations = mServer.getConversations();

        for (Conversation conversation : mConversations) {
            // Only scroll to new conversation if it was selected before
            if (conversation.getStatus() == Conversation.STATUS_SELECTED) {
                onNewConversation(conversation.getName());
            } else {
                createNewConversation(conversation.getName());
            }
        }

        int setInputTypeFlags = 0;

        setInputTypeFlags |= InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;

        input.setInputType(input.getInputType() | setInputTypeFlags);

        Log.d("ConversationFragment", "oncreateview");

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
        getActivity().startService(intent);
        getActivity().bindService(intent, this, 0);

        input.setEnabled(mServer.isConnected());

        Collection<Conversation> mConversations = mServer.getConversations();
        MessageListAdapter mAdapter;

        // Fill view with messages that have been buffered while paused
        for (Conversation conversation : mConversations) {
            String name = conversation.getName();
            mAdapter = mConversationManager.getItemAdapter(name);

            if (mAdapter != null) {
                mAdapter.addBulkMessages(conversation.getBuffer());
                conversation.clearBuffer();
            } else {
                // Was conversation created while we were paused?
                if (mConversationManager.getPositionByName(name) == -1) {
                    onNewConversation(name);
                }
            }
        }

//        // Remove views for conversations that ended while we were paused
//        int numViews = mConversationManager.getCount();
//        if (numViews > mConversations.size()) {
//            for (int i = 0; i < numViews; ++i) {
//                if (!mConversations.contains(mConversationManager.getItem(i))) {
//                    mConversationManager.removeConversation(i--);
//                    --numViews;
//                }
//            }
//        }

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

        if (mBinder != null && mBinder.getService() != null) {
            mBinder.getService().checkServiceStatus();
        }

        getActivity().unbindService(this);
        getActivity().unregisterReceiver(mChannelReceiver);
        getActivity().unregisterReceiver(mServerReceiver);
    }

    @Override
    public void onConversationMessage(String target) {
        Conversation conversation = mServer.getConversation(target);

        if (conversation == null) {
            // In an early state it can happen that the conversation object
            // is not created yet.
            return;
        }

        MessageListAdapter adapter = mConversationManager.getItemAdapter(target);

        while(conversation.hasBufferedMessages()) {
            Message message = conversation.pollBufferedMessage();

            if (adapter != null && message != null) {
                adapter.addMessage(message);
                int status;

                switch (message.getType())
                {
                    case Message.TYPE_MISC:
                        status = Conversation.STATUS_MISC;
                        break;

                    default:
                        status = Conversation.STATUS_MESSAGE;
                        break;
                }
                conversation.setStatus(status);
            }
        }
    }

    @Override
    public void onNewConversation(String target) {
        createNewConversation(target);

        mConversationManager.addConversation(mServer.getConversation(target));
        mMessageListAdapter = mConversationManager.getItemAdapter(target);
        messageRecyvlerView.setAdapter(mMessageListAdapter);
    }

    @Override
    public void onRemoveConversation(String target) {
        mMessageListAdapter = mConversationManager.getItemAdapter(ServerInfo.DEFAULT_NAME);
        messageRecyvlerView.setAdapter(mMessageListAdapter);
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
        TextKeyListener.clear(input.getText());
    }

    private void sendMessage(String text) {
        if (text.equals("")) {
            // ignore empty messages
            return;
        }

        if (!mServer.isConnected()) {
            Message message = new Message(getString(R.string.message_not_connected));
            message.setColor(Message.COLOR_RED);
            message.setIcon(R.drawable.error);
            mServer.getConversation(mServer.getSelectedConversation()).addMessage(message);
            onConversationMessage(mServer.getSelectedConversation());
        }

        mScrollback.addMessage(text);

        Conversation conversation = mConversationManager.getItem(mMessageListAdapter.getName());

        if (conversation != null) {
            if (!text.trim().startsWith("/")) {
                String nickname = mBinder.getService().getConnection(mServerId).getNick();
                conversation.addMessage(new Message(text, nickname));
//                mBinder.getService().getConnection(mServerId).sendMessage(conversation.getName(), text);
                mBinder.getService().getConnection(mServerId).sendMessage("#straifur", text);

//                if (conversation.getType() != Conversation.TYPE_SERVER) {
//                    String nickname = mBinder.getService().getConnection(mServerId).getNick();
//                    conversation.addMessage(new Message(text, nickname));
//                    mBinder.getService().getConnection(mServerId).sendMessage(conversation.getName(), text);
//                } else {
//                    Message message = new Message(getString(R.string.chat_only_form_channel));
//                    message.setColor(Message.COLOR_YELLOW);
//                    message.setIcon(R.drawable.warning);
//                    conversation.addMessage(message);
//                }
                onConversationMessage(conversation.getName());
            } else {
                CommandParser.getInstance().parse(text, mServer, conversation, mBinder.getService());
            }
        }
    }

    private void openSoftKeyboard(View view) {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }


    public Server getServer() {
        return mServer;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (IRCBinder) service;
        Log.d(TAG, "onServiceConnected");
        mBinder.connect(mServer);
        if (mServer.getStatus() == Status.PRE_CONNECTING) {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mServer.setStatus(Status.CONNECTING);
                }
            });
            mBinder.connect(mServer);
        } else {
            onStatusUpdate();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
    }

    @Override
    public void onStatusUpdate() {
        if (mServer.isConnected()) {
            input.setEnabled(true);
        } else {
            input.setEnabled(false);

            if (mServer.getStatus() == Status.CONNECTING) {
                return;
            }

            // Service is not connected or initialized yet - See #54
            if (mBinder == null || mBinder.getService() == null) {
                return;
            }
        }
    }

    public void createNewConversation(String target) {
        mConversationManager.addConversation(mServer.getConversation(target));
    }



}
