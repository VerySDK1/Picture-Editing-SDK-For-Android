package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.pesdk.uisdk.R;
import com.vecore.BaseVirtual;
import com.vecore.models.MaskObject;

/**
 * 蒙版
 */
public abstract class MaskRender {

    //缩放比例区间
    private static final float MAX_SCALE = 5.0f;
    private static final float MIN_SCALE = 0.1f;
    //最小圆角值
    protected static final int RADIUS_MIN = 16;

    protected Context mContext;
    //画笔
    protected Paint mPaint = new Paint();
    //矩阵
    private Matrix mMatrix = new Matrix();
    protected float[] mDstPoint;//点击旋转后的位置
    //按钮
    protected Bitmap mBtnCorner; //圆角
    protected Bitmap mBtnTop; //上
    protected Bitmap mBtnRight;// 右
    protected Bitmap mBtnRotate;//旋转 缩放
    //按钮位置
    protected RectF mCornerRectF = new RectF();
    protected RectF mTopRectF = new RectF();
    protected RectF mRightRectF = new RectF();
    protected RectF mRotateRectF = new RectF();

    //时间进度
    protected MaskObject.KeyFrame mCurrentFrame;
    //媒体初始旋转角度
    protected float mMediaAngle = 0;
    //显示区域 (0~1)
    protected RectF mShowRectF = new RectF();
    //控件区域 具体数值
    protected RectF mViewRectF = new RectF();
    //中心坐标具体
    public final PointF mViewCenterPointF = new PointF();
    //中心点坐标 相对于控件大小 为旋转角度
    protected PointF mCenterPointF = new PointF();


    public MaskRender(Context context) {
        mContext = context;
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);

