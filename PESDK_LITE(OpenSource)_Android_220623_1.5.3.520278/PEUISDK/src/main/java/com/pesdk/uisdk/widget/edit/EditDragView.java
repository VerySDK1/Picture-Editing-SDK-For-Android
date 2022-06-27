package com.pesdk.uisdk.widget.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 字幕、 贴纸 拖拽
 */
public class EditDragView extends View {
    private static final String TAG = "EditDragView";
    /**
     * 缩放比例区间
     */
    private final float MAX_SCALE = 30000.0f;//不设置最大
    private final float MIN_SCALE = 0.001f;
    /**
     * 左右上下限制宽高
     */
    private final float LIMIT_LR = 20;
    private final float LIMIT_TB = 20;
    /**
     * 限制最大边和最小边
     */
    private final float LIMIT_MAX_SIDE = 2560;
    private final float LIMIT_MIN_SIDE = 30;

    /**
     * 角度启用
     */
    private final int ANGLE_ENABLED = 30;

    /**
     * 显示的区域
     */
    private RectF mShowRect;
    private RectF mFrameRect;
    /**
     * 旋转角度
     */
    private float mAngle = 0;
    /**
     * 笔
     */
    private Paint mPaint;
    private Matrix mMatrix;
    private Paint mTextPain;
    /**
     * 显示旋转角度
     */
    private boolean mShowAngle = false;
    /**
     * 0 居左 、1 居中 、 2居右
     */
    private int mTextAlign = 1;

    /**
     * 不能手势控制
     */
    private boolean mIsControl = true;

    /**
     * 控制器
     */
    private boolean mIsRotation = false;
    private boolean mIsDelete = false;
    private boolean mIsCopy = false;
    private boolean mIsEdit = false;
    /**
     * 按钮
     */
    private Bitmap mRotationImgBtn = null;
    private Bitmap mDeleteBitmap = null;
    private Bitmap mCopyBitmap = null;
    private Bitmap mEditBitmap = null;
    /**
     * 按钮位置
     */
    private final RectF mRotationRectF = new RectF();
    private final RectF mDelRectF = new RectF();
    private final RectF mCopyRectF = new RectF();
    private final RectF mEditRectF = new RectF();

    /**
     * 限制显示区域
     */
    private final RectF mLimitRectF = new RectF();

    /**
     * 旋转角度
     */
    private boolean mEnabledAngle = true;

    /**
     * 改变比例
     */
    private boolean mEnabledProportion = false;

    /**
     * 其他显示区域 虚线
     */
    private boolean mIsOtherShow = false;
    private final RectF mOtherAreaRectF = new RectF();
    private final ArrayList<AreaInfo> mAreaList = new ArrayList<>();
    private Paint mDottedLinePaint;
    private DashPathEffect mEffect;
    private int mOtherIndex = -1;

    /**
     * 外框时虚线
     */
    private boolean mDashed = false;


    public EditDragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mShowRect = new RectF();
        mFrameRect = new RectF();
        mMatrix = new Matrix();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.WHITE);
        mPaint.setShadowLayer(3, 2, 2, ContextCompat.getColor(context, R.color.pesdk_drag_shadow_color));


        mTextPain = new Paint();
        mTextPain.setAntiAlias(true);
        mTextPain.setColor(Color.WHITE);
        mTextPain.setTextAlign(Paint.Align.CENTER);
        mTextPain.setTextSize(CoreUtils.dip2px(getContext(), 16));

        //设置虚线效果
        mDottedLinePaint = new Paint();
        mDottedLinePaint.setAntiAlias(true);
        mDottedLinePaint.setStyle(Paint.Style.STROKE);
        mDottedLinePaint.setColor(Color.WHITE);
        mDottedLinePaint.setStrokeWidth(2);
        mDottedLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        mDottedLinePaint.setShadowLayer(1, 1, 1, Color.GRAY);


        mRotationImgBtn = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_ic_drag_controller);
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_ic_drag_delete);
        mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_layer_copy);
//        mAlignBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.vepub_subtitle_effect_mid_new);
        mEditBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_mirror_hor);
