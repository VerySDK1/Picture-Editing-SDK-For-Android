package com.pesdk.album.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.pesdk.album.R;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.PaintUtils;

import java.util.Locale;

import androidx.core.content.ContextCompat;

/**
 * 需要超出父控件
 */
public class ExtSeekBar extends SeekBar {

    /**
     * Drawable
     */
    private Drawable mDrawable;//提示背景
    private Drawable mPoint;//点
    /**
     * 宽高
     */
    private int mPopWidth;
    private int mPopHeight;//提示背景宽高
    private int mPointW;
    private int mPointH;//点的宽高
    /**
     * 内边距
     */
    private int mProMargin;
    /**
     * 画笔
     */
    private Paint mProPaint;//进度
    private Paint mBgPaint;//背景
    private Paint mTextPaint;//文字
    /**
     * 进度颜色、文字颜色
     */
    private int mProColor;
    private int mTextColor;
    private int mBgColor;
    private int mShadowColor;
    /**
     * 社会最小值
     */
    private int mMinValue = 0;
    /**
     * 长按显示弹出提示
     */
    private boolean mIsShow = false;
    /**
     * 进度 、背景、图片
     */
    private final RectF mProgressRectF = new RectF();
    private final RectF mBgRect = new RectF();
    private final Rect mPointRect = new Rect();
    private final Rect mTempRect = new Rect();
    /**
     * 是否显示提示  默认显示
     */
    private boolean mIsShowPrompt = true;
    /**
     * 是否显示中心数值  默认不显示
     */
    private boolean mIsCenterPrompt = false;
    /**
     * 是否一直显示提示 默认不一直显示
     */
    private boolean mIsAlwaysPrompt = false;
    /**
     * 缩放比例   只对弹出提示
     * 如 1-----2秒    1.5秒 ---> 50 / 100 + 1
     */
    private float mProportion = 1;
    /**
     * 进度右边 左右相反
     */
    private boolean mIsReverse = false;
    /**
     * 隐藏背景
     */
    private boolean mIsHideBackground = false;


    public ExtSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mDrawable = ContextCompat.getDrawable(context, R.drawable.album_ic_bar_pop);
        mPoint = ContextCompat.getDrawable(context, R.drawable.album_ic_bar_thumb);
        mPopWidth = mDrawable.getIntrinsicWidth();
        mPopHeight = mDrawable.getIntrinsicHeight();

        mPointW = mPoint.getIntrinsicWidth();
        mPointH = mPoint.getIntrinsicHeight();
        //内边距
        mProMargin = CoreUtils.dpToPixel(20 * 2);
        //颜色
        mProColor = ContextCompat.getColor(context, R.color.album_main);
        mTextColor = ContextCompat.getColor(context, R.color.album_main);
        mBgColor = ContextCompat.getColor(context, R.color.white);
        mShadowColor = ContextCompat.getColor(context, R.color.transparent_black80);
        //文字
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(CoreUtils.dpToPixel(14));
        //进度
        mProPaint = new Paint();
        mProPaint.setAntiAlias(true);
        mProPaint.setStyle(Paint.Style.FILL);
        mProPaint.setColor(mProColor);
        //背景
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mBgColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = mPointH;
        setMeasuredDimension(w, h);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        //设置颜色
        mBgPaint.setColor(mBgColor);
        mProPaint.setColor(mProColor);
        mTextPaint.setColor(mTextColor);

