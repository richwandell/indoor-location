package sam.richwandell.com.myapplication.eventlisteners;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.RV;

import static sam.richwandell.com.myapplication.RV.TAG;

public class ToggleScannedArea {

    private boolean on;
    private MainActivity main;

    public ToggleScannedArea(MainActivity main) {
        this.on = false;
        this.main = main;
    }


    public void toggle() {
        this.on = !this.on;

        if(this.on){
            this.getScannedArea();
        }else{
            main
                .homeLayoutImageContainer
                .loadUrl("javascript:toggleScannedArea()");
        }
    }

    public void getScannedArea() {
        Log.d(TAG, "ToggleScannedArea.getScannedArea");
        String endPoint = RV.upnpDevice.get("endPoint");
        if(RV.floorPlanId == null) return;

        String fp_id = RV.floorPlanId;
        if (endPoint != null) {
            RequestQueue queue = Volley.newRequestQueue(main);
            String url = endPoint + "getScannedCoords/" + fp_id;
            Log.d(TAG, url);

            JsonArrayRequest req = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            String jsonData = response.toString();
                            main
                                .homeLayoutImageContainer
                                .loadUrl("javascript:updateScannedArea('" + jsonData + "')");
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
