package sam.richwandell.com.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import java.io.IOException;

public class HomeLayout extends WebView {

    public HomeLayout(Context context) {
        super(context);
        init();
    }

    public HomeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HomeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public HomeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (!this.isInEditMode()) {
            loadUrl("file:///android_asset/android.html");
            getSettings().setBuiltInZoomControls(true);
            getSettings().setDisplayZoomControls(false);
            getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(
                    new WebAppInterface(this),
                    "Android"
            );
        }
    }

    public static String[] getFloorPlans() {

        String[] list = null;
        try {
            list = RV.mainActivity.getAssets().list("floorplans");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null) {
            for (String l : list) {
                Log.d("rdebug", l);
            }
        }

        return list;
    }
}
