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

import java.util.HashMap;
import java.util.List;

import sam.richwandell.com.myapplication.db.DBOpenHelper;


public class Wifi {
    private boolean scanEnabled = true;
    private WifiManager mWifiManager;

    private final int numScans = 10;

    private int scanNumber;

    ProgressDialog progress;

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
                    if(!RV.locationKalmanMap.containsKey(name)){
                        kf = new ILocationKalman(level);
                        RV.locationKalmanMap.put(name, kf);
                    }else{
                        kf = RV.locationKalmanMap.get(name);
                        kf.addSample(level);
                    }
                }

                if(scanNumber > 0){
                    scanNumber--;
                    progress.setMessage("Scanning Location " + Integer.toString(numScans - scanNumber) + "/" + numScans);
                    mWifiManager.startScan();
                }else{
                    finishScan();
                }
            }
        }
    };

    public void runScan() {

        RV.mode = RV.MODE.FINGERPRINTING;
        RV.mainActivity.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                if(scanEnabled && RV.floorPlanId > -1){
                    scanNumber = numScans;
                    progress = new ProgressDialog(RV.mainActivity);
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
                    Log.d("rdebug", "scan enabled");
                    RV.locationKalmanMap = new HashMap<>();
                    RV.mainActivity.registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    mWifiManager = (WifiManager) RV.mainActivity.getSystemService(Context.WIFI_SERVICE);
                    mWifiManager.startScan();
                    scanEnabled = false;
                    RV.fm.toggleMenuItems();
                }else if(RV.floorPlanId == -1){
                    RV.showFloorPlanSelection();
                    RV.fm.toggleMenuItems();
                }
            }
        });

    }

    private void finishScan(){
        RV.mainActivity.unregisterReceiver(mWifiScanReceiver);
        scanEnabled = true;
        progress.hide();
        DBOpenHelper mDbHelper = new DBOpenHelper(RV.mainActivity);

        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();
        long id = writableDatabase.insert("scan", "s_id", null);
        int fp_id = RV.floorPlanId;
        String coords = RV.floorPlanCoords;

        for(String k : RV.locationKalmanMap.keySet()){
            ILocationKalman km = RV.locationKalmanMap.get(k);
            Log.d("rdebug",
                    "{\"" + k + "\": { " +
                            "\"values\": [" +
                            km.toString() + "], " +
                            "\"kalman\": " + Double.toString(km.getStateEstimation()) + "}}");

            ContentValues values = new ContentValues();
            values.put("s_id", id);
            values.put("fp_id", fp_id);
            values.put("ap_id", k);
            values.put("coords", coords);
            values.put("value", km.getStateEstimation());
            values.put("orig_values", "[" + km.toString() + "]");
            writableDatabase.insert("scan_results", null, values);
        }
        DBOpenHelper.debugAll();
    }
}
