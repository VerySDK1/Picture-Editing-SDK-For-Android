package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Canvas;

/**
 * 镜面
 */
public class MirrorRender extends MaskRender {

    public MirrorRender(Context context) {
        super(context);
    }

    @Override
    protected void drawPattern(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            //计算出宽度
            float height = mCurrentFrame.getSize().getHeight() * mShowRectF.height() * mViewRectF.height();
            float width = (float) Math.sqrt(Math.pow(mViewRectF.width(), 2) + Math.pow(mViewRectF.height(), 2));
            //两条直线
            canvas.drawLine(mCenterPointF.x - width, mCenterPointF.y - height / 2,
                    mCenterPointF.x + width, mCenterPointF.y - height / 2, mPaint);
            canvas.drawLine(mCenterPointF.x - width, mCenterPointF.y + height / 2,
                    mCenterPointF.x + width, mCenterPointF.y + height / 2, mPaint);
        }
    }

    @Override
    protected void drawBtn(Canvas canvas) {
        if (mViewRectF.isEmpty() || mCurrentFrame == null) {
            return;
        }
        float diff = mBtnRotate.getWidth() * 0.5f;
        float height = mCurrentFrame.getSize().getHeight() * mShowRectF.height() * mViewRectF.height();
        mRotateRectF.set(mCenterPointF.x - diff, mCenterPointF.y + height / 2 + diff,
                mCenterPointF.x + diff, 0);
        mRotateRectF.bottom = mRotateRectF.top + mBtnRotate.getHeight();
        canvas.drawBitmap(mBtnRotate, null, mRotateRectF, null);
    }

    @Override
    protected boolean isRotateScale() {
        return true;
    }

}
