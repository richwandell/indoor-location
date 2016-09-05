package sam.richwandell.com.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;

import java.util.HashMap;

import sam.richwandell.com.myapplication.items.Compass;


public class RV {
    public static View compassContainer;
    public static View homeLayoutImageContainer;

    public static SensorManager sensorManager;
    public static Sensor accelerometer;
    public static Sensor magneticfield;

    public static Compass compass;

    public static MainActivity mainActivity;

    public static String[] floorPlans;
    public static HashMap<String, ILocationKalman> locationKalmanMap;
}
