package com.pesdk.uisdk.beauty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * 视频拖拽
 */
public class DragMediaView extends View {

    /**
     * 缩放比例区间
     */
    private static final float MAX_SCALE = 6.0f;
    private static final float MIN_SCALE = 0.05f;
    /**
     * 缩放padding
     */
    private static final float PADDING = 0.5f;

    /**
     * 笔
     */
    private Paint mPaint;
    /**
     * 背景颜色
     */
    private int mBackgroundColor;
    /**
     * 矩阵
     */
    private Matrix mMatrix;
    /**
     * 手势
     */
    private GestureDetector mGestureDetector;

    /**
     * 显示的区域
     */
    private RectF mShowRect;
    private final RectF mFaceRectF = new RectF();

    /**
     * 限制区域
     */
    private boolean mLimitArea = true;
    /**
     * 绘制人脸
     */
    private boolean mDrawFace;


    public DragMediaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragMediaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mShowRect = new RectF();
        mMatrix = new Matrix();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);

        //背景颜色
        mBackgroundColor = Color.parseColor("#9A000000");
        mGestureDetector = new GestureDetector(context, new MyGestureDetector());
    }


    /**
     * 设置显示区域和角度
     */
    public void setData(RectF showRect) {
        if (showRect != null) {
            mShowRect.set(showRect);
        }
        invalidate();
    }

    /**
     * 限制显示区域
     */
    public void setLimitArea(boolean limitArea) {
        mLimitArea = limitArea;
    }

    /**
     * 绘制人脸
     */
    public void setDrawFace(boolean drawFace) {
        mDrawFace = drawFace;
        invalidate();
    }


    /**
     * 是否绘制人脸
     */
    public boolean isDrawFace() {
        return mDrawFace;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawFace && mListener != null && mShowRect != null) {
            List<BeautyFaceInfo> faceList = mListener.getFaceList();
            if (faceList != null && faceList.size() > 0) {
                //背景
                mPaint.setColor(mBackgroundColor);
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mFaceRectF.set(0, 0, getWidth(), getHeight());
                canvas.drawRect(mFaceRectF, mPaint);

                //人脸
                mPaint.setColor(Color.YELLOW);
                mPaint.setStyle(Paint.Style.STROKE);
                for (BeautyFaceInfo faceInfo : faceList) {
                    RectF faceRectF = faceInfo.getFaceRectF();
                    if (faceRectF != null) {
                        //相对于显示区域位置
                        mFaceRectF.set(faceRectF.left * mShowRect.width() + mShowRect.left,
                                faceRectF.top * mShowRect.height() + mShowRect.top,
                                faceRectF.right * mShowRect.width() + mShowRect.left,
                                faceRectF.bottom * mShowRect.height() + mShowRect.top);
                        canvas.drawRect(mFaceRectF, mPaint);
                    }
                }
            }
        }
    }


    /**
     * 点击时手指坐标
     */
    private final PointF mDownPoint = new PointF();
    /**
     * 操作时的显示区域
     */
    private final RectF mTemporaryRectF = new RectF();
    /**
     * 双指缩放开始之间的距离和与X轴角度
     */
    private double mStartLen;
    /**
     * 是否点击、是否是两个手指
     */
    private boolean mTwoPoint = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {
            return false;
        }
        mListener.onMove();
        mGestureDetector.onTouchEvent(event);
        int pointerNum = event.getPointerCount();
        int action = event.getAction();
        float containerWidth = getWidth();
        float containerHeight = getHeight();
        if (pointerNum == 2) {
            //缩放 旋转
            if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                mTwoPoint = true;
                mTemporaryRectF.set(mShowRect);
                mStartLen = getDistance(event);
            } else if (action == MotionEvent.ACTION_MOVE) {
                //移动
                double endLen = getDistance(event);
                // 计算缩放系数
                float scale = (float) (endLen / mStartLen);
                mMatrix.reset();
                float[] center = getCenter(event);
                mMatrix.postScale(scale, scale, center[0], center[1]);
                mMatrix.mapRect(mShowRect, mTemporaryRectF);
                //限制缩放范围
                float asp = mShowRect.width() / mShowRect.height();
                if (mShowRect.width() < MIN_SCALE * containerWidth) {
                    //缩小到最小
                    float width = MIN_SCALE * containerHeight;
                    mShowRect.set(mShowRect.centerX() - width / 2,
                            mShowRect.centerY() - width / asp / 2,
                            mShowRect.centerX() + width / 2,
                            mShowRect.centerY() + width / asp / 2);
                } else if (mShowRect.height() < MIN_SCALE * containerHeight) {
                    float height = MIN_SCALE * containerHeight;
                    mShowRect.set(mShowRect.centerX() - height * asp / 2,
                            mShowRect.centerY() - height / 2,
                            mShowRect.centerX() + height * asp / 2,
                            mShowRect.centerY() + height / 2);
                } else if (mShowRect.width() > MAX_SCALE * containerWidth) {
                    float width = MAX_SCALE * containerHeight;
                    mShowRect.set(mShowRect.centerX() - width / 2,
                            mShowRect.centerY() - width / asp / 2,
                            mShowRect.centerX() + width / 2,
                            mShowRect.centerY() + width / asp / 2);
                } else if (mShowRect.height() > MAX_SCALE * containerHeight) {
                    float height = MAX_SCALE * containerHeight;
                    mShowRect.set(mShowRect.centerX() - height * asp / 2,
                            mShowRect.centerY() - height / 2,
                            mShowRect.centerX() + height * asp / 2,
                            mShowRect.centerY() + height / 2);
                }
                mListener.onRectChange(mShowRect);
            }
            invalidate();
            return true;
        } else if (pointerNum == 1) {
            //移动
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownPoint.set(event.getX(), event.getY());
                    mTemporaryRectF.set(mShowRect);
                    mTwoPoint = false;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTwoPoint) {
                        return false;
                    }
                    float moveX = event.getX() - mDownPoint.x;
                    float moveY = event.getY() - mDownPoint.y;
                    mShowRect.set(mTemporaryRectF.left + moveX,
                            mTemporaryRectF.top + moveY,
                            mTemporaryRectF.right + moveX,
                            mTemporaryRectF.bottom + moveY);

                    //限制区域
                    if (mLimitArea) {
                        if (mShowRect.left > containerWidth - 1) {
                            float adjust = mShowRect.left - containerWidth + 1;
                            mShowRect.set(mShowRect.left - adjust, mShowRect.top,
                                    mShowRect.right - adjust, mShowRect.bottom);
                        } else if (mShowRect.right < 1) {
                            float adjust = mShowRect.right;
                            mShowRect.set(mShowRect.left - adjust, mShowRect.top,
                                    mShowRect.right - adjust, mShowRect.bottom);
                        } else if (mShowRect.top > containerHeight - 1) {
                            float adjust = mShowRect.top - containerHeight + 1;
                            mShowRect.set(mShowRect.left, mShowRect.top - adjust,
                                    mShowRect.right, mShowRect.bottom - adjust);
                        } else if (mShowRect.bottom < 1) {
                            float adjust = mShowRect.bottom;
                            mShowRect.set(mShowRect.left, mShowRect.top - adjust,
                                    mShowRect.right, mShowRect.bottom - adjust);
                        }
                    }
                    if (mListener.onRectChange(mShowRect)) {
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    //抬起手指 整体移动
                    if (mTwoPoint) {
                        return false;
                    }
                    invalidate();
                    break;
                default:
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 计算两点之间的距离 缩放
     */
    private double getDistance(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int pointerId = event.getPointerId(0);
        int pointerId1 = event.getPointerId(1);
        if (pointerId > pointerCount || pointerId1 > pointerCount) {
            return mStartLen;
        }
        int xlen = Math.abs((int) event.getX(event.getPointerId(0)) - (int) event.getX(event.getPointerId(1)));
        int ylen = Math.abs((int) event.getY(event.getPointerId(0)) - (int) event.getY(event.getPointerId(1)));
        return Math.sqrt(xlen * xlen + ylen * ylen);
    }

    /**
     * 计算两点之间的距离 缩放
     */
    private float[] getCenter(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int pointerId = event.getPointerId(0);
        int pointerId1 = event.getPointerId(1);
        if (pointerId > pointerCount || pointerId1 > pointerCount) {
            return new float[]{mTemporaryRectF.centerX(), mTemporaryRectF.centerY()};
        }
        float centerX = Math.abs(event.getX(event.getPointerId(0)) + (int) event.getX(event.getPointerId(1))) / 2;
        float centerY = Math.abs(event.getY(event.getPointerId(0)) + (int) event.getY(event.getPointerId(1))) / 2;
        return new float[]{centerX, centerY};
    }


    private OnDragListener mListener;

    public void setListener(OnDragListener listener) {
        mListener = listener;
    }

    /**
     * 位置改变
     */
    public interface OnDragListener {

        /**
         * 人脸
         */
        List<BeautyFaceInfo> getFaceList();

        /**
         * 改变   现在显示区域、旋转角度、
         */
        boolean onRectChange(RectF rectF);

        /**
         * down
         */
        void onMove();

        /**
         * 点击
         */
        void onFace(BeautyFaceInfo faceInfo, RectF RectF);

    }

    /**
     * 手势
     */
    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "MyGestureDetector";

        //单击确认，即很快的按下并抬起，但并不连续点击第二下
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
//            Log.e(TAG, "onSingleTapConfirmed: " + event);
            if (mDrawFace && mListener != null && mShowRect != null) {
                List<BeautyFaceInfo> faceList = mListener.getFaceList();
                if (faceList != null) {
                    float downX = (event.getX() - mShowRect.left) / mShowRect.width();
                    float downY = (event.getY() - mShowRect.top) / mShowRect.height();
                    for (BeautyFaceInfo faceInfo : faceList) {
                        RectF faceRectF = faceInfo.getFaceRectF();
                        if (faceRectF != null && faceRectF.contains(downX, downY)) {


                            //移动到人脸区域
                            //人脸区域
                            mFaceRectF.set(faceRectF.left * mShowRect.width() + mShowRect.left,
                                    faceRectF.top * mShowRect.height() + mShowRect.top,
                                    faceRectF.right * mShowRect.width() + mShowRect.left,
                                    faceRectF.bottom * mShowRect.height() + mShowRect.top);
                            //计算中心点移动距离
                            float offsetX = getWidth() / 2.0f - mFaceRectF.centerX();
                            float offsetY = getHeight() / 2.0f - mFaceRectF.centerY();
                            //计算缩放
                            float scale;
                            float asp = getWidth() * 1.0f / getHeight();
                            float faceAsp = mFaceRectF.width() / mFaceRectF.height();
                            if (asp > faceAsp) {
                                scale = getHeight() * (1 - PADDING) / mFaceRectF.height();
                            } else {
                                scale = getWidth() * (1 - PADDING) / mFaceRectF.width();
                            }
                            if (scale > MAX_SCALE) {
                                scale = MAX_SCALE;
                            }

                            //计算现在的显示区域
                            mMatrix.reset();
                            mMatrix.postTranslate(offsetX, offsetY);
                            mMatrix.postScale(scale, scale, getWidth() / 2.0f, getHeight() / 2.0f);
                            mMatrix.mapRect(mShowRect, mShowRect);
                            mListener.onFace(faceInfo, mShowRect);
                            break;
                        }
                    }
                }
            }
            return true;
        }

    }

}
