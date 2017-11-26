package xyz.sonbn.ircclient.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.sonbn.ircclient.fragment.ActiveUserFragment;
import xyz.sonbn.ircclient.fragment.ConversationFragment;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.util.AppManager;
import xyz.sonbn.ircclient.util.ConversationPlaceHolder;

public class ChannelViewPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private String[] title = new String[]{"Conversation", "Active Users"};
    private Conversation mConversation;
    private ConversationFragment mConversationFragment;
    private ActiveUserFragment mActiveUserFragment;

    public ChannelViewPagerAdapter(FragmentManager fm, int serverId, String channel) {
        super(fm);
        mConversation = AppManager.getInstance().getConversation(serverId, channel);

        mConversationFragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putInt(Extra.SERVER_ID, mConversation.getServerId());
        args.putString(Extra.CHANNELS, mConversation.getChannel());
        mConversationFragment.setArguments(args);

        mActiveUserFragment = new ActiveUserFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return mConversationFragment;
            case 1:
                return mActiveUserFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
