package sam.richwandell.com.myapplication.eventlisteners;

import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebView;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.RV;

import static sam.richwandell.com.myapplication.RV.TAG;

public class FloorplanSelectionClickListener implements DialogInterface.OnClickListener{
    private MainActivity main;

    public FloorplanSelectionClickListener(MainActivity main){

        this.main = main;
    }
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Log.d(TAG, Integer.toString(i));
        main.setSelectedFloorPlan(RV.allFloorplans[i]);
        main.homeLayoutImageContainer
                .loadUrl("javascript:loadFloorPlan('" + Integer.toString(i) + "')");
    }
}
