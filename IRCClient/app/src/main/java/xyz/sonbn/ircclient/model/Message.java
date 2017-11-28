package xyz.sonbn.ircclient.model;

import android.content.Context;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.Date;

public class Message {
    public static final int COLOR_GREEN   = 0xFF4caf50;
    public static final int COLOR_RED     = 0xFFf44336;
    public static final int COLOR_BLUE    = 0xFF3f51b5;
    public static final int COLOR_YELLOW  = 0xFFffc107;
    public static final int COLOR_GREY    = 0xFF607d8b;
    public static final int COLOR_DEFAULT = 0xFF212121;

    /* normal message, this is the default */
    public static final int TYPE_MESSAGE = 0;

    /* join, part or quit */
    public static final int TYPE_MISC    = 1;

    private static final int[] colors = {
            0xFFf44336, // Red
            0xFFe91e63, // Pink
            0xFF9c27b0, // Purple
            0xFF673ab7, // Deep Purple
            0xFF3f51b5, // Indigo
            0xFF2196f3, // Blue
            0xFF03a9f4, // Light Blue
            0xFF00bcd4, // Cyan
            0xFF009688, // Teal
            0xFF4caf50, // Green
            0xFF8bc34a, // Light green
            0xFFcddc39, // Lime
            0xFFffeb3b, // Yellow
            0xFFffc107, // Amber
            0xFFff9800, // Orange
            0xFFff5722, // Deep Orange
            0xFF795548, // Brown
    };

    public static final int NO_ICON  = -1;
    public static final int NO_TYPE  = -1;
    public static final int NO_COLOR = -1;

    private final String text;
    private final String sender;
    private long timestamp;

    private int color = NO_COLOR;
    private int type  = NO_ICON;
    private int icon  = NO_TYPE;

    public Message(String text)
    {
        this(text, null, TYPE_MESSAGE);
    }

    public Message(String text, int type)
    {
        this(text, null, type);
    }

    public Message(String text, String sender)
    {
        this(text, sender, TYPE_MESSAGE);
    }

    public Message(String text, String sender, int type)
    {
        this.text = text;
        this.sender = sender;
        this.timestamp = new Date().getTime();
        this.type = type;
    }

    public void setIcon(int icon)
    {
        this.icon = icon;
    }

    public int getIcon()
    {
        return icon;
    }

    public String getText()
    {
        return text;
    }

    public int getType()
    {
        return type;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public int getSenderColor()
    {
        /* It might be worth to use some hash table here */
        if (sender == null) {
            return COLOR_DEFAULT;
        }

        int color = 0;

        for(int i = 0; i < sender.length(); i++){
            color += sender.charAt(i);
        }

        /* we dont want color[colors.length-1] which is black */
        color = color % (colors.length - 1);

        return colors[color];
    }

    public int getColor(){
        return color;
    }

    public String getSender(){
        return sender;
    }

    public long getTimestamp(){
        return timestamp;
    }

    private boolean hasSender()
    {
        return sender != null;
    }

    private boolean hasColor()
    {
        return color != NO_COLOR;
    }

    private boolean hasIcon()
    {
        return icon != NO_ICON;
    }

    public TextView renderTextView(Context context, TextView view)
    {
        if (view == null) {
            view = new TextView(context);
        }

        view.setAutoLinkMask(Linkify.ALL);
        view.setLinksClickable(true);
        view.setLinkTextColor(COLOR_BLUE);
//        view.setText(this.render(context));
        view.setTextIsSelectable(true);

        return view;
    }

    public String renderTimeStamp(boolean use24hFormat, boolean includeSeconds)
    {
        Date date = new Date(timestamp);

        int hours = date.getHours();
        int minutes = date.getMinutes();
        int seconds = date.getSeconds();

        if (!use24hFormat) {
            hours = Math.abs(12 - hours);
            if (hours == 12) {
                hours = 0;
            }
        }

        if (includeSeconds) {
            return String.format(
                    "[%02d:%02d:%02d]",
                    hours,
                    minutes,
                    seconds);
        } else {
            return String.format(
                    "[%02d:%02d]",
                    hours,
                    minutes);
        }
    }
}
