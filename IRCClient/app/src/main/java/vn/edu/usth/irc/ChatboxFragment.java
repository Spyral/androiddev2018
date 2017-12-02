package vn.edu.usth.irc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Chat> chat = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chatbox, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.chatbox);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ChatboxAdapter(chat);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    private void prepareChat() {
        // TODO: get the data from the server and set it here

        mAdapter.notifyDataSetChanged();
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
            Chat chat = chatList.get(position);
            holder.chat_user.setText(chat.getUser());
            holder.chat_content.setText(chat.getContent());
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }
    }
}
