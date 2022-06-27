package com.pesdk.uisdk.beauty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;

/**
 * 列表轴
 */
public class ListSeekBarView extends View {

    private static final int PADDING = 45;//内边距
    private static int POINT_HEIGHT = 10;//点高度
    private static final int LINE_HEIGHT = 10;//线的高度
    private static final int ROUND_RADIUS = 5;//线的高度

    /**
     * 点
     */
    private Bitmap mPointBmp;
    /**
     * rectf
     */
    private final RectF mBgRectF = new RectF();//背景
    private final RectF mProRectF = new RectF();//进度
    private final RectF mPointRect = new RectF();//点
    /**
     * paint
     */
    private Paint mPaint;
    private Paint mTextPaint;
    /**
     * 进度颜色、文字颜色
     */
    private int mProColor, mTextColor, mBgColor;
    /**
     * 文字高度
     */
    private float mDistance;
    /**
     * 数据
     */
    private final ArrayList<String> mData = new ArrayList<>();
    /**
     * 下标
     */
    private int mIndex = 0;
    /**
     * 文字和字之间的距离
     */
    private int mSeparation = 0;

    public ListSeekBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListSeekBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPointBmp = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_beauty_thumb);
        POINT_HEIGHT = mPointBmp.getHeight();
        //颜色
        mProColor = Color.WHITE;
        mTextColor = Color.parseColor("#EBEBEB");
        mBgColor = Color.parseColor("#27262C");
        //文字
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(CoreUtils.dpToPixel(10));
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        //笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景
        mPaint.setColor(mBgColor);
        mBgRectF.set(PADDING, (POINT_HEIGHT - LINE_HEIGHT) / 2.0f,
                getWidth() - PADDING, (POINT_HEIGHT + LINE_HEIGHT) / 2.0f);
        canvas.drawRoundRect(mBgRectF, ROUND_RADIUS, ROUND_RADIUS, mPaint);
        //判断条件
        if (mData.size() <= 0) {
            return;
        }
        mIndex = Math.max(0, Math.min(mIndex, mData.size() - 1));
        //绘制进度
        mPaint.setColor(mProColor);
        float proportion = mIndex * 1.0f / (mData.size() - 1);
        mProRectF.set(mBgRectF);
        float end;
        if (mTouch) {
            end = Math.max(PADDING, Math.min(mEventX, getWidth()
                    - PADDING * 2));
        } else {
            end = mBgRectF.width() * proportion + mBgRectF.left;
        }
        mProRectF.right = end;
        canvas.drawRoundRect(mProRectF, ROUND_RADIUS, ROUND_RADIUS, mPaint);
        //绘制点
        mPointRect.set(end - POINT_HEIGHT / 2.0f, 0, end + POINT_HEIGHT / 2.0f, POINT_HEIGHT);
        canvas.drawBitmap(mPointBmp, null, mPointRect, null);
        //绘制文字
        for (int i = 0; i < mData.size(); i++) {
            drawText(canvas, i, mData.get(i));
        }
    }

    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas, int i, String s) {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        float proportion = i * 1.0f / (mData.size() - 1);
        float x = proportion * (getWidth() - PADDING * 2) + PADDING;
        float y = POINT_HEIGHT + mDistance + mSeparation;
        mTextPaint.setColor(mTextColor);
        canvas.drawText(s, x, y, mTextPaint);
    }

    private boolean mTouch = false;
    private float mEventX;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int point = event.getPointerCount();
        if (point == 1) {
            mEventX = event.getX();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //判断是是不是点击到了点上面
                mTouch = true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

            } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP) {
                mTouch = false;
                //计算现在位置
                float t = 0;
                int index = 0;
                for (int i = 0; i < mData.size(); i++) {
                    float proportion = i * 1.0f / (mData.size() - 1);
                    float x = proportion * getWidth();
                    if (x < mEventX) {
                        t = mEventX - x;
                        index = i;
                    } else {
                        if (t - (x - mEventX) > 0) {
                            index = i;
                            break;
                        }
                    }
                }
                mIndex = index;
                if (mListener != null) {
                    mListener.onChange(index, mData.get(index));
                }
            }
            invalidate();
            return true;
        } else {
            return false;
        }
    }


    public int getIndex() {
        return mIndex;
    }

    public String getData() {
        return mData.get(mIndex);
    }

    public void setProColor(int proColor) {
        mProColor = proColor;
        invalidate();
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidate();
    }

    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
        invalidate();
    }

    public void setData(ArrayList<String> data, int index) {
        mData.clear();
        if (data != null && data.size() > 0) {
            mData.addAll(data);
        }
        mIndex = index;
        invalidate();
    }

    public void setIndex(int index) {
        mIndex = index;
        invalidate();
    }

    public void setTextSize(int pixel) {
        mTextPaint.setTextSize(pixel);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        invalidate();
    }

    public void setSeparation(int separation) {
        mSeparation = separation;
    }

    private OnListSeekBarListener mListener;

    public void setListener(OnListSeekBarListener listener) {
        mListener = listener;
    }

    public interface OnListSeekBarListener {

        /**
         * 改变了
         */
        void onChange(int index, String data);

    }

}
