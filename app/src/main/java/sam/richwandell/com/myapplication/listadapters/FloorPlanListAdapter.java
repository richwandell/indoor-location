package sam.richwandell.com.myapplication.listadapters;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import sam.richwandell.com.myapplication.R;
import sam.richwandell.com.myapplication.db.FloorPlan;

public class FloorPlanListAdapter extends ArrayAdapter<FloorPlan> {

    private final Activity context;
    private final FloorPlan[] fps;

    public FloorPlanListAdapter(Activity context, FloorPlan[] fps) {
        super(context, R.layout.device_list, fps);
        this.context = context;
        this.fps = fps;
    }

}
