package sam.richwandell.com.myapplication.eventlisteners;

import sam.richwandell.com.myapplication.db.FloorPlan;

public interface FloorPlanReceiver {
    void onFloorPlanReceived(FloorPlan[] fps);
}
