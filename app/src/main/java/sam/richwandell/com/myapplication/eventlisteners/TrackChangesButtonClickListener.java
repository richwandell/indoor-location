package sam.richwandell.com.myapplication.eventlisteners;

import android.content.res.ColorStateList;
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
import sam.richwandell.com.myapplication.Wifi;


import static sam.richwandell.com.myapplication.RV.TAG;

public class TrackChangesButtonClickListener implements View.OnClickListener {

    private MainActivity main;

    public TrackChangesButtonClickListener(MainActivity main){
        this.main = main;
    }

    @Override
    public void onClick(View view) {
        main.resetFabColors();
        RV.mode = RV.MODE.LOCALIZING;

        int color = ContextCompat.getColor(view.getContext(), R.color.color3);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
        new Wifi(main).runLocalizer(new WifiLocalizationFinishedListener(main));
    }


}
