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

import io.realm.Realm;
import xyz.sonbn.ircclient.fragment.ActiveUserFragment;
import xyz.sonbn.ircclient.fragment.ConversationFragment;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.ServerInfo;
import xyz.sonbn.ircclient.util.AppManager;
import xyz.sonbn.ircclient.util.ConversationPlaceHolder;

public class ChannelViewPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private String[] title = new String[]{"Conversation", "Active Users"};
    private ConversationFragment mConversationFragment;
    private ActiveUserFragment mActiveUserFragment;
    private int serverId;

    public ChannelViewPagerAdapter(FragmentManager fm, int serverId) {
        super(fm);

        this.serverId = serverId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                mConversationFragment = new ConversationFragment();
                Bundle args = new Bundle();
                args.putInt(Extra.SERVER_ID, serverId);
                mConversationFragment.setArguments(args);
                return mConversationFragment;
            case 1:
                mActiveUserFragment = new ActiveUserFragment();
                return mActiveUserFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
