package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircleView extends View {

    private int mColor = 0;//颜色值
    private Paint mPaint;

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mColor != 0) {
            mPaint.setColor(mColor);
            int x = getWidth() / 2;
            canvas.drawCircle(x, x, x, mPaint);
        }
    }

    public void setColor(int color) {
        this.mColor = color;
        invalidate();
    }

}
