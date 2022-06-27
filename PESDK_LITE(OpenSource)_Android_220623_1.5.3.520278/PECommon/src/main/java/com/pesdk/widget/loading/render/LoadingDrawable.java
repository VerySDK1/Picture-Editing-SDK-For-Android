package com.pesdk.widget.loading.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

public class LoadingDrawable extends Drawable implements Animatable {

    private final LoadingRenderer mLoadingRender;
    //背景
    private Paint mPaint;
    private int mBackgroundColor = Color.TRANSPARENT;
    private float mRound = 20;
    private RectF mRectF = new RectF();

    private final Callback mCallback = new Callback() {

        @Override
        public void invalidateDrawable(Drawable d) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(Drawable d, Runnable what, long when) {
            scheduleSelf(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable d, Runnable what) {
            unscheduleSelf(what);
        }
    };

    public LoadingDrawable(LoadingRenderer loadingRender) {
        this.mLoadingRender = loadingRender;
        this.mLoadingRender.setCallback(mCallback);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mLoadingRender.setBounds(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        //背景
        if (mBackgroundColor != Color.TRANSPARENT) {
            mRectF.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
            mPaint.setColor(mBackgroundColor);
            canvas.drawRoundRect(mRectF, mRound, mRound, mPaint);
        }
        if (!getBounds().isEmpty()) {
            this.mLoadingRender.draw(canvas);
        }
    }

    public void setColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
    }

    public void setRound(float round) {
        mRound = round;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public float getRound() {
        return mRound;
    }

    @Override
    public void setAlpha(int alpha) {
        this.mLoadingRender.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        this.mLoadingRender.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start() {
        this.mLoadingRender.start();
    }

    @Override
    public void stop() {
        this.mLoadingRender.stop();
    }

    @Override
    public boolean isRunning() {
        return this.mLoadingRender.isRunning();
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) this.mLoadingRender.mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) this.mLoadingRender.mWidth;
    }
}
