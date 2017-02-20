package sam.richwandell.com.myapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import sam.richwandell.com.myapplication.eventlisteners.FingerPrintClickListener;
import sam.richwandell.com.myapplication.eventlisteners.SyncButtonClickListener;
import sam.richwandell.com.myapplication.items.Compass;
import sam.richwandell.com.myapplication.items.FanMenu;
import sam.richwandell.com.myapplication.upnp.UPnP;
import sam.richwandell.com.myapplication.upnp.UPnPListener;
import static sam.richwandell.com.myapplication.RV.TAG;

public class MainActivity extends AppCompatActivity {

    public HomeLayout homeLayoutImageContainer;
    private Compass compass;
    public FanMenu fm;

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up our toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up the sensor event listener
        this.compass = (Compass)findViewById(R.id.compasscontainer);
        RV.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        RV.magneticfield = RV.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        RV.sensorManager.registerListener(this.compass, RV.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        RV.accelerometer = RV.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        RV.sensorManager.registerListener(this.compass, RV.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.homeLayoutImageContainer = (HomeLayout)findViewById(R.id.homelayoutcontainer);

        FloatingActionButton fingerPrintButton = (FloatingActionButton)findViewById(R.id.start_fingerprint);
        fingerPrintButton.setOnClickListener(new FingerPrintClickListener(this));

        FloatingActionButton syncButton = (FloatingActionButton)findViewById(R.id.sync_with_server);
        syncButton.setOnClickListener(new SyncButtonClickListener(this));

        this.fm = (FanMenu)findViewById(R.id.the_fanmenu);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        } else {
            upnpDiscover();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        upnpDiscover();
                    }
                }
                break;
        }
    }


    private void upnpDiscover(){
        final MainActivity main = this;

        final UPnP upnp = new UPnP(this, new UPnPListener() {

            @Override
            public void onDiscover(UPnP.HeaderParser.UPnPDevice device) {
                String name = device.get("modelName");

                if(name != null) {
                    if(name.equals("location_tracker_server")){
                        RV.trackers.add(device);
                        Log.d(TAG, "found a location tracker server! " + device.get("modelNumber"));
                        RV.showTrackerServerSelection(main);
                    }
                }
            }

        });

        final Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                if(RV.floorPlanId == null) {
                    Log.d(TAG, "upnp discovering again...");
                    upnp.discover();
                }
                handler.postDelayed(this, 20000);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_load_floorplan:
                RV.showTrackerServerSelection(this);
                break;

            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RV.sensorManager.registerListener(this.compass, RV.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        RV.sensorManager.registerListener(this.compass, RV.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RV.sensorManager.unregisterListener(this.compass);
    }
}
