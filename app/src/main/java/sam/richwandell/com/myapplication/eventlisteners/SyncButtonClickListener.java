package sam.richwandell.com.myapplication.eventlisteners;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

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
import sam.richwandell.com.myapplication.R;
import sam.richwandell.com.myapplication.RV;
import sam.richwandell.com.myapplication.db.DBOpenHelper;

import static sam.richwandell.com.myapplication.RV.TAG;

public class SyncButtonClickListener implements View.OnClickListener {

    private MainActivity main;
    private View view;
    private ProgressDialog progress;

    public SyncButtonClickListener(MainActivity main){
        this.main = main;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "sync button clicked");
        main.resetFabColors();
        this.progress = new ProgressDialog(main);
        progress.setTitle("Sync");
        progress.setMessage("Saving data points to server...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();



        RV.mode = RV.MODE.UPDATING;
        this.view = view;
        int color = ContextCompat.getColor(view.getContext(), R.color.color3);
        view.setBackgroundTintList(ColorStateList.valueOf(color));

        DBOpenHelper mDbHelper = new DBOpenHelper(main);
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query("scan_results", new String[]{
                        "s_id", "fp_id", "ap_id", "x", "y", "value", "orig_values", "created"},
                null, null, null, null, null);

        JSONArray data = new JSONArray();

        try {
            cursor.moveToFirst();
            do {
                int s_id = cursor.getInt(0);
                String fp_id = cursor.getString(1);
                String ap_id = cursor.getString(2);
                int x = cursor.getInt(3);
                int y = cursor.getInt(4);
                double value = cursor.getDouble(5);
                String orig_values = cursor.getString(6);
                String time = cursor.getString(7);
                Log.d(TAG, Integer.toString(s_id) + ", " +
                        fp_id + ", "
                        + ap_id + ", "
                        + Integer.toString(x) + ", "
                        + Integer.toString(y) + ", "
                        + Double.toString(value) + ", "
                        + orig_values + ","
                        + time
                );
                JSONObject obj = new JSONObject();

                obj.put("s_id", s_id);
                obj.put("fp_id", fp_id);
                obj.put("ap_id", ap_id);
                obj.put("x", x);
                obj.put("y", y);
                obj.put("value", value);
                obj.put("orig_values", orig_values);
                obj.put("created", time);
                data.put(obj);

                cursor.moveToNext();
            } while (!cursor.isLast());
        }catch(Exception e){
            Log.d(TAG, "SyncButtonClickListener.onClick: " + e.toString());
            cursor.close();
            mDbHelper.close();
            progress.dismiss();
            main.resetFabColors();
        }finally {
            cursor.close();
            mDbHelper.close();
        }
        if(data.length() > 0){
            sendToServer(data);
            SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();
            writableDatabase.execSQL("delete from scan_results");

        }
    }

    private void sendToServer(JSONArray data){
        if(RV.upnpDevice == null) return;

        String endPoint = RV.upnpDevice.get("endPoint");
        if (endPoint != null) {
            RequestQueue queue = Volley.newRequestQueue(main);
            String url = endPoint + "saveReadings";
            Log.d(TAG, url);
            JSONObject json = new JSONObject();
            try {
                json.put("payload", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        int color = ContextCompat.getColor(view.getContext(), R.color.color1);
                        view.setBackgroundTintList(ColorStateList.valueOf(color));
                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                        int color = ContextCompat.getColor(view.getContext(), R.color.color1);
                        view.setBackgroundTintList(ColorStateList.valueOf(color));
                        progress.dismiss();
                    }
                }
            );
            queue.add(req);
        }
    }
}
