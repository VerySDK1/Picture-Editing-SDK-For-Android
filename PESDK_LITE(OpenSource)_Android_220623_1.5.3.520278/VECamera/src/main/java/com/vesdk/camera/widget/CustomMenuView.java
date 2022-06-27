package com.vesdk.camera.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.vesdk.camera.R;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;

/**
 * @author bl
 */
public class CustomMenuView extends View {

    private Context mContext;
    /**
     * 菜单名字
     */
    private final ArrayList<String> mMenuList = new ArrayList<>();
    private RectF[] mRectList;
    /**
     * 当前选中菜单的下标
     */
    private int mCurrentIndex = 0;
    private int mPreIndex = 0;
    private Paint mPaint;
    /**
     * 文字宽度
     */
    private final Rect mTextRect = new Rect();
    private int mEndY = 0;
    /**
     * 相聚
     */
    private int mPadding = 30;
    /**
     * 属性动画
     */
    private ValueAnimator mValueAnimator;
    private float mAnimValue = 0;

    public CustomMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mPadding = CoreUtils.dip2px(mContext, 20);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(CoreUtils.dpToPixel(14));
        mPaint.setStrokeWidth(4);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStyle(Paint.Style.FILL);

        mValueAnimator = ValueAnimator.ofFloat(1, 0);
        mValueAnimator.setDuration(300);
        mValueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            float distance = Math.abs(mRectList[mPreIndex].centerX() - mRectList[mCurrentIndex].centerX());
            mAnimValue = (mPreIndex - mCurrentIndex) * distance * value;
            invalidate();
        });
    }


    /**
     * 添加菜单
     */
    public void addMenu(ArrayList<String> menuList, int index) {
        mMenuList.clear();
        mCurrentIndex = index;
        if (menuList != null && menuList.size() > 0) {
            mMenuList.addAll(menuList);
        }
        mRectList = new RectF[mMenuList.size()];
        for (int i = 0; i < mRectList.length; i++) {
            mRectList[i] = new RectF();
        }
        invalidate();
    }

    /**
     * 切换菜单
     */
    public void onSwitch(int index) {
        int oldIndex = mCurrentIndex;
        mCurrentIndex = index;
        mCurrentIndex = Math.min(mMenuList.size() - 1, Math.max(0, mCurrentIndex));
        if (oldIndex != index) {
            mPreIndex = oldIndex;
            //添加动画
            if (mValueAnimator.isRunning()) {
                mValueAnimator.end();
                mAnimValue = 0;
            }
            mValueAnimator.start();
            invalidate();
        }

        if (mListener != null) {
            mListener.onSwitchItem(mCurrentIndex);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMenuList.size() <= 0) {
            return;
        }
        if (mCurrentIndex < 0 || mCurrentIndex >= mMenuList.size()) {
            mCurrentIndex = 0;
        }
        int wHalf = (int) (getWidth() / 2.0f);
        //绘制底部选中原点
        mPaint.setColor(ContextCompat.getColor(mContext, R.color.red));
        canvas.drawCircle(wHalf, getHeight() - 20, 8, mPaint);
        if (mEndY == 0) {
            mEndY = (int) (getHeight() - (mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top));
        }

        //开始位置
        float start = wHalf - mAnimValue;
        //绘制选中
        String s = mMenuList.get(mCurrentIndex);
        canvas.drawText(s, start, mEndY, mPaint);
        mPaint.getTextBounds(s, 0, s.length(), mTextRect);
        mRectList[mCurrentIndex].set(start - mTextRect.width() / 2.0f, 0, start + mTextRect.width() / 2.0f, getHeight());

        mPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        //绘制左边
        drawTextLeft(canvas, start, mTextRect.width() / 2);
        //绘制右边
        drawTextRight(canvas, start, mTextRect.width() / 2);
    }

    private void drawTextLeft(Canvas canvas, float start, int right) {
        int r = right;
        for (int i = mCurrentIndex - 1; i >= 0; i--) {
            String content = mMenuList.get(i);
            mPaint.getTextBounds(content, 0, content.length(), mTextRect);
            canvas.drawText(content, start - r - mPadding - mTextRect.width() / 2.0f, mEndY, mPaint);
            r = r + mPadding + mTextRect.width();
            mRectList[i].set(start - r, 0, start - r + mTextRect.width(), getHeight());
            if (r > start) {
                for (int j = i - 1; j >= 0; j--) {
                    mRectList[j].set(0, 0, 0, 0);
                }
                break;
            }
        }
    }

    private void drawTextRight(Canvas canvas, float start, int left) {
        float l = left + start;
        for (int i = mCurrentIndex + 1; i < mMenuList.size(); i++) {
            String content = mMenuList.get(i);
            mPaint.getTextBounds(content, 0, content.length(), mTextRect);
            canvas.drawText(content, l + mPadding + mTextRect.width() / 2.0f, mEndY, mPaint);
            l = l + mPadding + mTextRect.width();
            mRectList[i].set(l - mTextRect.width(), 0, l, getHeight());
            if (l > getWidth()) {
                for (int j = i + 1; j < mMenuList.size(); j++) {
                    mRectList[j].set(0, 0, 0, 0);
                }
                break;
            }
        }
    }

    private boolean mMoved;
    private float mDownX, mDownY;
    private long mDownTime;
    private long mMoveTime = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mMoved = false;
                    mMoveTime = 0;
                    mDownTime = System.currentTimeMillis();
                    mDownX = event.getX();
                    mDownY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //判断是否移动
                    if (!mMoved) {
                        if (Math.abs(event.getX() - mDownX) > 10 || Math.abs(event.getY() - mDownY) > 10) {
                            mMoved = true;
                        }
                    }
                    //移动
                    if (System.currentTimeMillis() - mMoveTime > 800 || Math.abs(event.getX() - mDownX) > 200) {
                        mMoveTime = System.currentTimeMillis();
                        if (event.getX() - mDownX > 20) {
                            //向左移动
                            onSwitch(mCurrentIndex - 1);
                            mDownX = event.getX();
                        } else if (event.getX() - mDownX < -20) {
                            //向右移动
                            onSwitch(mCurrentIndex + 1);
                            mDownX = event.getX();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (!mMoved && System.currentTimeMillis() - mDownTime < 1000) {
                        //没有移动 判断是否点击到了菜单
                        for (int i = 0; i < mRectList.length; i++) {
                            RectF rectF = mRectList[i];
                            if (rectF != null && rectF.width() != 0 && rectF.contains((int) mDownX, (int) mDownY)) {
                                onSwitch(i);
                                break;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }


    private OnSwitchMenuListener mListener;

    public void setListener(OnSwitchMenuListener listener) {
        mListener = listener;
    }

    public interface OnSwitchMenuListener {

        /**
         * 切换菜单
         *
         * @param index 下标
         */
        void onSwitchItem(int index);

    }

}
