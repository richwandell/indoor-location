package sam.richwandell.com.myapplication.eventlisteners;

import android.view.View;
import android.webkit.WebView;

import sam.richwandell.com.myapplication.RV;

public class FabOnClick implements View.OnClickListener {
    private boolean toggle = false;
    @Override
    public void onClick(View view) {
        ((WebView) RV.homeLayoutImageContainer)
                .loadUrl("javascript:builderToggleGridLines(" + (toggle ? "true" : "false") + ")");
        if(toggle){
            toggle = false;
        }else{
            toggle = true;
        }
    }
}
