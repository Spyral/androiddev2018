package vn.edu.usth.irc;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Local Boy on 12/3/2017.
 */

public class TaskSendMessage extends AsyncTask<String, Void, String> {

    StringBuilder sb = new StringBuilder();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        URL url = null;

        try {
            url = new URL("http://192.168.1.43/ChatApp/chat/send/Tom/1/hello");

            JSONObject postMessageJson = new JSONObject();
            postMessageJson.put("sender", Utils.user.getUsername());
            postMessageJson.put("channel", Utils.user.getChannel());
            postMessageJson.put("message", Utils.user.getMessage());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream localDataOutputStream = new DataOutputStream(conn.getOutputStream());
            localDataOutputStream.writeBytes(URLEncoder.encode(postMessageJson.toString(),"UTF-8"));
            localDataOutputStream.flush();
            localDataOutputStream.close();

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
