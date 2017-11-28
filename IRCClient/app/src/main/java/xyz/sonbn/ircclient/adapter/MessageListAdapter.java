/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package xyz.sonbn.ircclient.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Message;

/**
 * Adapter for (channel) messages in a ListView
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>
{
    private final String name;
    private final LinkedList<Message> messages;
    private int historySize;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView sender;
        public TextView content;
        public TextView timestamp;

        public ViewHolder(View view){
            super(view);
            sender = (TextView) view.findViewById(R.id.sender);
            content = (TextView) view.findViewById(R.id.content);
            timestamp = (TextView) view.findViewById(R.id.timestamp);
        }
    }

    /**
     * Create a new MessageAdapter.
     */
    public MessageListAdapter(Conversation conversation, String name)
    {
        LinkedList<Message> messages = new LinkedList<Message>();
        this.name = name;

        // Render channel name as first message in channel
        if (conversation.getType() != Conversation.TYPE_SERVER) {
            Message header = new Message(conversation.getName());
            header.setColor(Message.COLOR_RED);
            messages.add(header);
        }

        messages.addAll(conversation.getHistory());

        // XXX: We don't want to clear the buffer, we want to add only
        //      buffered messages that are not already added (history)
        conversation.clearBuffer();

        this.messages = messages;
        historySize = conversation.getHistorySize();
    }

    public void addMessage(Message message)
    {
        messages.add(message);

        if (messages.size() > historySize) {
            messages.remove(0);
        }

        notifyDataSetChanged();
    }

    public void addBulkMessages(LinkedList<Message> messages)
    {
        this.messages.addAll(messages);

        while (messages.size() > historySize) {
            messages.remove(0);
        }

        notifyDataSetChanged();
    }

    public String getName() {
        return name;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.sender.setTextColor(message.getSenderColor());
        holder.sender.setText(message.getSender());
        holder.content.setTextColor(message.getColor());
        holder.content.setText(message.getText());
        holder.timestamp.setText(String.valueOf(message.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }
}
