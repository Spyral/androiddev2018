package vn.edu.usth.irc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * Created by Minu'aHYHY on 10/26/2017.
 */

public class SenderFragment extends Fragment {

    private EditText sender;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sender, container, false);

        sender = (EditText) view.findViewById(R.id.sender_box);

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.send_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void sendMessage() {
        ConnectServerActivity.user.setMessage(sender.getText().toString());
        ChatboxFragment.updateChat(ConnectServerActivity.user.getUsername(), ConnectServerActivity.user.getMessage());
        sender.setText("");
    }

}
