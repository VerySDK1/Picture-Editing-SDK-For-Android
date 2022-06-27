package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.vecore.BaseVirtual;


/**
 * 矩形
 */
public class RectangleRender extends MaskRender {

    private float mRadius;
    private RectF mRectangleRectF = new RectF();

    public RectangleRender(Context context) {
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
            mRectangleRectF.set(mCenterPointF.x - width / 2, mCenterPointF.y - height / 2,
                    mCenterPointF.x + width / 2, mCenterPointF.y + height / 2);
            //绘制矩形
            if (width > height) {
                mRadius = height / 2 * mCurrentFrame.getCornerRadius();
            } else {
                mRadius = width / 2 * mCurrentFrame.getCornerRadius();
            }
            canvas.drawRoundRect(mRectangleRectF, mRadius, mRadius, mPaint);
        }
    }

    @Override
    protected void drawBtn(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            float diff = mBtnRotate.getWidth() * 0.5f;
            //旋转按钮
            mRotateRectF.set(mRectangleRectF.centerX() - diff, mRectangleRectF.bottom + RADIUS_MIN,
                    mRectangleRectF.centerX() + diff, mRectangleRectF.bottom + RADIUS_MIN + mBtnRotate.getHeight());
            canvas.drawBitmap(mBtnRotate, null, mRotateRectF, null);

            //top按钮
            diff = mBtnTop.getWidth() * 0.5f;
            mTopRectF.set(mRectangleRectF.centerX() - diff, mRectangleRectF.top - mBtnTop.getHeight() - RADIUS_MIN,
                    mRectangleRectF.centerX() + diff, mRectangleRectF.top - RADIUS_MIN);
            canvas.drawBitmap(mBtnTop, null, mTopRectF, null);

            //右按钮
            diff = mBtnRight.getWidth() * 0.5f;
            mRightRectF.set(mRectangleRectF.right + RADIUS_MIN, mRectangleRectF.centerY() - diff,
                    mRectangleRectF.right + RADIUS_MIN + mBtnRight.getWidth(), mRectangleRectF.centerY() + diff);
            canvas.drawBitmap(mBtnRight, null, mRightRectF, null);

            //圆角
            float d = (float) ((1 - 1 / Math.sqrt(2)) * mRadius);
            mCornerRectF.set(mRectangleRectF.left + d - RADIUS_MIN - mBtnCorner.getWidth(),
                    mRectangleRectF.top + d - RADIUS_MIN - mBtnCorner.getHeight(),
                    mRectangleRectF.left + d - RADIUS_MIN, mRectangleRectF.top + d - RADIUS_MIN);
            canvas.drawBitmap(mBtnCorner, null, mCornerRectF, null);
        }
    }

    @Override
    protected boolean isRotateScale() {
        return true;
    }

}
