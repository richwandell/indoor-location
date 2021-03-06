package sam.richwandell.com.myapplication;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static sam.richwandell.com.myapplication.RV.TAG;

class WebAppInterface {

    private WebView wView;

    private MainActivity main;

    /** Instantiate the interface and set the context */
    WebAppInterface(WebView c, MainActivity main) {
        wView = c;
        this.main = main;
    }

    @JavascriptInterface
    public void setCurrentSize(String width, String height){
        Log.d(TAG, "Size: " + width + " " + height);
        int w = Integer.parseInt(width);
        int h = Integer.parseInt(height);
        RV.floorPlanWidth = w;
        RV.floorPlanHeight = h;

    }

    @JavascriptInterface
    public String getRecordedPoints(String fp_id){
        return "";
    }

    @JavascriptInterface
    public void setSpace(String x, String y, String floorPlanId){
        Log.d(TAG, x + " " + y + " " + floorPlanId);
        RV.floorPlanCoords = new int[]{Integer.parseInt(x), Integer.parseInt(y)};
        RV.floorPlanId = floorPlanId;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(wView.getContext(), toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getData2(int floorPlanIndex){
        Log.d(TAG, Integer.toString(floorPlanIndex));
        String id = RV.allFloorplans[floorPlanIndex].getId();
        RV.floorPlanId = id;
        return RV.allFloorplans[floorPlanIndex].toString();
    }

    @JavascriptInterface
    public String getData(String id){
        StringBuilder buf = new StringBuilder();
        InputStream json = null;
        String filename = "floorplans/" + id;
        Log.d(TAG, filename);
        try {
            json = wView.getContext().getAssets().open(filename);
        } catch (IOException e) {

            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        String str = "";

        try {
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("rdebug", buf.toString());
        return buf.toString();
    }
}