package com.vesdk.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.pesdk.R;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;


/**
 *
 */
public class ExtFilterSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private Drawable mThumb_n, mThumb_p;
    private int thumbW, thumbH;
    private int mProgressDrawableMargin = 0;
    private Paint bgPaint, progressPaint;

    /**
     * @param changedByHand
     */
    public void setChangedByHand(boolean changedByHand) {
        isChangedByHand = changedByHand;
        invalidate();
    }

    private boolean isChangedByHand = false;

    public ExtFilterSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);


        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(ContextCompat.getColor(getContext(), R.color.pesdk_config_titlebar_bg));
        mThumb_n = ContextCompat.getDrawable(getContext(), R.drawable.pesdk_config_sbar_thumb_n);
        mThumb_p = ContextCompat.getDrawable(getContext(), R.drawable.pesdk_config_sbar_thumb_p);

        thumbW = mThumb_n.getIntrinsicWidth();
        thumbH = mThumb_n.getIntrinsicHeight();
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);

        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.pesdk_main_color));
        mProgressDrawableMargin = (int) (thumbW * 1.05);
    }

    public void setBGColor(@ColorInt int color) {
        bgPaint.setColor(color);
    }

    public void setProgressColor(@ColorInt int color) {
        progressPaint.setColor(color);
    }

    private final int RADIUS = 4;


    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    private int defaultValue = 0;


    @Override
    protected synchronized void onDraw(Canvas canvas) {

        int cp = getProgress();
        int width = getWidth();
        int paddingLeft = mProgressDrawableMargin / 2;
        int px = ((width - mProgressDrawableMargin) * cp / getMax()) + paddingLeft;


        int nTop = (getHeight() / 2) - RADIUS;

        RectF bgRectF = new RectF(paddingLeft, nTop, (width - paddingLeft), nTop + RADIUS * 2);

        //背景色
        canvas.drawRoundRect(bgRectF, RADIUS, RADIUS, bgPaint);

        RectF progressRectF;

        int beginPx = ((width - mProgressDrawableMargin) * defaultValue / getMax()) + paddingLeft;


        progressRectF = new RectF(beginPx, bgRectF.top, px, bgRectF.bottom);

        //当前进度
        canvas.drawRoundRect(progressRectF, RADIUS, RADIUS, progressPaint);

        int baseY = (int) bgRectF.centerY();
        Rect thumbRect = new Rect(px - (thumbW / 2), baseY - (thumbH / 2), px + (thumbW / 2), baseY + (thumbH / 2));
        if (isChangedByHand) {
            mThumb_p.setBounds(thumbRect);
            mThumb_p.draw(canvas);
        } else {
            mThumb_n.setBounds(thumbRect);
            mThumb_n.draw(canvas);

        }
    }


}