        // 初始化控制图片
        mBtnCorner = BitmapFactory.decodeResource(context.getResources(), R.drawable.pesdk_thumb_corner);
        mBtnTop = BitmapFactory.decodeResource(context.getResources(), R.drawable.pesdk_thumb_height);
        mBtnRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.pesdk_thumb_width);
        mBtnRotate = BitmapFactory.decodeResource(context.getResources(), R.drawable.pesdk_thumb_rotate);
    }

    /**
     * 设置初始值
     *
     * @param show 媒体显示区域
     */
    public void init(RectF show, RectF viewRectF, int angle) {
        if (show == null || viewRectF == null) {
            return;
        }
        mCurrentFrame = new MaskObject.KeyFrame();
        //显示区域
        mShowRectF.set(show);
        //控件区域
        mViewRectF.set(viewRectF);
        //媒体旋转角度
        mMediaAngle = angle;
    }

    /**
     * 返回 蒙版信息
     */
    public MaskObject.KeyFrame getKeyframe() {
        return mCurrentFrame;
    }

    /**
     * 设置当前keyframe
     */
    public void setKeyframe(MaskObject.KeyFrame keyframe) {
        mCurrentFrame.setKeyFrame(keyframe);
    }

    /**
     * 绘制
     */
    public void onDraw(Canvas canvas) {
        if (mCurrentFrame == null) {
            return;
        }

        //中心点 mCenterPoint相对于show的中心点 center范围是-1~1
        PointF center = mCurrentFrame.getCenter();
        float cx = ((center.x + 1) / 2 * mShowRectF.width() + mShowRectF.left) * mViewRectF.width();//相对于控件的显示大小
        float cy = ((center.y + 1) / 2 * mShowRectF.height() + mShowRectF.top) * mViewRectF.height();
        mCenterPointF.x = cx;
        mCenterPointF.y = cy;
        float[] p = calculateRotate(cx, cy, mMediaAngle);
        mViewCenterPointF.set(p[0], p[1]);

        if (Float.isNaN(mCurrentFrame.getAngle())) {
            mCurrentFrame.setAngle(0);
        }

        mMatrix.reset();
        //媒体旋转角度
        mMatrix.postRotate(mMediaAngle, mShowRectF.centerX() * mViewRectF.width(),
                mShowRectF.centerY() * mViewRectF.height());
        //mask的角度
        mMatrix.postRotate(-mCurrentFrame.getAngle(), mViewCenterPointF.x, mViewCenterPointF.y);

        //旋转
        int id = canvas.save();
        canvas.concat(mMatrix);
        //绘制
        drawPattern(canvas);
        drawBtn(canvas);
        canvas.restoreToCount(id);

        //绘制原点
        canvas.drawCircle(mViewCenterPointF.x, mViewCenterPointF.y, RADIUS_MIN, mPaint);
    }


    protected static final int TOUCH_STATUE_NO = 0;
    protected static final int TOUCH_STATUE_CORNER = 1;
    protected static final int TOUCH_STATUE_TOP = 2;
    protected static final int TOUCH_STATUE_RIGHT = 3;
    protected static final int TOUCH_STATUE_ROTATE = 4;
    //单击时 手指点下去时的 x y
    protected float mDownX = 0;
    protected float mDownY = 0;
    //临时保存的
    protected MaskObject.KeyFrame mTempKeyFrame;
    //点击时的距离、角度
    protected double mPreLength;
    protected float mStartAngle;
    //多指
    protected boolean mMorePointer;
    //操作状态
    protected int mTouchStatue = TOUCH_STATUE_NO;

    public boolean onTouchEvent(MotionEvent event) {
        if (mCurrentFrame == null) {
            return false;
        }
        int pointerNum = event.getPointerCount();
        if (pointerNum == 1) { //单点
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //旋转后的位置
                    mMorePointer = false;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    judgeBtn();
                    mTempKeyFrame = mCurrentFrame.copy();
                    mPreLength = getDistance(mDownX, mDownY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mMorePointer) {
                        if (mTouchStatue == TOUCH_STATUE_ROTATE) {//旋转
                            //角度
                            mCurrentFrame.setAngle(mTempKeyFrame.getAngle() - getAngle(event.getX(), event.getY()));
                            //缩放
                            scale((float) (getDistance(event.getX(), event.getY()) / mPreLength));
                        } else if (mTouchStatue == TOUCH_STATUE_RIGHT) {//右边
                            float scale = (float) (getDistance(event.getX(), event.getY()) / mPreLength);
                            BaseVirtual.SizeF size = mTempKeyFrame.getSize();
                            float width = size.getWidth() * scale;
                            if (width < MIN_SCALE) {
                                width = MIN_SCALE;
                            } else if (width > MAX_SCALE) {
                                width = MAX_SCALE;
                            }
                            mCurrentFrame.setSize(width, size.getHeight());
                        } else if (mTouchStatue == TOUCH_STATUE_TOP) {//左边
                            float scale = (float) (getDistance(event.getX(), event.getY()) / mPreLength);
                            BaseVirtual.SizeF size = mTempKeyFrame.getSize();
                            float height = size.getHeight() * scale;
                            if (height < MIN_SCALE) {
                                height = MIN_SCALE;
                            } else if (height > MAX_SCALE) {
                                height = MAX_SCALE;
                            }
                            mCurrentFrame.setSize(size.getWidth(), height);
                        } else if (mTouchStatue == TOUCH_STATUE_CORNER) {//圆角
                            float diff = (float) (getDistance(event.getX(), event.getY()) - mPreLength);
                            BaseVirtual.SizeF size = mTempKeyFrame.getSize();
                            float width = size.getWidth() * mShowRectF.width() * mViewRectF.width();
                            float height = size.getHeight() * mShowRectF.height() * mViewRectF.height();
                            float asp = size.getWidth() / size.getHeight();
                            float radius = mTempKeyFrame.getCornerRadius();
                            if (asp > 1) { // w > h
                                radius -= diff / height * 2;
                            } else {
                                radius -= diff / width * 2;
                            }
                            if (radius > 1) {
                                radius = 1;
                            } else if (radius < 0) {
                                radius = 0;
                            }
                            mCurrentFrame.setCornerRadius(radius);
                        } else {//移动
                            mCurrentFrame.setCenter(calculateCenter(event.getX(), event.getY()));
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
        } else { //多点
            //缩放 旋转
            int action = event.getAction();
            if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                mTempKeyFrame = mCurrentFrame.copy();
                mMorePointer = true;
                mPreLength = getDistance(event);
                mStartAngle = getAngle(event);
            } else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                    || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                //拿起
                mMorePointer = false;
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mMorePointer) {
                    // 计算缩放系数
                    double endLen = getDistance(event);
                    scale((float) (endLen / mPreLength));
                    //角度
                    mCurrentFrame.setAngle(mTempKeyFrame.getAngle() - (getAngle(event) - mStartAngle));
                }
            }
        }
        return true;
    }

    /**
     * 缩放
     */
    protected void scale(float scale) {
        if (isRotateScale() && mTempKeyFrame != null) {
            BaseVirtual.SizeF size = mTempKeyFrame.getSize();
            float width = size.getWidth() * scale;
            float height = size.getHeight() * scale;
            float asp = size.getWidth() / size.getHeight();
            if (width < MIN_SCALE) {
                width = MIN_SCALE;
                if (isKeepRatio()) {
                    height = width / asp;
                }
            } else if (width > MAX_SCALE) {
                width = MAX_SCALE;
                if (isKeepRatio()) {
                    height = width / asp;
                }
            }
            if (height < MIN_SCALE) {
                height = MIN_SCALE;
                if (isKeepRatio()) {
                    width = height * asp;
                }
            } else if (height > MAX_SCALE) {
                height = MAX_SCALE;
                if (isKeepRatio()) {
                    width = height * asp;
                }
            }
            mCurrentFrame.setSize(width, height);
        }
    }

    /**
     * 用于计算两个两个手指之间形成的直线与x轴的夹角
     *
     * @return 夹角
     */
    protected int getAngle(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int pointerId = event.getPointerId(0);
        int pointerId1 = event.getPointerId(1);
        if (pointerId > pointerCount || pointerId1 > pointerCount) {
            return 0;
        }
        Point point1 = new Point((int) event.getX(pointerId), (int) event.getY(pointerId));
        Point point2 = new Point((int) event.getX(pointerId1), (int) event.getY(pointerId1));
        float x = point2.x - point1.x;
        float y = point2.y - point1.y;
        return (int) (Math.atan2(y, x) * 180 / Math.PI);
    }

    /**
     * 旋转角度
     */
    protected float getAngle(float pointX, float pointY) {
        float radian = (float) (Math.atan2(pointY - mViewCenterPointF.y, pointX - mViewCenterPointF.x)
                - Math.atan2(mDownY - mViewCenterPointF.y, mDownX - mViewCenterPointF.x));
        // 弧度转换为角度
        return (float) (radian * 180 / Math.PI);
    }

    /**
     * 计算两点之间的距离 缩放
     */
    protected double getDistance(float pointX, float pointY) {
        float xlen = Math.abs(pointX - mViewCenterPointF.x);
        float ylen = Math.abs(pointY - mViewCenterPointF.y);
        return Math.sqrt(xlen * xlen + ylen * ylen);
    }

    /**
     * 计算两点之间的距离 缩放
     */
    protected double getDistance(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int pointerId = event.getPointerId(0);
        int pointerId1 = event.getPointerId(1);
        if (pointerId > pointerCount || pointerId1 > pointerCount) {
            return 1;
        }
        int xlen = Math.abs((int) event.getX(event.getPointerId(pointerId)) - (int) event.getX(event.getPointerId(pointerId1)));
        int ylen = Math.abs((int) event.getY(event.getPointerId(pointerId)) - (int) event.getY(event.getPointerId(pointerId1)));
        return Math.sqrt(xlen * xlen + ylen * ylen);
    }

    /**
     * 计算中心点位置
     *
     * @param x 当前X 当前Y
     */
    protected PointF calculateCenter(float x, float y) {
        //还原回媒体的角度
        float[] pointF = calculateRotate(x, y, -mMediaAngle);
        float[] down = calculateRotate(mDownX, mDownY, -mMediaAngle);
        //计算新的点
        float offsetX = (pointF[0] - down[0]) / mViewRectF.width() / mShowRectF.width() * 2;
        float offsetY = (pointF[1] - down[1]) / mViewRectF.height() / mShowRectF.height() * 2;
        PointF center = mTempKeyFrame.getCenter();
        PointF result = new PointF();
        result.x = center.x + offsetX;
        result.y = center.y + offsetY;
        if (result.x < -1) {
            result.x = -1;
        } else if (result.x > 1) {
            result.x = 1;
        }
        if (result.y < -1) {
            result.y = -1;
        } else if (result.y > 1) {
            result.y = 1;
        }
        return new PointF(result.x, result.y);
    }

    /**
     * 计算旋转后的位置 相对于媒体
     */
    protected float[] calculateRotate(float x, float y, float angle) {
        float[] srcPoint = new float[2];
        float[] dstPoint = new float[2];
        srcPoint[0] = x;
        srcPoint[1] = y;
        mMatrix.reset();
        mMatrix.postRotate(angle, mShowRectF.centerX() * mViewRectF.width(),
                mShowRectF.centerY() * mViewRectF.height());
        mMatrix.mapPoints(dstPoint, srcPoint);
        return dstPoint;
    }

    /**
     * 判断是否点击到了按钮
     */
    protected void judgeBtn() {
        float[] srcPoint = new float[2];
        mDstPoint = new float[2];
        srcPoint[0] = mDownX;
        srcPoint[1] = mDownY;
        mMatrix.reset();
        mMatrix.reset();
        //mask的角度
        mMatrix.postRotate(mCurrentFrame.getAngle(), mViewCenterPointF.x, mViewCenterPointF.y);
        //媒体旋转角度
        mMatrix.postRotate(-mMediaAngle, mShowRectF.centerX() * mViewRectF.width(),
                mShowRectF.centerY() * mViewRectF.height());
        mMatrix.mapPoints(mDstPoint, srcPoint);
        //判断
        if (mCornerRectF.contains(mDstPoint[0], mDstPoint[1])) {
            mTouchStatue = TOUCH_STATUE_CORNER;
        } else if (mTopRectF.contains(mDstPoint[0], mDstPoint[1])) {
            mTouchStatue = TOUCH_STATUE_TOP;
        } else if (mRightRectF.contains(mDstPoint[0], mDstPoint[1])) {
            mTouchStatue = TOUCH_STATUE_RIGHT;
        } else if (mRotateRectF.contains(mDstPoint[0], mDstPoint[1])) {
            mTouchStatue = TOUCH_STATUE_ROTATE;
        } else {
            mTouchStatue = TOUCH_STATUE_NO;
        }
    }


    /**
     * 缩放保持比例
     */
    protected boolean isKeepRatio() {
        return false;
    }

    /**
     * 绘制图形
     */
    protected abstract void drawPattern(Canvas canvas);

    /**
     * 绘制按钮
     */
    protected abstract void drawBtn(Canvas canvas);

    /**
     * 旋转按钮是否具有缩放的效果
     */
    protected abstract boolean isRotateScale();


}
