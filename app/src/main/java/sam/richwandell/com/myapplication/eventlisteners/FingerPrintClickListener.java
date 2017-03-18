package sam.richwandell.com.myapplication.eventlisteners;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.R;
import sam.richwandell.com.myapplication.RV;
import sam.richwandell.com.myapplication.Wifi;


public class FingerPrintClickListener implements View.OnClickListener {

    private MainActivity main;

    public FingerPrintClickListener(MainActivity main){
        this.main = main;
    }

    @Override
    public void onClick(View view) {
        main.resetFabColors();
        RV.mode = RV.MODE.FINGERPRINTING;
        int color = ContextCompat.getColor(view.getContext(), R.color.color3);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
        new Wifi(main).runScan();
    }
}
