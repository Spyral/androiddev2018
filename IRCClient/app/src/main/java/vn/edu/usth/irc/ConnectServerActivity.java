package vn.edu.usth.irc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

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
        Utils.setupUserInfo(userName.getText().toString());

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
//        Function to check connection to the server
//        *
//        *
//        *
//        *
//        *
//        *
//        *
//        put here
        finish();
        SharedPreferences preferences = getSharedPreferences("first_time", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", true);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        SharedPreferences preferences = getSharedPreferences("first_time", Context.MODE_PRIVATE);
        if (preferences.getBoolean("RanBefore", false) == true)
        {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
        super.onBackPressed();
    }
}

