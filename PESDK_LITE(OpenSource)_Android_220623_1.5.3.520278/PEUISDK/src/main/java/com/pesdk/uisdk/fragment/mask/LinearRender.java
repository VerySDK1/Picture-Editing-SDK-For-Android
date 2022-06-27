package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Canvas;

/**
 * 线性
 */
public class LinearRender extends MaskRender {

    public LinearRender(Context context) {
        super(context);
    }

    @Override
    protected void drawPattern(Canvas canvas) {
        if (!mViewRectF.isEmpty()) {
            //计算出宽度
            float width = (float) Math.sqrt(Math.pow(mViewRectF.width(), 2) + Math.pow(mViewRectF.height(), 2));
            //两条直线
            canvas.drawLine(mCenterPointF.x - width, mCenterPointF.y,
                    mCenterPointF.x - RADIUS_MIN, mCenterPointF.y, mPaint);
            canvas.drawLine(mCenterPointF.x + width, mCenterPointF.y,
                    mCenterPointF.x + RADIUS_MIN, mCenterPointF.y, mPaint);
        }
    }

    @Override
    protected void drawBtn(Canvas canvas) {
        float diff = mBtnRotate.getWidth() * 0.5f;
        mRotateRectF.set(mCenterPointF.x - diff, mCenterPointF.y + RADIUS_MIN + diff,
                mCenterPointF.x + diff, 0);
        mRotateRectF.bottom = mRotateRectF.top + mBtnRotate.getHeight();
        canvas.drawBitmap(mBtnRotate, null, mRotateRectF, null);
    }

    @Override
    protected boolean isRotateScale() {
        return false;
    }

}
