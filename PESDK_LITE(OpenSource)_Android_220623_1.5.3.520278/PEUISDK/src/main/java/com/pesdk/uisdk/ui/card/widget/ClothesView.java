package com.pesdk.uisdk.ui.card.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 证件照手动扣图时，显示衣服
 */
public class ClothesView extends View {
    public ClothesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Bitmap mBitmap;
    private int mAnagle;
    private Rect mRect;

    public void setData(Bitmap bitmap, int angle, Rect rect) {
        mBitmap = bitmap;
        mAnagle = angle;
        mRect = rect;
    }

    private static final String TAG = "ClothesView";

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != mBitmap) {
            int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
            canvas.rotate(-mAnagle, mRect.centerX(), mRect.centerY());
            canvas.drawBitmap(mBitmap, null, mRect, null);
            canvas.restoreToCount(layer);
        }
    }

    public void recycle() {
        if (null != mBitmap) {
            mBitmap.recycle();
        }
        mBitmap = null;

    }
}
