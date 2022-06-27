package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.pesdk.uisdk.R;

import androidx.core.content.ContextCompat;

/**
 * 圆形进度条
 *
 * @author abreal
 */
public class CircleProgressBarView extends View {
    private int mMax = 100;
    private int mProgress = 1;
    private Paint mProgressPaint = null, mBgPaint = null, mBorderPaint = null;

    public CircleProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProgressPaint = new Paint();
        mBgPaint = new Paint();
        mBorderPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        int color = ContextCompat.getColor(context, R.color.pesdk_main_color);
        mProgressPaint.setColor(color);
        mProgressPaint.setStyle(Style.FILL);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(Color.TRANSPARENT);
        mBgPaint.setStyle(Style.FILL);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(color);
//        mBorderPaint.setColor(Color.BLACK);
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setStrokeWidth(2);
    }


    public void setProgress(int progress) {
        if (progress >= 1) {
            mProgress = progress;
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - 2, mBorderPaint);

        int sweep = 360 * mProgress / mMax;
        RectF rect = new RectF(5, 5, getWidth() - 5, getHeight() - 5);
        canvas.drawArc(rect, 270, 360, true, mBgPaint);
        canvas.drawArc(rect, 270, sweep, true, mProgressPaint);
    }

}
