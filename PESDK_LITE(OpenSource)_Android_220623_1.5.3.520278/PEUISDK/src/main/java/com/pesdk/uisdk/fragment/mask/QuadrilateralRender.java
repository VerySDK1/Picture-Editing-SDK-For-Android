package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 四边形
 */
public class QuadrilateralRender extends MaskRender {

    private final RectF mCorner = new RectF();
    private final Matrix mMatrix = new Matrix();

    //是个点的位置 真实
    protected final ArrayList<RectF> mPointFList = new ArrayList<>();
    private final int VALUE = 30;


    public QuadrilateralRender(Context context) {
        super(context);
        mPointFList.add(new RectF());
        mPointFList.add(new RectF());
        mPointFList.add(new RectF());
        mPointFList.add(new RectF());
    }

    @Override
    public void init(RectF show, RectF viewRectF, int angle) {
        super.init(show, viewRectF, angle);
        ArrayList<PointF> pointFS = new ArrayList<>();
        pointFS.add(new PointF(0.3f, 0.3f));
        pointFS.add(new PointF(0.7f, 0.3f));
        pointFS.add(new PointF(0.7f, 0.7f));
        pointFS.add(new PointF(0.3f, 0.7f));
        mCurrentFrame.setPointFList(pointFS);
        mCurrentFrame.setAngle(0);
    }


