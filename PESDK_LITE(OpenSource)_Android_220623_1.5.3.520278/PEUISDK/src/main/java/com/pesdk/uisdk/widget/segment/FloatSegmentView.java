package com.pesdk.uisdk.widget.segment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.pesdk.uisdk.util.Utils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 放大抠图微调组件
 */
public class FloatSegmentView extends View implements LifecycleObserver {
    private Paint mPaint;
    private Rect mClip = new Rect();
    private Rect mRect = null;
    private Bitmap mBitmap;

    public FloatSegmentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        Utils.autoBindLifecycle(context, this);
    }


    public void setBitmap(Bitmap bitmap, Rect clip) {
        mBitmap = bitmap;
        mClip = clip;
        invalidate();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mRect = new Rect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != mRect && !mRect.isEmpty() && null != mBitmap) {
            int layer = canvas.saveLayer(0, 0, mRect.width(), mRect.height(), null);
            mPaint.reset();
            mPaint.setAntiAlias(true);
            canvas.drawRoundRect(new RectF(mRect), 20, 20, mPaint);

            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(mBitmap, mClip, mRect, mPaint);
            canvas.restoreToCount(layer);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void recycle() {
        if (null != mBitmap) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