        //背景
        mBgRect.set(mProMargin / 2.0f, mPointH / 2.0f - 2.5f, getWidth() - mProMargin / 2.0f,
                mPointH / 2.0f + 2.5f);
        if (!mIsHideBackground) {
            canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);
        }

        //进度
        int px;
        if (mIsReverse) {
            int cp = getMax() - getProgress();
            px = (int) (mBgRect.right - mBgRect.width() * cp / getMax());
            mProgressRectF.set(px, mBgRect.top, mBgRect.right, mBgRect.bottom);
        } else {
            int cp = getProgress();
            px = (int) (mBgRect.width() * cp / getMax() + mBgRect.left);
            mProgressRectF.set(mBgRect.left, mBgRect.top, px, mBgRect.bottom);
        }
        canvas.drawRoundRect(mProgressRectF, 5, 5, mProPaint);

        //阴影
        if (!isEnabled()) {
            mBgPaint.setColor(mShadowColor);
            canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);
        }

        //绘制点
        int baseY = (int) mProgressRectF.centerY();
        mPointRect.set(px - (mPointW / 2), baseY - (mPointH / 2),
                px + (mPointW / 2), baseY + (mPointH / 2));
        mPoint.setBounds(mPointRect);
        mPoint.draw(canvas);

        //绘制提示 弹出
        if (mIsShowPrompt && (mIsShow || mIsAlwaysPrompt)) {
            int top = mPointRect.top - mPopHeight + 15;
            mTempRect.set(px - (mPopWidth / 2), top, px + (mPopWidth / 2), top + mPopHeight);
            mDrawable.setBounds(mTempRect);
            mDrawable.draw(canvas);
            //绘制文字
            mTextPaint.setTextSize(CoreUtils.dpToPixel(14));
            float value;
            String text;
            if (mProportion != 1) {
                if (mIsReverse) {
                    value = ((mMinValue + getMax() - getProgress()) / (mProportion + 0.0f));
                } else {
                    value = ((mMinValue + getProgress()) / (mProportion + 0.0f));
                }
                text = String.format(Locale.CHINA, "%.1f", value);
            } else {
                if (mIsReverse) {
                    text = String.valueOf((mMinValue + getMax() - getProgress()));
                } else {
                    text = String.valueOf((mMinValue + getProgress()));
                }
            }

            px = px - PaintUtils.getWidth(mTextPaint, text) / 2;
            int[] textArr = PaintUtils.getHeight(mTextPaint);
            //完全保留图标底部
            canvas.drawText(text, px, top + ((mPopHeight - 6) / 2.0f) + textArr[1], mTextPaint);
        } else if (mIsCenterPrompt) {
            //直接绘制在点上
            //绘制文字
            mTextPaint.setTextSize(CoreUtils.dpToPixel(8));
            String text;
            if (mIsReverse) {
                text = Integer.toString(mMinValue + getMax() - getProgress());
            } else {
                text = Integer.toString(mMinValue + getProgress());
            }
            px = px - PaintUtils.getWidth(mTextPaint, text) / 2;
            int[] textHArr = PaintUtils.getHeight(mTextPaint);
            //完全保留图标底部
            canvas.drawText(text, px, baseY + textHArr[0] / 2.0f - textHArr[1], mTextPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mIsShow = true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            mIsShow = false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


    /**
     * 比例
     */
    public void setProportion(float proportion) {
        this.mProportion = proportion;
        invalidate();
    }

    /**
     * 一直显示
     */
    public void setAlwaysPrompt() {
        this.mIsAlwaysPrompt = true;
    }

    /**
     * 隐藏提示
     */
    public void setHidePrompt() {
        mIsShowPrompt = false;
    }

    /**
     * 显示中心点提示
     */
    public void setShowCenterPrompt() {
        this.mIsCenterPrompt = true;
    }

    /**
     * 设置最小值
     */
    public void setMinValue(int minValue) {
        this.mMinValue = minValue;
    }

    /**
     * 设置相反
     */
    public void setReverse() {
        mIsReverse = true;
        invalidate();
    }

    /**
     * 进度颜色
     */
    public void setProColor(int proColor) {
        mProColor = proColor;
    }

    /**
     * 设置文字颜色
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    /**
     * 设置背景颜色
     */
    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
    }

    /**
     * 隐藏背景
     */
    public void setHideBackground(boolean hideBackground) {
        mIsHideBackground = hideBackground;
    }

}
