package sam.richwandell.com.myapplication.eventlisteners;

import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.view.View;

import sam.richwandell.com.myapplication.MainActivity;
import sam.richwandell.com.myapplication.R;
import sam.richwandell.com.myapplication.RV;


public class SyncButtonClickListener implements View.OnClickListener {

    private MainActivity main;

    public SyncButtonClickListener(MainActivity main){
        this.main = main;
    }

    @Override
    public void onClick(View view) {
        RV.mode = RV.MODE.INFO;
        int color = ContextCompat.getColor(view.getContext(), R.color.color3);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
