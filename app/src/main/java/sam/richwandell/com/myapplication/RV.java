package sam.richwandell.com.myapplication;

import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.eventlisteners.DeviceSelectionClickListener;
import sam.richwandell.com.myapplication.listadapters.DeviceListAdapter;
import sam.richwandell.com.myapplication.upnp.UPnP;

/**
 * RV represents the application state
 */
public class RV {

    public static String DEVICE_ID;
    public static String TAG = "rdebug";

    public static float phoneRotation = 0.0f;

    public static MODE mode;
    public static FloorPlan[] allFloorplans;
    public static boolean isConnected = false;
    /**
     * When a upnp tracking service is selected we will save an instance here
     */
    public static UPnP.HeaderParser.UPnPDevice upnpDevice;

    /**
     * The app can be in fingerprinting mode or localizing mode
     */
    public enum MODE {
        FINGERPRINTING, LOCALIZING, UPDATING
    }

    //all floorplans in the assets folder
    public static String[] floorPlans;

    //kalman filters

    //floor plan variables
    static int[] floorPlanCoords;
    static String spaceName;
    public static String floorPlanId;
    public static int floorPlanWidth = 0;
    public static int floorPlanHeight = 0;

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
