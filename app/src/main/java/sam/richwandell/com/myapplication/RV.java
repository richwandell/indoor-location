package sam.richwandell.com.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.eventlisteners.DeviceSelectionClickListener;
import sam.richwandell.com.myapplication.listadapters.DeviceListAdapter;
import sam.richwandell.com.myapplication.upnp.UPnP;

/**
 * RV represents the application state
 */
public class RV {

    public static String TAG = "rdebug";

    public static MODE mode;
    public static FloorPlan[] allFloorplans;
    public static boolean isConnected = false;

    /**
     * The app can be in fingerprinting mode or localizing mode
     */
    public enum MODE {
        FINGERPRINTING, LOCALIZING, INFO
    }

    //compass variables
    static SensorManager sensorManager;
    static Sensor accelerometer;
    static Sensor magneticfield;

    //all floorplans in the assets folder
    public static String[] floorPlans;

    //kalman filters
    static HashMap<String, ILocationKalman> locationKalmanMap;

    //floor plan variables
    static int[] floorPlanCoords;
    static String spaceName;
    static String floorPlanId;

    public static List<UPnP.HeaderParser.UPnPDevice> trackers = new ArrayList<>();

    static void showTrackerServerSelection(MainActivity main) {
        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle("Select Tracking Server");
        List<String> names = new ArrayList<>();
        for (UPnP.HeaderParser.UPnPDevice d : trackers) {
            String n = d.get("friendlyName");
            names.add(n);
        }

        Integer[] ids = new Integer[]{
                R.drawable.ic_info_black_24dp,
                R.drawable.ic_info_black_24dp
        };

        builder.setAdapter(
                new DeviceListAdapter(main, names.toArray(new String[names.size()]), ids),
                new DeviceSelectionClickListener(main)
        );
        AlertDialog alert = builder.create();
        alert.show();
    }
}
