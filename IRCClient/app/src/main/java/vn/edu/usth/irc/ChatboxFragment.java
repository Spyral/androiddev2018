package vn.edu.usth.irc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minu'aHYHY on 10/26/2017.
 */

public class ChatboxFragment extends Fragment{

    private static RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private static RecyclerView.Adapter mAdapter;
    private static List<Chat> chatList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chatbox, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.chatbox);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ChatboxAdapter(chatList);
        mRecyclerView.setAdapter(mAdapter);

//        new TaskCheckNewMess(getContext(), Utils.user.getChannel()).fetchNewMessID();

        return view;
    }


    public static void updateChat(String user, String content) {
        Chat chat = new Chat(user, content);
        chatList.add(chat);

        mAdapter.notifyDataSetChanged();

        // Scroll to bottom
        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
    }

    public static void clearChat() {
        mRecyclerView.removeAllViewsInLayout();
        chatList.clear();
    }

    public class ChatboxAdapter extends RecyclerView.Adapter<ChatboxAdapter.MyViewHolder> {
        private List<Chat> chatList;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView chat_user, chat_content;

            public MyViewHolder(View view) {
                super(view);
                chat_user    = (TextView) view.findViewById(R.id.chat_user);
                chat_content = (TextView) view.findViewById(R.id.chat_content);
            }
        }

        public ChatboxAdapter(List<Chat> chatList) {
            this.chatList = chatList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatbox_list_row, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            String username = "";
            Chat current_chat = chatList.get(position);
            Chat prev_chat;

            if (position > 0) {
                prev_chat = chatList.get(position - 1);

                // Will not show the username again for the same sender
                if (!current_chat.getUser().equals(prev_chat.getUser())) {
                    username = "<" + current_chat.getUser() + ">:";
                }
            } else {
                username = "<" + current_chat.getUser() + ">:";
            }

            holder.chat_user.setText(username);
            holder.chat_content.setText(current_chat.getContent());

            if (Utils.user.getUsername().equals(current_chat.getUser())) {
                holder.chat_user.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            } else {
                holder.chat_user.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlue));
            }
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }
    }
}
