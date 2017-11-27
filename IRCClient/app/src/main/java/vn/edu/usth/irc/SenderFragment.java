package vn.edu.usth.irc;

import android.content.Context;
import android.content.SharedPreferences;
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

<<<<<<< HEAD

=======
    private EditText input;
>>>>>>> 8abdd617b58c04d2b6f4073828c6ade20c098c86

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sender, container, false);
        input = (EditText) view.findViewById(R.id.sender_box);

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
        ConnectServerActivity.user.setMessage(input.getText().toString());
        ChatboxFragment.updateChat(ConnectServerActivity.user.getUsername(), ConnectServerActivity.user.getMessage());
        input.setText("");
    }

}
