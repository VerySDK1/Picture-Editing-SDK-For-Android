package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.PaintUtils;

import androidx.core.content.ContextCompat;

/**
 * 需要超出父控件
 */
public class ExtSeekBar2 extends androidx.appcompat.widget.AppCompatSeekBar {
    private Paint mTextPaint = null;
    private boolean isProgressColor = true; //字体颜色： true 为使用mProgressColor的颜色值 ， false为不使用进度颜色值
    private boolean isSpeedReverse = false; //true 进度条颜色从右到左 false 从左到右
    private Drawable mDrawable;//提示背景
    private Drawable mThumb;//点
    private int dw = 0, dh = 0;//提示背景宽高
    private int thumbW = 0, thumbH = 0;//点的宽高
    private int mProgressDrawableMargin = 0;
    private Paint mProgressPaint;
    private Paint bgPaint;
    private int mProgressColor, mColor;

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    private int minValue = 0;
    private boolean mIsShow = false;
    private int mUnProgressColor;
    private int mTextSize = 10;

    public ExtSeekBar2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.pesdk_config_sbar_text_bg);
        mThumb = ContextCompat.getDrawable(getContext(), R.drawable.pesdk_config_sbar_thumb_n);

        mProgressColor = ContextCompat.getColor(getContext(), R.color.pesdk_main_press_color);
        mUnProgressColor = ContextCompat.getColor(getContext(), R.color.pesdk_un_progress_color);
        mColor = ContextCompat.getColor(getContext(), R.color.pesdk_main_press_color);


        dw = mDrawable.getIntrinsicWidth();
        dh = mDrawable.getIntrinsicHeight();

        thumbW = mThumb.getIntrinsicWidth();
        thumbH = mThumb.getIntrinsicHeight();
        mProgressDrawableMargin = CoreUtils.dpToPixel(20 * 2);
        //文字
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(CoreUtils.dpToPixel(14));
        //进度
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setColor(mProgressColor);
        //背景
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(ContextCompat.getColor(getContext(), R.color.pesdk_config_titlebar_bg));
        mTextSize = CoreUtils.dpToPixel(14);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), thumbH);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //绘制进度和背景
        int cp = getProgress();
        int px = ((getWidth() - mProgressDrawableMargin) * cp / getMax()) + (mProgressDrawableMargin / 2);
        RectF progressRectF = new RectF(mProgressDrawableMargin / 2, thumbH / 2 - 2.5f, px, thumbH / 2 + 2.5f);
        //绘制背景
        if (isSpeedReverse) {
            canvas.drawRoundRect(new RectF((mProgressDrawableMargin / 2), progressRectF.top + 1, (getWidth() - mProgressDrawableMargin / 2), progressRectF.bottom - 1), 5, 5, bgPaint);
        } else {
            canvas.drawRoundRect(new RectF((mProgressDrawableMargin / 2), progressRectF.top, (getWidth() - mProgressDrawableMargin / 2), progressRectF.bottom), 5, 5, bgPaint);
        }
        mProgressPaint.setColor(isEnabled() ? mProgressColor : mUnProgressColor);
        //绘制进度
        canvas.drawRoundRect(progressRectF, 5, 5, mProgressPaint);

        //绘制点
        int baseY = (int) progressRectF.centerY();
        Rect thumbRect = new Rect(px - (thumbW / 2), baseY - (thumbH / 2), px + (thumbW / 2), baseY + (thumbH / 2));
        mThumb.setBounds(thumbRect);
        mThumb.draw(canvas);
        //绘制提示 弹出
        if (mIsShowPrompt && (mIsShow || mIsAlwayPrompt)) {
            int top = (thumbRect.top - dh + 15);
            Rect rect = new Rect(px - (dw / 2), top, px + (dw / 2), top + dh);
            mDrawable.setBounds(rect);
            mDrawable.draw(canvas);
            //绘制文字
            if (isProgressColor) {
                mTextPaint.setColor(mProgressColor);

            }
            mTextPaint.setTextSize(mTextSize);
            float value;
            String text;
            if (mProportion != 1) {
                value = ((minValue + getProgress()) / (mProportion + 0.0f));
                text = String.format("%.1f", value);
            } else {
                text = String.valueOf((minValue + getProgress()));
            }

            px = px - PaintUtils.getWidth(mTextPaint, text) / 2;
            int[] textHArr = PaintUtils.getHeight(mTextPaint);
            //完全保留图标底部
            canvas.drawText(text, px, top + ((dh - 6) / 2) + textHArr[1], mTextPaint);
        } else if (mIsCenterPrompt) {
            //直接绘制在点上
            //绘制文字
            mTextPaint.setColor(mColor);
            mTextPaint.setTextSize(CoreUtils.dpToPixel(8));
            String text = Integer.toString(minValue + getProgress());
            px = px - PaintUtils.getWidth(mTextPaint, text) / 2;
            int[] textHArr = PaintUtils.getHeight(mTextPaint);
            //完全保留图标底部
            canvas.drawText(text, px, baseY + textHArr[0] / 2 - textHArr[1], mTextPaint);
        }
    }

    //缩放比例   只对弹出提示
    // 如 1-----2秒    1.5秒 ---> 50 / 100 + 1
    private int mProportion = 1;

    public void setProportion(int proportion) {
        this.mProportion = proportion;
        invalidate();
    }

    //是否一直显示提示
    private boolean mIsAlwayPrompt = false;

    public void setIsAlwayPrompt(boolean mIsAlwayShow) {
        this.mIsAlwayPrompt = mIsAlwayShow;
    }

    //是否显示提示
    private boolean mIsShowPrompt = true;

    public void setIsShowPrompt(boolean mIsShowPrompt) {
        this.mIsShowPrompt = mIsShowPrompt;
    }

    /**
     * 设置提示颜色
     */
    public void setProgressColor(int color) {
        mProgressColor = color;
    }

    public void setUnProgressColor(int color) {
        mUnProgressColor = color;
    }

    public void setBgPaintColor(int color) {
        bgPaint.setColor(color);
    }

    public void setProgressPaintColor(int color) {
        mProgressPaint.setColor(color);
    }

    public void setPaintColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    //是否显示中心数值
    private boolean mIsCenterPrompt = false;

    public void setIsCenterShow(boolean mIsCenterShow) {
        this.mIsCenterPrompt = mIsCenterShow;
    }

    public void setIsProgressColor(boolean isProgressColor) {
        this.isProgressColor = isProgressColor;
    }

    public void setIsSpeedReverse(boolean isSpeedReverse) {
        this.isSpeedReverse = isSpeedReverse;
    }

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
}
