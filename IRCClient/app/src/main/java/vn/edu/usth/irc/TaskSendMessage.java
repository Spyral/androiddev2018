package vn.edu.usth.irc;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Local Boy on 12/3/2017.
 */

public class TaskSendMessage extends AsyncTask<String, Void, String> {
    private HttpURLConnection conn = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            URL url = new URL(Utils.ipAddress + "ChatApp/chat/send/");

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
