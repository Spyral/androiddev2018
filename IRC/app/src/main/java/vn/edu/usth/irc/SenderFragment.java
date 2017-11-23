package vn.edu.usth.irc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Minu'aHYHY on 10/26/2017.
 */

public class SenderFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sender, container, false);
        return view;
    }

//    public void sendMess(View view){
//        Log.i("Sender", "Send message");
//        Intent i = new Intent("event");
//        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(i);
//    }
}
