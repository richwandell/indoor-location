package sam.richwandell.com.myapplication.listadapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View rowView = inflater.inflate(R.layout.device_list, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.device_list_text);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.device_list_icon);

        txtTitle.setText(fps[position].getFloorPlanName());
        imageView.setImageResource(R.drawable.ic_info_black_24dp);

        return rowView;
    };
}
