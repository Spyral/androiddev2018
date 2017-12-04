package vn.edu.usth.irc;

import android.content.Context;
import android.widget.Toast;

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

public class TaskCheckNewMess {
    private Context context;
    private String channel;

    public TaskCheckNewMess(Context context, String channel) {
        this.context = context;
        this.channel = channel;
    }

    public void fetchNewMessID() {
        String url = Utils.ipAddress + "ChatApp/chat/check_mess/" + channel;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray check = response.getJSONArray("check");

                            for (int i=0; i < check.length(); i++) {
                                JSONObject idArray = check.getJSONObject(i);
                                String newId = idArray.getString("MAX(ID)");
                                Utils.setNewestMessIdServer(Integer.parseInt(newId));
                            }

                            // Fetch new message ONLY after new message check has done.
                            if (Utils.getNewestMessIdServer() > Utils.getNewestMessIdLocal()) {
                                new TaskGetMessage(context, channel).fetchMessage();
                            }

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
