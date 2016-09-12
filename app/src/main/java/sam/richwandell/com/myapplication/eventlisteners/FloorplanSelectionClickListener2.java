package sam.richwandell.com.myapplication.eventlisteners;

import android.content.DialogInterface;
import android.webkit.WebView;

import sam.richwandell.com.myapplication.RV;

public class FloorplanSelectionClickListener2 implements DialogInterface.OnClickListener{
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        ((WebView) RV.homeLayoutImageContainer)
                .loadUrl("javascript:builderLoadFloorplan2('" + Integer.toString(i) + "')");
    }
}
