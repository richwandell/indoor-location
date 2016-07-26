package sam.richwandell.com.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

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

    private void init(){
        loadUrl("file:///android_asset/index.html");
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
    }
}
