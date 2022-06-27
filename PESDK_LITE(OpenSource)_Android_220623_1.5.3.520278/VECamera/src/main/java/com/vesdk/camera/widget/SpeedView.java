package com.vesdk.camera.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.vesdk.camera.R;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.PaintUtils;

public class SpeedView extends View {

    private static final int ANIM_DURATION = 200;

    /**
     * 颜色
     */
    private final int mTextN;
    private final int mTextP;
    private final int mBgColor;
    private final int mColorN;
    private final int mColorP;
    private final int mCheckedColor;
    /**
     * 菜单列表
     */
    private final String[] mMenuList = getResources().getStringArray(R.array.camera_record_speed);
    /**
     * 笔
     */
    private final Paint mPaint;
    private final Paint mBgPaint;
    private final Rect mSrc = new Rect();
    private int mCheckedId;
    private final Rect mDst = new Rect();
    /**
     * 移动中
     */
    private boolean mIsMoving = false;
    /**
     * 文字高度
     */
    private int[] mTextHeight;
    private int[] mTextPHeight;
    /**
     * 圆角
     */
    private int mRadio = 5;
    /**
     * 左
     */
    private final int mMarginLeft;
    /**
     * 绘制
     */
    private final RectF mDrawRectF = new RectF();
    /**
     * 回调
     */
    private IGroupListener iListener;

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createHandler();
        mBgColor = ContextCompat.getColor(context, R.color.transparent_black50);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mColorN = ContextCompat.getColor(context, R.color.white);
        mColorP = ContextCompat.getColor(context, R.color.white);
        mCheckedColor = ContextCompat.getColor(context, R.color.red);
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mCheckedId = 2;
        mTextN = CoreUtils.dip2px(context, 13);
        mTextP = CoreUtils.dip2px(context, 14);
        mMarginLeft = CoreUtils.dpToPixel(5);
    }

    /**
     * 设置选中id
     */
    public void setCheckedId(int id) {
        mCheckedId = id;
        if (null != iListener) {
            iListener.onItemChanged(mCheckedId);
        }
        invalidate();
    }

    /**
     * 被选中的Item
     */
    public int getCheckedId() {
        return mCheckedId;
    }

    /**
     * 下标获取菜单
     */
    public String getCurrent(int index) {
        return mMenuList[index];
    }

    /**
     * 释放
     */
    public void recycle() {
        mHandler.removeMessages(MSG_ANIM);
        mHandler = null;
    }

    /**
     * listener
     */
    public void setListener(IGroupListener listener) {
        iListener = listener;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mSrc.set(getLeft(), getTop(), getRight(), getBottom());
            mRadio = (getHeight() / 2) - 1;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        mBgPaint.setColor(mBgColor);
        mDrawRectF.set(mSrc);
        canvas.drawRoundRect(mDrawRectF, mRadio, mRadio, mBgPaint);

        int len = mMenuList.length;
        if (len > 0) {
            int itemWidth = (getWidth() - mMarginLeft * 2) / len;
            int left;
            int tw, th;
            if (mIsMoving) {
                mBgPaint.setColor(mCheckedColor);
                mDrawRectF.set(mDst.left, mDst.top + 0.1f, mDst.right, mDst.bottom - 0.1f);
                canvas.drawRoundRect(mDrawRectF, mRadio, mRadio, mBgPaint);
            }
            for (int i = 0; i < len; i++) {
                left = getLeft() + itemWidth * i + mMarginLeft;
                if (mCheckedId == i) {
                    if (!mIsMoving) {
                        mDst.set(left - mMarginLeft, getTop(), left + itemWidth + mMarginLeft, getBottom());
                        mBgPaint.setColor(mCheckedColor);
                        mDrawRectF.set(mDst.left, mDst.top + 0.1f, mDst.right, mDst.bottom - 0.1f);
                        canvas.drawRoundRect(mDrawRectF, mRadio, mRadio, mBgPaint);
                    }
                    mPaint.setColor(mColorP);
                    mPaint.setTextSize(mTextP);
                    tw = PaintUtils.getWidth(mPaint, mMenuList[i]);
                    if (null == mTextPHeight) {
                        mTextPHeight = PaintUtils.getHeight(mPaint);
                    }
                    th = mTextPHeight[0];
                    canvas.drawText(mMenuList[i], left + itemWidth / 2.0f - tw / 2.0f, mRadio + th / 2.0f - mTextPHeight[1], mPaint);
                } else {
                    mPaint.setTextSize(mTextN);
                    mPaint.setColor(mColorN);
                    tw = PaintUtils.getWidth(mPaint, mMenuList[i]);
                    if (null == mTextHeight) {
                        mTextHeight = PaintUtils.getHeight(mPaint);
                    }
                    th = mTextHeight[0];
                    canvas.drawText(mMenuList[i], left + itemWidth / 2.0f - tw / 2.0f, mRadio + th / 2.0f - mTextHeight[1], mPaint);
                }
            }
        }
    }

    private int mItemWidth = 5;
    private int mHalf = 2;
    private boolean mIsMoved = false;
    private int mLastCheckIndex = 2;
    private int mDownX = 0;
    private int mTargetLeft = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = (int) event.getX();
                mIsMoved = false;
                mItemWidth = getWidth() / mMenuList.length;
                mHalf = mItemWidth / 2;
                mLastCheckIndex = mCheckedId;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                int tempx = (int) event.getX();
                int mleft = tempx - mHalf;
                int mright = tempx + mHalf;
                if (Math.abs(tempx - mDownX) > mItemWidth / 6 && mleft > getLeft() && mright < getRight()) {
                    mIsMoved = true;
                    int left;
                    int tempCheckId = 0;
                    int len = mMenuList.length;
                    for (int i = 0; i < len; i++) {
                        left = getLeft() + mItemWidth * i + mMarginLeft;
                        if (event.getX() >= left && event.getX() <= left + mItemWidth) {
                            tempCheckId = i;
                            break;
                        }
                    }
                    if (tempCheckId != mCheckedId) {
                        mCheckedId = tempCheckId;
                    }
                    mDst.set(mleft, getTop(), tempx + mHalf, getBottom());
                    mIsMoving = true;
                    invalidate();
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                int left;
                int tempCheckId = 0;
                int len = mMenuList.length;
                for (int i = 0; i < len; i++) {
                    left = getLeft() + mItemWidth * i;
                    if (event.getX() >= left && event.getX() <= left + mItemWidth) {
                        tempCheckId = i;
                        break;
                    }
                }
                mCheckedId = tempCheckId;
                if (null != iListener) {
                    iListener.onItemChanged(mCheckedId);
                }
                if (mIsMoved) {
                    mIsMoving = false;
                    invalidate();
                } else {
                    if (mLastCheckIndex != mCheckedId) {
                        mIsMoving = true;
                        //执行移动rect
                        mTargetLeft = mItemWidth * mCheckedId + mMarginLeft;
                        int lastLeft = (mItemWidth * mLastCheckIndex) + getLeft() + mMarginLeft;
                        createAnimation(lastLeft, mTargetLeft);
                        mHandler.obtainMessage(MSG_ANIM, lastLeft, mTargetLeft).sendToTarget();
                    }
                }
                mLastCheckIndex = mCheckedId;
            }
            break;
            default:
                break;
        }
        return true;
    }

    /**
     * 定义滑动加速器
     */
    private void createAnimation(int last, int target) {
        ValueAnimator anim = ValueAnimator.ofInt(last, target);
        anim.addUpdateListener(animation -> {
            //更新按住缩放
            mHandler.obtainMessage(MSG_ANIM, (int) animation.getAnimatedValue(), mTargetLeft).sendToTarget();
        });
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }


    /**
     * 动画
     */
    private final int MSG_ANIM = 56;

    private Handler mHandler;

    private void createHandler() {
        mHandler = new Handler(msg -> {
            if (msg.what == MSG_ANIM) {
                if (Math.abs(mTargetLeft - msg.arg1) < 5) {
                    mIsMoving = false;
                } else {
                    mDst.set(msg.arg1, getTop(), msg.arg1 + mItemWidth, getBottom());
                }
                invalidate();
            }
            return false;
        });
    }

    public interface IGroupListener {

        void onItemChanged(int itemId);

    }

}
