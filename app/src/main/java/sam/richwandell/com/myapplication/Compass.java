package sam.richwandell.com.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class Compass extends View {
    public static float azimuthInDegrees = 0;
    Paint paint;

    public Compass(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Compass(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public Compass(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
    }

    @Override
    public void onDraw(Canvas c){
        super.onDraw(c);
//        int height = c.getHeight();
//        float radius = (float)height / .5f;
//        float degrees = azimuthInDegrees;
//
//        float cx = getPivotX();
//        float cy = getPivotY();
//
//        double x2 = cx + radius * Math.cos(degrees * Math.PI / 180);
//        double y2 = cy + radius * Math.sin(degrees * Math.PI / 180);
//
//        c.drawLine(cx, cy,  0, 0, paint);
//        invalidate();
    }
}
