package com.pesdk.uisdk.widget.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.models.FlipType;
import com.vecore.models.caption.CaptionObject;

import java.util.HashMap;
import java.util.Map;

import androidx.core.content.ContextCompat;

/**
 * layer:媒体拖动
 */
public class DragMediaView extends View {


    @interface IClick {
        int CLICK_NONE = 0;
        int CLICKED_EDIT = 1;  //翻转
        int CLICKED_CONTROL = 2; //  缩放
        int CLICKED_DELETE = 3; // 删除
        int CLICKED_COPY = 4; // 复制
    }

    @IClick
    private int checkPosition = IClick.CLICK_NONE;

    private String TAG = "DragMediaView";
    private Context mContext;
    /**
     * 图片的初始状态，就是开始运行的时候的原始状态
     */
    private static final int NONE = 0;

    /**
     * 拖动状态
     */
    private static final int DRAG = 1;

    /**
     * 缩放和旋转状态
     */
    private static final int ZOOM_ROTATE = 4;
    /**
     * 缩放比例区间
     */
    private final float MAX_SCALE = CaptionObject.MAX_SCALE;
    private final float MIN_SCALE = CaptionObject.MIN_SCALE;


    /**
     * 旋转之后的图片宽度
     */
    private int mRotatedImageWidth;

    /**
     * 旋转之后图片的高度
     */
    private int mRotatedImageHeight;

    /**
     * 图片的宽度
     */
    private int mImageViewWidth;
    /**
     * 图片的高度
     */
    private int mImageViewHeight;
    /**
     * 图片的左边
     */
    private int mImageViewLeft;
    /**
     * 图片的上边
     */
    private int mImageViewTop;

    /**
     * 当前matrix
     */
    private Matrix mMatrix;


    /**
     * 父容器大小
     */
    private Point mParentSize;
    /**
     * 默认状态为初始状态
     */
    private int mDefautMode = NONE;

    /**
     * 图片控制点的坐标
     */
    private PointF mAPoint = new PointF();
    /**
     * 图片删除点坐标
     */
    private PointF mBPoint = new PointF();

    /**
     * 原始图片,的备份
     */
    private Bitmap mOriginalBackupBitmap;

    /**
     * 删除图片
     */
    private Bitmap mDeleteBitmap;

    /*复制图片*/
    private Bitmap mCopyBitmap;

    private boolean isShowDeleteButton = false;
    private boolean isShowEditButton = false;

    /**
     * 可以控制图片旋转伸缩的图片
     */
    private Bitmap mContralBitmap, mEditLayerBitmap;


    /**
     * 画刷
     */
    private Paint mPaint;
    private Paint mPaintLineLT;
    private Paint mPaintLineRB;

    /**
     * 图片中心坐标
     */
    private Point mImageCenterPoint = new Point(0, 0);
    /**
     * 旋转角度
     */
    private float mRotateAngle;

    /**
     * 缩放系数
     */
    private float disf = 1f;

    /**
     * 缩放框外面放置删除按钮需要的宽度<br>
     * 用于放2个图标
     */
    private int mOutLayoutImageWidth;

    /**
     * 缩放框外面放置删除按钮需要的高度
     */
    private int mOutLayoutImageHeight;

    /**
     * 镜像图片中心坐标
     */
    private Point mDeleteCenterPoint;


    /**
     * * 删除图片中心坐标
     */
    private Point mEditCenterPoint;

    /*复制图片中心坐标*/
    private Point mCopyImageCenterPoint;


    /**
     * 控制图片中心坐标
     */
    private Point mContralImageCenterPoint;

    /**
     * 边框的左上角顶点坐标
     */
    private Point mPoint1;

    /**
     * 边框的右上角顶点坐标
     */
    private Point mPoint2;

    /**
     * 边框的右下角顶点坐标
     */
    private Point mPoint3;

    /**
     * 边框的左下角顶点坐标
     */
    private Point mPoint4;

    private int dx;
    private int dy;
    private final Rect mRect;
    /**
     * 手势
     */
    private GestureDetector mGestureDetector;

    /**
     * @param rotate     旋转角度  （顺时针方向）
     * @param parentSize 父容器的大小
     * @param rect       显示位置  旋转角度：0度时
     * @param flipType
     */
    public DragMediaView(Context context, float rotate, int[] parentSize, Rect rect, FlipType flipType) {
        super(context);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);

        mPaintLineLT = new Paint();
        mPaintLineLT.setAntiAlias(true);
        mPaintLineLT.setColor(Color.WHITE);
        mPaintLineLT.setStrokeWidth(3);
        mPaintLineLT.setStyle(Paint.Style.STROKE);
        mPaintLineLT.setShadowLayer(3, -2, -2, ContextCompat.getColor(context, R.color.pesdk_drag_shadow_color));
//        mPaintLineLT.setShadowLayer(3, -2, -2, Color.BLACK);

