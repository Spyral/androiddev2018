package xyz.sonbn.ircclient.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by sonbn on 11/22/2017.
 */

public abstract class Conversation {
    public static final int TYPE_CHANNEL = 1;
    public static final int TYPE_QUERY   = 2;
    public static final int TYPE_SERVER  = 3;
    private static final int DEFAULT_HISTORY_SIZE = 30;

    public static final int STATUS_DEFAULT   = 1;
    public static final int STATUS_SELECTED  = 2;
    public static final int STATUS_MESSAGE   = 3;
    public static final int STATUS_HIGHLIGHT = 4;
    public static final int STATUS_MISC      = 5; // join/part/quit

    private final LinkedList<Message> buffer;
    private final LinkedList<Message> history;
    private final String name;
    private int status = 1;

    private int historySize = DEFAULT_HISTORY_SIZE;

    public abstract int getType();

    public Conversation(String name)
    {
        this.buffer = new LinkedList<Message>();
        this.history = new LinkedList<Message>();
        this.name = name.toLowerCase();
    }

    public String getName()
    {
        return name;
    }

    public void addMessage(Message message)
    {
        buffer.add(0, message);
        history.add(message);

        if (history.size() > historySize) {
            history.remove(0);
        }
    }

    public LinkedList<Message> getHistory()
    {
        return history;
    }

    public Message pollBufferedMessage()
    {
        Message message = buffer.get(buffer.size() - 1);
        buffer.remove(buffer.size() - 1);
        return message;
    }

    public LinkedList<Message> getBuffer()
    {
        return buffer;
    }

    public boolean hasBufferedMessages()
    {
        return buffer.size() > 0;
    }

    public void clearBuffer()
    {
        buffer.clear();
    }

    public void setStatus(int status)
    {
        // Selected status can only be changed by deselecting
        if (this.status == STATUS_SELECTED && status != STATUS_DEFAULT) {
            return;
        }

        // Highlight status can only be changed by selecting
        if (this.status == STATUS_HIGHLIGHT && status != STATUS_SELECTED) {
            return;
        }

        // Misc cannot change any other than default
        if (this.status != STATUS_DEFAULT && status == STATUS_MISC) {
            return;
        }

        this.status = status;
    }

    public int getStatus()
    {
        return status;
    }

    public int getHistorySize()
    {
        return historySize;
    }

    public void setHistorySize(int size)
    {
        if (size <= 0) {
            return;
        }

        historySize = size;
        if (history.size() > size) {
            history.subList(size, history.size()).clear();
        }
    }
}
