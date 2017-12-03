package vn.edu.usth.irc;

import android.icu.text.LocaleDisplayNames;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Integer.parseInt;


/**
 * Created by Local Boy on 12/3/2017.
 */

public class TaskSendMessage extends AsyncTask<String, Void, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            URL url = new URL("http://192.168.1.110/ChatApp/chat/send/");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestMethod("POST");

            JSONObject postMessageJson = new JSONObject();
            postMessageJson.put("sender", Utils.user.getUsername());
            postMessageJson.put("channel", Utils.user.getChannel());
            postMessageJson.put("message", Utils.user.getMessage());

            DataOutputStream localDataOutputStream = new DataOutputStream(conn.getOutputStream());
            localDataOutputStream.writeBytes(postMessageJson.toString());
            localDataOutputStream.flush();
            localDataOutputStream.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String string = StreamToString.iStreamToString(in);
            JSONObject getId = new JSONObject(string);
            Utils.user.setId(parseInt(getId.getJSONArray("sent").getJSONObject(0).getString("MAX(ID)")));

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
