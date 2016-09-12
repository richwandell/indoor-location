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


public class WebAppInterface {
    WebView wView;

    /** Instantiate the interface and set the context */
    WebAppInterface(WebView c) {
        wView = c;
    }

    @JavascriptInterface
    public void setSpace(String coords, String name, int floorPlanId){
        RV.floorPlanCoords = coords;
        RV.spaceName = name;
        RV.floorPlanId = floorPlanId;
        new Wifi().runScan();
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(wView.getContext(), toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getData2(int floorPlanIndex){
        Log.d("rdebug", Integer.toString(floorPlanIndex));
        int id = RV.allFloorplans[floorPlanIndex].getId();
        RV.floorPlanId = id;
        return RV.allFloorplans[floorPlanIndex].toString();
    }

    @JavascriptInterface
    public String getData(String id){
        StringBuilder buf = new StringBuilder();
        InputStream json = null;
        String filename = "floorplans/" + id;
        Log.d("rdebug", filename);
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