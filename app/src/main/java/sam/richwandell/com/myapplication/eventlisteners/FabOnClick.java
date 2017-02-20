package sam.richwandell.com.myapplication.eventlisteners;

import android.view.View;

import sam.richwandell.com.myapplication.MainActivity;

public class FabOnClick implements View.OnClickListener {
    private boolean toggle = false;
    private MainActivity main;

    public FabOnClick(MainActivity main){

        this.main = main;
    }

    @Override
    public void onClick(View view) {
        main.homeLayoutImageContainer
                .loadUrl("javascript:builderToggleGridLines(" + (toggle ? "true" : "false") + ")");
        if(toggle){
            toggle = false;
        }else{
            toggle = true;
        }
    }
}
