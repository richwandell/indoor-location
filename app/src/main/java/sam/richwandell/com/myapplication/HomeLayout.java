package sam.richwandell.com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
