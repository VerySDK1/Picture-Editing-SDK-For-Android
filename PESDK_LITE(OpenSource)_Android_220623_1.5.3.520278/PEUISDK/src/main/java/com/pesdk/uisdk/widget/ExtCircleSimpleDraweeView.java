package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.GraphicsHelper;


/**
 * 支持下载进度、选择状态  （基于fresco 更成熟 、稳定）
 *
 * @author JIAN
 * @date 2018/07/03
 */
public class ExtCircleSimpleDraweeView extends androidx.appcompat.widget.AppCompatImageView implements Checkable {

    private int mBorderWidth = 4;
    private int mDrawBorderColor = 0;
    private int mDrawBgColor = 0;
    private int mProgress = 100;
    private boolean mIsChecked = false;

    public ExtCircleSimpleDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray tArray = context.obtainStyledAttributes(attrs,
                R.styleable.pesdk_ExtCircle);

        mIsChecked = tArray.getBoolean(R.styleable.pesdk_ExtCircle_circleChecked,
                false);

        Resources res = getResources();

        mDrawBgColor = tArray.getInt(R.styleable.pesdk_ExtCircle_circleBgColor,
                res.getColor(R.color.pesdk_transparent));
        mDrawBorderColor = tArray.getInt(
                R.styleable.pesdk_ExtCircle_circleBorderColor,
                res.getColor(R.color.pesdk_main_color));

        tArray.recycle();
    }


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

    // private    String TAG = "ExtCircleSimpleDraweeView";
    private boolean showShadow = false;
    private Paint mShadowPaint = new Paint();

    public void showShadow(int color) {
        showShadow = true;
        if (mShadowPaint == null) {
            mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mShadowPaint.setColor(color);
        mShadowPaint.setStyle(Paint.Style.FILL);
    }


    public void cancelShadow() {
        showShadow = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        GraphicsHelper.drawRoundedBorderCorner(canvas, getWidth(), getHeight(), mBorderWidth, mDrawBorderColor, isChecked(), mProgress);

        if (showShadow && mShadowPaint != null) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mShadowPaint);
        }

    }


    public void setBorderWidth(int mBorderWidth) {
        this.mBorderWidth = mBorderWidth;
    }

    public void setBorderColor(int borderColor) {
        this.mDrawBorderColor = borderColor;
        invalidate();
    }

    public void setBgColor(int bgColor) {
        this.mDrawBgColor = bgColor;
        invalidate();
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
    }


    @Override
    public void setChecked(boolean checked) {
        mIsChecked = checked;
        invalidate();
    }
}
