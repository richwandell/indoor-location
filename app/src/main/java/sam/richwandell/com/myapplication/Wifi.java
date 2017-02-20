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
import static sam.richwandell.com.myapplication.RV.TAG;


public class Wifi {

    private boolean scanEnabled = true;

    private WifiManager mWifiManager;

    private final int numScans = 3;

    private int scanNumber;

    private ProgressDialog progress;


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
    private MainActivity main;

    public Wifi(MainActivity main){
        this.main = main;
    }

    public void runScan() {

        RV.mode = RV.MODE.FINGERPRINTING;
        main.runOnUiThread(new Runnable(){

            @Override
            public void run() {
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
                    RV.locationKalmanMap = new HashMap<>();
                    main.registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    mWifiManager = (WifiManager) main.getSystemService(Context.WIFI_SERVICE);
                    mWifiManager.startScan();
                    scanEnabled = false;
                    main.fm.toggleMenuItems();
                }else if(RV.floorPlanId == null){
                    RV.showTrackerServerSelection(main);
                    main.fm.toggleMenuItems();
                }
            }
        });

    }

    private void finishScan(){
        Log.d(TAG, "Wifi.finishScan");
        main.unregisterReceiver(mWifiScanReceiver);
        scanEnabled = true;
        progress.hide();
        DBOpenHelper mDbHelper = new DBOpenHelper(main);

        SQLiteDatabase writableDatabase = mDbHelper.getWritableDatabase();
        long id = writableDatabase.insert("scan", "s_id", null);
        String fp_id = RV.floorPlanId;
        int[] coords = RV.floorPlanCoords;

        for(String k : RV.locationKalmanMap.keySet()){
            ILocationKalman km = RV.locationKalmanMap.get(k);
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
}
