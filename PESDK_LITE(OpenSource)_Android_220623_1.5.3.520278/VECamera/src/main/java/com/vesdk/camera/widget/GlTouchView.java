package com.vesdk.camera.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;

import com.vesdk.camera.R;
import com.vesdk.camera.listener.OnCameraViewListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.recorder.api.ICameraZoomHandler;
import com.vecore.recorder.api.RecorderCore;

/**
 * 录制界面左右切换滤镜，点击摄像头聚焦
 */
public class GlTouchView extends View {

    private static final float PRECISION = 0.0001f;

    private final Rect mDstRect = new Rect();
    /**
     * 判断手势离开时，是继续切换滤镜还是取消切换
     */
    private boolean mDoEnd = false;
    /**
     * 滤镜比例(从左到右 左边滤镜所占百分比)
     */
    private double mFilterProportion = 0.01;
    private ValueAnimator mValueAnimator;
    private boolean mIsMoving = false;

    private float mDownFocusX = 0;
    private int mTargetX = 0, mCurrentX = 0;
    private boolean mIsLeftToRight = false;
    private boolean mEnableMoveFilter = false;

    private final Paint mPaint;
    private int mXPosition;
    private int mYPosition;
    private final int mRadius;

    private boolean needDraw = false;

    private GestureDetector mFlignerDetector;

    protected OnCameraViewListener mCcvlListener;

    private ICameraZoomHandler mZoomHandler;

