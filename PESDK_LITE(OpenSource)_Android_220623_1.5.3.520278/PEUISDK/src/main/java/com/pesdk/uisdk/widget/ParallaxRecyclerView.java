package com.pesdk.uisdk.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 阻尼
 */
public class ParallaxRecyclerView extends RecyclerView {

    //恢复中
    private boolean isRestoring;
    private int mActivePointerId;
    private float mInitialMotionX;
    private boolean isBeingDragged;
    private float mScale;
    private float mDistance;
    private int mTouchSlop;

    public ParallaxRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (isRestoring && action == MotionEvent.ACTION_DOWN) {
            isRestoring = false;
        }
        if (!isEnabled() || isRestoring || (!isScrollToTop() && !isScrollToBottom())) {
            return super.onInterceptTouchEvent(event);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = event.getPointerId(0);
                isBeingDragged = false;
                float initialMotionX = getMotionEventX(event);
                if (initialMotionX == -1) {
                    return super.onInterceptTouchEvent(event);
                }
                mInitialMotionX = initialMotionX;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == MotionEvent.INVALID_POINTER_ID) {
                    return super.onInterceptTouchEvent(event);
                }
                final float x = getMotionEventX(event);
                if (x == -1f) {
                    return super.onInterceptTouchEvent(event);
                }
                if (isScrollToTop() && !isScrollToBottom()) {
                    // 在顶部不在底部
                    float xDiff = x - mInitialMotionX;
                    if (xDiff > mTouchSlop && !isBeingDragged) {
                        isBeingDragged = true;
                    }
                } else if (!isScrollToTop() && isScrollToBottom()) {
                    // 在底部不在顶部
                    float xDiff = mInitialMotionX - x;
                    if (xDiff > mTouchSlop && !isBeingDragged) {
                        isBeingDragged = true;
                    }
                } else if (isScrollToTop() && isScrollToBottom()) {
                    // 在底部也在顶部
                    float xDiff = x - mInitialMotionX;
                    if (Math.abs(xDiff) > mTouchSlop && !isBeingDragged) {
                        isBeingDragged = true;
                    }
                } else {
                    // 不在底部也不在顶部
                    return super.onInterceptTouchEvent(event);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                isBeingDragged = false;
                break;
        }
        return isBeingDragged || super.onInterceptTouchEvent(event);
    }

    /**
     * 滚动状态变化时回调
     *
     * @param state
     */
    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        /**
         * state==0 静止没有滚动
         * state==1 正在被外部拖拽,一般为用户正在用手指滚动
         * state==2 自动滚动
         */
        if (state == 0 && mListener != null) {

//            //返回组中指定位置的视图。 @param index从@获取视图的位置，返回指定位置的视图；如果该位置在组中不存在，则为null
//            int firstItem = this.getChildLayoutPosition(this.getChildAt(0));
//            mListener.firstItem(firstItem);

            //该代码解决由于选项数据没有占满列表导致数据出现问题的原因
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();

            mListener.firstItem(linearLayoutManager.findFirstVisibleItemPosition());
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                isBeingDragged = false;
                mStatue = 0;
                break;
            case MotionEvent.ACTION_MOVE: {
                float x = getMotionEventX(event);
                if (isScrollToTop() && !isScrollToBottom()) {
                    // 在顶部不在底部
                    mDistance = x - mInitialMotionX;
                    if (mDistance < 0) {
                        return super.onTouchEvent(event);
                    }
                    mScale = calculateRate(mDistance);
                    pull(mScale);
                    return true;
                } else if (!isScrollToTop() && isScrollToBottom()) {
                    // 在底部不在顶部
                    mDistance = mInitialMotionX - x;
                    if (mDistance < 0) {
                        return super.onTouchEvent(event);
                    }
                    mScale = calculateRate(mDistance);
                    push(mScale);
                    return true;
                } else if (isScrollToTop() && isScrollToBottom()) {
                    // 在底部也在顶部
                    mDistance = x - mInitialMotionX;
                    if (mDistance > 0) {
                        mScale = calculateRate(mDistance);
                        pull(mScale);
                    } else {
                        mScale = calculateRate(-mDistance);
                        push(mScale);
                    }
                    return true;
                } else {
                    // 不在底部也不在顶部
                    return super.onTouchEvent(event);
                }
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                mActivePointerId = event.getPointerId(MotionEventCompat.getActionIndex(event));
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (isScrollToTop() && !isScrollToBottom()) {
                    animateRestore(true);
                } else if (!isScrollToTop() && isScrollToBottom()) {
                    animateRestore(false);
                } else if (isScrollToTop() && isScrollToBottom()) {
                    if (mDistance > 0) {
                        animateRestore(true);
                    } else {
                        animateRestore(false);
                    }
                } else {
                    Log.d("ParallaxDragListener", "isTop:" + isScrollToTop() + ", isBottom:" + isScrollToBottom());
                    return super.onTouchEvent(event);
                }
                if (mListener != null) {
                    if (mStatue == 1) {
                        mListener.onPull();
                    } else if (mStatue == 2) {
                        mListener.onPush();
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isScrollToTop() {
        return !ViewCompat.canScrollHorizontally(this, -1);
    }

    private boolean isScrollToBottom() {
        return !ViewCompat.canScrollHorizontally(this, 1);
    }

    private float getMotionEventX(MotionEvent event) {
        int index = event.findPointerIndex(mActivePointerId);
        return index < 0 ? -1f : event.getX(index);
    }

    private void onSecondaryPointerUp(MotionEvent event) {
        final int pointerIndex = MotionEventCompat.getActionIndex(event);
        final int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = event.getPointerId(newPointerIndex);
        }
    }

    private float calculateRate(float distance) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float originalDragPercent = distance / screenWidth;
        float dragPercent = Math.min(1f, originalDragPercent);
        float rate = 2f * dragPercent - (float) Math.pow(dragPercent, 2f);
        return 1 + rate / 5f;
    }

    private void animateRestore(final boolean isPullRestore) {
        ValueAnimator animator = ValueAnimator.ofFloat(mScale, 1f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (isPullRestore) {
                    pull(value);
                } else {
                    push(value);
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRestoring = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isRestoring = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void pull(float scale) {
        if (scale > 1.1) {
            mStatue = 1;
        } else {
            mStatue = 0;
        }
//        this.setPivotX(0);
//        this.setScaleX(scale);
    }

    private void push(float scale) {
        if (scale > 1.1) {
            mStatue = 2;
        } else {
            mStatue = 0;
        }
//        this.setPivotX(this.getWidth());
//        this.setScaleX(scale);
    }

    private int mStatue = 0;//0默认  1顶部   2底部
    private OnLoadListener mListener;

    public void setListener(OnLoadListener listener) {
        this.mListener = listener;
    }

    public interface OnLoadListener {

        /**
         * 顶部
         */
        void onPull();

        /**
         * 底部
         */
        void onPush();

        void firstItem(int firstItem);

    }
}