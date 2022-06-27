package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.vecore.base.lib.utils.CoreUtils;

import androidx.annotation.Nullable;

public class DownBgView extends View {

    private int mRadius = 20;//dp
    private int mRadiuSmall = 3;//dp
    private Paint mPaint = new Paint();
    private Path mPointPath;

    public DownBgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownBgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#414141"));
        mPointPath = new Path();

        mRadius = CoreUtils.dip2px(context, mRadius);
        mRadiuSmall = CoreUtils.dip2px(context, mRadiuSmall);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF oval = new RectF(0, -mRadius, 2 * mRadius, mRadius);
        RectF oval2 = new RectF(mRadius - 2 * mRadiuSmall, 0, mRadius, 2 * mRadiuSmall);
        mPointPath.moveTo(0, 0);
        mPointPath.lineTo(mRadius - mRadiuSmall, 0);
        mPointPath.arcTo(oval2, -90, 90, false);
        mPointPath.lineTo(mRadius, mRadius);
        mPointPath.arcTo(oval, 90, 90, false);
        mPointPath.close();
        canvas.drawPath(mPointPath, mPaint);

    }

}
