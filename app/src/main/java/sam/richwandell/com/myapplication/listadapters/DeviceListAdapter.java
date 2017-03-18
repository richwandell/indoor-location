package sam.richwandell.com.myapplication.listadapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sam.richwandell.com.myapplication.R;

public class DeviceListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;

    public DeviceListAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.device_list, itemname);
        this.context = context;
        this.itemname = itemname;
        this.imgid = imgid;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View rowView = inflater.inflate(R.layout.device_list, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.device_list_text);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.device_list_icon);

        txtTitle.setText(itemname[position]);
        imageView.setImageResource(imgid[position]);


        return rowView;
    }
}