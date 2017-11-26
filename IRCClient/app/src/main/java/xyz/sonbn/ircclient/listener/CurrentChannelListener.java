package xyz.sonbn.ircclient.listener;

import android.content.Context;
import android.support.v4.view.ViewPager;

import xyz.sonbn.ircclient.adapter.ConversationViewPagerAdapter;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Server;

/**
 * Created by sonbn on 11/22/2017.
 */

public class CurrentChannelListener implements ViewPager.OnPageChangeListener {
    private final Context mContext;
    private final Server mServer;
    private final ConversationViewPagerAdapter mConversationViewPagerAdapter;

    public CurrentChannelListener(Context context, Server server, ConversationViewPagerAdapter conversationViewPagerAdapter) {
        mContext = context;
        mServer = server;
        mConversationViewPagerAdapter = conversationViewPagerAdapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Conversation conversation = mConversationViewPagerAdapter.getConversation();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
