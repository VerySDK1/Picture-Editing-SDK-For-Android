package com.pesdk.uisdk.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.vecore.models.PEImageObject;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 比例裁剪
 */
public class ProportionalCropView extends View {

    private static final int SHRINK = 40;//收缩
    private static final int ARC = 4;//圆弧
    private static final int SCALE_MAX = 4;//最大
    private static final float SCALE_MIN = 0.1f;//最小

    //笔
    private Paint mShadowPaint;//阴影
    private Paint mSelectedPaint;//选中区域

    //阴影区域
    private final RectF mShadowRectF = new RectF();
    private final RectF mSelectedRectF = new RectF();

    private int mShadowColor;
    private Bitmap mBitmap;
    private Bitmap mBitmapMask;

    //混合模式
    private PorterDuffXfermode mXcode;

    //手势控制
    private boolean mIsControl = true;
    //回调
    private OnProportionalListener mListener;
    //手势
    private GestureDetector mGestureDetector;

    //框显示区域
    private final RectF mFrameRectF = new RectF();
    //媒体显示区域
    private final RectF mShowRectF = new RectF();


    public ProportionalCropView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProportionalCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //阴影
        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setColor(ContextCompat.getColor(context, R.color.transparent_black20));

        mSelectedPaint = new Paint();
        mSelectedPaint.setColor(ContextCompat.getColor(context, R.color.white));
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setStrokeWidth(3);
        mSelectedPaint.setStyle(Paint.Style.STROKE);

        mXcode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        mGestureDetector = new GestureDetector(context, new MyGestureDetector());

