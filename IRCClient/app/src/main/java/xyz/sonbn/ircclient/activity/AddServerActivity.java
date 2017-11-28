package xyz.sonbn.ircclient.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

public class AddServerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_CHANNELS       = 1;
    private static final int REQUEST_CODE_AUTHENTICATION = 2;

    private Server mServer;
    private ArrayList<String> channels;
    private Realm mRealm;

    private EditText title, host, port, nickname, realname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.addView(LayoutInflater.from(this).inflate(R.layout.item_done_discard, toolbar, false));

        setSupportActionBar(toolbar);

        channels = new ArrayList<>();

        title = (EditText) findViewById(R.id.title);
        host = (EditText) findViewById(R.id.host);
        port = (EditText) findViewById(R.id.port);
        nickname = (EditText) findViewById(R.id.nickname);
        realname = (EditText) findViewById(R.id.real_name);

        ((Button) findViewById(R.id.authentication)).setOnClickListener(this);
        ((Button) findViewById(R.id.autoJoinChannels)).setOnClickListener(this);

        mRealm = Realm.getDefaultInstance();

        Bundle extras = getIntent().getExtras();
        //check add server or update
        if (extras != null && extras.containsKey(Extra.SERVER)){
            setTitle(R.string.edit_server_label);
            mServer = mRealm.where(Server.class).equalTo("id", extras.getInt(Extra.SERVER)).findFirst();
            channels = mServer.getAutoJoinChannels();

            //Set server value
            title.setText(mServer.getTitle());
            host.setText(mServer.getHost());
            port.setText(mServer.getPort());
            nickname.setText(mServer.getNickname());
            realname.setText(mServer.getRealname());
        }

        // Disable suggestions for host name
        if (android.os.Build.VERSION.SDK_INT >= 5) {
            EditText serverHostname = (EditText) findViewById(R.id.host);
            serverHostname.setInputType(0x80000);
        }

        Uri uri = getIntent().getData();
        if (uri != null && uri.getScheme().equals("irc")) {
            // handling an irc:// uri

            ((EditText) findViewById(R.id.host)).setText(uri.getHost());
            if (uri.getPort() != -1) {
                ((EditText) findViewById(R.id.port)).setText(String.valueOf(uri.getPort()));
            }
            if (uri.getPath() != null) {
                channels.add(uri.getPath().replace('/', '#'));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.authentication:
                Intent authIntent = new Intent(this, AuthenticationActivity.class);
                startActivityForResult(authIntent, REQUEST_CODE_AUTHENTICATION);
                break;
            case R.id.autoJoinChannels:
                Intent channelIntent = new Intent(this, AddChannelActivity.class);
                channelIntent.putExtra(Extra.CHANNELS, channels);
                startActivityForResult(channelIntent, REQUEST_CODE_CHANNELS);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK){
            return;
        }

        switch (requestCode){
            case REQUEST_CODE_CHANNELS:
                channels = data.getExtras().getStringArrayList(Extra.CHANNELS);
                break;
            case REQUEST_CODE_AUTHENTICATION:
                break;
        }
    }

    public void onCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSave(View view) {
        try {
            validateServer();
            if (mServer == null) {
                addServer();
            } else {
                updateServer();
            }
            setResult(RESULT_OK);
            finish();
        } catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addServer(){
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number currentMaxId = mRealm.where(Server.class).max("id");
                int serverId = (currentMaxId == null) ? 1 : currentMaxId.intValue() + 1;

                mServer = new Server();
                mServer.setId(serverId);
                mServer.setTitle(((EditText) findViewById(R.id.title)).getText().toString());
                mServer.setHost(((EditText) findViewById(R.id.host)).getText().toString());
                mServer.setPort(Integer.parseInt(((EditText) findViewById(R.id.port)).getText().toString()));
                mServer.setNickname(((EditText) findViewById(R.id.nickname)).getText().toString());
                mServer.setRealname(((EditText) findViewById(R.id.real_name)).getText().toString());

                if (channels.size() == 0){
                    channels.add("#General");
                }
                mServer.setAutoJoinChannels(channels);

                mRealm.insertOrUpdate(mServer);
            }
        });
    }

    private void updateServer(){
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number currentMaxId = mRealm.where(Server.class).max("id");
                int serverId = (currentMaxId == null) ? 1 : currentMaxId.intValue() + 1;

                mServer.setId(serverId);
                mServer.setTitle(((EditText) findViewById(R.id.title)).getText().toString());
                mServer.setHost(((EditText) findViewById(R.id.host)).getText().toString());
                mServer.setPort(Integer.parseInt(((EditText) findViewById(R.id.port)).getText().toString()));
                mServer.setNickname(((EditText) findViewById(R.id.nickname)).getText().toString());
                mServer.setRealname(((EditText) findViewById(R.id.real_name)).getText().toString());

                mRealm.insertOrUpdate(mServer);
            }
        });
    }

    private void validateServer(){
        if (mRealm.where(Server.class).equalTo("title", title.getText().toString()).findAll().size() == 0){
            mServer = null;
        } else {
            mServer = mRealm.where(Server.class).equalTo("title", title.getText().toString()).findFirst();
        }
    }
}
