package sam.richwandell.com.myapplication.eventlisteners;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;

import sam.richwandell.com.myapplication.R;
import sam.richwandell.com.myapplication.RV;


public class FingerPrintClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {

        RV.mode = RV.MODE.FINGERPRINTING;
        int color = ContextCompat.getColor(view.getContext(), R.color.color3);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
