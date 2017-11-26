package xyz.sonbn.ircclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.model.Extra;

public class AddChannelActivity extends Activity implements View.OnClickListener{

    private EditText channelInput;
    private ArrayList<String> channels;
    private ArrayAdapter<String> mAdapter;
    private Button okButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_add_channel);

        channelInput = (EditText) findViewById(R.id.channel);
        channelInput.setSelection(1);

        mAdapter = new ArrayAdapter<String>(this, R.layout.channelitem);

        ListView listView = (ListView) findViewById(R.id.channels);
        listView.setAdapter(mAdapter);

        ((Button) findViewById(R.id.add)).setOnClickListener(this);
        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);

        okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(this);
        okButton.setEnabled(false);

        channels = getIntent().getExtras().getStringArrayList(Extra.CHANNELS);

        for (String channel: channels){
            mAdapter.add(channel);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add:
                String channel = channelInput.getText().toString().trim();
                channels.add(channel);
                mAdapter.add(channel);
                channelInput.setText("#");
                channelInput.setSelection(1);
                okButton.setEnabled(true);
                break;
            case R.id.ok:
                Intent intent = new Intent();
                intent.putExtra(Extra.CHANNELS, channels);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
