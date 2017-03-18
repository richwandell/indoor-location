package sam.richwandell.com.myapplication.items;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.db.FloorPlan;

import static sam.richwandell.com.myapplication.RV.TAG;


public class AndroidPhone extends RelativeLayout implements SensorEventListener {

    float[] accelValues;
    float[] magnetValues;
    private float[] rotationMatrix = new float[9];
    private float[] orientationMatrix = new float[3];

    private float currentDegree = 0f;

    boolean accelValuesSet = false;
    boolean magnetValuesSet = false;
    private MainActivity main;

    public void setMain(MainActivity main){
        this.main = main;
    }

    public AndroidPhone(Context context) {
        super(context);
    }

    public AndroidPhone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AndroidPhone(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AndroidPhone(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getCurrentDegree(){
        return currentDegree;
    }

    public void stepSensorChanged(SensorEvent sensorEvent){
        Log.d(TAG, "hi rich");
        float[] diffs = {
                Math.abs(0f - currentDegree),
                Math.abs(45f - currentDegree),
                Math.abs(90f - currentDegree),
                Math.abs(135f - currentDegree),
                Math.abs(180f - currentDegree),
                Math.abs(225f - currentDegree),
                Math.abs(270f - currentDegree),
                Math.abs(315f - currentDegree),
                Math.abs(360f - currentDegree)
        };
        StringBuilder sb = new StringBuilder();
        sb.append(currentDegree);
        sb.append(", ");
        for(float i : diffs){
            sb.append(i);
            sb.append(", ");
        }
        Log.d(TAG, sb.toString());
    }

    public void magnetChanged(SensorEvent sensorEvent){
        magnetValues = sensorEvent.values;
        magnetValuesSet = true;
        updateCurrentRotation(sensorEvent);
    }

    public void accelChanged(SensorEvent sensorEvent){
        accelValues = sensorEvent.values;
        accelValuesSet = true;
        updateCurrentRotation(sensorEvent);
    }

    public void updateCurrentRotation(SensorEvent sensorEvent){
        FloorPlan fp = main.getSelectedFloorPlan();
        int extraRotation = 0;
        if(fp != null){
            extraRotation = fp.getRotation();
        }

        if(accelValuesSet && magnetValuesSet){
            SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magnetValues);
            SensorManager.getOrientation(rotationMatrix, orientationMatrix);
            float azimuthInRadians = orientationMatrix[0];
            float azimuthInDegrees = (float)(Math.toDegrees(azimuthInRadians)+360)%360;

            float rotation = (180 + azimuthInDegrees + extraRotation) % 360;

            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    rotation,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
            );

            ra.setDuration(250);
            ra.setFillAfter(true);
            startAnimation(ra);

            currentDegree = rotation;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()){
            case Sensor.TYPE_STEP_DETECTOR:
                stepSensorChanged(sensorEvent);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetChanged(sensorEvent);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelChanged(sensorEvent);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
