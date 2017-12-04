package vn.edu.usth.irc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Integer.parseInt;

public class ConnectServerActivity extends AppCompatActivity {

    public EditText userName;
    public EditText realName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_sever);

        userName = (EditText) findViewById(R.id.username);
        realName = (EditText) findViewById(R.id.real_name);
    }

    public void connect (View view){
        //        String sender = userName.getText().toString();
        Utils.setupUserInfo(userName.getText().toString());
        checkUsername();
        Log.i("username", Utils.user.getUsername());
    }

    public void createChatActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        SharedPreferences preferences = getSharedPreferences("first_time", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", true);
        editor.commit();
    }

    public void beenTaken(){
        recreate();
        Toast.makeText(this, "Username has been taken", Toast.LENGTH_SHORT).show();
    }

    public void checkUsername () {
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            HttpURLConnection conn;
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL("http://"+Utils.ipAddress+"/ChatApp/chat/check_sender/");

                    conn = (HttpURLConnection) url.openConnection();

                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestMethod("POST");

                    JSONObject postMessageJson = new JSONObject();
                    postMessageJson.put("sender", Utils.user.getUsername());

                    DataOutputStream localDataOutputStream = new DataOutputStream(conn.getOutputStream());
                    localDataOutputStream.writeBytes(postMessageJson.toString());
                    localDataOutputStream.flush();
                    localDataOutputStream.close();

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String string = StreamToString.iStreamToString(in);
                    JSONObject getSender = new JSONObject(string);
                    Utils.user.setUserCount(parseInt(getSender.getJSONArray("check").getJSONObject(0).getString("COUNT(Sender)")));
                } catch (Exception e) {
                    e.printStackTrace();

                }
                finally {
                    conn.disconnect();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                if (Utils.user.getUserCount() >= 1)
                {
                    beenTaken();
                }
                else
                {
                    createChatActivity();
                }
            }
        };
        task.execute();
    }

}

