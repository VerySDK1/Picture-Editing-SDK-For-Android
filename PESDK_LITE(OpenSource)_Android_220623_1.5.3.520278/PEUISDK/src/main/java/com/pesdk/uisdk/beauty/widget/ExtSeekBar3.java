package com.pesdk.uisdk.beauty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.PaintUtils;

import java.util.Locale;

import androidx.core.content.ContextCompat;

/**
 * 需要超出父控件
 */
public class ExtSeekBar3 extends SeekBar {

    /**
     * 点
     */
    private Drawable mPoint;
    /**
     * 提示背景
     */
    private Drawable mHintDrawable;
    private final Rect mHintRect = new Rect();
    private int mHintWidth;
    private int mHintHeight;
    /**
     * 宽高
     */
    private int mPointW;//点的宽高
    private int mPointH;
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
    /**
     * 进度 、背景、图片
     */
    private final RectF mProgressRectF = new RectF();
    private final RectF mBgRect = new RectF();
    private final Rect mPointRect = new Rect();
    /**
     * 左右的比例
     */
    private float mLeftValue;

    //是否显示提示
    private boolean mIsShowPrompt = true;


    public ExtSeekBar3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHintDrawable = ContextCompat.getDrawable(context, R.drawable.pesdk_config_sbar_text_bg);
        mPoint = ContextCompat.getDrawable(context, R.drawable.pesdk_config_sbar_thumb_n);
        mHintWidth = mHintDrawable.getIntrinsicWidth();
        mHintHeight = mHintDrawable.getIntrinsicHeight();

        if (mPoint != null) {
            mPointW = mPoint.getIntrinsicWidth();
            mPointH = mPoint.getIntrinsicHeight();
        }
        //内边距
        mProMargin = CoreUtils.dpToPixel(20 * 2);
        //颜色
        mProColor = ContextCompat.getColor(getContext(), R.color.pesdk_main_press_color);
        mTextColor = ContextCompat.getColor(getContext(), R.color.pesdk_transparent_white);
        mBgColor = ContextCompat.getColor(getContext(), R.color.pesdk_config_titlebar_bg);
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
        canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);

        //进度
        float progress = getProgress() * 1.0f / getMax();
        float start = (mBgRect.width() * mLeftValue + mBgRect.left);
        float end = (mBgRect.width() * progress + mBgRect.left);

        if (progress > mLeftValue) {
            mProgressRectF.set(start, mBgRect.top, end, mBgRect.bottom);
        } else {
            mProgressRectF.set(end, mBgRect.top, start, mBgRect.bottom);
        }
        canvas.drawRoundRect(mProgressRectF, 5, 5, mProPaint);

        //绘制点
        int baseY = (int) mProgressRectF.centerY();
        mPointRect.set((int) (end - (mPointW / 2.0f)), baseY - (mPointH / 2),
                (int) (end + (mPointW / 2.0f)), baseY + (mPointH / 2));
        mPoint.setBounds(mPointRect);
        mPoint.draw(canvas);

        //提示
        if (mIsShowPrompt && mHintDrawable != null && mIsShow) {
            float v = progress * mBgRect.width() + mBgRect.left;
            int startY = baseY - (mPointH / 3);
            mHintRect.set((int) (v - mHintWidth / 2), startY - mHintHeight,
                    (int) (v + mHintWidth / 2), startY);
            mHintDrawable.setBounds(mHintRect);
            mHintDrawable.draw(canvas);
            //绘制文字
            String text = String.format(Locale.CHINA, "%.1f", (progress - mLeftValue) * 2);
            float startX = v - PaintUtils.getWidth(mTextPaint, text) / 2.0f;
            int[] textHArr = PaintUtils.getHeight(mTextPaint);
            //完全保留图标底部
            canvas.drawText(text, startX, startY - mHintHeight + ((mHintHeight - 6) / 2.0f) + textHArr[1], mTextPaint);
        }
    }


    private boolean mIsShow = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mIsShow = true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP) {
            mIsShow = false;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置左边所赞比例
     */
    public void setLeftValue(float leftValue) {
        mLeftValue = leftValue;
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
     * 提示
     */
    public void setShowPrompt(boolean showPrompt) {
        mIsShowPrompt = showPrompt;
    }
}
