package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.pesdk.uisdk.R;

import androidx.core.content.ContextCompat;


/**
 * 自定义listview的item ,支持是否被选中，图片资源
 *
 * @author JIAN
 */
public class ExtListItemStyle extends ImageView {
    private Paint mBorderPaint = new Paint();
    private Rect mBorderRect = new Rect(), mContentDst = new Rect();
    public boolean mIsSelected = false;
    private int mBorderWidth = 2;
    private int mBorderRoundRadius = 6;
    private Bitmap mBitmap;

    public ExtListItemStyle(Context context) {
        this(context, null, 0);
    }

    public ExtListItemStyle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Drawable mDrawableSrc;

    public ExtListItemStyle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.pesdk_vepub_ExtImage);
        if (null != array) {
            mDrawableSrc = array.getDrawable(R.styleable.pesdk_vepub_ExtImage_extSrc);
            mBorderWidth = array.getDimensionPixelSize(R.styleable.pesdk_vepub_ExtImage_extBorderLineWidth, 2);
            mBorderRoundRadius = array.getDimensionPixelSize(R.styleable.pesdk_vepub_ExtImage_extBorderRoundRadius, 6);
            array.recycle();
        } else {
            mBorderWidth = getResources().getDimensionPixelSize(
                    R.dimen.borderline_width2);
        }
        mBorderPaint.setColor(ContextCompat.getColor(context, R.color.pesdk_main_color));
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBgColor = ContextCompat.getColor(context, R.color.pesdk_style_bg);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        if (null != mBitmap && !mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, null, mContentDst, null);
        } else if (null != mDrawableSrc) {
            mDrawableSrc.setBounds(mContentDst);
            mDrawableSrc.draw(canvas);
        } else if (mBgColor != Integer.MIN_VALUE) {
            mPaint.setColor(mBgColor);
            canvas.drawRoundRect(new RectF(mBorderRect), mBorderRoundRadius, mBorderRoundRadius, mPaint);
        }
        if (mIsSelected) {
            canvas.drawRoundRect(new RectF(mBorderRect), mBorderRoundRadius, mBorderRoundRadius, mBorderPaint);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mBorderRect.set(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth,
                getHeight() - mBorderWidth);
        mContentDst.set(mBorderRect.left + mBorderWidth, mBorderRect.top
                        + mBorderWidth, mBorderRect.right - mBorderWidth,
                mBorderRect.bottom - mBorderWidth);
    }

    /**
     * 设置是否选中
     */
    public void setSelected(boolean mIsSelected) {

        this.mIsSelected = mIsSelected;

        invalidate();

    }

    public boolean isSelected() {
        return mIsSelected;
    }


    /**
     * 设置图片资源
     *
     * @param mBitmap
     */
    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
        invalidate();
    }

    private int mBgColor = Integer.MIN_VALUE;
    private Paint mPaint;

    /**
     * 设置背景颜色
     */
    public void setBGColor(int color) {
        this.mBgColor = color;
        invalidate();
    }

}
