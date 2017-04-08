package sam.richwandell.com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import java.io.IOException;

public class HomeLayout extends WebView {


    private MainActivity main;

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

    public void setMain(MainActivity main){
        this.main = main;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {

        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(RV.mode == RV.MODE.LOCALIZING){
                    RV.mode = RV.MODE.FINGERPRINTING;
                }
                return false;
//                return RV.mode == RV.MODE.LOCALIZING;
            }
        });

        if (!this.isInEditMode()) {
            getSettings().setUserAgentString(
                    getSettings().getUserAgentString()
                            + " "
                            + getContext().getString(R.string.user_agent_suffix)
            );
            getSettings().setBuiltInZoomControls(false);
            getSettings().setDisplayZoomControls(false);
            getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(
                    new WebAppInterface(this, main),
                    "Android"
            );
            loadUrl("file:///android_asset/builder.html");
        }
    }
}
