package sam.richwandell.com.myapplication.eventlisteners;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.RV;
import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.listadapters.FloorPlanListAdapter;
import sam.richwandell.com.myapplication.upnp.UPnP;


public class DeviceSelectionClickListener implements DialogInterface.OnClickListener{

    private MainActivity main;

    public DeviceSelectionClickListener(MainActivity main){

        this.main = main;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Log.d("rdebug", "dialog clicked");
        UPnP.HeaderParser.UPnPDevice tracker = RV.trackers.get(i);
        tracker.loadFloorPlans(new FloorPlanReceiver() {
            @Override
            public void onFloorPlanReceived(FloorPlan[] fps) {
                Log.d("rdebug", "got the floorplans!!!");
                RV.allFloorplans = fps;
                AlertDialog.Builder builder = new AlertDialog.Builder(main);
                builder.setTitle("Select Floor Plan");
                builder.setAdapter(new FloorPlanListAdapter(main, fps), new FloorplanSelectionClickListener(main));
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
