package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.utils.CoreUtils;

/**
 * 颜色横向拖动view
 */
public class ColorDragView extends View {

    //选中圆
    private Paint mPaint = new Paint();//颜色
    //当前选中的下标
    private int mIndex = 4;
    //宽度
    private int mHalfItemWidth;
    private int mItemWidth = CoreUtils.dpToPixel(20);
    //颜色
    protected int[] colorArr;
    private Rect[] mRects = null;
    //手势
    private GestureDetector mGesDetector;
    //当前选中
    private Rect mCurrentCheckedRect;
    //圆角半径
    private int mRoundRadius = CoreUtils.dpToPixel(3);

    public ColorDragView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray tA = context.obtainStyledAttributes(attrs, R.styleable.pesdk_extdragarray);
        tA.recycle();
        colorArr=AppConfig.colors;
        mRects = new Rect[colorArr.length];
        mPaint.setAntiAlias(true);
        mGesDetector = new GestureDetector(context, new pressGestureListener());
    }

    public ColorDragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorDragView(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        //绘制颜色
        //第一个 左上角和左下角圆角
        RectF rectF = new RectF(mRects[0]);
        mPaint.setColor(colorArr[0]);
        canvas.drawRoundRect(rectF, mRoundRadius, mRoundRadius, mPaint);
        canvas.drawRect(rectF.right - mRoundRadius, rectF.top, rectF.right, rectF.bottom, mPaint);

        Rect temp = null;
        int len = mRects.length;
        for (int i = 1; i < len - 1; i++) {
            temp = mRects[i];
            mPaint.setColor(colorArr[i]);
            canvas.drawRect(temp, mPaint);
        }

        //最后一个 右上角和右下角圆角
        rectF = new RectF(mRects[len - 1]);
        mPaint.setColor(colorArr[len - 1]);
        canvas.drawRoundRect(rectF, mRoundRadius, mRoundRadius, mPaint);
        canvas.drawRect(rectF.left, rectF.top, rectF.left + mRoundRadius, rectF.bottom, mPaint);

        if (mIndex >= 0) {
            drawSelect(canvas, mIndex);
        }

    }

    private int padding = 5;

    //当前选中的item
    private void drawSelect(Canvas canvas, int index) {
        RectF tmp = new RectF(mRects[index]);
        tmp.inset(-padding, -padding);
        int color = colorArr[index];
        mPaint.setColor(color);
        canvas.drawRoundRect(tmp, mRoundRadius, mRoundRadius, mPaint);
    }

    /**
     * 设置颜色
     *
     * @param colorArr
     */
    public void setColorArr(int[] colorArr) {
        this.colorArr = colorArr;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = mItemWidth * colorArr.length + padding * 2;
        int h = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //每一个宽度
        mHalfItemWidth = mItemWidth / 2;
        int len = colorArr.length;
        Rect rect = null;
        int mleft = padding;
        for (int i = 0; i < len; i++) {
            rect = new Rect(mleft, padding, mleft + mItemWidth, getHeight() - padding);
            mRects[i] = rect;
            mleft += mItemWidth;
        }
        mCurrentCheckedRect = new Rect(mRects[mIndex]);

    }

    private boolean mIsForced = false;

    private float mXPosition = -1;
    private boolean mIsMove = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mIsForced) {
                mXPosition = event.getX();
                onActionUp();
            }
            mIsMove = false;
        }
        invalidate();
        return true;
    }

    private class pressGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mIsMove = true;
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mIsMove = false;
            mIsForced = true;
            mXPosition = e.getX();
            mCurrentCheckedRect.offsetTo((int) mXPosition - mItemWidth / 2, mCurrentCheckedRect.top);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float veSlocityX, float velocityY) {
            onActionUp();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mXPosition = e.getX();
            onActionUp();
            return false;
        }
    }

    private void onActionUp() {
        mIsForced = false;
        setIndex();
    }

    private void setIndex() {
        for (int i = 0; i < mRects.length; i++) {
            Rect tempRect = mRects[i];
            if (mCurrentCheckedRect.left >= tempRect.left - mHalfItemWidth && mCurrentCheckedRect.right < tempRect.right + mHalfItemWidth) {
                mIndex = i;
                break;
            }
        }
        setLocation();
    }

    /**
     * 选中的颜色值
     *
     * @param color
     */
    public void setColor(int color) {
        int tmp = -1;
        for (int i = 0; i <  colorArr.length; i++) {
            if (colorArr[i] == color) {
                tmp = i;
                break;
            }
        }
        if (tmp >= 0) {
            mIndex = tmp;
            invalidate();
        }
    }


    private void setLocation() {
        Rect tagRect = mRects[mIndex];
        mCurrentCheckedRect = new Rect(tagRect);
        if (null != mColorListener && !mIsMove) {
            mColorListener.getColor(colorArr[mIndex], mIndex);
        }
    }

    public int getColor() {
        return null != colorArr && colorArr.length > mIndex ? colorArr[mIndex] : Color.BLACK;
    }

    private ColorPicker.IColorListener mColorListener;

    /**
     * 设置速度改变listener
     *
     * @param colorListener
     */
    public void setColorChangedListener(ColorPicker.IColorListener colorListener) {
        mColorListener = colorListener;
    }
}
