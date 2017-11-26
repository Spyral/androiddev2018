package xyz.sonbn.ircclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.fragment.ActiveUserFragment;
import xyz.sonbn.ircclient.fragment.ConversationFragment;
import xyz.sonbn.ircclient.fragment.dummy.DummyContent;
import xyz.sonbn.ircclient.model.Message;

/**
 * Created by sonbn on 11/27/2017.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private final ConversationFragment.OnListFragmentInteractionListener mListener;
    private ArrayList<Message> mMessages;

    public ConversationAdapter(ArrayList<Message> items, ConversationFragment.OnListFragmentInteractionListener listener) {
        mMessages = items;
        mListener = listener;
    }

    @Override
    public ConversationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message, parent, false);
        return new ConversationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ConversationAdapter.ViewHolder holder, int position) {
        holder.mMessage = mMessages.get(position);
        String text = holder.mMessage.getNickname() + ": " + holder.mMessage.getContent();
        holder.mMessageTv.setText(text);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mMessage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void swap(ArrayList<Message> messages){
        if (messages == null || messages.size() == 0){
            return;
        } else {
            mMessages.clear();
            mMessages.addAll(messages);
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageTv;
        public Message mMessage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMessageTv = (TextView) view.findViewById(R.id.message);
        }
    }
}
