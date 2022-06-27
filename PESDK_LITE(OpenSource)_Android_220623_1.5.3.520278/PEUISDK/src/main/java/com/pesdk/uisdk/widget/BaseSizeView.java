package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.CoreUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 *
 */
public abstract class BaseSizeView extends View implements LifecycleObserver {

    public BaseSizeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Utils.autoBindLifecycle(context, this);
    }

    /**
     * true 演示画笔size
     */
    public boolean bPaintSizeMode = false;

    /**
     * 演示画笔
     */
    public void beginPaintSizeMode() {
        bPaintSizeMode = true;
        invalidate();
    }

    /**
     * 结束画笔演示
     */
    public void endPaintSizeMode() {
        bPaintSizeMode = false;
        postInvalidate();
    }

    /**
     * 画笔的粗细
     */
    public float mPaintWidth = CoreUtils.dpToPixel(3);

    /**
     * 画笔宽度
     *
     * @param paintWidth
     */
    public void setPaintWidth(float paintWidth) {
        mPaintWidth = paintWidth;
    }

    public float getPaintWidth() {
        return mPaintWidth;
    }

    public void drawTestPaint(Canvas canvas, Paint paint) {
//        if (null == mTestPaint) {
//            mTestPaint = new Paint();
//        } else {
//            mTestPaint.reset();
//        }
//        mTestPaint.setAntiAlias(true);
//        mTestPaint.setStyle(Paint.Style.FILL);
//        mTestPaint.setColor(mPaintColor);
//        mTestPaint.setAlpha(mAlpha);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mPaintWidth, paint); //画笔size
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public abstract void recycle();

}