        mPaintLineRB = new Paint();
        mPaintLineRB.setAntiAlias(true);
        mPaintLineRB.setColor(Color.WHITE);
        mPaintLineRB.setStrokeWidth(3);
        mPaintLineRB.setStyle(Paint.Style.STROKE);
        mPaintLineRB.setShadowLayer(3, 2, 2, ContextCompat.getColor(context, R.color.pesdk_drag_shadow_color));

//        mPaintLine.setShadowLayer(5,5,5, ContextCompat.getColor(context,R.color.pesdk_drag_shadow_color));
        mParentSize = new Point(parentSize[0], parentSize[1]);
        mRotateAngle = rotate;
        // 初始化删除图片
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_layer_delete);
        mCopyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_layer_copy);
        // 初始化控制图片
        mContralBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_layer_control);
        mEditLayerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_layer_edit);
        mRect = new Rect(rect);
        setFlipType(flipType);
        // 删除图片的半宽
        mOutLayoutImageWidth = mDeleteBitmap.getWidth() / 2;
        mOutLayoutImageHeight = mDeleteBitmap.getHeight() / 2;
        drawFrame();
        setCenter(new Point(rect.centerX(), rect.centerY()));
        setImageStyle(false);

        mGestureDetector = new GestureDetector(mContext, new MyGestureDetector());
    }

    private boolean mEnableAutoExitEdit = true;


    /**
     * 是否启用最后的单击手势，用来辅助UI退出编辑
     *
     * @param enable true   启用监听空白区域，自动退出编辑模式
     */
    public void enableAutoExit(boolean enable) {
        Log.e(TAG, "enableAutoExit: " + enable);
        mEnableAutoExitEdit = enable;
    }

    public void setShowDeleteButton(boolean show) {
        isShowDeleteButton = show;
        invalidate();
    }

    public void setShowEditButton(boolean show) {
        isShowEditButton = show;
        invalidate();
    }

    /**
     * @param flipType
     */
    public void setFlipType(FlipType flipType) {
//        mFlipType = flipType;
        fixFlipBmp();
    }


    /**
     * 设置显示样式
     *
     * @param invalidate
     */
    public void setImageStyle(boolean invalidate) {
        drawFrame();
        invaView(invalidate);
    }

    /**
     * 刷新图片信息的配置，缩放比
     *
     * @param invalidate
     */
    private void invaView(boolean invalidate) {
        setImageViewParams(mOriginalBitmap, mImageCenterPoint, mRotateAngle, disf);
        if (invalidate)
            invalidate();
    }


    private void setCenter(Point center) {
        if (!center.equals(mImageCenterPoint.x, mImageCenterPoint.y)) {
            this.mImageCenterPoint = center;
        }
    }


    public void offSet(int dx, int dy) {
        Point point = new Point(getCenter());
        point.offset(dx, dy);
        setCenter(new Point(point));
        invalidate();
    }

    /**
     * 设置放大程度
     *
     * @param disf
     */
    public boolean setDisf(float disf) {
        if (disf <= MIN_SCALE) {
            disf = MIN_SCALE;
        } else if (disf >= MAX_SCALE) {
            disf = MAX_SCALE;
        }
        if (this.disf != disf) {
            this.disf = disf;
            invaView(true);
            return true;
        }
        return false;
    }

    /***
     * 缩放
     * @param offsetScale  每次与上次的缩放变化比
     */
    public boolean offsetScale(float offsetScale) {
        if (0 != offsetScale) {
            return setDisf(disf + (disf * offsetScale));
        }
        return false;
    }

    /**
     * 更新中心点
     *
     * @param center
     */
    public void update(PointF center) {
        setCenter(new Point((int) (center.x * mParentSize.x), (int) (center.y * mParentSize.y)));
        invalidate();
    }

    public Point getCenter() {
        return mImageCenterPoint;
    }

    public FlipType getFlipType() {
//        return mFlipType;
        return null;
    }

    public float getDisf() {
        return disf;
    }

    public int getRotateAngle() {
        return (int) mRotateAngle;
    }

    private Bitmap mOriginalBitmap;

    /**
     * 画单独的一帧的画面
     */
    private void drawFrame() {
        if (null != mOriginalBitmap && !mOriginalBitmap.isRecycled()) {
            mOriginalBitmap.recycle();
            mOriginalBitmap = null;
            if (null != mOriginalBackupBitmap && !mOriginalBackupBitmap.isRecycled()) {
                mOriginalBackupBitmap.recycle();
                mOriginalBackupBitmap = null;
            }
        }
        if (null == mOriginalBitmap && mRect.width() > 0 && mRect.height() > 0) {
            // -1的情况
            GradientDrawable gd = new GradientDrawable();// 创建drawable
            gd.setColor(Color.TRANSPARENT);
            gd.setCornerRadius(5);
            if (drawControl) {
                gd.setStroke(5, Color.parseColor("#85B0E9"));
            } else {
                gd.setStroke(5, Color.TRANSPARENT);
            }
            mOriginalBitmap = BitmapUtils
                    .drawableToBitmap(gd, mRect.width(), mRect.height());
        }
        if (mOriginalBitmap != null && !mOriginalBitmap.isRecycled()) {
            mOriginalBackupBitmap = Bitmap.createBitmap(mOriginalBitmap);
        }
    }

    private boolean drawControl = false; // 设置是否支持拖拽(编辑模式下、预览模式下的区别)

    /**
     * 是否可以随意拖动
     *
     * @param isControl
     */
    public void setControl(boolean isControl) {
        drawControl = isControl;
        invalidate();
    }

    private HashMap<Long, Bitmap> maps = new HashMap<Long, Bitmap>();

    @Override
    protected void onDraw(Canvas canvas) {
        clearSomeBitmap();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawColor(Color.CYAN);// 测试用。查看区域


//        {
//            //测试专用
//            mPaint.setColor(Color.RED);
//            canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y,
//                    mPaint);
//            canvas.drawLine(mPoint2.x, mPoint2.y, mPoint3.x, mPoint3.y,
//                    mPaint);
//            canvas.drawLine(mPoint3.x, mPoint3.y, mPoint4.x, mPoint4.y,
//                    mPaint);
//            canvas.drawLine(mPoint4.x, mPoint4.y, mPoint1.x, mPoint1.y,
//                    mPaint);
//        }
        if (null == mOriginalBitmap || mOriginalBitmap.isRecycled()) {
            Log.e(TAG, "mOnDraw no has mOriginalBitmap.... ");
            return;
        }
        int bwidth = mOriginalBitmap.getWidth();
        int bheight = mOriginalBitmap.getHeight();


//        Log.e(TAG, "mOnDraw: " + msi.mlocalpath + "   " + bwidth + "*" + bheight + "  >>>" + msi.w + "*" + msi.h + "  >" + isLaShen+"  "+msi);
        Bitmap newb = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);

        Canvas canvasTmp = new Canvas();
//        canvasTmp.drawColor(Color.GRAY);
        canvasTmp.setBitmap(newb);
        canvasTmp.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        Rect dst = new Rect(0, 0, bwidth, bheight);

        Matrix tmp = new Matrix();

//        if (mFlipType == FlipType.FLIP_TYPE_VERTICAL || mFlipType == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
//            tmp.postScale(1, -1);   //镜像垂直翻转
//            tmp.postTranslate(0, mOriginalBitmap.getHeight());
//        }
//        if (mFlipType == FlipType.FLIP_TYPE_HORIZONTAL || mFlipType == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
//            tmp.postScale(-1, 1);   //镜像水平翻转
//            tmp.postTranslate(mOriginalBitmap.getWidth(), 0);
//        }


        canvasTmp.drawBitmap(mOriginalBitmap, tmp, null);
