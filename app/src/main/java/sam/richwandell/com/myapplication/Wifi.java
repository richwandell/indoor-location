package sam.richwandell.com.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import sam.richwandell.com.myapplication.db.DBOpenHelper;
import sam.richwandell.com.myapplication.eventlisteners.LocalizationFinishedListener;

import static sam.richwandell.com.myapplication.RV.TAG;


public class Wifi {

    private boolean scanEnabled = true;

    private WifiManager mWifiManager;

    private final int numScans = 4;

    private int scanNumber;

    private ProgressDialog progress;

    private HashMap<String, ILocationKalman> locationKalmanMap;

    private MainActivity main;

    private LocalizationFinishedListener listener;

    private RV.MODE mode;

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                for(ScanResult s : mScanResults){
                    String name = s.BSSID;
                    if(name == null){
                        name = s.SSID;
                    }
                    double level = (double)s.level;
                    ILocationKalman kf;
                    if(!locationKalmanMap.containsKey(name)){
                        kf = new ILocationKalman(level);
                        locationKalmanMap.put(name, kf);
                    }else{
                        kf = locationKalmanMap.get(name);
                        kf.addSample(level);
                    }
                }

                if(scanNumber > 0){
                    scanNumber--;
                    progress.setMessage("Scanning Location " + Integer.toString(numScans - scanNumber) + "/" + numScans);
                    mWifiManager.startScan();
                }else{
                    if(mode == RV.MODE.FINGERPRINTING) {
                        finishScan();
                    }else{
                        finishLocalizing();
                    }
                }
            }
        }
    };


    public Wifi(MainActivity main){
        this.main = main;
    }

    public void runLocalizer(LocalizationFinishedListener listener){
        this.listener = listener;
        RV.mode = RV.MODE.LOCALIZING;
        this.mode = RV.mode;
        if(scanEnabled && RV.floorPlanId != null){
            scanEnabled = false;
            scanNumber = 0;
            Log.d(TAG, "scan enabled");
            locationKalmanMap = new HashMap<>();
            main.registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            mWifiManager = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mWifiManager.startScan();
        }else if(RV.floorPlanId == null){
            RV.showTrackerServerSelection(main);
            main.fm.toggleMenuItems();
        }
    }

    public void runScan() {
        RV.mode = RV.MODE.FINGERPRINTING;
        this.mode = RV.mode;
        if(scanEnabled && RV.floorPlanId != null){
            scanNumber = numScans;
            progress = new ProgressDialog(main);
            progress.setMessage("Scanning Location 1/" + scanNumber);
            progress.setCancelable(true);
            progress.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    scanNumber = 0;
                    finishScan();
                }
            });
            progress.show();
            Log.d(TAG, "scan enabled");
            locationKalmanMap = new HashMap<>();
            main.registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            mWifiManager = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mWifiManager.startScan();
            scanEnabled = false;
            main.fm.toggleMenuItems();
        }else if(RV.floorPlanId == null){
            RV.showTrackerServerSelection(main);
            main.fm.toggleMenuItems();
        }
    }

    private void finishLocalizing(){
        main.unregisterReceiver(mWifiScanReceiver);
        JSONObject json = new JSONObject();
        String fp_id = RV.floorPlanId;
        try {
            json.put("action", "localize");
            json.put("fp_id", fp_id);
            JSONArray aps = new JSONArray();
            for(String ap_id : locationKalmanMap.keySet()) {
                ILocationKalman km = locationKalmanMap.get(ap_id);
                JSONObject ap = new JSONObject();
                ap.put("ap_id", ap_id);
                ap.put("value", km.getFirstMeasurement());
                aps.put(ap);
            }
            json.put("ap_ids", aps);
            json.put("device_id", RV.DEVICE_ID);
            json.put("type", "PHONE");
            listener.onWifiLocalizationFinished(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void finishScan(){
        Log.d(TAG, "Wifi.finishScan");
        main.unregisterReceiver(mWifiScanReceiver);
        scanEnabled = true;
        progress.hide();

        new Thread() {
            @Override
            public void run() {
                DBOpenHelper mDbHelper = new DBOpenHelper(main);

                SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();
                long id = writableDatabase.insert("scan", "s_id", null);
                String fp_id = RV.floorPlanId;
                int[] coords = RV.floorPlanCoords;

                for(String k : locationKalmanMap.keySet()){
                    ILocationKalman km = locationKalmanMap.get(k);
                    Log.d(TAG,
                            "{\"" + k + "\": { " +
                                    "\"values\": [" +
                                    km.toString() + "], " +
                                    "\"kalman\": " + Double.toString(km.getStateEstimation()) + "}}");

                    ContentValues values = new ContentValues();
                    values.put("s_id", id);
                    values.put("fp_id", fp_id);
                    values.put("ap_id", k);
                    values.put("x", coords[0]);
                    values.put("y", coords[1]);
                    values.put("value", km.getStateEstimation());
                    values.put("orig_values", "[" + km.toString() + "]");
                    writableDatabase.insert("scan_results", null, values);
                }
                DBOpenHelper.debugAll(main);
            }
        }.run();
    }
}