//        mEditBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_ic_drag_edit);

        mEffect = new DashPathEffect(new float[]{13, 6}, 0);
    }


    /**
     * 设置显示第二区域
     */
    public void setOtherShow(boolean otherShow) {
        mIsOtherShow = otherShow;
    }

    /**
     * 设置其他区域信息
     */
    public void setOtherAreaList(ArrayList<AreaInfo> areaList) {
        mAreaList.clear();
        if (areaList != null) {
            mAreaList.addAll(areaList);
        }
    }

    /**
     * 选中的下标
     */
    public void setOtherIndex(int otherIndex) {
        mOtherIndex = otherIndex;
        invalidate();
    }


    /**
     * 开启角度
     */
    public void setEnabledAngle(boolean enabledAngle) {
        mEnabledAngle = enabledAngle;
    }

    /**
     * 设置显示区域和角度    真实大小 不是0~1
     */
    public void setData(RectF showRect, float angle) {
        boolean invalidate = false;
        if (showRect != null) {
            if (mShowRect.centerX() != showRect.centerX()
                    || mShowRect.centerY() != showRect.centerY()
                    || mShowRect.width() != showRect.width()
                    || mShowRect.height() != showRect.height()) {
                invalidate = true;
                if (showRect.width() != 0 && showRect.height() != 0) {
                    mShowRect.set(showRect);
                }
            }
            limitSize(false);
        }
        angle = (angle + 360) % 360;
        mAngle = angle;
        if (invalidate) {
            invalidate();
        }
    }

    /**
     * 获取显示区域
     *
     * @return RectF
     */
    public RectF getShowRect() {
        return mShowRect;
    }

    /**
     * 设置显示区域
     *
     * @param showRect 显示区域
     */
    public void setShowRect(RectF showRect) {
        if (showRect != null) {
            mShowRect.set(showRect);
        }
        invalidate();
    }

    /**
     * 获取旋转角度
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * 设置角度
     */
    public void setAngle(float angle) {
        mAngle = angle;
        invalidate();
    }

    /**
     * 设置对齐按钮
     */
    public void setCopy(int copy) {
        mTextAlign = copy % 3;
//        if (mTextAlign == 0) {
//            mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_left_new);
//        } else if (mTextAlign == 1) {
//            mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_mid_new);
//        } else {
//            mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_right_new);
//        }
        invalidate();
    }

    /**
     * 虚线
     */
    public void setDashed(boolean dashed) {
        this.mDashed = dashed;
        invalidate();
    }


    /**
     * 设置显示角度 页面显示
     */
    public void setShowAngle(boolean showAngle) {
        mShowAngle = showAngle;
    }

    /**
     * 中心点坐标
     */
    public PointF getCenter() {
        return new PointF(mShowRect.centerX(), mShowRect.centerY());
    }

    /**
     * 移动中强制设置位置
     */
    public void setMoveShowRect(float left, float top, float right, float bottom) {
        mShowRect.set(left, top, right, bottom);
        invalidate();
    }

    /**
     * 重置
     */
    public void reset() {
        mShowRect.set(0, 0, 0, 0);
        invalidate();
    }


    /**
     * 设置控制
     */
    public void setCtrRotation(boolean rotation) {
        mIsRotation = rotation;
    }

    public void setCtrDelete(boolean delete) {
        mIsDelete = delete;
    }

    public void setCtrCopy(boolean enable) {
        mIsCopy = enable;
    }

    /**
     * 编辑（字幕）|镜像(贴纸)
     *
     * @param enable
     */
    public void setCtrEdit(boolean enable) {
        mIsEdit = enable;
    }

    /**
     * 不能手势操作
     */
    public void setControl(boolean control) {
        mIsControl = control;
    }


    /**
     * 贴纸UI
     */
    public void onSticker() {
        Bitmap tmp = mEditBitmap;
        mEditBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_mirror_hor);
        invalidate();
        postDelayed(() -> {
            if (null != tmp && !tmp.isRecycled()) {
                tmp.recycle();
            }
        }, 200);
    }

    /**
     * 字幕UI
     */
    public void onCaption() {
        Bitmap tmp = mEditBitmap;
        mEditBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_ic_drag_edit);
        invalidate();
        postDelayed(() -> {
            if (null != tmp && !tmp.isRecycled()) {
                tmp.recycle();
            }
        }, 200);


    }

    /**
     * 改变比例
     */
    public void setEnabledProportion(boolean enabledProportion) {
        mEnabledProportion = enabledProportion;
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            super.setVisibility(visibility);
        } else {
            super.setVisibility(GONE);
        }
    }

    public void setHideRect(boolean hideRect) {
        this.hideRect = hideRect;
        invalidate();
    }

    private boolean hideRect = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        if (mShowRect.width() <= 0) {
            return;
        }

        //限制宽高
        float scale = 1;
        if (mShowRect.width() > mShowRect.height()) {
            if (mShowRect.height() < LIMIT_MIN_SIDE) {
                scale = LIMIT_MIN_SIDE / mShowRect.height();
            } else if (mShowRect.width() > LIMIT_MAX_SIDE) {
                scale = LIMIT_MAX_SIDE / mShowRect.width();
            }
        } else {
            if (mShowRect.width() < LIMIT_MIN_SIDE) {
                scale = LIMIT_MIN_SIDE / mShowRect.width();
            } else if (mShowRect.height() > LIMIT_MAX_SIDE) {
                scale = LIMIT_MAX_SIDE / mShowRect.height();
            }
        }
        mMatrix.reset();
        mMatrix.postScale(scale, scale, mShowRect.centerX(), mShowRect.centerY());
        mMatrix.mapRect(mFrameRect, mShowRect);
        //中心点
        float centerX = mFrameRect.centerX();
        float centerY = mFrameRect.centerY();
        //计算四个点的位置
        mMatrix.reset();
        mMatrix.postRotate(mAngle, centerX, centerY);

        canvas.save();
        canvas.concat(mMatrix);

        if (mDashed) {
            mPaint.setPathEffect(mEffect);
        }
        if (!hideRect) { //隐藏区域画框
            canvas.drawRect(mFrameRect, mPaint);
        }

        //虚线
        if (mIsOtherShow && mAreaList.size() > 0) {
            for (int i = 0; i < mAreaList.size(); i++) {

                AreaInfo area = mAreaList.get(i);
                int save = canvas.save();
                //绘制选中的颜色
                if (i == mOtherIndex) {
                    mDottedLinePaint.setColor(Color.YELLOW);
                } else {
                    mDottedLinePaint.setColor(Color.WHITE);
                }
                mOtherAreaRectF.set(mFrameRect.left + area.getRectF().left * mFrameRect.width(),
                        mFrameRect.top + area.getRectF().top * mFrameRect.height(),
                        mFrameRect.left + area.getRectF().right * mFrameRect.width(),
                        mFrameRect.top + area.getRectF().bottom * mFrameRect.height());
                mMatrix.reset();
                mMatrix.postRotate(area.getAngle(), mFrameRect.centerX(), mFrameRect.centerY());
                canvas.concat(mMatrix);
                mMatrix.reset();
                mMatrix.postScale(scale, scale, mShowRect.centerX(), mShowRect.centerY());
                mMatrix.mapRect(mOtherAreaRectF, mOtherAreaRectF);
                canvas.drawRect(mOtherAreaRectF, mDottedLinePaint);

                canvas.restoreToCount(save);
            }
        }

        //绘制控制图标
        if (mIsRotation) {
            mRotationRectF.set(
                    mFrameRect.right - mRotationImgBtn.getWidth() / 2.0f,
                    mFrameRect.bottom - mRotationImgBtn.getHeight() / 2.0f,
                    mFrameRect.right + mRotationImgBtn.getWidth() / 2.0f,
                    mFrameRect.bottom + mRotationImgBtn.getWidth() / 2.0f);
            canvas.drawBitmap(mRotationImgBtn, null, mRotationRectF, null);
        } else {
            mRotationRectF.set(0, 0, 0, 0);
        }


        if (mIsDelete) {
            mDelRectF.set(
                    mFrameRect.left - mDeleteBitmap.getWidth() / 2.0f,
                    mFrameRect.top - mDeleteBitmap.getHeight() / 2.0f,
                    mFrameRect.left + mDeleteBitmap.getWidth() / 2.0f,
                    mFrameRect.top + mDeleteBitmap.getWidth() / 2.0f);
            canvas.drawBitmap(mDeleteBitmap, null, mDelRectF, null);
        } else {
            mDelRectF.set(0, 0, 0, 0);
        }
        if (mIsCopy) {
            mCopyRectF.set(
                    mFrameRect.left - mCopyBitmap.getWidth() / 2.0f,
                    mFrameRect.bottom - mCopyBitmap.getHeight() / 2.0f,
                    mFrameRect.left + mCopyBitmap.getWidth() / 2.0f,
                    mFrameRect.bottom + mCopyBitmap.getWidth() / 2.0f);
            canvas.drawBitmap(mCopyBitmap, null, mCopyRectF, null);
        } else {
            mCopyRectF.set(0, 0, 0, 0);
        }

        if (mIsEdit) {
            mEditRectF.set(
                    mFrameRect.right - mEditBitmap.getWidth() / 2.0f,
                    mFrameRect.top - mEditBitmap.getHeight() / 2.0f,
                    mFrameRect.right + mEditBitmap.getWidth() / 2.0f,
                    mFrameRect.top + mEditBitmap.getWidth() / 2.0f);
            canvas.drawBitmap(mEditBitmap, null, mEditRectF, null);
        } else {
            mEditRectF.set(0, 0, 0, 0);
        }

        canvas.restore();


        //角度
        if (mShowAngle) {
            canvas.drawText(String.format(Locale.CHINA, "%.0f°", mAngle),
                    getWidth() / 2.0f, 130, mTextPain);
        }
    }

    /**
     * 点击时手指坐标
     */
    private final PointF mDownPoint = new PointF();
    private final RectF mTemp = new RectF();
    /**
     * 是否修正角度
     */
    private boolean mCorrectAngle = false;
    /**
     * 双指缩放开始之间的距离和与X轴角度
     */
    private double mStartLen;
    private int mStartAngle = 0;
    /**
     * 是否点击、是否是两个手指
     */
    private boolean mTwoPoint = false;
    /**
     * 临时记录点下时的角度
     */
    private float mTempAngle = 0;
    /**
     * 缩放
     */
    private float mScale = 1;
    /**
     * 角度启动
     */
    private boolean mAngleThreshold = false;

    /**
     * 缩放宽高分开
     */
    private float mScaleWidth = 1;
    private float mScaleHeight = 1;


    private boolean isDeleteClick = false;//删除
    private boolean isCopyClick = false;//对齐/复制
    private boolean isEditClick = false;//编辑
    private boolean isRotationClick = false;//旋转 缩放
    private boolean isItemClick = false;//单击事件

    private boolean isMove = false;//移动
    private final PointF prePointF = new PointF(0, 0);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getVisibility() != VISIBLE) {
            return super.onTouchEvent(event);
        }
        if (mListener == null || mIsControl) {
            Log.e(TAG, "onTouchEvent: xxxxxxxxx" + mListener + " " + mIsControl);
            return false;
        }
        float targetX = event.getX();
        float targetY = event.getY();
        int pointerNum = event.getPointerCount();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mListener.onTouchDown();
        }

        float containerWidth = mListener.getWidth();
        float containerHeight = mListener.getHeight();

        if (pointerNum == 2) {
            //缩放 旋转
            if ((action & MotionEvent.ACTION_MASK)
                    == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                mCorrectAngle = false;
                mTwoPoint = true;
                mTempAngle = mAngle;
                mTemp.set(mShowRect);
                mStartLen = getDistance(event);
                mStartAngle = getDeg(event);

                mAngleThreshold = false;
            } else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                    || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                //拿起
                mCorrectAngle = false;
                mHandler.removeMessages(MSG_ANGLE);
                mListener.onTouchUp();
                return false;
            } else if (action == MotionEvent.ACTION_MOVE) {
                //移动
                double endLen = getDistance(event);
                //角度复原后重新设置其实数据
                if (mDownPoint.x == -1000 && mDownPoint.y == -1000) {
                    mStartLen = endLen;
                    mStartAngle = getDeg(event);
                    mTempAngle = mAngle;
                    mTemp.set(mShowRect);
                    //只是作为恢复的判断
                    mDownPoint.set(0, 0);
                }
                // 计算缩放系数
                float newDisf = (float) (endLen / mStartLen);
                if (mCorrectAngle) {
                    if (newDisf < 0.7 || newDisf > 1.3) {
                        mHandler.removeMessages(MSG_ANGLE);
                        mCorrectAngle = false;
                        mScale = newDisf;
                    } else {
                        mScale = 1;
                    }
                } else {
                    mScale = newDisf;
                }

                //计算旋转角度
                float angle = mTempAngle;
                if (mEnabledAngle) {//锁定角度
                    //如果角度 相差5  是90整数倍
                    int correctDistance = 15;
                    int correctAngle = 90;
                    int angleDown = 5;//矫正差值
                    int angleUp = 85;//范围

                    int diffAngle = getDeg(event) - mStartAngle;
                    if (!mAngleThreshold && Math.abs(diffAngle) > ANGLE_ENABLED) {
                        //启动角度
                        mAngleThreshold = true;
                        mStartAngle = getDeg(event);
                        diffAngle = 0;
                    }
                    if (!mAngleThreshold) {
                        diffAngle = 0;
                    }

                    angle = mTempAngle + diffAngle;
                    if (mCorrectAngle && Math.abs(getDeg(event) - mStartAngle) > correctDistance) {
                        mHandler.removeMessages(MSG_ANGLE);
                        mCorrectAngle = false;
                    }

                    if (!mCorrectAngle && Math.abs(mAngle % correctAngle)
                            >= angleDown && Math.abs(mAngle % correctAngle)
                            <= angleUp && (Math.abs(angle % correctAngle)
                            < angleDown || Math.abs(angle % correctAngle)
                            > angleUp)) {
                        int newAngle = (int) angle;
                        if (newAngle > 0) {
                            if (newAngle % correctAngle < angleDown) {
                                newAngle = newAngle / correctAngle * correctAngle;
                            } else if (newAngle % correctAngle > angleUp) {
                                newAngle = newAngle / correctAngle * correctAngle + correctAngle;
                            }
                        } else {
                            if (newAngle % correctAngle > -angleDown) {
                                newAngle = newAngle / correctAngle * correctAngle;
                            } else if (newAngle % correctAngle < -angleUp) {
                                newAngle = newAngle / correctAngle * correctAngle - correctAngle;
                            }
                        }
                        //触发震动
                        vibration();
                        //矫正角度
                        mCorrectAngle = true;
                        mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                        mDownPoint.set(-1000, -1000);
                        //最终角度和大小
                        mAngle = (newAngle + 360) % 360;
                        mMatrix.reset();
                        mMatrix.postScale(mScale, mScale, mTemp.centerX(), mTemp.centerY());
                        mMatrix.mapRect(mShowRect, mTemp);
                        mListener.onRectChange(mShowRect, mAngle);
                    }
                }

                //最终角度和大小
                if (!mCorrectAngle) {
                    mAngle = (angle + 360) % 360;
                    mMatrix.reset();
                    mMatrix.postScale(mScale, mScale, mTemp.centerX(), mTemp.centerY());
                    mMatrix.mapRect(mShowRect, mTemp);
                    //限制缩放范围
                    limitSize(true);
                    mListener.onRectChange(mShowRect, mAngle);
                }
            }
            invalidate();
            return true;
        } else if (pointerNum == 1) {
            //移动
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownPoint.set(event.getX(), event.getY());
                    mTemp.set(mShowRect);
                    mTwoPoint = false;
                    mTempAngle = mAngle;
                    prePointF.set(targetX, targetY);

                    //判断手指点击位置 是否是在控制按钮
                    isMove = false;
                    isItemClick = isContains(mAngle, mShowRect, mDownPoint.x, mDownPoint.y);
                    isDeleteClick = isContains(mAngle, mDelRectF, mDownPoint.x, mDownPoint.y);
                    isCopyClick = isContains(mAngle, mCopyRectF, mDownPoint.x, mDownPoint.y);
                    isEditClick = isContains(mAngle, mEditRectF, mDownPoint.x, mDownPoint.y);
                    isRotationClick = isContains(mAngle, mRotationRectF, mDownPoint.x, mDownPoint.y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTwoPoint) {
                        return false;
                    }
                    if (isDeleteClick || isCopyClick || isEditClick) {
                        return true;
                    } else if (isRotationClick) {

                        //单指旋转
                        if (prePointF.x == -1000 && prePointF.y == -1000) {
                            mDownPoint.set(targetX, targetY);
                            mTempAngle = mAngle;
                            mTemp.set(mShowRect);
                            //只是作为恢复的判断
                            prePointF.set(0, 0);
                        }

                        //缩放大小
                        if (mEnabledProportion) {
                            float preX = Math.abs(mDownPoint.x - mTemp.left);
                            float postX = Math.abs(targetX - mTemp.left);
                            float preY = Math.abs(mDownPoint.y - mTemp.top);
                            float postY = Math.abs(targetY - mTemp.top);
                            mScaleWidth = postX / preX;
                            mScaleHeight = postY / preY;
                        } else {
                            // 计算手指在屏幕上滑动的距离比例
                            double temp = Math.pow(mDownPoint.x - mShowRect.centerX(), 2) + Math.pow(mDownPoint.y - mShowRect.centerY(), 2);
                            //上一次的点到中心点的距离
                            double preLength = Math.sqrt(temp);

                            double temp2 = Math.pow(targetX - mShowRect.centerX(), 2) + Math.pow(targetY - mShowRect.centerY(), 2);
                            //新的点到中心点的距离
                            double length = Math.sqrt(temp2);

                            //每次缩放变化的比
                            float newDisf = (float) (length / preLength);

                            if (mCorrectAngle) {
                                if (newDisf < 0.7 || newDisf > 1.3) {
                                    mHandler.removeMessages(MSG_ANGLE);
                                    mCorrectAngle = false;
                                    mScale = newDisf;
                                } else {
                                    mScale = 1;
                                }
                            } else {
                                mScale = newDisf;
                            }
                        }

                        //转动的角度
                        float angleFinally = mTempAngle;
                        if (mEnabledAngle) {
                            // 计算手指滑动的角度
                            float radian = (float) (Math.atan2(targetY - mShowRect.centerY(), targetX - mShowRect.centerX())
                                    - Math.atan2(mDownPoint.y - mShowRect.centerY(), mDownPoint.x - mShowRect.centerX()));
                            // 弧度转换为角度
                            float angle = (float) (radian * 180 / Math.PI);

                            if (mCorrectAngle && Math.abs(angle) > 15) {
                                mHandler.removeMessages(MSG_ANGLE);
                                mCorrectAngle = false;
                            }

                            //角度恢复
                            int correctAngle = 90;
                            int angleDown = 5;//矫正差值
                            int angleUp = 85;//范围

                            angleFinally = mTempAngle + angle;//转动的角度
                            //如果角度 相差5  靠近90整数倍  恢复成90倍数
                            if (!mCorrectAngle && Math.abs(mAngle % correctAngle)
                                    >= angleDown && Math.abs(mAngle % correctAngle)
                                    <= angleUp && (Math.abs(angleFinally % correctAngle)
                                    < angleDown || Math.abs(angleFinally % correctAngle)
                                    > angleUp)) {

                                int newAngle = (int) angleFinally;
                                if (newAngle > 0) {
                                    if (newAngle % correctAngle < angleDown) {
                                        newAngle = newAngle / correctAngle * correctAngle;
                                    } else if (newAngle % correctAngle > angleUp) {
                                        newAngle = newAngle / correctAngle * correctAngle + correctAngle;
                                    }
                                } else {
                                    if (newAngle % correctAngle > -angleDown) {
                                        newAngle = newAngle / correctAngle * correctAngle;
                                    } else if (newAngle % correctAngle < -angleUp) {
                                        newAngle = newAngle / correctAngle * correctAngle - correctAngle;
                                    }
                                }
                                angleFinally = (newAngle + 360) % 360;
                                vibration();
                                mAngle = angleFinally;
                                mMatrix.reset();
                                mMatrix.postScale(mScale, mScale, mTemp.centerX(), mTemp.centerY());
                                mMatrix.mapRect(mShowRect, mTemp);
                                if (null != mListener) {
                                    isItemClick = false;
                                    mListener.onRectChange(mShowRect, mAngle);
                                }

                                mCorrectAngle = true;
                                prePointF.set(-1000, -1000);
                                mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                            }
                        }

                        //最后角度大小
                        if (!mCorrectAngle) {
                            mAngle = (angleFinally + 360) % 360;
                            mMatrix.reset();
                            if (mEnabledProportion) {
                                mMatrix.postScale(mScaleWidth, mScaleHeight, mTemp.left, mTemp.top);
                            } else {
                                mMatrix.postScale(mScale, mScale, mTemp.centerX(), mTemp.centerY());
                            }
                            mMatrix.mapRect(mShowRect, mTemp);

                            //限制缩放范围
                            limitSize(false);

                            if (null != mListener) {
                                isItemClick = false;
                                mListener.onRectChange(mShowRect, mAngle);
                            }
                        }
                    } else {
                        float moveX = event.getX() - mDownPoint.x;
                        float moveY = event.getY() - mDownPoint.y;
                        mShowRect.set(mTemp.left + moveX,
                                mTemp.top + moveY,
                                mTemp.right + moveX,
                                mTemp.bottom + moveY);
                        //限制区域
                        mLimitRectF.set(LIMIT_LR, LIMIT_TB, containerWidth - LIMIT_LR, containerHeight - LIMIT_TB);
                        if (mShowRect.centerX() < mLimitRectF.left) {
                            float dx = mLimitRectF.left - mShowRect.centerX();
                            mShowRect.offset(dx, 0);
                        } else if (mShowRect.centerX() > mLimitRectF.right) {
                            float dx = mLimitRectF.right - mShowRect.centerX();
                            mShowRect.offset(dx, 0);
                        }
                        if (mShowRect.centerY() < mLimitRectF.top) {
                            float dy = mLimitRectF.top - mShowRect.centerY();
                            mShowRect.offset(0, dy);
                        } else if (mShowRect.centerY() > mLimitRectF.bottom) {
                            float dy = mLimitRectF.bottom - mShowRect.centerY();
                            mShowRect.offset(0, dy);
                        }
                        if (mListener.onRectChange(mShowRect, mAngle)) {
                            invalidate();
                        }
                    }
                    isMove = true;
                    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    //抬起手指 整体移动
                    if (mTwoPoint) {
                        return false;
                    }
                    if (isDeleteClick) {
                        mListener.onDelete();
                        invalidate();
                        return false;
                    } else if (isCopyClick) {
//                        mTextAlign++;
//                        if (mTextAlign >= 3) {
//                            mTextAlign = 0;
//                        }
//                        if (mTextAlign == 0) {
//                            mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_left_new);
//                        } else if (mTextAlign == 1) {
//                            mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_mid_new);
//                        } else {
//                            mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_right_new);
//                        }
//                        mListener.onAlign(mTextAlign);
                        mListener.onCopy();
                        invalidate();
                        return false;
                    } else if (isEditClick) {
                        mListener.onEdit();
                        invalidate();
                        return false;
                    } else if (!isMove) {//点击其他区域
                        boolean other = false;
                        if (mOtherIndex >= 0 && mIsOtherShow && isItemClick) {
                            for (int i = 0; i < mAreaList.size(); i++) {
                                if (i == mOtherIndex) {
                                    continue;
                                }
                                AreaInfo area = mAreaList.get(i);
                                mOtherAreaRectF.set(mFrameRect.left + area.getRectF().left * mFrameRect.width(),
                                        mFrameRect.top + area.getRectF().top * mFrameRect.height(),
                                        mFrameRect.left + area.getRectF().right * mFrameRect.width(),
                                        mFrameRect.top + area.getRectF().bottom * mFrameRect.height());
                                Log.e(TAG, "onTouchEvent: " + i + "/" + mAreaList.size() + " " + area);
                                boolean contains = isContains(mAngle + area.getAngle(), mOtherAreaRectF, mDownPoint.x, mDownPoint.y);
                                if (contains) {
                                    mOtherIndex = i;
                                    other = true;
                                    break;
                                }
                            }
                        }
                        Log.e(TAG, "onTouchEvent: upxxxxxxxx: " + other + " " + mOtherIndex);
                        if (other) {
                            mListener.onClickOther(mOtherIndex);
                        } else {
                            mListener.onClick(isItemClick, event.getX(), event.getY());
                        }
                    } else {
                        boolean autoExit = null != mCallback && mCallback.enableAutoExit();
                        Log.e(TAG, "onTouchEvent: up: " + isRotationClick + " " + autoExit + " " + mShowRect);
                        if (!isRotationClick && autoExit) {
                            boolean result = isContains(mAngle, mShowRect, event.getX(), event.getY());
                            Log.e(TAG, "onTouchEvent: " + result + " " + mShowRect);
                            if (!result) {//点击了画框以外的区域
                                // 通知UI,验证，是否点击了rect 之外的其他空白区域，用以标记 退出编辑
                                mHandler.postDelayed(() -> {
                                    mListener.onExitEdit();
                                }, 30);
                            }
                        }
                    }
                    mListener.onTouchUp();
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
     * 限制大小
     */
    private void limitSize(boolean center) {
        float containerWidth = mListener.getWidth();
        float containerHeight = mListener.getHeight();

        int correctDistance = 15;
        boolean sizeWCorrect = Math.abs(mShowRect.width() - containerWidth) > correctDistance;
        boolean sizeHCorrect = Math.abs(mShowRect.height() - containerHeight) > correctDistance;
        boolean sizeWCorrect2 = Math.abs(mShowRect.width() - containerHeight) > correctDistance;
        boolean sizeHCorrect2 = Math.abs(mShowRect.height() - containerWidth) > correctDistance;

        float asp = mShowRect.width() / mShowRect.height();
        if (mShowRect.width() < MIN_SCALE * containerWidth) {
            //缩小到最小
            float width = MIN_SCALE * containerHeight;
            if (center) {
                mShowRect.set(mShowRect.centerX() - width / 2,
                        mShowRect.centerY() - width / asp / 2,
                        mShowRect.centerX() + width / 2,
                        mShowRect.centerY() + width / asp / 2);
            } else {
                mShowRect.set(mShowRect.left, mShowRect.top,
                        mShowRect.left + width,
                        mShowRect.top + width / asp);
            }
        } else if (mShowRect.height() < MIN_SCALE * containerHeight) {
            float height = MIN_SCALE * containerHeight;
            if (center) {
                mShowRect.set(mShowRect.centerX() - height * asp / 2,
                        mShowRect.centerY() - height / 2,
                        mShowRect.centerX() + height * asp / 2,
                        mShowRect.centerY() + height / 2);
            } else {
                mShowRect.set(mShowRect.left, mShowRect.top,
                        mShowRect.left + height * asp,
                        mShowRect.top + height);
            }
        } else if (mShowRect.width() > MAX_SCALE * containerWidth) {
            float width = MAX_SCALE * containerHeight;
            if (center) {
                mShowRect.set(mShowRect.centerX() - width / 2,
                        mShowRect.centerY() - width / asp / 2,
                        mShowRect.centerX() + width / 2,
                        mShowRect.centerY() + width / asp / 2);
            } else {
                mShowRect.set(mShowRect.left, mShowRect.top,
                        mShowRect.left + width,
                        mShowRect.top + width / asp);
            }
        } else if (mShowRect.height() > MAX_SCALE * containerHeight) {
            float height = MAX_SCALE * containerHeight;
            if (center) {
                mShowRect.set(mShowRect.centerX() - height * asp / 2,
                        mShowRect.centerY() - height / 2,
                        mShowRect.centerX() + height * asp / 2,
                        mShowRect.centerY() + height / 2);
            } else {
                mShowRect.set(mShowRect.left, mShowRect.top,
                        mShowRect.left + height * asp,
                        mShowRect.top + height);
            }
        }

        //矫正宽高
        if (mEnabledAngle) {
            if (Math.abs(mAngle) == 0 || Math.abs(mAngle) == 180) {
                if (sizeWCorrect && Math.abs(mShowRect.width() - containerWidth) < correctDistance) {
                    float differenceW = (mShowRect.width() - containerWidth) / 2;
                    float differenceH = (mShowRect.height() - containerWidth
                            / (mShowRect.width() / mShowRect.height())) / 2;
                    mShowRect.left = mShowRect.left + differenceW;
                    mShowRect.right = mShowRect.right - differenceW;
                    mShowRect.top = mShowRect.top + differenceH;
                    mShowRect.bottom = mShowRect.bottom - differenceH;
                    vibration();
                    //矫正角度
                    mCorrectAngle = true;
                    mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                    mDownPoint.set(-1000, -1000);
                } else if (sizeHCorrect && Math.abs(mShowRect.height() - containerHeight) < correctDistance) {
                    float differenceH = (mShowRect.height() - containerHeight) / 2;
                    float differenceW = (mShowRect.width() - containerHeight * (mShowRect.width() / mShowRect.height())) / 2;
                    mShowRect.left = mShowRect.left + differenceW;
                    mShowRect.right = mShowRect.right - differenceW;
                    mShowRect.top = mShowRect.top + differenceH;
                    mShowRect.bottom = mShowRect.bottom - differenceH;
                    vibration();
                    //矫正角度
                    mCorrectAngle = true;
                    mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                    mDownPoint.set(-1000, -1000);
                }
            } else if (Math.abs(mAngle) == 90 || Math.abs(mAngle) == 270) {
                if (sizeWCorrect2 && Math.abs(mShowRect.width() - containerHeight) < correctDistance) {
                    float differenceW = (mShowRect.width() - containerHeight) / 2;
                    float differenceH = (mShowRect.height() - containerHeight
                            / (mShowRect.width() / mShowRect.height())) / 2;
                    mShowRect.left = mShowRect.left + differenceW;
                    mShowRect.right = mShowRect.right - differenceW;
                    mShowRect.top = mShowRect.top + differenceH;
                    mShowRect.bottom = mShowRect.bottom - differenceH;
                    vibration();
                    //矫正角度
                    mCorrectAngle = true;
                    mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                    mDownPoint.set(-1000, -1000);
                } else if (sizeHCorrect2 && Math.abs(mShowRect.height() - containerWidth) < correctDistance) {
                    float differenceH = (mShowRect.height() - containerWidth) / 2;
                    float differenceW = (mShowRect.width() - containerWidth * (mShowRect.width() / mShowRect.height())) / 2;
                    mShowRect.left = mShowRect.left + differenceW;
                    mShowRect.right = mShowRect.right - differenceW;
                    mShowRect.top = mShowRect.top + differenceH;
                    mShowRect.bottom = mShowRect.bottom - differenceH;
                    vibration();
                    //矫正角度
                    mCorrectAngle = true;
                    mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                    mDownPoint.set(-1000, -1000);
                }
            }
        }
    }

    /**
     * 是否控制按钮
     */
    private boolean isContains(float angle, RectF rectF, float x, float y) {
        //x y 旋转角度
        float[] down = new float[]{x, y};
        mMatrix.reset();
        mMatrix.postRotate(-angle, mShowRect.centerX(), mShowRect.centerY());
        mMatrix.mapPoints(down, down);
        return rectF.contains(down[0], down[1]);
    }

    /**
     * 振动
     */
    private void vibration() {
        Utils.onVibrator(getContext());
    }

    /**
     * 矫正
     */
    private final int MSG_ANGLE = 22;

    private final Handler mHandler = new Handler(message -> {
        if (message.what == MSG_ANGLE) {
            mCorrectAngle = false;
        }
        return false;
    });

    /**
     * 计算两点之间的距离 缩放
     */
    private double getDistance(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int pId0 = event.getPointerId(0);
        int pId1 = event.getPointerId(1);
        if (pId0 > pointerCount || pId1 > pointerCount) {
            return mStartLen;
        }
        int xlen = Math.abs((int) event.getX(pId0) - (int) event.getX(pId1));
        int ylen = Math.abs((int) event.getY(pId0) - (int) event.getY(pId1));
        return Math.sqrt(xlen * xlen + ylen * ylen);
    }

    /**
     * 用于计算两个两个手指之间形成的直线与x轴的夹角
     *
     * @return 夹角
     */
    private int getDeg(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int pId0 = event.getPointerId(0);
        int pId1 = event.getPointerId(1);
        if (pId0 > pointerCount || pId1 > pointerCount) {
            return 0;
        }
        Point point1 = new Point((int) event.getX(pId0), (int) event.getY(pId0));
        Point point2 = new Point((int) event.getX(pId1), (int) event.getY(pId1));
        float x = point2.x - point1.x;
        float y = point2.y - point1.y;
        return (int) (Math.atan2(y, x) * 180 / Math.PI);
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
         * 选中其他区域
         *
         * @param position 下标
         */
        void onClickOther(int position);

        /**
         * 点击控制器的任意一个区域
         *
         * @param in 在框内
         * @param x  x坐标
         * @param y  y坐标
         */
        void onClick(boolean in, float x, float y);

        /**
         * 删除
         */
        void onDelete();

        /**
         * 对齐
         *
         * @param align 对齐
         */
        @Deprecated
        void onAlign(int align);

        void onCopy();


        /**
         * 编辑
         */
        void onEdit();

        /**
         * 改变   现在显示区域、旋转角度、
         *
         * @param rectF 现在的显示区域
         * @param angle 角度
         * @return 是否刷新
         */
        boolean onRectChange(RectF rectF, float angle);


        /**
         * down
         */
        void onTouchDown();

        /**
         * 抬起
         */
        void onTouchUp();

        /***
         * 点击了空白区域
         */
        void onExitEdit();

        /**
         * 获取宽度
         *
         * @return 容器宽度
         */
        float getWidth();

        /**
         * 获取高度
         *
         * @return 容器高度
         */
        float getHeight();

    }

    public static interface Callback {
        /**
         * @return true 仅在主界面时，允许点击空白自动退出(启用监听空白区域，自动退出编辑模式)
         */
        boolean enableAutoExit();
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }
}
