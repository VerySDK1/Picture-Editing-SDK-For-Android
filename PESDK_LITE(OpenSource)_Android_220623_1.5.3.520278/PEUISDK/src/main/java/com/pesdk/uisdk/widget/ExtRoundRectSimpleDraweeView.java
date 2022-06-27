package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.GraphicsHelper;
import com.vecore.base.lib.utils.CoreUtils;

import androidx.core.content.ContextCompat;


/**
 * 支持下载进度、选择状态
 */
public class ExtRoundRectSimpleDraweeView extends androidx.appcompat.widget.AppCompatImageView implements Checkable {

    private int mBorderWidth = 4;//线宽
    private int mCheckColor = 0;//线颜色、选中
    private int mProgressBgColor = 0;//背景颜色、进度颜色
    private int mProgress = 100;//进度
    private boolean mIsChecked = false;//是否选中
    private int mCornersRadius = 20;//矩形圆角size

    private Paint mProgressPaint = new Paint(), mCheckPaint = new Paint();
    private boolean enableShadowStyle = false;

    public ExtRoundRectSimpleDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.pesdk_ExtRoundRect);
        mIsChecked = tArray.getBoolean(R.styleable.pesdk_ExtRoundRect_roundRectChecked, false);
        enableShadowStyle = tArray.getBoolean(R.styleable.pesdk_ExtRoundRect_roundRectShadow, false);
        //进度颜色、线颜色、圆角大小、
        mProgressBgColor = tArray.getColor(R.styleable.pesdk_ExtRoundRect_roundRectProgressColor, ContextCompat.getColor(context, R.color.pesdk_main_press_color));
        mCheckColor = tArray.getColor(R.styleable.pesdk_ExtRoundRect_roundRectCheckColor, ContextCompat.getColor(context, R.color.pesdk_main_press_color));
        mCornersRadius = (int) tArray.getDimension(R.styleable.pesdk_ExtRoundRect_roundRectRadius, CoreUtils.dip2px(context, 5));
        mBorderWidth = (int) tArray.getDimension(R.styleable.pesdk_ExtRoundRect_roundRectBorderWidth, 4);
        tArray.recycle();

        mProgressPaint.setAntiAlias(true);// 设置去锯齿
        mProgressPaint.setColor(mProgressBgColor);
        //设置画笔模式：填充
        mProgressPaint.setStyle(Paint.Style.FILL);

        mCheckPaint.setAntiAlias(true);// 设置去锯齿
        mCheckPaint.setColor(mCheckColor);
        if (enableShadowStyle) {
            mCheckPaint.setStyle(Paint.Style.FILL);
        } else {
            mCheckPaint.setStyle(Paint.Style.STROKE);
        }
        mCheckPaint.setStrokeWidth(mBorderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }
        try {
            Bitmap bitmap = GraphicsHelper.getBitmap(drawable);
            if (null != bitmap) {
                int w = this.getWidth();
                int h = this.getHeight();
                GraphicsHelper.drawRoundedCornerBitmap(canvas, w, h, bitmap,
                        mCornersRadius, mBorderWidth / 2, Color.TRANSPARENT, Color.TRANSPARENT,
                        true, 0);
                bitmap.recycle();
            }
        } catch (Exception e) {
        }

        if (isChecked()) {
            int w = enableShadowStyle ? 0 : mBorderWidth;
            RectF rect = new RectF(w, w, getWidth() - w, getHeight() - w);
            canvas.drawRoundRect(rect, mCornersRadius, mCornersRadius, mCheckPaint);
        }

        //绘制遮罩
        if (mProgress < 100) {
            drawProgress(canvas);
        }
    }

    /**
     * 绘制进度
     *
     * @param canvas
     */
    private void drawProgress(Canvas canvas) {
        int width = getWidth() - mBorderWidth * 2;
        int space = (int) spacing(mCornersRadius / 2, mCornersRadius / 2, getWidth() / 2, getHeight() / 2);
//        int w = (int) Math.ceil(((Math.sqrt(2) * width - (Math.sqrt(2) - 1) * (mCornersRadius + mBorderWidth) * 2) - width) / 2);
        int w = space - getWidth() / 2;
        Rect dst = new Rect(-w, -w, width + w, width + w);
        dst.offset(mBorderWidth, mBorderWidth);
        canvas.drawArc(new RectF(dst), 270, mProgress * 360 / 100, true, mProgressPaint);
    }

    /**
     * 设置进度颜色
     *
     * @param color
     */
    public void setProgressColor(int color) {
        this.mProgressBgColor = color;
        mProgressPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置选中颜色
     */
    public void setCheckColor(int color) {
        this.mCheckColor = color;
        mCheckPaint.setColor(color);
        invalidate();
    }

    private static final String TAG = "ExtRoundRectSimpleDrawe";

    /**
     * 设置当前下载进度
     *
     * @param pro 未下载时设置为0
     */
    public void setProgress(int pro) {
        mProgress = Math.min(100, pro);
        if (pro > 0) {
            if (!mIsChecked) {
                mIsChecked = true;
            }
        }
        this.invalidate();
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
    }

    /**
     * 设置选中
     *
     * @param checked
     */
    @Override
    public void setChecked(boolean checked) {
        mIsChecked = checked;
        invalidate();
    }


    /**
     * 两点的距离
     */
    public static double spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }
}
