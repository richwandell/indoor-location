package sam.richwandell.com.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticfield;
    float[] accelValues;
    float[] magnetValues;
    private float[] rotationMatrix = new float[9];
    private float[] orientationMatrix = new float[3];

    private float currentDegree = 0f;

    boolean accelValuesSet = false;
    boolean magnetValuesSet = false;

    View compassContainer;
    View homeLayoutImageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticfield = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeLayoutImageContainer.setVisibility(
                        homeLayoutImageContainer.getVisibility() == View.VISIBLE ?
                        View.INVISIBLE : View.VISIBLE);
            }
        });
        compassContainer = findViewById(R.id.compasscontainer);
        homeLayoutImageContainer = findViewById(R.id.homelayoutcontainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetValues = sensorEvent.values;
            magnetValuesSet = true;
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelValues = sensorEvent.values;
            accelValuesSet = true;
        }

        if(accelValuesSet && magnetValuesSet){
            SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magnetValues);
            SensorManager.getOrientation(rotationMatrix, orientationMatrix);
            float azimuthInRadians = orientationMatrix[0];
            float azimuthInDegrees = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(currentDegree, -azimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

            ra.setDuration(250);
            ra.setFillAfter(true);
            compassContainer.startAnimation(ra);
//            if(homeLayoutImageContainer.getVisibility() == View.VISIBLE) {
//                RotateAnimation ra1 = new RotateAnimation(currentDegree, -azimuthInDegrees,
//                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//                ra1.setDuration(250);
//                ra1.setFillAfter(true);
//                homeLayoutImage.startAnimation(ra1);
//            }

            currentDegree = -azimuthInDegrees;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magneticfield, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
