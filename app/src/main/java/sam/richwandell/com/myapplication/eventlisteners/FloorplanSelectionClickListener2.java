package sam.richwandell.com.myapplication.eventlisteners;

import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebView;

import sam.richwandell.com.myapplication.RV;

public class FloorplanSelectionClickListener2 implements DialogInterface.OnClickListener{
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Log.d("rdebug", Integer.toString(i));
        ((WebView) RV.homeLayoutImageContainer)
                .loadUrl("javascript:builderLoadFloorplan2('" + Integer.toString(i) + "')");
    }
}
