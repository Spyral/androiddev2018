package xyz.sonbn.ircclient.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.activity.ClientActivity;
import xyz.sonbn.ircclient.adapter.ChannelViewPagerAdapter;
import xyz.sonbn.ircclient.listener.ChannelListener;
import xyz.sonbn.ircclient.listener.ServerListener;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.util.AppManager;


public class ChannelFragment extends Fragment {
    public static final String TRANSACTION_TAG = "fragment_channel";

    private int mServerId;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.pager);

        ChannelViewPagerAdapter mChannelViewPagerAdapter = new ChannelViewPagerAdapter(getFragmentManager(), mServerId);
        mViewPager.setAdapter(mChannelViewPagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab);
        tabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.conversations, menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.disconnect:
//                server.setStatus(Status.DISCONNECTED);
//                server.setMayReconnect(false);
//                binder.getService().getConnection(serverId).quitServer();
//                server.clearConversations();
//                break;
//
//            case R.id.close:
//                Conversation conversationToClose = pagerAdapter.getItem(pager.getCurrentItem());
//                // Make sure we part a channel when closing the channel conversation
//                if (conversationToClose.getType() == Conversation.TYPE_CHANNEL) {
//                    binder.getService().getConnection(serverId).partChannel(conversationToClose.getName());
//                }
//                else if (conversationToClose.getType() == Conversation.TYPE_QUERY) {
//                    server.removeConversation(conversationToClose.getName());
//                    onRemoveConversation(conversationToClose.getName());
//                } else {
//                    Toast.makeText(getActivity(), getResources().getString(R.string.close_server_window), Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            case R.id.join:
//                startActivityForResult(new Intent(getActivity(), JoinActivity.class), REQUEST_CODE_JOIN);
//                break;
//
//            case R.id.users:
//                Conversation conversationForUserList = pagerAdapter.getItem(pager.getCurrentItem());
//                if (conversationForUserList.getType() == Conversation.TYPE_CHANNEL) {
//                    Intent intent = new Intent(getActivity(), UsersActivity.class);
//                    intent.putExtra(
//                            Extra.USERS,
//                            binder.getService().getConnection(server.getId()).getUsersAsStringArray(
//                                    conversationForUserList.getName()
//                            )
//                    );
//                    startActivityForResult(intent, REQUEST_CODE_USERS);
//                } else {
//                    Toast.makeText(getActivity(), getResources().getString(R.string.only_usable_from_channel), Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            case R.id.notify:
//                Conversation conversationForNotify = pagerAdapter.getItem(pager.getCurrentItem());
//                conversationForNotify.setAlwaysNotify(!item.isChecked());
//                break;
//        }
//
//        return true;
//    }
}
