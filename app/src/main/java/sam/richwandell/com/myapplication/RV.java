package sam.richwandell.com.myapplication;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.eventlisteners.FloorPlanReceiver;
import sam.richwandell.com.myapplication.eventlisteners.FloorplanSelectionClickListener;
import sam.richwandell.com.myapplication.eventlisteners.FloorplanSelectionClickListener2;
import sam.richwandell.com.myapplication.items.Compass;
import sam.richwandell.com.myapplication.items.FanMenu;
import sam.richwandell.com.myapplication.listadapters.DeviceListAdapter;
import sam.richwandell.com.myapplication.listadapters.FloorPlanListAdapter;
import sam.richwandell.com.myapplication.upnp.UPnP;

/**
 * RV represents the application state
 */
public class RV {

    public static MODE mode;

    /**
     * The app can be in fingerprinting mode or localizing mode
     */
    public enum MODE {
        FINGERPRINTING, LOCALIZING
    }

    ;

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

    public static void showFloorPlanSelection() {
        RV.floorPlans = HomeLayout.getFloorPlans();
        AlertDialog.Builder builder = new AlertDialog.Builder(RV.mainActivity);
        builder.setTitle("Select Floor Plan");
        builder.setItems(RV.floorPlans, new FloorplanSelectionClickListener());
        AlertDialog alert = builder.create();
        alert.show();
    }

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

        builder.setAdapter(new DeviceListAdapter(RV.mainActivity,
                names.toArray(new String[names.size()]), ids), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("rdebug", "dialog clicked");
                UPnP.HeaderParser.UPnPDevice tracker = trackers.get(i);
                tracker.loadFloorPlans(new FloorPlanReceiver() {
                    @Override
                    public void onFloorPlanReceived(FloorPlan[] fps) {
                        Log.d("rdebug", "got the floorplans!!!");
                        AlertDialog.Builder builder = new AlertDialog.Builder(RV.mainActivity);
                        builder.setTitle("Select Floor Plan");
                        builder.setAdapter(new FloorPlanListAdapter(RV.mainActivity, fps), new FloorplanSelectionClickListener2());
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
