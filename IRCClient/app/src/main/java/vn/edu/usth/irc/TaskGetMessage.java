package vn.edu.usth.irc;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Local Boy on 12/3/2017.
 */

public class TaskGetMessage {
    private Context context;
    private String channel;

    public TaskGetMessage(Context context, String channel) {
        this.context = context;
        this.channel = channel;
    }

    public void fetchMessage() {
        String targetId = Integer.toString(Utils.getNewestMessIdLocal());
        String url = "http://"+Utils.ipAddress+"ChatApp/chat/receive/" + channel + "/" + targetId;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray received = response.getJSONArray("received");

                            for (int i = 1; i < received.length(); i++) {
                                JSONObject chat = received.getJSONObject(i);
                                String sender = chat.getString("Sender");
                                String message = chat.getString("Message");
                                ChatboxFragment.updateChat(sender, message);
                            }

                            Utils.setNewestMessIdLocal(Utils.getNewestMessIdServer());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                });

        VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }
}