//        canvasTmp.drawBitmap(mOriginalBitmap, new Rect(0, 0, mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight()), dst, null);
        RectF rectF = new RectF(0f, 0, 1f, 1f);
        if (null != rectF) {

            int mleft = (int) (bwidth * rectF.left);
            int mtop = (int) (bheight * rectF.top);

            int mright = (int) (bwidth * rectF.right);
            int mbottom = (int) (bheight * rectF.bottom);

            // 左上角的坐标
            Point mp11 = new Point(mleft, mtop);

            // 右上角的坐标
            Point mp21 = new Point(mright, mtop);

            // 右下角坐标
            Point mp31 = new Point(mright, mbottom);

            // 左下角坐标
            Point mp41 = new Point(mleft, mbottom);


//            mPaint.setColor(Color.BLACK);
//            canvasTmp.drawLine(mp11.x, mp11.y, mp21.x, mp21.y, mPaint);
//            canvasTmp.drawLine(mp21.x, mp21.y, mp31.x, mp31.y, mPaint);
//            canvasTmp.drawLine(mp31.x, mp31.y, mp41.x, mp41.y, mPaint);
//            canvasTmp.drawLine(mp41.x, mp41.y, mp11.x, mp11.y, mPaint);

            Point centerPoint = intersects(mp41, mp21, mp11, mp31);

            // 转化坐标系
            canvasTmp.translate(centerPoint.x, centerPoint.y);

            // 设置变化（旋转缩放）之后图片的宽高

            setImageViewWH(mRotatedImageWidth, mRotatedImageHeight, (mImageCenterPoint.x - mRotatedImageWidth / 2), (mImageCenterPoint.y - mRotatedImageHeight / 2));


            canvas.drawBitmap(newb, mMatrix, mPaint);
            maps.put(System.currentTimeMillis(), newb);
        }

        if (drawControl) {
            // 画图片的包围框 ，顺时针画
            //保证阴影在框外
            canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y, mPaintLineLT);
            canvas.drawLine(mPoint2.x, mPoint2.y, mPoint3.x, mPoint3.y, mPaintLineRB);
            canvas.drawLine(mPoint3.x, mPoint3.y, mPoint4.x, mPoint4.y, mPaintLineRB);
            canvas.drawLine(mPoint4.x, mPoint4.y, mPoint1.x, mPoint1.y, mPaintLineLT);

            if (!bLock) {
                //绘制3个按钮图片
                if (bShowControl) {
                    drawControl(canvas, mContralBitmap, mContralImageCenterPoint);
                }
                if (isShowDeleteButton) {
                    drawControl(canvas, mDeleteBitmap, mDeleteCenterPoint);
                    drawControl(canvas, mCopyBitmap, mCopyImageCenterPoint);
                }
                if (isShowEditButton) {
                    drawControl(canvas, mEditLayerBitmap, mEditCenterPoint);
                }
            }
        }


