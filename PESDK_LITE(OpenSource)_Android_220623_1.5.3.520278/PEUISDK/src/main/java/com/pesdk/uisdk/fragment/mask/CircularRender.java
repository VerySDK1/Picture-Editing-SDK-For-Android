package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.vecore.BaseVirtual;


/**
 * 圆形
 */
public class CircularRender extends MaskRender {

    private RectF mOvalRectF = new RectF();

    public RectF getOvalRectF() {
        return mOvalRectF;
    }

    public CircularRender setOvalRectF(RectF ovalRectF) {
        mOvalRectF = ovalRectF;
        return this;
    }

    public CircularRender(Context context) {
        super(context);
    }

    @Override
    protected boolean isKeepRatio() {
        return true;
    }

    @Override
    protected void drawPattern(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            //计算出宽度
            BaseVirtual.SizeF size = mCurrentFrame.getSize();
            float width = size.getWidth() * mShowRectF.width() * mViewRectF.width();
            float height = size.getHeight() * mShowRectF.height() * mViewRectF.height();
            mOvalRectF.set(mCenterPointF.x - width / 2, mCenterPointF.y - height / 2,
                    mCenterPointF.x + width / 2, mCenterPointF.y + height / 2);
            //绘制椭圆
            canvas.drawOval(mOvalRectF, mPaint);
        }
    }

    @Override
    protected void drawBtn(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            float diff = mBtnRotate.getWidth() * 0.5f;
            //旋转按钮
            mRotateRectF.set(mOvalRectF.centerX() - diff, mOvalRectF.bottom + RADIUS_MIN,
                    mOvalRectF.centerX() + diff, mOvalRectF.bottom + RADIUS_MIN + mBtnRotate.getHeight());
            canvas.drawBitmap(mBtnRotate, null, mRotateRectF, null);

            //top按钮
            diff = mBtnTop.getWidth() * 0.5f;
            mTopRectF.set(mOvalRectF.centerX() - diff, mOvalRectF.top - mBtnTop.getHeight() - RADIUS_MIN,
                    mOvalRectF.centerX() + diff, mOvalRectF.top - RADIUS_MIN);
            canvas.drawBitmap(mBtnTop, null, mTopRectF, null);

            //右按钮
            diff = mBtnRight.getWidth() * 0.5f;
            mRightRectF.set(mOvalRectF.right + RADIUS_MIN, mOvalRectF.centerY() - diff,
                    mOvalRectF.right + RADIUS_MIN + mBtnRight.getWidth(), mOvalRectF.centerY() + diff);
            canvas.drawBitmap(mBtnRight, null, mRightRectF, null);
        }
    }

    @Override
    protected boolean isRotateScale() {
        return true;
    }

}
