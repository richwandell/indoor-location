package sam.richwandell.com.myapplication.db;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class JsonArrayRequestWithData extends JsonObjectRequest {

    private final JSONArray data;

    public JsonArrayRequestWithData(JSONArray data, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);
        this.data = data;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("payload", data.toString());

        return params;
    }
}