        mShadowColor = ContextCompat.getColor(context, R.color.white);
        mMatrix = new Matrix();
    }

    /**
     * 设置比例
     */
    public RectF init(float asp, RectF oldCrop, RectF oldShow, PEImageObject mediaObject) {
        //比例
        float cropAsp = asp;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float viewAsp = viewWidth * 1.0f / viewHeight;
        if (cropAsp == 0) {
            cropAsp = viewAsp;
        }

        //框显示区域
        float tmp = (viewWidth - SHRINK) * 1.0f / (viewHeight - SHRINK);
        float w;
        float h;
        if (tmp > cropAsp) {
            h = viewHeight - SHRINK;
            w = h * cropAsp;
        } else {
            w = viewWidth - SHRINK;
            h = w / cropAsp;
        }
        w /= 2.0f;
        h /= 2.0f;
        float centerX = viewWidth / 2.0f;
        float centerY = viewHeight / 2.0f;
        mSelectedRectF.set(centerX - w, centerY - h, centerX + w, centerY + h);

        mFrameRectF.set(mSelectedRectF.left / viewWidth, mSelectedRectF.top / viewHeight,
                mSelectedRectF.right / viewWidth, mSelectedRectF.bottom / viewHeight);

        //媒体显示区域
        int angle = mListener.getAngle();
        float mediaAsp = mediaObject.getWidth() * 1.0f / mediaObject.getHeight();
        if ((oldCrop == null || oldCrop.isEmpty()) && (oldShow == null || oldShow.isEmpty())) {
            if (mediaAsp > cropAsp) {
                float showH = mSelectedRectF.height() / viewHeight / 2;
                float showW = mSelectedRectF.height() * mediaAsp / viewWidth / 2;
                mShowRectF.set(0.5f - showW, 0.5f - showH, 0.5f + showW, 0.5f + showH);
            } else {
                float showW = mSelectedRectF.width() / viewWidth / 2;
                float showH = mSelectedRectF.width() / mediaAsp / viewHeight / 2;
                mShowRectF.set(0.5f - showW, 0.5f - showH, 0.5f + showW, 0.5f + showH);
            }
        } else {
            if (oldCrop == null || oldCrop.isEmpty()) {
                float frameW = mFrameRectF.width() * getWidth();
                float frameH = mFrameRectF.height() * getHeight();
                if (angle % 180 != 0) {
                    oldShow.set(oldShow.left * frameH, oldShow.top * frameW,
                            oldShow.right * frameH, oldShow.bottom * frameW);
                    oldShow.offset(frameW / 2.0f - frameH / 2.0f, frameH / 2.0f - frameW / 2.0f);
                } else {
                    oldShow.set(oldShow.left * frameW, oldShow.top * frameH,
                            oldShow.right * frameW, oldShow.bottom * frameH);
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(-angle, frameW / 2.0f, frameH / 2.0f);
                matrix.mapRect(oldShow, oldShow);
                oldShow.set(oldShow.left / frameW, oldShow.top / frameH,
                        oldShow.right / frameW, oldShow.bottom / frameH);

                //裁剪区域为null 根据显示区域计算
                mShowRectF.set(oldShow.left * mFrameRectF.width() + mFrameRectF.left,
                        oldShow.top * mFrameRectF.height() + mFrameRectF.top,
                        oldShow.right * mFrameRectF.width() + mFrameRectF.left,
                        oldShow.bottom * mFrameRectF.height() + mFrameRectF.top);
            } else if (oldShow == null || oldShow.isEmpty()) {
                int[] mediaSize = mListener.getMediaSize();
                oldCrop.set(oldCrop.left * mediaSize[0], oldCrop.top * mediaSize[1],
                        oldCrop.right * mediaSize[0], oldCrop.bottom * mediaSize[1]);
                if (angle % 180 == 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(-angle, mediaSize[0] / 2.0f, mediaSize[1] / 2.0f);
                    matrix.mapRect(oldCrop, oldCrop);
                    oldCrop.set(oldCrop.left / mediaSize[0], oldCrop.top / mediaSize[1],
                            oldCrop.right / mediaSize[0], oldCrop.bottom / mediaSize[1]);
                } else {
                    oldCrop.offset(mediaSize[1] / 2.0f - mediaSize[0] / 2.0f,
                            mediaSize[0] / 2.0f - mediaSize[1] / 2.0f);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(-angle, mediaSize[1] / 2.0f, mediaSize[0] / 2.0f);
                    matrix.mapRect(oldCrop, oldCrop);
                    oldCrop.set(oldCrop.left / mediaSize[1], oldCrop.top / mediaSize[0],
                            oldCrop.right / mediaSize[1], oldCrop.bottom / mediaSize[0]);
                }
                float l, r, t, b;
                if (oldCrop.left == 0 || oldCrop.right == 0) {
                    if (oldCrop.left == 0 && oldCrop.right == 0) {
                        l = mFrameRectF.left;
                        r = mFrameRectF.right;
                    } else if (oldCrop.left == 0) {
                        l = mFrameRectF.left;
                        r = (mFrameRectF.right - l) / oldCrop.right + l;
                    } else {
                        r = mFrameRectF.right;
                        l = (oldCrop.left * r - mFrameRectF.left) / (oldCrop.left - 1);
                    }
                } else {
                    l = (oldCrop.left * mFrameRectF.right - oldCrop.right * mFrameRectF.left)
                            / (oldCrop.left - oldCrop.right);
                    r = (mFrameRectF.left - l) / oldCrop.left + l;
                }
                if (oldCrop.top == 0 || oldCrop.bottom == 0) {
                    if (oldCrop.top == 0 && oldCrop.bottom == 0) {
                        t = mFrameRectF.top;
                        b = mFrameRectF.bottom;
                    } else if (oldCrop.top == 0) {
                        t = mFrameRectF.top;
                        b = (mFrameRectF.bottom - t) / oldCrop.bottom + t;
                    } else {
                        b = mFrameRectF.bottom;
                        t = (oldCrop.top * b - mFrameRectF.top) / (oldCrop.top - 1);
                    }
                } else {
                    t = (oldCrop.top * mFrameRectF.bottom - oldCrop.bottom * mFrameRectF.top)
                            / (oldCrop.top - oldCrop.bottom);
                    b = (mFrameRectF.top - t) / oldCrop.top + t;
                }
                mShowRectF.set(l, t, r, b);
            } else {
                float frameW = mFrameRectF.width() * getWidth();
                float frameH = mFrameRectF.height() * getHeight();
                if (angle % 180 != 0) {
                    oldShow.set(oldShow.left * frameH, oldShow.top * frameW,
                            oldShow.right * frameH, oldShow.bottom * frameW);
                    oldShow.offset(frameW / 2.0f - frameH / 2.0f, frameH / 2.0f - frameW / 2.0f);
                } else {
                    oldShow.set(oldShow.left * frameW, oldShow.top * frameH,
                            oldShow.right * frameW, oldShow.bottom * frameH);
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(-angle, frameW / 2.0f, frameH / 2.0f);
                matrix.mapRect(oldShow, oldShow);
                oldShow.set(oldShow.left / frameW, oldShow.top / frameH,
                        oldShow.right / frameW, oldShow.bottom / frameH);

                int[] mediaSize = mListener.getMediaSize();
                oldCrop.set(oldCrop.left * mediaSize[0], oldCrop.top * mediaSize[1],
                        oldCrop.right * mediaSize[0], oldCrop.bottom * mediaSize[1]);
                if (angle % 180 == 0) {
                    matrix.reset();
                    matrix.postRotate(-angle, mediaSize[0] / 2.0f, mediaSize[1] / 2.0f);
                    matrix.mapRect(oldCrop, oldCrop);
                    oldCrop.set(oldCrop.left / mediaSize[0], oldCrop.top / mediaSize[1],
                            oldCrop.right / mediaSize[0], oldCrop.bottom / mediaSize[1]);
                } else {
                    oldCrop.offset(mediaSize[1] / 2.0f - mediaSize[0] / 2.0f,
                            mediaSize[0] / 2.0f - mediaSize[1] / 2.0f);
                    matrix.reset();
                    matrix.postRotate(-angle, mediaSize[1] / 2.0f, mediaSize[0] / 2.0f);
                    matrix.mapRect(oldCrop, oldCrop);
                    oldCrop.set(oldCrop.left / mediaSize[1], oldCrop.top / mediaSize[0],
                            oldCrop.right / mediaSize[1], oldCrop.bottom / mediaSize[0]);
                }

                //都不为null 根据裁剪区域计算出完整的显示区域
                RectF crop = new RectF(
                        oldCrop.left * mediaObject.getWidth(),
                        oldCrop.top * mediaObject.getHeight(),
                        oldCrop.right * mediaObject.getWidth(),
                        oldCrop.bottom * mediaObject.getHeight());
                float l = oldShow.left - crop.left / crop.width() * oldShow.width();
                float r = (mediaObject.getWidth() - crop.right) / crop.width() * oldShow.width() + oldShow.right;
                float t = oldShow.top - crop.top / crop.height() * oldShow.height();
                float b = (mediaObject.getHeight() - crop.bottom) / crop.height() * oldShow.height() + oldShow.bottom;

                mShowRectF.set(l * mFrameRectF.width() + mFrameRectF.left,
                        t * mFrameRectF.height() + mFrameRectF.top,
                        r * mFrameRectF.width() + mFrameRectF.left,
                        b * mFrameRectF.height() + mFrameRectF.top);
            }
        }
        if (angle != 0) {
            //旋转
            mShowRectF.set(mShowRectF.left * getWidth(), mShowRectF.top * getHeight(),
                    mShowRectF.right * getWidth(), mShowRectF.bottom * getHeight());
            Matrix matrix = new Matrix();
            matrix.postRotate(angle, mShowRectF.centerX(), mShowRectF.centerY());
            matrix.mapRect(mShowRectF, mShowRectF);
            mShowRectF.set(mShowRectF.left / getWidth(), mShowRectF.top / getHeight(),
                    mShowRectF.right / getWidth(), mShowRectF.bottom / getHeight());
        }
        //矫正变形 根据媒体比例计算
        if (Math.abs(mShowRectF.width() * getWidth() / mShowRectF.height() / getHeight() - mediaAsp) > 0.0001f) {
            float showH = mShowRectF.width() * getWidth() / mediaAsp / getHeight();
            mShowRectF.bottom = mShowRectF.top + showH;
        }
        restrictedArea();
        invalidate();
        return new RectF(mShowRectF);
    }

    /**
     * 设置颜色
     */
    public void setShadowColor(int color) {
        mShadowColor = color;
        mBitmap = null;
        invalidate();
    }

    /**
     * 手势控制
     */
    public void setControl(boolean control) {
        mIsControl = control;
    }

    /**
     * 计算裁剪区域
     * {显示区域, 裁剪区域}
     */
    public RectF[] getCropRectF() {
        //旋转
        int angle = mListener.getAngle();

        //媒体显示区域
        RectF mediaShowRectF;
        if (angle != 0) {
            //旋转
            mediaShowRectF = new RectF(mShowRectF.left * getWidth(), mShowRectF.top * getHeight(),
                    mShowRectF.right * getWidth(), mShowRectF.bottom * getHeight());
            Matrix matrix = new Matrix();
            matrix.postRotate(-angle, mediaShowRectF.centerX(), mediaShowRectF.centerY());
            matrix.mapRect(mediaShowRectF, mediaShowRectF);
            mediaShowRectF.set(mediaShowRectF.left / getWidth(), mediaShowRectF.top / getHeight(),
                    mediaShowRectF.right / getWidth(), mediaShowRectF.bottom / getHeight());
        } else {
            mediaShowRectF = new RectF(mShowRectF);
        }

        //裁剪和显示区域
        RectF cropRectF = null;
        RectF showRectF = null;

        if (mediaShowRectF.left < mFrameRectF.left && mediaShowRectF.right > mFrameRectF.right
                && mediaShowRectF.top < mFrameRectF.top && mediaShowRectF.bottom > mFrameRectF.bottom) {
            //显示大于框且完全显示 只计算裁剪区域
            cropRectF = new RectF((mFrameRectF.left - mediaShowRectF.left) / mediaShowRectF.width(),
                    (mFrameRectF.top - mediaShowRectF.top) / mediaShowRectF.height(),
                    (mFrameRectF.right - mediaShowRectF.left) / mediaShowRectF.width(),
                    (mFrameRectF.bottom - mediaShowRectF.top) / mediaShowRectF.height());
        } else if (mediaShowRectF.left > mFrameRectF.left && mediaShowRectF.right < mFrameRectF.right
                && mediaShowRectF.top > mFrameRectF.top && mediaShowRectF.bottom < mFrameRectF.bottom) {
            //视频完全显示在框内 只计算显示区域
            showRectF = new RectF((mediaShowRectF.left - mFrameRectF.left) / mFrameRectF.width(),
                    (mediaShowRectF.top - mFrameRectF.top) / mFrameRectF.height(),
                    (mediaShowRectF.right - mFrameRectF.left) / mFrameRectF.width(),
                    (mediaShowRectF.bottom - mFrameRectF.top) / mFrameRectF.height());
        } else {
            //既要计算裁剪区域 又要计算显示区域

            //裁剪区域
            cropRectF = new RectF((mFrameRectF.left - mediaShowRectF.left) / mediaShowRectF.width(),
                    (mFrameRectF.top - mediaShowRectF.top) / mediaShowRectF.height(),
                    (mFrameRectF.right - mediaShowRectF.left) / mediaShowRectF.width(),
                    (mFrameRectF.bottom - mediaShowRectF.top) / mediaShowRectF.height());
            if (cropRectF.left < 0) {
                cropRectF.left = 0;
            } else if (cropRectF.left > 1) {
                cropRectF.left = 1;
            }
            if (cropRectF.right < 0) {
                cropRectF.right = 0;
            } else if (cropRectF.right > 1) {
                cropRectF.right = 1;
            }
            if (cropRectF.top < 0) {
                cropRectF.top = 0;
            } else if (cropRectF.top > 1) {
                cropRectF.top = 1;
            }
            if (cropRectF.bottom < 0) {
                cropRectF.bottom = 0;
            } else if (cropRectF.bottom > 1) {
                cropRectF.bottom = 1;
            }

            //显示区域
            showRectF = new RectF((mediaShowRectF.left - mFrameRectF.left) / mFrameRectF.width(),
                    (mediaShowRectF.top - mFrameRectF.top) / mFrameRectF.height(),
                    (mediaShowRectF.right - mFrameRectF.left) / mFrameRectF.width(),
                    (mediaShowRectF.bottom - mFrameRectF.top) / mFrameRectF.height());
            if (showRectF.left < 0) {
                showRectF.left = 0;
            } else if (showRectF.left > 1) {
                showRectF.left = 1;
            }
            if (showRectF.right < 0) {
                showRectF.right = 0;
            } else if (showRectF.right > 1) {
                showRectF.right = 1;
            }
            if (showRectF.top < 0) {
                showRectF.top = 0;
            } else if (showRectF.top > 1) {
                showRectF.top = 1;
            }
            if (showRectF.bottom < 0) {
                showRectF.bottom = 0;
            } else if (showRectF.bottom > 1) {
                showRectF.bottom = 1;
            }
        }

        if (angle != 0) {

            if (showRectF != null) {
                float w = mFrameRectF.width() * getWidth();
                float h = mFrameRectF.height() * getHeight();
                showRectF.set(showRectF.left * w, showRectF.top * h,
                        showRectF.right * w, showRectF.bottom * h);
                Matrix matrix = new Matrix();
                matrix.postRotate(angle, w / 2.0f, h / 2.0f);
                matrix.mapRect(showRectF, showRectF);
                if (angle % 180 != 0) {
                    showRectF.offset(h / 2.0f - w / 2.0f, w / 2.0f - h / 2.0f);
                    showRectF.set(showRectF.left / h, showRectF.top / w,
                            showRectF.right / h, showRectF.bottom / w);
                } else {
                    showRectF.set(showRectF.left / w, showRectF.top / h,
                            showRectF.right / w, showRectF.bottom / h);
                }
            }

            if (cropRectF != null) {
                int[] mediaSize = mListener.getMediaSize();
                if (angle % 180 == 0) {
                    cropRectF.set(cropRectF.left * mediaSize[0], cropRectF.top * mediaSize[1],
                            cropRectF.right * mediaSize[0], cropRectF.bottom * mediaSize[1]);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(angle, mediaSize[0] / 2.0f, mediaSize[1] / 2.0f);
                    matrix.mapRect(cropRectF, cropRectF);
                } else {
                    cropRectF.set(cropRectF.left * mediaSize[1], cropRectF.top * mediaSize[0],
                            cropRectF.right * mediaSize[1], cropRectF.bottom * mediaSize[0]);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(angle, mediaSize[1] / 2.0f, mediaSize[0] / 2.0f);
                    matrix.mapRect(cropRectF, cropRectF);
                    cropRectF.offset(mediaSize[0] / 2.0f - mediaSize[1] / 2.0f,
                            mediaSize[1] / 2.0f - mediaSize[0] / 2.0f);
                }
                cropRectF.set(cropRectF.left / mediaSize[0], cropRectF.top / mediaSize[1],
                        cropRectF.right / mediaSize[0], cropRectF.bottom / mediaSize[1]);
            }
        }
        return new RectF[]{showRectF, cropRectF};
    }

    /**
     * 回调
     */
    public void setListener(OnProportionalListener listener) {
        mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas bitmapCanvas = new Canvas(mBitmap);
            bitmapCanvas.drawColor(mShadowColor);
        }
        if (mBitmapMask == null) {
            mBitmapMask = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas bitmapCanvas = new Canvas(mBitmapMask);
            bitmapCanvas.drawColor(Color.BLACK);
        }

        //绘制阴影
        mShadowRectF.set(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(mBitmap, null, mShadowRectF, mShadowPaint);
        mShadowPaint.setXfermode(mXcode);
        canvas.drawBitmap(mBitmapMask, null, mSelectedRectF, mShadowPaint);
        mShadowPaint.setXfermode(null);

        //选中区域
        canvas.drawRoundRect(mSelectedRectF, ARC, ARC, mSelectedPaint);
    }

    //矩阵
    private Matrix mMatrix;
    //是否点击、是否是两个手指
    private boolean mTwoPoint = false;
    //点击
    private float mDownX = 0;
    private float mDownY = 0;
    private final RectF mTempRectF = new RectF();
    //双指缩放开始之间的距离和与X轴角度
    private double mStartLen;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null || !mIsControl) {
            return false;
        }
        mGestureDetector.onTouchEvent(event);

        int action = event.getAction();

        int pointerNum = event.getPointerCount();
        if (pointerNum == 2) {
            //缩放 旋转
            if ((action & MotionEvent.ACTION_MASK)
                    == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                mTwoPoint = true;
                mTempRectF.set(mShowRectF);
                mStartLen = getDistance(event);
            } else if (action == MotionEvent.ACTION_MOVE) {
                //移动
                double endLen = getDistance(event);
                // 计算缩放系数
                float newDisf = (float) (endLen / mStartLen);
                mMatrix.reset();
                mMatrix.postScale(newDisf, newDisf, mTempRectF.centerX(), mTempRectF.centerY());
                mMatrix.mapRect(mShowRectF, mTempRectF);
                //限制大小
                if (mShowRectF.width() > SCALE_MAX) {
                    float scale = SCALE_MAX / mShowRectF.width();
                    mMatrix.reset();
                    mMatrix.postScale(scale, scale, mShowRectF.centerX(), mShowRectF.centerY());
                    mMatrix.mapRect(mShowRectF, mShowRectF);
                } else if (mShowRectF.height() > SCALE_MAX) {
                    float scale = SCALE_MAX / mShowRectF.height();
                    mMatrix.reset();
                    mMatrix.postScale(scale, scale, mShowRectF.centerX(), mShowRectF.centerY());
                    mMatrix.mapRect(mShowRectF, mShowRectF);
                } else if (mShowRectF.width() < SCALE_MIN) {
                    float scale = SCALE_MIN / mShowRectF.width();
                    mMatrix.reset();
                    mMatrix.postScale(scale, scale, mShowRectF.centerX(), mShowRectF.centerY());
                    mMatrix.mapRect(mShowRectF, mShowRectF);
                } else if (mShowRectF.height() < SCALE_MIN) {
                    float scale = SCALE_MIN / mShowRectF.height();
                    mMatrix.reset();
                    mMatrix.postScale(scale, scale, mShowRectF.centerX(), mShowRectF.centerY());
                    mMatrix.mapRect(mShowRectF, mShowRectF);
                }
                //限制位置
                restrictedArea();
                mListener.onChange(mShowRectF);
            }
            return true;
        } else if (pointerNum == 1) {
            //移动
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mTwoPoint = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTwoPoint) {
                        return false;
                    }
                    float x = (event.getX() - mDownX) / getWidth();
                    float y = (event.getY() - mDownY) / getHeight();
                    mShowRectF.set(mTempRectF);
                    mShowRectF.offset(x, y);
                    //限制位置
                    restrictedArea();
                    mListener.onChange(mShowRectF);
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
     * 矫正位置
     */
    private void restrictedArea() {
        if (mListener.getAngle() % 180 != 0) {
            //旋转后宽高需要变换
            RectF showRectF = new RectF(mShowRectF.left * getWidth(), mShowRectF.top * getHeight(),
                    mShowRectF.right * getWidth(), mShowRectF.bottom * getHeight());
            Matrix matrix = new Matrix();
            matrix.postRotate(90, showRectF.centerX(), showRectF.centerY());
            matrix.mapRect(showRectF, showRectF);
            showRectF.set(showRectF.left / getWidth(), showRectF.top / getHeight(),
                    showRectF.right / getWidth(), showRectF.bottom / getHeight());

            boolean b = false;
            if (showRectF.left > mFrameRectF.right) {
                showRectF.offset(mFrameRectF.right - showRectF.left, 0);
                b = true;
            } else if (showRectF.right < mFrameRectF.left) {
                showRectF.offset(mFrameRectF.left - showRectF.right, 0);
                b = true;
            }
            if (showRectF.top > mFrameRectF.bottom) {
                showRectF.offset(0, mFrameRectF.bottom - showRectF.top);
                b = true;
            } else if (showRectF.bottom < mFrameRectF.top) {
                showRectF.offset(0, mFrameRectF.top - showRectF.bottom);
                b = true;
            }

            if (b) {
                showRectF.set(showRectF.left * getWidth(), showRectF.top * getHeight(),
                        showRectF.right * getWidth(), showRectF.bottom * getHeight());
                matrix.reset();
                matrix.postRotate(-90, showRectF.centerX(), showRectF.centerY());
                matrix.mapRect(showRectF, showRectF);
                mShowRectF.set(showRectF.left / getWidth(), showRectF.top / getHeight(),
                        showRectF.right / getWidth(), showRectF.bottom / getHeight());
            }
        } else {
            if (mShowRectF.left > mFrameRectF.right) {
                mShowRectF.offset(mFrameRectF.right - mShowRectF.left, 0);
            } else if (mShowRectF.right < mFrameRectF.left) {
                mShowRectF.offset(mFrameRectF.left - mShowRectF.right, 0);
            }
            if (mShowRectF.top > mFrameRectF.bottom) {
                mShowRectF.offset(0, mFrameRectF.bottom - mShowRectF.top);
            } else if (mShowRectF.bottom < mFrameRectF.top) {
                mShowRectF.offset(0, mFrameRectF.top - mShowRectF.bottom);
            }
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
     * 手势
     */
    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        //按下
        @Override
        public boolean onDown(MotionEvent e) {
            mDownX = e.getX();
            mDownY = e.getY();
            mTempRectF.set(mShowRectF);
            mListener.onDown();
            return true;
        }

        //单击确认，即很快的按下并抬起，但并不连续点击第二下
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            mListener.onClick();
            return true;
        }

    }

    public interface OnProportionalListener {

        /**
         * down
         */
        void onDown();

        /**
         * 点击
         */
        void onClick();

        /**
         * 改变
         */
        void onChange(RectF rectF);

        /**
         * 是否旋转了90 180 ...
         */
        int getAngle();

        /**
         * 媒体宽高
         */
        int[] getMediaSize();


    }

}
