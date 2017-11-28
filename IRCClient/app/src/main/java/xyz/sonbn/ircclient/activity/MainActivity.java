package xyz.sonbn.ircclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.fragment.ActiveUserFragment;
import xyz.sonbn.ircclient.fragment.ChannelFragment;
import xyz.sonbn.ircclient.fragment.OverviewFragment;
import xyz.sonbn.ircclient.fragment.dummy.DummyContent;
import xyz.sonbn.ircclient.irc.IRCBinder;
import xyz.sonbn.ircclient.irc.IRCService;
import xyz.sonbn.ircclient.listener.ServerListener;
import xyz.sonbn.ircclient.model.Broadcast;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.model.Status;
import xyz.sonbn.ircclient.receiver.ServerReceiver;
import xyz.sonbn.ircclient.util.AppManager;

public class MainActivity extends AppCompatActivity implements ClientActivity, ActiveUserFragment.OnListFragmentInteractionListener, ServiceConnection, ServerListener {
    private static final String TAG = "MainActivity";
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private LinearLayout serverContainer;
    private View drawerEmptyView;
    private IRCBinder mBinder;
    private ServerReceiver mServerReceiver;
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);

        AppManager.getInstance().loadServer(this);

        mRealm = Realm.getDefaultInstance();

        initializeToolbar();
        initializeDrawer();

        if (savedInstanceState == null) {
            onOverview(null);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        toggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mServerReceiver = new ServerReceiver(this);
        registerReceiver(mServerReceiver, new IntentFilter(Broadcast.SERVER_UPDATE));

        Intent intent = new Intent(this, IRCService.class);
        startService(intent);

        bindService(intent, this, 0);

        updateDrawerServerList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mServerReceiver);
        if (mBinder != null && mBinder.getService() != null){
            mBinder.getService().checkServiceStatus();
        }

        unbindService(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return false;
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public IRCBinder getBinder() {
        return mBinder;
    }

    @Override
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onServerSelected(final Server server) {
        Bundle arguments = new Bundle();

        if (server.getStatus() == Status.DISCONNECTED){
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    server.setStatus(Status.PRE_CONNECTING);
                }
            });

            arguments.putBoolean(Extra.CONNECT, true);
        }
        arguments.putInt(Extra.SERVER_ID, server.getId());

        ChannelFragment channelFragment = new ChannelFragment();
        channelFragment.setArguments(arguments);

        switchToFragment(channelFragment, ChannelFragment.TRANSACTION_TAG + "-" + server.getId());
    }

    public void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void initializeDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);

        drawer.setDrawerListener(toggle);

        serverContainer = (LinearLayout) findViewById(R.id.server_container);

        drawerEmptyView = findViewById(R.id.drawer_empty_servers);
        drawerEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddServerActivity.class);
                startActivity(intent);

                drawer.closeDrawers();
            }
        });
    }

    public void updateDrawerServerList() {
        List<Server> servers = AppManager.getInstance().getServers();
        drawerEmptyView.setVisibility(servers.size() > 0 ? View.GONE : View.VISIBLE);

        serverContainer.removeAllViews();

        for (final Server server : servers) {
            TextView serverView = (TextView) getLayoutInflater().inflate(R.layout.item_drawer_server, drawer, false);
            serverView.setText(server.getTitle());

            Drawable left = ContextCompat.getDrawable(this, server.isConnected() ? R.drawable.ic_navigation_server_connected : R.drawable.ic_navigation_server_disconnected);
            serverView.setCompoundDrawablesWithIntrinsicBounds(
                    left,
                    null,
                    null,
                    null
            );

            int colorResource = server.isConnected() ? R.color.connected : R.color.disconnected;
            serverView.setTextColor(ContextCompat.getColor(this, colorResource));

            serverView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onServerSelected(server);

                    drawer.closeDrawers();
                }
            });

            serverContainer.addView(serverView, 0);
        }
    }

    public void onOverview(View view) {
        switchToFragment(new
                OverviewFragment(), OverviewFragment.TRANSACTION_TAG);
    }

    private void switchToFragment(Fragment fragment, String tag) {
        drawer.closeDrawers();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(tag) != null) {
            // We are already showing this fragment
            return;
        }

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                .replace(R.id.container, fragment, tag)
                .commit();
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (IRCBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
    }

    @Override
    public void onStatusUpdate() {
        updateDrawerServerList();
    }
}
