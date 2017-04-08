package sam.richwandell.com.myapplication.eventlisteners;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.RV;

import static sam.richwandell.com.myapplication.RV.TAG;

public class WifiLocalizationFinishedListener implements LocalizationFinishedListener {

    private MainActivity main;

    public WifiLocalizationFinishedListener(MainActivity main){

        this.main = main;
    }

    @Override
    public void onWifiLocalizationFinished(JSONObject wifiResult) {
        Log.d(TAG, wifiResult.toString());
        String endPoint = RV.upnpDevice.get("endPoint");
        if (endPoint != null) {
            RequestQueue queue = Volley.newRequestQueue(main);
            String url = endPoint + "localize";
            Log.d(TAG, url);


            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, wifiResult,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray guess = response.getJSONArray("guess");

                            int x = guess.getInt(0);
                            int y = guess.getInt(1);
                            main.homeLayoutImageContainer
                                .loadUrl("javascript:clickCanvas('" + Integer.toString(x) + "', '" + Integer.toString(y) + "')");
                            main.toggleScannedArea.getScannedArea();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }
            );
            queue.add(req);
        }
    }
}
