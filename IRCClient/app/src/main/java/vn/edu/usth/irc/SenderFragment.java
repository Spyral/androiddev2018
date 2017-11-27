package vn.edu.usth.irc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Collection;

import vn.edu.usth.irc.Model.*;

/**
 * Created by Minu'aHYHY on 10/26/2017.
 */

public class SenderFragment extends Fragment {

    public EditText input;
    public Server server;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sender, container, false);

        input = (EditText) view.findViewById(R.id.sender_box);
        Collection<vn.edu.usth.irc.Model.Chat> mConversations = server.getConversations();

        int setInputTypeFlags = 0;
        setInputTypeFlags |= InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;

        input.setInputType(input.getInputType() | setInputTypeFlags);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getText().length() > 0) {
                    sendCurrentMessage();
                }
            }
        });

        return view;
    }

    private void sendCurrentMessage() {
        sendMessage(input.getText().toString());

        // Workaround for a race condition in EditText
        // Instead of calling input.setText("");
        // See:
        // - https://github.com/pocmo/Yaaic/issues/67
        // - http://code.google.com/p/android/issues/detail?id=17508
        TextKeyListener.clear(input.getText());
    }

    private void sendMessage(String text) {
        Chat chat;
        String nickname = binder.getService().getConnection(serverId).getNick();
        //conversation.addMessage(new Message("<" + nickname + "> " + text));
        conversation.addMessage(new Message(text, nickname));
        binder.getService().getConnection(serverId).sendMessage(conversation.getName(), text);
        onConversationMessage(conversation.getName());
    }

}
