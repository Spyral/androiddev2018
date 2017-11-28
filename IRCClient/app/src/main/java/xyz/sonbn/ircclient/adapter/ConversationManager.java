package xyz.sonbn.ircclient.adapter;

import android.content.Context;
import java.util.LinkedList;

import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.view.MessageListView;

/**
 * Created by sonbn on 11/27/2017.
 */

public class ConversationManager {
    private final Server server;
    private LinkedList<ConversationInfo> conversations;

    public class ConversationInfo {
        public Conversation conv;
        public MessageListAdapter adapter;
        public MessageListView view;

        public ConversationInfo(Conversation conv) {
            this.conv = conv;
            this.adapter = null;
        }
    }

    public ConversationManager(Context context, Server server) {
        this.server = server;

        conversations = new LinkedList<ConversationInfo>();
    }

    public ConversationInfo addConversation(Conversation conversation) {
        ConversationInfo conversationInfo = new ConversationInfo(conversation);
        conversations.add(conversationInfo);
        return conversationInfo;
    }

    public void removeConversation(int position) {
        conversations.remove(position);
    }

    public Conversation getItem(int position)
    {
        ConversationInfo convInfo = getItemInfo(position);
        if (convInfo != null) {
            return convInfo.conv;
        } else {
            return null;
        }
    }

    public Conversation getItem(String name)
    {
        ConversationInfo convInfo = getItemInfo(name);
        if (convInfo != null) {
            return convInfo.conv;
        } else {
            return null;
        }
    }

    public MessageListAdapter getItemAdapter(int position)
    {
        ConversationInfo convInfo = getItemInfo(position);
        if (convInfo != null) {
            return convInfo.adapter;
        } else {
            return null;
        }
    }

    public MessageListAdapter getItemAdapter(String name)
    {
        int position = getPositionByName(name);
        if (position == -1){
            ConversationInfo conversationInfo = addConversation(server.getConversation(name));
            conversationInfo.adapter = new MessageListAdapter(server.getConversation(name), name);
            return conversationInfo.adapter;
        }
        return getItemAdapter(position);
    }

    private ConversationInfo getItemInfo(int position) {
        if (position >= 0 && position < conversations.size()) {
            return conversations.get(position);
        }
        return null;
    }

    private ConversationInfo getItemInfo(String name) {
        int mSize = conversations.size();
        LinkedList<ConversationInfo> mItems = this.conversations;

        for (int i = 0; i <  mSize; i++) {
            if (mItems.get(i).conv.getName().equalsIgnoreCase(name)) {
                return mItems.get(i);
            }
        }

        return null;
    }

    public int getPositionByName(String name)
    {
        // Optimization - cache field lookups
        int mSize = conversations.size();
        LinkedList<ConversationInfo> mItems = this.conversations;

        for (int i = 0; i <  mSize; i++) {
            if (mItems.get(i).conv.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }

        return -1;
    }

    public void clearConversations()
    {
        conversations = new LinkedList<ConversationInfo>();
    }
}
