package sam.richwandell.com.myapplication.eventlisteners;

import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebView;

import sam.richwandell.com.myapplication.RV;

public class FloorplanSelectionClickListener implements DialogInterface.OnClickListener {

    @Override
    public void onClick(DialogInterface dialog, int item) {
        // Do something with the selection
        Log.d("rdebug", RV.floorPlans[item]);
        ((WebView) RV.homeLayoutImageContainer)
                .loadUrl("javascript:builderLoadFloorplan('" + RV.floorPlans[item] + "')");
    }
}
