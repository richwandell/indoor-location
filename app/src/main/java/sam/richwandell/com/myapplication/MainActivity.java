package sam.richwandell.com.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.eventlisteners.FingerPrintClickListener;
import sam.richwandell.com.myapplication.eventlisteners.SyncButtonClickListener;
import sam.richwandell.com.myapplication.eventlisteners.ToggleScannedArea;
import sam.richwandell.com.myapplication.eventlisteners.TrackChangesButtonClickListener;
import sam.richwandell.com.myapplication.items.AndroidPhone;
import sam.richwandell.com.myapplication.items.Compass;
import sam.richwandell.com.myapplication.items.FanMenu;
import sam.richwandell.com.myapplication.upnp.UPnP;
import sam.richwandell.com.myapplication.upnp.UPnPListener;

import static sam.richwandell.com.myapplication.RV.TAG;

public class MainActivity extends AppCompatActivity {
    public ToggleScannedArea toggleScannedArea;
    public HomeLayout homeLayoutImageContainer;

    public FanMenu fm;

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 12345;

    private static final int GET_NEW_SERVER_RESULT = 12344;

    //Sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticfield;

    //Floating Action Buttons
    private FloatingActionButton fingerPrintButton;
    private FloatingActionButton syncButton;
    private FloatingActionButton trackChangesButton;


    private Compass compass;
    private AndroidPhone androidPhone;

    private FloorPlan selectedFloorPlan;
    private int yPos;
    private int xPos;

    UPnP upnp;
    private Sensor linearAccelerationSensor;
    private Sensor gravitySensor;

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            yPos = homeLayoutImageContainer.getScrollY();
            xPos = homeLayoutImageContainer.getScrollX();
            Log.d(TAG, "yPos = " + Integer.toString(yPos) + " xPos = " + Integer.toString(xPos));
        }
        return false;
    }

    public void setSelectedFloorPlan(FloorPlan fp){
        selectedFloorPlan = fp;
    }

    public FloorPlan getSelectedFloorPlan(){
        return selectedFloorPlan;
    }

    public void resetFabColors(){
        int color = ContextCompat.getColor(this, R.color.color1);
        fingerPrintButton.setBackgroundTintList(ColorStateList.valueOf(color));
        syncButton.setBackgroundTintList(ColorStateList.valueOf(color));
        trackChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RV.DEVICE_ID = Settings.Secure.getString(getContentResolver(),
            Settings.Secure.ANDROID_ID);

        //set up our toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up the sensor event listener
        this.compass = (Compass)findViewById(R.id.compasscontainer);
        this.androidPhone = new AndroidPhone(this);
        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        this.magneticfield = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.linearAccelerationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.gravitySensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        this.sensorManager.registerListener(this.compass, this.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.compass, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        this.homeLayoutImageContainer = (HomeLayout)findViewById(R.id.homelayoutcontainer);

        this.fingerPrintButton = (FloatingActionButton)findViewById(R.id.start_fingerprint);
        fingerPrintButton.setOnClickListener(new FingerPrintClickListener(this));

        this.syncButton = (FloatingActionButton)findViewById(R.id.sync_with_server);
        syncButton.setOnClickListener(new SyncButtonClickListener(this));

        this.trackChangesButton = (FloatingActionButton) findViewById(R.id.track_changes);
        trackChangesButton.setOnClickListener(new TrackChangesButtonClickListener(this));

        this.fm = (FanMenu)findViewById(R.id.the_fanmenu);

        this.toggleScannedArea = new ToggleScannedArea(this);

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

        upnp = new UPnP(this, new UPnPListener() {

            @Override
            public void onDiscover(UPnP.HeaderParser.UPnPDevice device) {
                String name = device.get("modelName");

                if(name != null) {
                    if(name.equals("location_tracker_server")){
                        RV.trackers.add(device);
                        Log.d(TAG, "found a location tracker server! " + device.get("modelNumber"));
                        RV.showTrackerServerSelection(main);
                        RV.upnpDevice = device;
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
        Intent i;

        switch(item.getItemId()){
            case R.id.action_load_floorplan:
                RV.showTrackerServerSelection(this);
                break;

            case R.id.action_settings:
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;

            case R.id.action_toggle_scanned_area:
                this.toggleScannedArea.toggle();
                break;

            case R.id.action_set_server:
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                String ip = "";
                String port = "";
                if(sharedPref.contains(CreateServerActivity.NEW_SERVER_IP)){
                    ip = sharedPref.getString(CreateServerActivity.NEW_SERVER_IP, "");
                }
                if(sharedPref.contains(CreateServerActivity.NEW_SERVER_PORT)){
                    port = sharedPref.getString(CreateServerActivity.NEW_SERVER_PORT, "");
                }
                i = new Intent(this, CreateServerActivity.class);
                i.putExtra(CreateServerActivity.NEW_SERVER_IP, ip);
                i.putExtra(CreateServerActivity.NEW_SERVER_PORT, port);
                startActivityForResult(i, GET_NEW_SERVER_RESULT);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if(resultCode == RESULT_OK) {
            String serverIp = data.getStringExtra(CreateServerActivity.NEW_SERVER_IP);
            String serverPort = data.getStringExtra(CreateServerActivity.NEW_SERVER_PORT);

            String headers = "200/OK\r\nLOCATION:http://"+serverIp+":"+serverPort + "/devicedescription.xml";
            upnp.add(headers);

            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(CreateServerActivity.NEW_SERVER_IP, serverIp);
            editor.putString(CreateServerActivity.NEW_SERVER_PORT, serverPort);
            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.sensorManager.registerListener(this.compass, this.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.compass, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this.androidPhone, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sensorManager.unregisterListener(this.compass);
        this.sensorManager.unregisterListener(this.androidPhone);
    }
}
