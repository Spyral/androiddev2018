package vn.edu.usth.irc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.RequestQueue;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navView;
    public TabLayout tabLayout;
    private int pageLimit = 2;

    private Handler h = new Handler();
    private int delay = 2000; //2 seconds
    private Runnable runnable;

    public SharedPreferences preferences;
    public boolean ranBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("first_time", Context.MODE_PRIVATE);
        ranBefore = preferences.getBoolean("RanBefore", false);
        if (ranBefore == false)
        {
            Intent i = new Intent(this, ConnectServerActivity.class);
            startActivity(i);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Drawer Layout and NavigationView code
        mDrawerLayout = (DrawerLayout) findViewById(R.id.container);
        mToggle       = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navView = (NavigationView) findViewById(R.id.menu_drawer);
        if (navView != null)
        {
            setupDrawerContent(navView);
        }

        // ViewPager and TabLayout code
        PagerAdapter pagerAdapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(pageLimit);
        mPager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(mPager);

        RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
    }

    //start handler as activity become visible
    @Override
    protected void onResume() {
        h.postDelayed(new Runnable() {
            public void run() {
                // do something
                new TaskCheckNewMess(getApplicationContext(), Utils.user.getChannel()).fetchNewMessID();

                runnable = this;
                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    //stop handler when activity not visible
    @Override
    protected void onPause() {
        h.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navView) {
        navView.setNavigationItemSelectedListener(this);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.create_channel) {
            selectCurrentChannel(item);
        }
        return true;
    }

    private void addNewChannelInNavDrawer(String newChannelName) {
        Menu menu = navView.getMenu();
        MenuItem newChannel = menu.add(R.id.menu_top, Menu.NONE, 0, newChannelName);
        selectCurrentChannel(newChannel);
    }

    private void selectCurrentChannel(MenuItem item) {
        Utils.user.setChannel((String) item.getTitle());
        item.setCheckable(true);
        item.setChecked(true);
        mDrawerLayout.closeDrawers();
        tabLayout.getTabAt(0).select();
    }

    public void createAndShowNewChannelDialog(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Enter a name:");

        final EditText editText = new EditText(this);
        editText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        builder.setView(editText);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String newChannelName = editText.getText().toString();
                addNewChannelInNavDrawer(newChannelName);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
        private final int PAGE_COUNT = 3;
        private String titles[] = new String[] { "Messages", "People", "Servers" };

        public HomeFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT; // number of pages for a ViewPager
        }

        @Override
        public Fragment getItem(int page) {
            // returns an instance of Fragment corresponding to the specified page
            switch (page) {
                case 0: return new ChatboxAndSenderFragment();
                case 1: return new PeopleFragment();
                case 2: return new ServerFragment();
            }
            return new Fragment();
        }

        @Override
        public CharSequence getPageTitle(int page) {
            // returns a tab title corresponding to the specified page
            return titles[page];
        }
    }
    
    public void newServer(View view){
        Intent i = new Intent(this, ConnectServerActivity.class);
        startActivity(i);
        finish();
    }
}