    public GlTouchView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mFlignerDetector = new GestureDetector(context, new PressGestureListener());
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(context, R.color.white));
        mRadius = CoreUtils.dpToPixel(35);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

    }

    public void setViewHandler(OnCameraViewListener ccvl) {
        mCcvlListener = ccvl;
    }

    public void setZoomHandler(ICameraZoomHandler hlrZoom) {
        mZoomHandler = hlrZoom;
    }

    public void recycle() {
        if (null != mValueAnimator) {
            mValueAnimator.end();
            mValueAnimator = null;
        }
        needDraw = false;
        mFlignerDetector = null;
        mCcvlListener = null;
    }

    public void onPrepared() {
        needDraw = false;
    }

    public void mEnableMoveFilter(boolean enable) {
        mEnableMoveFilter = enable;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (needDraw) {
            canvas.drawCircle(mXPosition, mYPosition, mRadius, mPaint);
            canvas.drawCircle(mXPosition, mYPosition, 15, mPaint);
        }
    }

    private boolean isZoomTouch = false;
    private int mTouchEventCounter = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mFlignerDetector) {
            mFlignerDetector.onTouchEvent(event);
        }
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN) {
            mTouchEventCounter = 0;
            if (mEnableMoveFilter) {
                mDoEnd = false;
                mDownFocusX = event.getX();
                mIsMoving = false;
                if (null != mValueAnimator) {
                    mValueAnimator.end();
                    mValueAnimator = null;
                }
                mHandler.removeMessages(MSG_END);
            }
        }
        mTouchEventCounter++;
        if (mTouchEventCounter == 9) {
            //新增i ==9防止双指时，刚开始的几次取MotionEventCompat.getPointerCount(event)值！=2
            int pCount = MotionEventCompat.getPointerCount(event);
            isZoomTouch = pCount > 1;
        }
        if (mTouchEventCounter < 9) {
            return true;
        }
        if (isZoomTouch) {
            //缩放相机
            if (null != mZoomHandler) {
                mZoomHandler.onTouch(event);
            }
        } else {
//            if (pCount <= 1 || (re == MotionEvent.ACTION_CANCEL || re == MotionEvent.ACTION_UP)) {
            //左右滑动切换相机滤镜
            if (mEnableMoveFilter) {
                if (eventAction == MotionEvent.ACTION_MOVE) {
                    float focusX = event.getX();
                    int offX;
                    if (focusX - mDownFocusX > 10) {// 从左到右
                        if (!mIsLeftToRight) {// 当前右到左---->左到右
                            mIsMoving = false;
                            mIsLeftToRight = true;
                        }
                        int nleft = getLeft();
                        offX = (int) (focusX - mDownFocusX);
                        double temp = (offX + 0.0f) / getWidth();
                        if (mFilterProportion != temp) {
                            mFilterProportion = temp;
                            mCurrentX = offX;
                            mDstRect.set(nleft, getTop(), nleft + offX, getBottom());
                            mTargetX = getRight();
                            if (!mIsMoving) {
                                mIsMoving = true;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChangeStart(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            } else {
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            }
                            invalidate();
                        }
                    } else if (mDownFocusX - focusX > 10) {// 右到左
                        if (mIsLeftToRight) {// 防止滑动--左到右-->右到左
                            mIsMoving = false;
                            mIsLeftToRight = false;
                        }
                        mTargetX = getLeft();
                        offX = (int) (mDownFocusX - focusX);
                        double temp = 1 - ((offX + 0.0) / getWidth());
                        if (mFilterProportion != temp) {
                            mFilterProportion = temp;
                            mCurrentX = getWidth() - offX;
                            mDstRect.set(mCurrentX, getTop(), getRight(), getBottom());
                            if (!mIsMoving) {
                                mIsMoving = true;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChangeStart(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            } else {
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            }
                            invalidate();
                        }
                    }
                } else if (eventAction == MotionEvent.ACTION_CANCEL
                        || eventAction == MotionEvent.ACTION_UP) {

                    float poffx = Math.abs(event.getX() - mDownFocusX);
                    if ((mDoEnd && poffx >= getWidth() / 5.0f) || (!mDoEnd && poffx > getWidth() / 2.0f)) {
                        if (mIsMoving) {// 松开手势时，执行切换
                            getNewAnimationSet(mCurrentX, mTargetX, true);
                        }
                    } else {
                        if (mIsMoving) {// 松开手势时，取消切换
                            if (mIsLeftToRight) {
                                mTargetX = 0;
                            } else {
                                mTargetX = getRight();
                            }
                            getNewAnimationSet(mCurrentX, mTargetX, false);
                        }
                    }
                    removeAll();
                }
            }
        }
        if (eventAction == MotionEvent.ACTION_UP || eventAction == MotionEvent.ACTION_CANCEL) {
            isZoomTouch = false;
        }
        return true;
    }

    private void setLocation(int x, int y) {
        needDraw = true;
        this.mXPosition = x;
        this.mYPosition = y;
        removeCallbacks(mRunnable);
        postDelayed(mRunnable, 800);
        invalidate();
    }

    private void removeAll() {
        removeCallbacks(mRunnable);
        alphaGone();
    }

    private void alphaGone() {
        needDraw = false;
        invalidate();
    }

    private final Runnable mRunnable = this::alphaGone;

    private void getNewAnimationSet(final int current, final int target, final boolean doEnd) {
        // 创建一个加速器
        mValueAnimator = ValueAnimator.ofInt(current, target);
        mValueAnimator.addUpdateListener(animation -> {
            int t = (Integer) animation.getAnimatedValue();
            if (doEnd) {
                mHandler.removeMessages(MSG_END);
                mHandler.obtainMessage(MSG_END, current, t).sendToTarget();
            } else {
                mHandler.removeMessages(MSG_CANCEL);
                if (current < target) {
                    mHandler.obtainMessage(MSG_CANCEL, current, t + 1)
                            .sendToTarget();
                } else {
                    mHandler.obtainMessage(MSG_CANCEL, current, t - 1)
                            .sendToTarget();
                }
            }
        });
        // 匀速移动
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(200);
        mValueAnimator.start();
    }


    private final int MSG_END = 564, MSG_CANCEL = 565;

    private final Handler mHandler = new Handler(Looper.myLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_END: {
                    // 松开手势，自动完成剩余部分的滑动
                    int tempTarget = msg.arg2;
                    int cur = msg.arg1;
                    if (cur < tempTarget) {// 从左往右
                        if (tempTarget >= getRight()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChanging(true, 1.0);
                                mCcvlListener.onFilterChangeEnd();
                            }
                        } else {
                            int nleft = getLeft();
                            double temp = (tempTarget - nleft + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                mDstRect.set(nleft, getTop(), tempTarget, getBottom());
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(true, mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    } else if (cur != tempTarget) {// 从右往左
                        if (tempTarget <= getLeft()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChanging(false, 0);
                                mCcvlListener.onFilterChangeEnd();
                            }
                        } else {
                            double temp = (tempTarget + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                mDstRect.set(tempTarget, getTop(), getRight(), getBottom());
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(false, mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    }
                }
                break;
                case MSG_CANCEL: {
                    // 响应松开时，取消切换
                    int tempTarget = msg.arg2; // 定速取消 ,,变量 ->1080
                    int cur = msg.arg1;// 离开时手势的位置
                    if (cur < tempTarget) {// 滑动时从右到左--->取消时从左到右
                        if (tempTarget >= getRight()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterCanceling(true, 1);
                                mCcvlListener.onFilterChangeCanceled();
                            }
                        } else {
                            mDstRect.set(tempTarget, getTop(), getRight(), getBottom());
                            double temp = (tempTarget - getLeft() + 0.0f) / getWidth();
                            if (Math.abs(mFilterProportion - temp) > PRECISION) {
                                mFilterProportion = temp;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterCanceling(true, mFilterProportion);
                                }
                            }
                        }
                    } else {// 从右往左
                        if (tempTarget <= getLeft()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterCanceling(false, 0);
                                mCcvlListener.onFilterChangeCanceled();
                            }
                        } else {
                            mDstRect.set(getLeft(), getTop(), tempTarget, getBottom());
                            double temp = (tempTarget + 0.0f) / getWidth();
                            if (Math.abs(mFilterProportion - temp) > PRECISION) {
                                mFilterProportion = temp;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterCanceling(false, mFilterProportion);
                                }
                            }
                        }
                    }
                    invalidate();
                }
                break;
                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 手势listener
     *
     * @author abreal
     */
    private class PressGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            mDoEnd = true;
            if (mTouchEventCounter < 9) {
                if (mEnableMoveFilter) {
                    if (e1.getX() < e2.getX()) { // 向右fling
                        getNewAnimationSet(0, getRight(), true);
                    } else { // 向左fling
                        getNewAnimationSet(getRight(), 0, true);
                    }
                } else {
                    if (e1.getX() < e2.getX()) { // 向右fling
                        mCcvlListener.onSwitchFilterToRight();
                    } else { // 向左fling
                        mCcvlListener.onSwitchFilterToLeft();
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mCcvlListener != null) {
                mCcvlListener.onSingleTapUp(e);
            }
            if (!RecorderCore.isFaceFront()) {
                int dx = (int) e.getX(), dy = (int) e.getY();
                int height = getHeight();
                int width = getWidth();
                if (mRadius < dx && dx < (width - mRadius) && dy > mRadius && dy < (height - mRadius)) {
                    setLocation(dx, dy);
                }
            }
            return super.onSingleTapUp(e);
        }

        @Override
        // 双击
        public boolean onDoubleTap(MotionEvent e) {
            removeAll();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            removeAll();
            if (mCcvlListener != null) {
                mCcvlListener.onDoubleTap(e);
            }
            return super.onDoubleTapEvent(e);
        }
    }

}

