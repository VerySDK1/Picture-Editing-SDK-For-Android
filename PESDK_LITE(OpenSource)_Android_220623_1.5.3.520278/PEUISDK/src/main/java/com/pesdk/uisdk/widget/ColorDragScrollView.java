package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.utils.CoreUtils;

/**
 * 颜色横向拖动view
 */
public class ColorDragScrollView extends View {
    private final int MPADDING = 30;
    private final int BORDER_WIDTH = 4;
    private Paint mBorderPaint = new Paint(), mWhitePaint = new Paint();

    private Rect mWhiteSelectedRect = new Rect();
    private int mIndex = 3;
    private Paint mPaint = new Paint();
    private Rect[] mRects = null;
    private int mHalfItemWidth;
    private int mItemWidth;
    private int mItemSizeN = 0, mItemSizeP = 0, mItemP = 0;
    protected int[] colorArr;

    public ColorDragScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (colorArr == null || colorArr.length == 0) {
            colorArr = new int[]{Color.parseColor("#000000"), Color.parseColor("#484848"),
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#e8ce6b"), Color.parseColor("#f9b73c"),
                    Color.parseColor("#e3573b"), Color.parseColor("#be213b"),
                    Color.parseColor("#00ffff"), Color.parseColor("#5da9cf"),
                    Color.parseColor("#0695b5"), Color.parseColor("#2791db"),
                    Color.parseColor("#3564b7"), Color.parseColor("#e9c930"),
                    Color.parseColor("#a6b45c"), Color.parseColor("#87a522"),
                    Color.parseColor("#32b16c"), Color.parseColor("#017e54"),
                    Color.parseColor("#fdbacc"), Color.parseColor("#ff5a85"),
                    Color.parseColor("#ca4f9b"), Color.parseColor("#71369a"),
                    Color.parseColor("#6720d4"), Color.parseColor("#164c6e"),
                    Color.parseColor("#9f9f9f"),};
        }
        mRects = new Rect[colorArr.length];
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(BORDER_WIDTH);

        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources()
                .getColor(R.color.pesdk_speed_line_button_back_color));

        mGesDetector = new GestureDetector(context, new pressGestureListener());
    }

    /**
     * 指定选中的颜色
     *
     * @param color
     */
    public void setChecked(int color) {
        int len = colorArr.length;
        int index = 0;
        for (int i = 0; i < len; i++) {
            if (colorArr[i] == color) {
                index = i;
                break;
            }
        }
        setCheckIndex(index);
    }

    public int getIndex(int color) {
        int len = colorArr.length;
        for (int i = 0; i < len; i++) {
            if (colorArr[i] == color) {
                return i;
            }
        }
        return -1;
    }

    private GestureDetector mGesDetector;

    public ColorDragScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorDragScrollView(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawColor(Color.GREEN);
        Rect temp = null;
        int itemlength = 0;
        int len = mRects.length;
        for (int i = 0; i < len; i++) {
            temp = mRects[i];
            mPaint.setColor(colorArr[i]);
            canvas.drawRect(temp, mPaint);
            int x = (temp.right + temp.right - mItemWidth) / 2 - itemlength / 2;
        }
        mBorderPaint.setColor(getResources().getColor(R.color.pesdk_scroll_color_border));
        float borderWidth = mBorderPaint.getStrokeWidth() / 2;
        canvas.drawRect(mRects[0].left - borderWidth, mRects[0].top - borderWidth,
                mRects[len - 1].right + borderWidth, mRects[0].bottom + borderWidth, mBorderPaint);

        mWhiteSelectedRect.set(mCurrentCheckedRect.left - mItemP,
                mCurrentCheckedRect.top - mItemP, mCurrentCheckedRect.right
                        + mItemP, mCurrentCheckedRect.bottom + mItemP);

        mBorderPaint.setColor(getResources().getColor(R.color.pesdk_item_duration_txcolor));
        canvas.drawCircle(mWhiteSelectedRect.centerX(), mWhiteSelectedRect.centerY(), mItemSizeP / 2 + CoreUtils.dpToPixel(2), mBorderPaint);
        mWhitePaint.setColor(colorArr[mIndex]);
        canvas.drawCircle(mWhiteSelectedRect.centerX(), mWhiteSelectedRect.centerY(), mItemSizeP / 2 + CoreUtils.dpToPixel(2), mWhitePaint);
    }

    public void setColorArr(int[] colorArr) {
        this.colorArr = colorArr;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mItemSizeN = (getWidth() - CoreUtils.dpToPixel(8)) / colorArr.length;
        mItemSizeP = mItemSizeN + CoreUtils.dpToPixel(2);

        mItemP = (mItemSizeP - mItemSizeN) / 2;
        int len = colorArr.length;
        Rect rect = null;
        mItemWidth = mItemSizeN;

        mHalfItemWidth = mItemWidth / 2;
        int mleft = CoreUtils.dpToPixel(4);
        for (int i = 0; i < len; i++) {
            int mtop = (getHeight() - mItemWidth) / 2;
            rect = new Rect(mleft, mtop, mleft + mItemWidth, mtop
                    + mItemWidth);
            mRects[i] = rect;
            mleft += mItemWidth;
        }

        mCurrentCheckedRect = new Rect(mRects[mIndex]);
    }


    private boolean mIsForced = false;

    private float mXPosition = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mIsForced) {
                mXPosition = event.getX();
                onActionUp();
            }
        }
        invalidate();
        return true;
    }

    private class pressGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            mXPosition = e2.getX();
            mCurrentCheckedRect.offsetTo((int) mXPosition - mItemWidth / 2,
                    mCurrentCheckedRect.top);
            setIndex();
            return true;

        }

        @Override
        public boolean onDown(MotionEvent e) {
            mIsForced = true;
            mXPosition = e.getX();
            mCurrentCheckedRect.offsetTo((int) mXPosition - mItemWidth / 2,
                    mCurrentCheckedRect.top);
            setIndex();
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float veSlocityX, float velocityY) {
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

            if (mCurrentCheckedRect.left >= tempRect.left - mHalfItemWidth
                    && mCurrentCheckedRect.right < tempRect.right
                    + mHalfItemWidth) {
                mIndex = i;
                break;
            }
        }
        setLocation();
    }

    private Rect mCurrentCheckedRect;

    /**
     * 设置选中颜色
     *
     * @param index
     */
    public void setCheckIndex(int index) {
        mIndex = index;
        if (mIndex > colorArr.length || mIndex < 0) {
            mIndex = (int) (Math.ceil(colorArr.length / 2.0) - 1);
        }
        mCurrentCheckedRect = new Rect(mRects[mIndex]);
        invalidate();
    }

    private void setLocation() {
        Rect tagRect = mRects[mIndex];
        mCurrentCheckedRect = new Rect(tagRect);
        if (null != mColorListener) {
            mColorListener.getColor(colorArr[mIndex], mIndex);
        }
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
