package xyz.sonbn.ircclient.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.activity.ClientActivity;
import xyz.sonbn.ircclient.adapter.ChannelViewPagerAdapter;
import xyz.sonbn.ircclient.irc.IRCBinder;
import xyz.sonbn.ircclient.irc.IRCService;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.listener.ServerListener;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Message;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.receiver.ChannelReceiver;
import xyz.sonbn.ircclient.receiver.ServerReceiver;
import xyz.sonbn.ircclient.util.AppManager;


public class ChannelFragment extends Fragment implements ChannelListener, ServerListener, ServiceConnection {
    public static final String TRANSACTION_TAG = "fragment_channel";

    private int mServerId;
    private Server mServer;

    private ChannelReceiver mChannelReceiver;
    private ServerReceiver mServerListener;

    public ChannelFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof ClientActivity)) {
            throw new IllegalArgumentException("Activity has to implement ClientActivity interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServerId = getArguments().getInt(Extra.SERVER_ID);
        mServer = AppManager.getInstance().getServerById(mServerId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.pager);

        ChannelViewPagerAdapter mChannelViewPagerAdapter = new ChannelViewPagerAdapter(getFragmentManager(), mServerId, mServer.getChannels().first());
        mViewPager.setAdapter(mChannelViewPagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab);
        tabLayout.setupWithViewPager(mViewPager);

        if (mServer.getStatus() == Status.PRE_CONNECTING){

        }

        return view;
    }

    @Override
    public void onResume() {
        mChannelReceiver = new ChannelReceiver(mServerId, this);
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_MESSAGE));
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_NEW));
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_REMOVE));
        getActivity().registerReceiver(mChannelReceiver, new IntentFilter(Broadcast.CONVERSATION_TOPIC));

        mServerListener = new ServerReceiver(this);
        getActivity().registerReceiver(mServerListener, new IntentFilter(Broadcast.SERVER_UPDATE));
        super.onResume();

        //Start service
        Intent intent = new Intent(getActivity(), IRCService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, this, 0);
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
