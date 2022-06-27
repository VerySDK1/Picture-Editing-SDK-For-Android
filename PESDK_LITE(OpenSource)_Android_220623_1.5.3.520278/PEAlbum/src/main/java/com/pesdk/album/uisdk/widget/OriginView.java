package com.pesdk.album.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class OriginView extends View {

    /**
     *  笔
     */
    private Paint mPaint;

    private int mColor = Color.RED;
    private float mRadius = 6;

    public OriginView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OriginView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int v = (int) (mRadius * 2);
        setMeasuredDimension(v, v);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mColor);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
    }

    /**
     * 设置颜色
     */
    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    /**
     * 设置半径
     */
    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

}
