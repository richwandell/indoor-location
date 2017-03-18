package sam.richwandell.com.myapplication.eventlisteners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.RelativeLayout;

import sam.richwandell.com.myapplication.MainActivity;

public class MovementSensorEventListener extends RelativeLayout implements SensorEventListener {

    private final MainActivity main;

    public MovementSensorEventListener(MainActivity main) {
        super(main);
        this.main = main;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch(type){
            case Sensor.TYPE_ACCELEROMETER:
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                break;
        }
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            magnetValues = sensorEvent.values;
//            magnetValuesSet = true;
//        }
//
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            accelValues = sensorEvent.values;
//            accelValuesSet = true;
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
