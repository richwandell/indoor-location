package sam.richwandell.com.myapplication;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import sam.richwandell.com.myapplication.eventlisteners.FingerPrintClickListener;
import sam.richwandell.com.myapplication.items.Compass;
import sam.richwandell.com.myapplication.items.FanMenu;
import sam.richwandell.com.myapplication.upnp.UPnP;
import sam.richwandell.com.myapplication.upnp.UPnPListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up our toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up the sensor event listener
        RV.compass = (Compass)findViewById(R.id.compasscontainer);
        RV.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        RV.magneticfield = RV.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        RV.sensorManager.registerListener(RV.compass, RV.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        RV.accelerometer = RV.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        RV.sensorManager.registerListener(RV.compass, RV.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        RV.homeLayoutImageContainer = findViewById(R.id.homelayoutcontainer);
        RV.mainActivity = this;

        FloatingActionButton fingerPrintButton = (FloatingActionButton)findViewById(R.id.start_fingerprint);
        fingerPrintButton.setOnClickListener(new FingerPrintClickListener());
        RV.fm = (FanMenu)RV.mainActivity.findViewById(R.id.the_fanmenu);

        final UPnP upnp = new UPnP(this, new UPnPListener() {

            @Override
            public void onDiscover(UPnP.HeaderParser.UPnPDevice device) {
                String name = device.get("modelName");

                if(name != null) {
                    if(name.equals("location_tracker_server")){
                        RV.trackers.add(device);
                        Log.d("rdebug", "found a location tracker server! " + device.get("modelNumber"));
                        RV.showTrackerServerSelection();
                    }
                }
            }

        });

        final Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                upnp.discover();
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
                RV.showFloorPlanSelection();
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
        RV.sensorManager.registerListener(RV.compass, RV.magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        RV.sensorManager.registerListener(RV.compass, RV.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RV.sensorManager.unregisterListener(RV.compass);
    }
}
