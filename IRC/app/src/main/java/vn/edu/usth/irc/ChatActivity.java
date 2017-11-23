package vn.edu.usth.irc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;


public class ChatActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navView;

    public String[] channels = {"# general"};

    public SharedPreferences preferences;
    public boolean ranBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("first_time", Context.MODE_PRIVATE);
        ranBefore = preferences.getBoolean("RanBefore", false);
        if (ranBefore == false)
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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
        item.setCheckable(true);
        item.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void addNewChannelInNavDrawer(String newChannelName) {
        Menu menu = navView.getMenu();
        MenuItem newChannel = menu.add(R.id.menu_top, Menu.NONE, 0, newChannelName);
        newChannel.setCheckable(true);
        newChannel.setChecked(true);
    }

    public void createAndShowLogOutDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_logout, null));

        builder.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                GoBackToLogIn();
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

    public void createAndShowNewChannelDialog(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a name:");

        final EditText editText = new EditText(this);
        builder.setView(editText);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String newChannelName = "# " + editText.getText().toString();
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
    public void GoBackToLogIn(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
        SharedPreferences preferences = getSharedPreferences("first_time", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", false);
        editor.apply();
    }

    public void OpenSettings(View view){
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void ServerSelection(View view){
        Intent i = new Intent(this, ServerActivity.class);
        startActivity(i);
    }

    public void connect(View view){
        AsyncTask<String, Integer, Socket> task = new AsyncTask<String, Integer, Socket>() {

            @Override
            protected void onPreExecute() {
                Log.i("Chat activity", "start Asynctask");
                Toast.makeText(ChatActivity.this, "Start socket connection", Toast.LENGTH_SHORT).show();
                super.onPreExecute();
            }

            @Override
            protected Socket doInBackground(String... strings) {
                Socket socket = null;
                try {
                    socket = new Socket(strings[0], 6667);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return socket;
            }

            @Override
            protected void onPostExecute(Socket socket) {
                Toast.makeText(ChatActivity.this, "Done", Toast.LENGTH_SHORT).show();
                Log.i("Socket", "InetAddress: " + socket);
                super.onPostExecute(socket);
            }
        };
        task.execute("irc.freenode.net");
    }
}
