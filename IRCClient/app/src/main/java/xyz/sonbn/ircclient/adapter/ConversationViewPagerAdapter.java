package xyz.sonbn.ircclient.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.sonbn.ircclient.fragment.ConversationFragment;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.util.ConversationPlaceHolder;

public class ConversationViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private Conversation mConversation;

    public ConversationViewPagerAdapter(Context context){
        mContext = context;
    }

    public Conversation getConversation(){
        return mConversation;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        ConversationPlaceHolder conversationFragment = ConversationPlaceHolder.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(conversationFragment.getLayoutResId(), collection, false);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return ConversationPlaceHolder.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        ConversationPlaceHolder customPagerEnum = ConversationPlaceHolder.values()[position];
        return mContext.getString(customPagerEnum.getTitleResId());
    }


}