    private static final int TOUCH_STATUE_LT = 11;
    private static final int TOUCH_STATUE_RT = 12;
    private static final int TOUCH_STATUE_RB = 13;
    private static final int TOUCH_STATUE_LB = 14;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCurrentFrame == null || mCurrentFrame.getPointFList() == null
                || mCurrentFrame.getPointFList().size() < 4) {
            return false;
        }
        int pointerNum = event.getPointerCount();
        if(pointerNum == 2){ //多点
            //缩放 旋转
            int action = event.getAction();
            if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                mTempKeyFrame = mCurrentFrame.copy();
                mMorePointer = true;
                mPreLength = getDistance(event);
                mStartAngle = getAngle(event);
            } else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                    || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                //拿起
                mMorePointer = false;
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mMorePointer) {
                    // 计算缩放系数
                    double endLen = getDistance(event);
                    float angle = mTempKeyFrame.getAngle() - (getAngle(event) - mStartAngle);
                    float scale = (float) (endLen / mPreLength);
                    rotsal(-angle ,scale );
                }
            }
        } else if (pointerNum == 1) { //单点
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //旋转后的位置
                    mDownX = event.getX();
                    mDownY = event.getY();
                    judgeBtn();
                    mTempKeyFrame = mCurrentFrame.copy();
                    mPreLength = getDistance(mDownX, mDownY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTouchStatue == TOUCH_STATUE_CORNER) {//圆角
                        float diff = (float) (getDistance(event.getX(), event.getY()) - mPreLength);
                        float radius = mTempKeyFrame.getCornerRadius();
                        radius -= diff / VALUE;
                        if (radius > 1) {
                            radius = 1;
                        } else if (radius < 0) {
                            radius = 0;
                        }
                        mCurrentFrame.setCornerRadius(radius);
                    } else if (mTouchStatue == TOUCH_STATUE_ROTATE) { //角度、缩放
                        float angle = getAngle(event.getX(), event.getY());
                        float scale = ((float) (getDistance(event.getX(), event.getY()) / mPreLength));
                        rotsal(angle ,scale );
                    } else if (mTouchStatue != TOUCH_STATUE_NO) {
                        float[] current = calculateRotate(event.getX(), event.getY(), -mMediaAngle);
                        float x = (current[0] / mViewRectF.width() - mShowRectF.left) / mShowRectF.width();
                        float y = (current[1] / mViewRectF.height() - mShowRectF.top) / mShowRectF.height();
                        List<PointF> pointFList = mCurrentFrame.getPointFList();
                        if (mTouchStatue == TOUCH_STATUE_LT) {
                            //判断 不能超过bottom和right
                            pointFList.get(0).set(x, y);
                        } else if (mTouchStatue == TOUCH_STATUE_RT) {
                            pointFList.get(1).set(x, y);
                        } else if (mTouchStatue == TOUCH_STATUE_RB) {
                            pointFList.get(2).set(x, y);
                        } else if (mTouchStatue == TOUCH_STATUE_LB) {
                            pointFList.get(3).set(x, y);
                        }
                        mCurrentFrame.setPointFList(pointFList);
                    } else {
                        //移动
                        float[] current = calculateRotate(event.getX(), event.getY(), -mMediaAngle);
                        float[] down = calculateRotate(mDownX, mDownY, -mMediaAngle);

                        float offsetX = (current[0] - down[0]) / mViewRectF.width() / mShowRectF.width();
                        float offsetY = (current[1] - down[1]) / mViewRectF.height() / mShowRectF.height();

                        PointF center = mTempKeyFrame.getCenter();
                        PointF result = new PointF();
                        result.x = center.x + offsetX * 2;
                        result.y = center.y + offsetY * 2;
                        boolean beyond = false;
                        if (result.x < -1) {
                            result.x = -1;
                            beyond = true;
                        } else if (result.x > 1) {
                            result.x = 1;
                            beyond = true;
                        }
                        if (result.y < -1) {
                            result.y = -1;
                            beyond = true;
                        } else if (result.y > 1) {
                            result.y = 1;
                            beyond = true;
                        }

                        if (beyond) {
                            mDownX = event.getX();
                            mDownY = event.getY();
                            mTempKeyFrame = mCurrentFrame.copy();
                            return true;
                        }

                        mCurrentFrame.setCenter(result);
                        List<PointF> pointFList = mTempKeyFrame.getPointFList();
                        List<PointF> pointFNewList = mCurrentFrame.getPointFList();

                        PointF pointF = pointFList.get(0);
                        pointFNewList.get(0).set(pointF.x + offsetX, pointF.y + offsetY);
                        pointF = pointFList.get(1);
                        pointFNewList.get(1).set(pointF.x + offsetX, pointF.y + offsetY);
                        pointF = pointFList.get(2);
                        pointFNewList.get(2).set(pointF.x + offsetX, pointF.y + offsetY);
                        pointF = pointFList.get(3);
                        pointFNewList.get(3).set(pointF.x + offsetX, pointF.y + offsetY);
                        mCurrentFrame.setPointFList(pointFNewList);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
        }
        return true;
    }

    private void  rotsal( float angle ,float scale ){
        mMatrix.reset();
        PointF center = mTempKeyFrame.getCenter();
        float px = ((center.x + 1) / 2 * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
        float py = ((center.y + 1) / 2 * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
        mMatrix.postRotate(angle, px, py);
        mMatrix.postScale(scale, scale, px, py);

        List<PointF> pointFList = mTempKeyFrame.getPointFList();
        List<PointF> pointFNewList = mCurrentFrame.getPointFList();
        float[] dst = new float[2];
        float[] src = new float[2];

        PointF pointF = pointFList.get(0);
        src[0] = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
        src[1] = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
        mMatrix.mapPoints(dst, src);
        pointFNewList.get(0).set((dst[0] / mViewRectF.width() - mShowRectF.left) / mShowRectF.width(),
                (dst[1] / mViewRectF.height() - mShowRectF.top) / mShowRectF.height());

        pointF = pointFList.get(1);
        src[0] = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
        src[1] = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
        mMatrix.mapPoints(dst, src);
        pointFNewList.get(1).set((dst[0] / mViewRectF.width() - mShowRectF.left) / mShowRectF.width(),
                (dst[1] / mViewRectF.height() - mShowRectF.top) / mShowRectF.height());

        pointF = pointFList.get(2);
        src[0] = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
        src[1] = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
        mMatrix.mapPoints(dst, src);
        pointFNewList.get(2).set((dst[0] / mViewRectF.width() - mShowRectF.left) / mShowRectF.width(),
                (dst[1] / mViewRectF.height() - mShowRectF.top) / mShowRectF.height());

        pointF = pointFList.get(3);
        src[0] = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
        src[1] = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
        mMatrix.mapPoints(dst, src);
        pointFNewList.get(3).set((dst[0] / mViewRectF.width() - mShowRectF.left) / mShowRectF.width(),
                (dst[1] / mViewRectF.height() - mShowRectF.top) / mShowRectF.height());

        mCurrentFrame.setPointFList(pointFNewList);
    }


    @Override
    protected void judgeBtn() {
        super.judgeBtn();
        //判断
        if (mTouchStatue == TOUCH_STATUE_NO) {
            int value = 40;
            if (Math.abs(mPointFList.get(0).centerX() - mDstPoint[0]) < value
                    && Math.abs(mPointFList.get(0).centerY() - mDstPoint[1]) < value) {
                mTouchStatue = TOUCH_STATUE_LT;
            } else if (Math.abs(mPointFList.get(1).centerX() - mDstPoint[0]) < value
                    && Math.abs(mPointFList.get(1).centerY() - mDstPoint[1]) < value) {
                mTouchStatue = TOUCH_STATUE_RT;
            } else if (Math.abs(mPointFList.get(2).centerX() - mDstPoint[0]) < value
                    && Math.abs(mPointFList.get(2).centerY() - mDstPoint[1]) < value) {
                mTouchStatue = TOUCH_STATUE_RB;
            } else if (Math.abs(mPointFList.get(3).centerX() - mDstPoint[0]) < value
                    && Math.abs(mPointFList.get(3).centerY() - mDstPoint[1]) < value) {
                mTouchStatue = TOUCH_STATUE_LB;
            }
        }
    }

    @Override
    protected boolean isKeepRatio() {
        return true;
    }

    @Override
    protected void drawPattern(Canvas canvas) {

    }

    @Override
    protected void drawBtn(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            //绘制四个点的位置
            List<PointF> pointFList = mCurrentFrame.getPointFList();
            if (pointFList != null && pointFList.size() >= 4) {
                int value = 15;

                PointF pointF = pointFList.get(0);
                float x = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
                float y = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
                mPointFList.get(0).set(x - value, y - value, x + value, y + value);

                pointF = pointFList.get(1);
                x = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
                y = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
                mPointFList.get(1).set(x - value, y - value, x + value, y + value);

                pointF = pointFList.get(2);
                x = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
                y = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
                mPointFList.get(2).set(x - value, y - value, x + value, y + value);

                pointF = pointFList.get(3);
                x = (pointF.x * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();
                y = (pointF.y * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
                mPointFList.get(3).set(x - value, y - value, x + value, y + value);


                //绘制线
                RectF pointF1 = mPointFList.get(0);
                RectF pointF2 = mPointFList.get(1);
                RectF pointF3 = mPointFList.get(2);
                RectF pointF4 = mPointFList.get(3);
//                canvas.drawLine(pointF1.right, pointF1.centerY(), pointF2.left, pointF2.centerY(), mPaint);
//                canvas.drawLine(pointF3.centerX(), pointF3.top, pointF2.centerX(), pointF2.bottom, mPaint);
//                canvas.drawLine(pointF3.left, pointF3.centerY(), pointF4.right, pointF4.centerY(), mPaint);
//                canvas.drawLine(pointF4.centerX(), pointF4.top, pointF1.centerX(), pointF1.bottom, mPaint);
                //点
                canvas.drawCircle(pointF1.centerX(), pointF1.centerY(), value, mPaint);
                canvas.drawCircle(pointF2.centerX(), pointF2.centerY(), value, mPaint);
                canvas.drawCircle(pointF3.centerX(), pointF3.centerY(), value, mPaint);
                canvas.drawCircle(pointF4.centerX(), pointF4.centerY(), value, mPaint);

                //圆角
                mCorner.set(pointF1);
                float d = (1 - mCurrentFrame.getCornerRadius()) * VALUE + RADIUS_MIN / 2.0f;
                mCornerRectF.set(mCorner.centerX() - d - mBtnCorner.getWidth(),
                        mCorner.centerY() - d - mBtnCorner.getHeight(),
                        mCorner.centerX() - d,
                        mCorner.centerY() - d);
                canvas.drawBitmap(mBtnCorner, null, mCornerRectF, null);

                //旋转缩放
                mCorner.set(pointF3);
                float startX = (pointF3.centerX() + pointF4.centerX()) / 2.0f;
                float startY = (pointF3.centerY() + pointF4.centerY()) / 2.0f;
                float w = mBtnRotate.getWidth() / 2.0f;
                mRotateRectF.set(startX - w, startY + w, startX + w, startY + 3 * w);
                canvas.drawBitmap(mBtnRotate, null, mRotateRectF, null);
            }
        }
    }

    @Override
    protected boolean isRotateScale() {
        return true;
    }

}
