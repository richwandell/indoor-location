package sam.richwandell.com.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.eventlisteners.DeviceSelectionClickListener;
import sam.richwandell.com.myapplication.items.Compass;
import sam.richwandell.com.myapplication.items.FanMenu;
import sam.richwandell.com.myapplication.listadapters.DeviceListAdapter;
import sam.richwandell.com.myapplication.upnp.UPnP;

/**
 * RV represents the application state
 */
public class RV {

    public static MODE mode;
    public static FloorPlan[] allFloorplans;
    public static boolean isConnected = false;

    /**
     * The app can be in fingerprinting mode or localizing mode
     */
    public enum MODE {
        FINGERPRINTING, LOCALIZING
    }

    public static View compassContainer;
    public static View homeLayoutImageContainer;

    //compass variables
    public static SensorManager sensorManager;
    public static Sensor accelerometer;
    public static Sensor magneticfield;
    public static Compass compass;

    //reference for the main activity
    public static MainActivity mainActivity;

    //all floorplans in the assets folder
    public static String[] floorPlans;

    //kalman filters
    public static HashMap<String, ILocationKalman> locationKalmanMap;

    //floor plan variables
    public static String floorPlanCoords;
    public static String spaceName;
    public static int floorPlanId = -1;

    public static FanMenu fm;

    public static List<UPnP.HeaderParser.UPnPDevice> trackers = new ArrayList<>();

    public static void showTrackerServerSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RV.mainActivity);
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
                new DeviceListAdapter(RV.mainActivity, names.toArray(new String[names.size()]), ids),
                new DeviceSelectionClickListener()
        );
        AlertDialog alert = builder.create();
        alert.show();
    }
}
