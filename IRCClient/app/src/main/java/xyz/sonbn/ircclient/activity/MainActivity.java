package xyz.sonbn.ircclient.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import io.realm.Realm;
import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.fragment.ServerFragment;
import xyz.sonbn.ircclient.fragment.OverviewFragment;
import xyz.sonbn.ircclient.model.Extra;
import xyz.sonbn.ircclient.model.Server;
import xyz.sonbn.ircclient.util.AppManager;

public class MainActivity extends AppCompatActivity implements ClientActivity {
    private static final String TAG = "MainActivity";
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private LinearLayout serverContainer;
    private View drawerEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);

        AppManager.getInstance().loadServer(this);

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
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onServerSelected(Server server) {
        Bundle arguments = new Bundle();

        arguments.putInt(Extra.SERVER_ID, server.getId());
        ServerFragment serverFragment = new ServerFragment();
        serverFragment.setArguments(arguments);
        switchToFragment(serverFragment, ServerFragment.TRANSACTION_TAG + "-" + server.getId());
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
                .replace(R.id.container, fragment, tag)
                .commit();
    }
}