//        Rect rect = getSrcRect2();
////        Rect rect = getSrcRect();
//        Paint p = new Paint();
//        p.setColor(Color.argb(100, 100, 0, 0));
//        canvas.drawRect(rect, p);
    }

    private void clearSomeBitmap() {
        if (maps.size() > 0) {
            for (Map.Entry<Long, Bitmap> item : maps.entrySet()) {
                Bitmap b = item.getValue();
                if (null != b) {
                    if (!b.isRecycled()) {
                        b.recycle();
                    }
                    b = null;
                }
                maps.remove(item.getKey());
            }
            maps.clear();
        }

    }


    public void setShowControl(boolean bShowControl) {
        this.bShowControl = bShowControl;
    }

    //true 显示控制把手
    private boolean bShowControl = false;

    /**
     * @param canvas
     * @param bmp
     * @param point
     */
    private void drawControl(Canvas canvas, Bitmap bmp, Point point) {
        if (null != bmp && !bmp.isRecycled())
            canvas.drawBitmap(bmp, point.x - mOutLayoutImageWidth, point.y - mOutLayoutImageHeight, mPaint);
    }

    //是否修正角度
    private boolean mCorrectAngle = false;
    //双指缩放开始之间的距离和与X轴角度
    private double mStartLen;
    private int mStartAngle = 0;
    //是否点击、是否是两个手指
    private boolean mDown = false;
    private boolean mTwoPoint = false;
    //临时记录点下时的角度和倍数
    private float mTempAngle = 0;
    private float mTempDisf = 1.0f;


    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    private boolean unavailable = false; //true 不可用:拦截所有touch

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (unavailable) {
            return super.onTouchEvent(event);
        }
        if (!drawControl) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (null != listener) {
                    listener.onClick(this);
                }
            }
            return false;
        }
        if (mGestureDetector.onTouchEvent(event)) {
            Log.e(TAG, "onTouchEvent: ....");
            return true;
        }
        //两个手指  缩放模式
        if (event.getPointerCount() == 2) {
            int re = event.getAction();
            if ((re & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                mCorrectAngle = false;
                mDown = true;
                mTwoPoint = true;
                mTempDisf = disf;
                mTempAngle = mRotateAngle;
                try {
                    mStartLen = getDistance(event);
                    mStartAngle = getDeg(event);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else if ((re & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP || re == MotionEvent.ACTION_UP || re == MotionEvent.ACTION_CANCEL) {
                //拿起
                mDown = false;
                mCorrectAngle = false;
                mHandler.removeMessages(MSG_ANGLE);
                mHandler.postDelayed(() -> mTouchListener.onTouchUp(), 30);
                return false;
            } else if (re == MotionEvent.ACTION_MOVE) {//移动
                double endLen = 0;
                try {
                    endLen = getDistance(event);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                if (mDown) {
                    //角度复原后重新设置其实数据
                    if (mAPoint.x == -1000 && mAPoint.y == -1000) {
                        mStartLen = endLen;
                        mStartAngle = getDeg(event);
                        mTempAngle = mRotateAngle;
                        mTempDisf = disf;
                        mAPoint.set(0, 0);//只是作为恢复的判断
                    }

                    // 计算缩放系数，太复杂了。看不懂
                    float newDisf = (float) (endLen / mStartLen);
                    if (mCorrectAngle) {
                        if (newDisf < 0.7 || newDisf > 1.3) {
                            mHandler.removeMessages(MSG_ANGLE);
                            mCorrectAngle = false;
                            disf = mTempDisf * newDisf;
                        }
                    } else {
                        disf = mTempDisf * newDisf;
                    }
                    //判断缩放倍数范围
                    if (disf <= MIN_SCALE) {
                        disf = MIN_SCALE;
                    } else if (disf >= MAX_SCALE) {
                        disf = MAX_SCALE;
                    }
                    //计算旋转角度
                    int angle = (int) (mTempAngle + getDeg(event) - mStartAngle);
                    if (mCorrectAngle && Math.abs(getDeg(event) - mStartAngle) > 10) {
                        mHandler.removeMessages(MSG_ANGLE);
                        mCorrectAngle = false;
                    }
                    //如果角度 相差5  是90整数倍
                    if (!mCorrectAngle && Math.abs(mRotateAngle % 90) >= 5 && Math.abs(mRotateAngle % 90) <= 85 && (Math.abs(angle % 90) < 5 || Math.abs(angle % 90) > 85)) {
                        if (angle > 0) {
                            if (angle % 90 < 5) {
                                angle = angle / 90 * 90;
                            } else if (angle % 90 > 85) {
                                angle = angle / 90 * 90 + 90;
                            }
                        } else {
                            if (angle % 90 > -5) {
                                angle = angle / 90 * 90;
                            } else if (angle % 90 < -85) {
                                angle = angle / 90 * 90 - 90;
                            }
                        }
                        //触发震动
                        Utils.onVibrator(mContext);
                        mCorrectAngle = true;
                        mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                        mAPoint.set(-1000, -1000);
                        // 设置图片参数
                        setImageViewParams(mOriginalBitmap, mImageCenterPoint, angle, disf);
                        if (null != mTouchListener) {
                            mTouchListener.onRectChange();
                        }
                    }
                    if (!mCorrectAngle) {
                        // 设置图片参数
                        setImageViewParams(mOriginalBitmap, mImageCenterPoint, angle, disf);
                        if (null != mTouchListener) {
                            mTouchListener.onRectChange();
                        }
                    }
                }
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                    Log.e(TAG, "onTouchEvent: down.." + event.getPointerCount());
                    // 先获取点击坐标吗？
                    mAPoint.set(event.getX() + mImageViewLeft, event.getY() + mImageViewTop);
                    // 先判断用户点击的是哪个按钮（图片）, 如果是2，表示要旋转和伸缩图片
                    if (!bLock) {
                        checkPosition = getClickPosition((int) event.getX(), (int) event.getY());
                        if (checkPosition == IClick.CLICK_NONE) {
                            if (null != listener) {
                                listener.onClick(this);
                            }
                        }
                    } else {
                        checkPosition = IClick.CLICK_NONE;
                    }
                    mCorrectAngle = false;
                    mTwoPoint = false;
                    mTempAngle = mRotateAngle;
                    if (drawControl) {
                        if (checkPosition == IClick.CLICKED_EDIT) {
                            onNextFlip();
                            invalidate();
                            if (isShowEditButton) {
                                if (null != onDelListener) {
                                    onDelListener.onEdit(this);
                                }
                            }
                        } else if (checkPosition == IClick.CLICKED_DELETE) {
                            if (null != onDelListener) {
                                onDelListener.onDelete(this);
                            }
                        } else if (checkPosition == IClick.CLICKED_CONTROL) {
                            // 设置操作模式为移动缩放模式
                            mDefautMode = ZOOM_ROTATE;
                        } else if (checkPosition == IClick.CLICKED_COPY) {
                            onDelListener.onCopy();
                        } else {
                            // 设置操作模式为拖动模式
                            mDefautMode = DRAG;
                        }
                    } else {
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
//                    Log.e(TAG, "onTouchEvent: move " + mTwoPoint + " " + mDefautMode + " " + checkPosition);
                    if (!mTwoPoint && (checkPosition == IClick.CLICK_NONE || checkPosition == IClick.CLICKED_CONTROL)) {
                        // 如果为移动缩放模式
                        mBPoint.set(event.getX() + mImageViewLeft, event.getY() + mImageViewTop);
                        //旋转 缩放
                        if (mDefautMode == ZOOM_ROTATE) {
                            //自定义了默认大小
                            int tmpW = 0, tmpH;
                            tmpW = mOriginalBitmap.getWidth();
                            tmpH = mOriginalBitmap.getHeight();
                            float realL = (float) Math
                                    .sqrt((float) (tmpW * tmpW + tmpH * tmpH) / 4);
                            float newL = (float) Math
                                    .sqrt((mBPoint.x - (float) mImageCenterPoint.x)
                                            * (mBPoint.x - (float) mImageCenterPoint.x)
                                            + (mBPoint.y - (float) mImageCenterPoint.y)
                                            * (mBPoint.y - (float) mImageCenterPoint.y));

                            // 计算缩放系数，太复杂了。看不懂
                            float newDisf = newL / realL;
                            if (mCorrectAngle) {
                                if (Math.abs(disf - newDisf) > 0.3) {
                                    mHandler.removeMessages(MSG_ANGLE);
                                    mCorrectAngle = false;
                                    disf = newDisf;
                                }
                            } else {
                                disf = newDisf;
                            }
                            if (disf <= MIN_SCALE) {
                                disf = MIN_SCALE;
                            } else if (disf >= MAX_SCALE) {
                                disf = MAX_SCALE;
                            }
                            // 计算旋转角度
                            double a = spacing(mAPoint.x, mAPoint.y,
                                    mImageCenterPoint.x, mImageCenterPoint.y);
                            double b = spacing(mAPoint.x, mAPoint.y, mBPoint.x,
                                    mBPoint.y);
                            double c = spacing(mBPoint.x, mBPoint.y,
                                    mImageCenterPoint.x, mImageCenterPoint.y);
                            double cosB = (a * a + c * c - b * b) / (2 * a * c);
                            if (cosB > 1) {// 浮点运算的时候 cosB 有可能大于1.
                                cosB = 1f;
                            }
                            double angleB = Math.acos(cosB);
                            // 新的旋转角度
                            float newAngle = (float) (angleB / Math.PI * 180);
                            //center -> proMove的向量， 我们使用PointF来实现
                            PointF centerToProMove = new PointF((mAPoint.x - mImageCenterPoint.x), (mAPoint.y - mImageCenterPoint.y));
                            //center -> curMove 的向量
                            PointF centerToCurMove = new PointF((mBPoint.x - mImageCenterPoint.x), (mBPoint.y - mImageCenterPoint.y));
                            //向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
                            float result = centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;
                            if (result < 0) {
                                newAngle = -newAngle;
                            }
                            mTempAngle = (mTempAngle + newAngle) % 360;
                            if (mCorrectAngle && Math.abs(mTempAngle - mRotateAngle) > 10) {
                                mHandler.removeMessages(MSG_ANGLE);
                                mCorrectAngle = false;
                            }
                            //如果角度 相差5  靠近90整数倍  恢复成90倍数
                            if (!mCorrectAngle
                                    && Math.abs(mRotateAngle % 90) >= 5 && Math.abs(mRotateAngle % 90) <= 85
                                    && (Math.abs(mTempAngle % 90) < 5 || Math.abs(mTempAngle % 90) > 85)) {
                                if (mTempAngle > 0) {
                                    if (mTempAngle % 90 < 5) {
                                        mTempAngle = (int) (mTempAngle / 90) * 90;
                                    } else if (mTempAngle % 90 > 85) {
                                        mTempAngle = (int) (mTempAngle / 90) * 90 + 90;
                                    }
                                } else {
                                    if (mTempAngle % 90 > -5) {
                                        mTempAngle = (int) (mTempAngle / 90) * 90;
                                    } else if (mTempAngle % 90 < -85) {
                                        mTempAngle = (int) (mTempAngle / 90) * 90 - 90;
                                    }
                                }
                                //触发震动
                                Utils.onVibrator(getContext());
                                mCorrectAngle = true;
                                mHandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                                // 设置图片参数
                                setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                        (int) mTempAngle, disf);
                            }
                            if (!mCorrectAngle) {
                                // 设置图片参数
                                setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                        (int) mTempAngle, disf);
                            }
                            // 如果为拖动模式
                        } else if (mDefautMode == DRAG) {
                            // 修改中心坐标
                            mImageCenterPoint.x += mBPoint.x - mAPoint.x;
                            mImageCenterPoint.y += mBPoint.y - mAPoint.y;
                            // 设置中心坐标
                            setCenterPoint(mImageCenterPoint);
                        }
                        mAPoint.x = mBPoint.x;
                        mAPoint.y = mBPoint.y;
                        if (null != mTouchListener) {
                            mTouchListener.onRectChange();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
//                    Log.e(TAG, "onTouchEvent: up " + checkPosition + " " + bLock + " " + mDefautMode + " " + mEnableAutoExitEdit);
                    // 设置操作模式为什么都不做
                    mCorrectAngle = false;
                    mTwoPoint = false;
                    mHandler.removeMessages(MSG_ANGLE);
                    if (!bLock && checkPosition == IClick.CLICK_NONE && mDefautMode == NONE && mEnableAutoExitEdit) {
                        checkExitRect(event.getX(), event.getY());
                    }
                    mHandler.removeCallbacks(mDelayTouchUpRunnable);
                    mHandler.postDelayed(mDelayTouchUpRunnable, 30);
                    mDefautMode = NONE;
                    break;
            }
        }
        return true;
    }

    private Runnable mDelayTouchUpRunnable = new Runnable() {
        @Override
        public void run() {
            mTouchListener.onTouchUp();
        }
    };

    /**
     * 验证空白点击
     */
    private void checkExitRect(float x, float y) {
        int mAngle = getRotateAngle();
        Rect rect = getSrcRect2();
        PointF pointF = isContains2(mAngle, rect, x, y);
//        Log.e(TAG, "checkExitRect: " + pointF + " " + rect);
        if (pointF == null) {//点击了画框以外的区域
            // 通知UI,验证，是否点击了rect 之外的其他空白区域，用以标记 退出编辑
            mHandler.postDelayed(() -> {
//                Log.e(TAG, "checkExitRect: " + this);
                mTouchListener.onExitEdit();
            }, 30);
        }
    }

    private Matrix mRectMatrix = new Matrix();

    /**
     * 是否控制按钮
     */
    private PointF isContains2(float angle, Rect rect, float x, float y) {
        //x y 旋转角度
        float[] src = new float[]{x, y};
        float[] dst = new float[2];
        mRectMatrix.reset();
        mRectMatrix.postRotate(-angle, rect.centerX(), rect.centerY());
        mRectMatrix.mapPoints(dst, src);

        RectF dstRect = new RectF();
        mRectMatrix.mapRect(dstRect, new RectF(rect));
//        Log.e(TAG, "isContains2: " + angle + " " + rect + " dstRect:" + dstRect + " a:" + rect.width() + "*" + rect.height() + " d:" + dstRect.width() + "" +
//                "*" + dstRect.height() + " " + Arrays.toString(src) + " dst:" + Arrays.toString(dst) + " " + mParentSize + " " + getWidth() + "*" + getHeight());
        if (rect.contains((int) dst[0], (int) dst[1])) {
            return new PointF((dst[0] - rect.left) / (rect.width()), (dst[1] - rect.top) / rect.height()); //相对无角度时，在预览区域的相对位置 0~1.0f
        } else {
            return null;
        }

    }

    /**
     * 是否控制按钮
     */
    private PointF isContains(float x, float y) {
        int mAngle = getRotateAngle();
//        Rect rect = getSrcRect();
        Rect rect = getSrcRect2();
        return isContains2(mAngle, rect, x, y);
    }


    private static final int MSG_ANGLE = 22;

    private Handler mHandler = new Handler(message -> {
        if (message.what == MSG_ANGLE) {
            mCorrectAngle = false;
        }
        return false;
    });

    /**
     * 计算两点之间的距离 缩放
     */
    private double getDistance(MotionEvent event) {
        int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
        int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));
        return Math.sqrt(xlen * xlen + ylen * ylen);
    }

    /**
     * 用于计算两个两个手指之间形成的直线与x轴的夹角
     *
     * @return
     */
    private int getDeg(MotionEvent event) {
        Point point1 = new Point((int) event.getX(0), (int) event.getY(0));
        Point point2 = new Point((int) event.getX(1), (int) event.getY(1));
        float x = point2.x - point1.x;
        float y = point2.y - point1.y;
        return (int) (Math.atan2(y, x) * 180 / Math.PI);
    }

    /**
     * 偏移
     */
    private float mTranX = 0, mTranY = 0;

    /**
     * 设置偏移距离 相对于左上角原点
     *
     * @param tranX x
     * @param tranY y
     */
    public void setTranX(float tranX, float tranY) {
        mTranX = tranX;
        mTranY = tranY;
    }

    /***
     * 切换镜像
     */
    private void onNextFlip() {
//        if (mFlipType == null || mFlipType == FlipType.FLIP_TYPE_NONE) {
//            //左右镜像
//            mFlipType = FlipType.FLIP_TYPE_HORIZONTAL;
//        } else if (mFlipType == FlipType.FLIP_TYPE_HORIZONTAL) {
//            //上下左右都镜像
//            mFlipType = FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL;
//        } else {
//            //取消所有镜像效果
//            mFlipType = null;
//        }
        fixFlipBmp();
    }

    private void fixFlipBmp() {
//        if (mFlipType == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL || mFlipType == FlipType.FLIP_TYPE_VERTICAL) {
//            mFlipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_matting_lr_dark);
//        } else {
//            mFlipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_matting_lr_dark);
//        }
    }

    private Point rotateCenterPoint;


    /**
     * 设置图片的中心点，
     */
    private void setImageViewParams(Bitmap bm, Point centerPoint, float rotateAngle, float zoomFactor) {
        // 要缩放的原始图片
        mOriginalBitmap = bm;
        // 图片的中心坐标
        mImageCenterPoint = centerPoint;
        // 图片旋转的角度
        mRotateAngle = rotateAngle;
        // 图片缩放系数
        int sbmpW = 100, sbmpH = 50;
        try {
            sbmpW = (int) (mOriginalBitmap.getWidth() * zoomFactor);
            sbmpH = (int) (mOriginalBitmap.getHeight() * zoomFactor);
        } catch (Exception e) {
            sbmpW = 100;
            sbmpH = 100;
        }
        // 计算图片的位置
        calculateImagePosition(0, 0, sbmpW, sbmpH, rotateAngle);

        // 开始构造旋转缩放参数
        mMatrix = new Matrix();

        mMatrix.setScale(zoomFactor, zoomFactor);
        // 设置旋转比例
        mMatrix.postRotate(rotateAngle % 360, sbmpW / 2, sbmpH / 2);
        // 设置移动
        mMatrix.postTranslate(dx + mOutLayoutImageWidth, dy + mOutLayoutImageHeight);

        // 设置小图片的宽高
        setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                (mImageCenterPoint.x - mRotatedImageWidth / 2),
                (mImageCenterPoint.y - mRotatedImageHeight / 2));
    }


    /**
     * 设置图片的宽和高
     *
     * @param w
     * @param h
     * @param l
     * @param t
     */
    private void setImageViewWH(int w, int h, int l, int t) {
        int imageWidth = w + mOutLayoutImageWidth * 2;
        int imageHeight = h + mOutLayoutImageHeight * 2;
        int imageleft = l - mOutLayoutImageWidth;
        int imageTop = t - mOutLayoutImageHeight;

        mImageViewWidth = imageWidth;
        mImageViewHeight = imageHeight;


        mImageViewLeft = imageleft;
        mImageViewTop = imageTop;
        int mbwidth = mOutLayoutImageWidth;
        int mbheight = mOutLayoutImageHeight;
        int mright = mImageViewLeft + mImageViewWidth;
        int mbottom = mImageViewTop + mImageViewHeight;
        if (null != mParentSize) {
            if (mright < mbwidth) {
                mImageViewLeft = mbwidth - mImageViewWidth;
            }
            if (mbottom < mbheight) {
                mImageViewTop = mbottom - mImageViewHeight;
            }
            if (mImageViewLeft > mParentSize.x - mbwidth) {
                mImageViewLeft = mParentSize.x - mbwidth;
            }
            if (mImageViewTop > mParentSize.y - mbheight) {
                mImageViewTop = mParentSize.y - mbheight;
            }

        }
        mImageCenterPoint.x = mImageViewLeft + mImageViewWidth / 2;
        mImageCenterPoint.y = mImageViewTop + mImageViewHeight / 2;
        // 设置图片的布局
        this.layout(mImageViewLeft, mImageViewTop, mImageViewLeft + mImageViewWidth, mImageViewTop + mImageViewHeight);

    }

    private void setCenterPoint(Point c) {
        mImageCenterPoint = c;
        setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                (mImageCenterPoint.x - mRotatedImageWidth / 2),
                (mImageCenterPoint.y - mRotatedImageHeight / 2));

    }


    /**
     * 计算图片的位置
     */
    private void calculateImagePosition(int left, int top, int right,
                                        int bottom, float angle) {
        // 左上角的坐标
        Point p1 = new Point(left, top);

        // 右上角的坐标
        Point p2 = new Point(right, top);

        // 右下角坐标
        Point p3 = new Point(right, bottom);

        // 左下角坐标
        Point p4 = new Point(left, bottom);

        // 需要围绕参考点做旋转
        rotateCenterPoint = new Point((left + right) / 2, (top + bottom) / 2);

        // 旋转之后边框顶点的坐标
        mPoint1 = rotatePoint(rotateCenterPoint, p1, angle);
        mPoint2 = rotatePoint(rotateCenterPoint, p2, angle);
        mPoint3 = rotatePoint(rotateCenterPoint, p3, angle);
        mPoint4 = rotatePoint(rotateCenterPoint, p4, angle);
        int w = 0;
        int h = 0;
        int maxX = mPoint1.x;
        int minX = mPoint1.x;

        // 这是要选出那个坐标点的X坐标最大吗？
        if (mPoint2.x > maxX) {

            maxX = mPoint2.x;
        }

        if (mPoint3.x > maxX) {

            maxX = mPoint3.x;
        }

        if (mPoint4.x > maxX) {

            maxX = mPoint4.x;
        }

        // 这是要选出那个坐标的X坐标最小吗？
        if (mPoint2.x < minX) {
            minX = mPoint2.x;
        }
        if (mPoint3.x < minX) {
            minX = mPoint3.x;
        }

        if (mPoint4.x < minX) {
            minX = mPoint4.x;
        }

        // 计算差值
        w = maxX - minX;

        int maxY = mPoint1.y;
        int minY = mPoint1.y;

        // 选最大的Y坐标
        if (mPoint2.y > maxY) {
            maxY = mPoint2.y;
        }
        if (mPoint3.y > maxY) {
            maxY = mPoint3.y;
        }
        if (mPoint4.y > maxY) {
            maxY = mPoint4.y;
        }

        // 选最小Y坐标
        if (mPoint2.y < minY) {
            minY = mPoint2.y;
        }
        if (mPoint3.y < minY) {
            minY = mPoint3.y;
        }
        if (mPoint4.y < minY) {
            minY = mPoint4.y;
        }

        // 计算差值
        h = maxY - minY;

        // 计算边框的中心坐标
        Point centerPoint = intersects(mPoint4, mPoint2, mPoint1, mPoint3);

        // 这是要计算哪个中心的坐标？
        dx = w / 2 - centerPoint.x;
        dy = h / 2 - centerPoint.y;


        // 加了这么多距离，就相当于向右移动了这么多 的距离
        mPoint1.x = mPoint1.x + dx + mOutLayoutImageWidth;
        mPoint2.x = mPoint2.x + dx + mOutLayoutImageWidth;
        mPoint3.x = mPoint3.x + dx + mOutLayoutImageWidth;
        mPoint4.x = mPoint4.x + dx + mOutLayoutImageWidth;

        // 向下移动了这么多的距离
        mPoint1.y = mPoint1.y + dy + mOutLayoutImageHeight;
        mPoint2.y = mPoint2.y + dy + mOutLayoutImageHeight;
        mPoint3.y = mPoint3.y + dy + mOutLayoutImageHeight;
        mPoint4.y = mPoint4.y + dy + mOutLayoutImageHeight;

        //
        mRotatedImageWidth = w;
        mRotatedImageHeight = h;

        //
        mDeleteCenterPoint = mPoint1;
        mEditCenterPoint = mPoint2;
        mContralImageCenterPoint = mPoint3;
        mCopyImageCenterPoint = mPoint4;
    }

    /**
     * 对角线的交点
     *
     * @param sp3
     * @param sp4
     * @param sp1
     * @param sp2
     * @return
     */
    private Point intersects(Point sp3, Point sp4, Point sp1, Point sp2) {
        Point localPoint = new Point(0, 0);
        double num = (sp4.y - sp3.y) * (sp3.x - sp1.x) - (sp4.x - sp3.x) * (sp3.y - sp1.y);
        double denom = (sp4.y - sp3.y) * (sp2.x - sp1.x) - (sp4.x - sp3.x) * (sp2.y - sp1.y);
        localPoint.x = (int) (sp1.x + (sp2.x - sp1.x) * num / denom);
        localPoint.y = (int) (sp1.y + (sp2.y - sp1.y) * num / denom);
        return localPoint;
    }


    /**
     * 是否点中3个图标， 1点中 镜像图片 ；2点中 control图片  ；3 删除图片； 0 没有点中
     * <p>
     * * 是否点中2个图标， 1点中 delete图片 ；2点中 control图片； 0 没有点中
     */
    private int getClickPosition(int x, int y) {
        int xx = x;
        int yy = y;
        int kk1 = ((xx - mDeleteCenterPoint.x) * (xx - mDeleteCenterPoint.x) + (yy - mDeleteCenterPoint.y) * (yy - mDeleteCenterPoint.y));
        int kk2 = ((xx - mContralImageCenterPoint.x) * (xx - mContralImageCenterPoint.x) + (yy - mContralImageCenterPoint.y) * (yy - mContralImageCenterPoint.y));

        int kk3 = ((xx - mEditCenterPoint.x) * (xx - mEditCenterPoint.x) + (yy - mEditCenterPoint.y) * (yy - mEditCenterPoint.y));
        int kk4 = ((xx - mCopyImageCenterPoint.x) * (xx - mCopyImageCenterPoint.x) + (yy - mCopyImageCenterPoint.y) * (yy - mCopyImageCenterPoint.y));


        int itemSize = 2 * mOutLayoutImageWidth * mOutLayoutImageWidth;

        if (kk1 < itemSize) {
            return IClick.CLICKED_DELETE;
        } else if (kk2 < itemSize) {
            return IClick.CLICKED_CONTROL;
        } else if (kk3 <= itemSize) {
            return IClick.CLICKED_EDIT;
        } else if (kk4 <= itemSize) {
            return IClick.CLICKED_COPY;
        } else {
            return IClick.CLICK_NONE;
        }
    }

    /**
     * 旋转顶点坐标
     *
     * @param rotateCenterPoint 围绕该点进行旋转
     * @param sourcePoint
     * @param angle
     * @return
     */
    private Point rotatePoint(Point rotateCenterPoint, Point sourcePoint, float angle) {

        // 不明白什么意思
        sourcePoint.x = sourcePoint.x - rotateCenterPoint.x;
        sourcePoint.y = sourcePoint.y - rotateCenterPoint.y;

        // 角度a
        double alpha = 0.0;

        // 角度b
        double bate = 0.0;

        Point resultPoint = new Point();

        // 两点之间的距离
        double distance = Math.sqrt(sourcePoint.x * sourcePoint.x
                + sourcePoint.y * sourcePoint.y);

        // 如果在原点
        if (sourcePoint.x == 0 && sourcePoint.y == 0) {

            return rotateCenterPoint;

            // 在第一象限
        } else if (sourcePoint.x >= 0 && sourcePoint.y >= 0) {

            // 计算与X轴正方向的夹角, 用反三角函数，
            alpha = Math.asin(sourcePoint.y / distance);

            // 第二象限
        } else if (sourcePoint.x <= 0 && sourcePoint.y >= 0) {
            // 计算与X轴正方向的夹角, 用反三角函数，
            alpha = Math.asin(Math.abs(sourcePoint.x) / distance);
            alpha = alpha + Math.PI / 2;
            // 第三象限
        } else if (sourcePoint.x <= 0 && sourcePoint.y <= 0) {

            // 计算与x正方向的夹角
            alpha = Math.asin(Math.abs(sourcePoint.y) / distance);
            alpha = alpha + Math.PI;

            // 第四象限
        } else if (sourcePoint.x >= 0 && sourcePoint.y <= 0) {

            // 计算与x正方向的夹角
            alpha = Math.asin(sourcePoint.x / distance);
            alpha = alpha + Math.PI * 3 / 2;
        }

        // 将弧度换算成角度
        alpha = radianToDegree(alpha);

        // 旋转之后的角度
        bate = alpha + angle;

        // 将角度换算成弧度
        bate = degreeToRadian(bate);

        // 计算旋转之后的坐标点
        resultPoint.x = (int) Math.round(distance * Math.cos(bate));
        resultPoint.y = (int) Math.round(distance * Math.sin(bate));

        resultPoint.x += rotateCenterPoint.x;
        resultPoint.y += rotateCenterPoint.y;
        return resultPoint;
    }

    /**
     * 将弧度换算成角度
     */
    private double radianToDegree(double radian) {
        return radian * 180 / Math.PI;
    }

    /**
     * 将角度换算成弧度
     *
     * @param degree
     * @return
     */
    private double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }

    /**
     * 两点的距离
     */
    private double spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }


    public void setOnClickListener(onClickListener _listener) {
        listener = _listener;
    }

    private onClickListener listener;
    private boolean bLock = false; //true 编辑单个图册-滤镜时,屏蔽相关事件

    public void setLock(boolean lock) {
        if (bLock != lock) {
            bLock = lock;
            invalidate();
        }
    }

    public interface onClickListener {

        void onClick(DragMediaView view);
    }


    public void recycle() {
        if (null != mContralBitmap && !mContralBitmap.isRecycled()) {
            mContralBitmap.recycle();
        }
        if (null != mOriginalBitmap && !mOriginalBitmap.isRecycled()) {
            mOriginalBitmap.recycle();
        }
        if (null != mDeleteBitmap && !mDeleteBitmap.isRecycled()) {
            mDeleteBitmap.recycle();
        }
        clearSomeBitmap();
        mHandler.removeCallbacksAndMessages(null);
    }

    private onDelListener onDelListener;

    /**
     * 删除图标的监听
     *
     * @param listener
     */
    public void setDelListener(onDelListener listener) {
        onDelListener = listener;
    }

    public interface onDelListener {
        /**
         * 删除当前
         */
        void onDelete(DragMediaView mediaView);

        void onCopy();

        void onEdit(DragMediaView mediaView);

    }

    public void setTouchListener(OnTouchListener touchListener) {
        mTouchListener = touchListener;
    }

    private OnTouchListener mTouchListener;

    /**
     * 位置改变
     */
    public interface OnTouchListener {


        /***
         * 该点在当前collage 显示区域中的位置
         * @param x 0~1.0f
         * @param y
         */
        void onClick(float x, float y);

        void onRectChange();


        /***
         * 点击了空白区域
         */
        void onExitEdit();


        /**
         * 手势离开
         */
        void onTouchUp();

    }

    /**
     * 获取底图的顶点坐标 （没有旋转角度的顶点）
     *
     * @return 0~1.0f
     */
    public RectF getSrcRectFInPlayer() {
        Rect srcRect = getSrcRect();
        float fw = mParentSize.x - mTranX * 2f; //需排除上下左右间隙
        float fh = mParentSize.y - mTranY * 2f;
        RectF dst = new RectF((srcRect.left - mTranX) / fw, (srcRect.top - mTranY) / fh, (srcRect.right - mTranX) / fw, (srcRect.bottom - mTranY) / fh);
//        Log.e(TAG, "getSrcRectF: " + srcRect + "  " + dst);
        return dst;
    }

    /**
     * 在容器的显示坐标
     */
    public Rect getSrcRect() {
        Rect show = new Rect(getLeft(), getTop(), getRight(), getBottom());
        int angle = getRotateAngle();

//        Log.e(TAG, "getSrcRectF: angle:" + angle + " show:" + show + "  size:" + show.width() + "*" + show.height() + " point :" + mPoint1 + " " + mPoint2 + " " + mPoint3 + " _" + mPoint4);

        Matrix matrix = new Matrix();
        Point center = new Point(show.width() / 2, show.height() / 2);
        matrix.setRotate(-angle, center.x, center.y);
        float[] dst1 = new float[2];
        float[] dst3 = new float[2];


        float[] src1 = new float[]{mPoint1.x, mPoint1.y};
        float[] src3 = new float[]{mPoint3.x, mPoint3.y};


        matrix.mapPoints(dst1, src1);
        matrix.mapPoints(dst3, src3);

        //未旋转时的顶点坐标
        Rect srcRect = new Rect((int) dst1[0], (int) dst1[1], (int) dst3[0], (int) dst3[1]);

        //转化为相对于容器的坐标
        srcRect.offset(show.left, show.top);
        return srcRect;
    }

    public Rect getSrcRect2() {
        Rect show = new Rect(getLeft(), getTop(), getRight(), getBottom());
        int angle = getRotateAngle();

//        Log.e(TAG, "getSrcRectF: angle:" + angle + " show:" + show + "  size:" + show.width() + "*" + show.height() + " point :" + mPoint1 + " " + mPoint2 + " " + mPoint3 + " _" + mPoint4);

        Matrix matrix = new Matrix();
        Point center = new Point(show.width() / 2, show.height() / 2);
        matrix.setRotate(-angle, center.x, center.y);
        float[] dst1 = new float[2];
        float[] dst3 = new float[2];


        float[] src1 = new float[]{mPoint1.x, mPoint1.y};
        float[] src3 = new float[]{mPoint3.x, mPoint3.y};


        matrix.mapPoints(dst1, src1);
        matrix.mapPoints(dst3, src3);

        //未旋转时的顶点坐标
        Rect srcRect = new Rect((int) dst1[0], (int) dst1[1], (int) dst3[0], (int) dst3[1]);

//        //转化为相对于容器的坐标
//        srcRect.offset(show.left, show.top);
//        return srcRect;
        return srcRect;
    }

    public void setType(FlipType flipType) {
//        mFlipType = flipType;
        fixFlipBmp();
        invalidate();
    }

    public void setAngle(int angle) {
        this.mRotateAngle = -angle;
        // 设置图片参数
        setImageViewParams(mOriginalBitmap, mImageCenterPoint, mRotateAngle, disf);
        invalidate();
    }

    /**
     * 手势
     */
    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        //单击确认，即很快的按下并抬起，但并不连续点击第二下
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            PointF pointF = isContains(event.getX(), event.getY());
//            Log.e(TAG, "onSingleTapConfirmed: " + pointF + " " + event.getX() + "*" + event.getY() + " mDefautMode:" + mDefautMode + " bLock:" + bLock + " checkPosition:" + checkPosition);
            if (!bLock && checkPosition == IClick.CLICK_NONE) {
                if (pointF != null) { //点击点在collage显示范围内
                    mTouchListener.onClick(pointF.x, pointF.y);
                } else if (mDefautMode == NONE) { //ontouch.up中 mDefautMode拦截了拖动
                    //轻触空白,响应为退出选中
                    checkExitRect(event.getX(), event.getY());
                }
            }
            return true;
        }
    }
}
