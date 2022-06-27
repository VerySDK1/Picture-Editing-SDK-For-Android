package com.pesdk.uisdk.widget.doodle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.pesdk.uisdk.widget.BaseSizeView;
import com.pesdk.uisdk.widget.doodle.bean.DrawGraphBean;
import com.pesdk.uisdk.widget.doodle.bean.DrawPathBean;
import com.pesdk.uisdk.widget.doodle.bean.IPaint;
import com.pesdk.uisdk.widget.doodle.bean.Mode;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 自定义View.使用Canvas、Paint等来实现图片编辑功能（包括普通涂鸦、画圆、画矩形、画箭头、写字）
 */
public class DoodleView extends BaseSizeView {
    private static final String TAG = "DoodleView";


    public enum GRAPH_TYPE {
        //矩形、圆、三角形
        RECT, OVAL, ARROW, LINE, DIRECT_LINE
    }


    public interface DoodleCallback {
        void onDrawStart();

        void onDrawing();


        /**
         * 手势离开时
         *
         * @param hand
         */
        void onDrawComplete(boolean hand);

        void onRevertStateChanged(boolean canRevert);

    }

    private DoodleCallback mCallBack;

    private int mViewWidth, mViewHeight;


    /**
     * 判断手指移动距离，是否画了有效图形的区域
     */
    private float mGraphValidRange = CoreUtils.dpToPixel(6);

    /**
     * 点击选择图形，扩大的相应的有效范围
     */
    private float mGraphValidClickRange = CoreUtils.dpToPixel(8);
    /**
     * 可拖动的点的半径
     */
    private float mDotRadius = CoreUtils.dpToPixel(8);

    /**
     * 暂时的涂鸦画笔
     */
    private Paint mTempPaint;
    /**
     * 暂时的涂鸦路径
     */
    private Path mTempPath;

    /**
     * 暂时的马赛克路径
     */
    private Path mTempMosaicPath;

    private Paint mTempMosaicPaint;

    /**
     * 暂时的图形实例，用来move时实时画路径
     */
    private DrawGraphBean mTempGraphBean;

    private Paint mMosaicPaint;
    /**
     * 画笔的颜色
     */
    private int mPaintColor = Color.RED;
    private int mAlpha = 255;


    private Paint mBitmapPaint;
    /**
     * 框住图形的path的画笔
     */
    private Paint mGraphRectPaint;
    private Paint mDotPaint;

    private Bitmap mMoasicBitmap;
    private Bitmap mOriginBitmap;

    private Mode mMode = Mode.NONE;

    private GRAPH_TYPE mCurrentGraphType = GRAPH_TYPE.LINE;


    /**
     * 是否可编辑
     */
    private boolean mIsEditable = false;

//    /**
//     * 矩形|箭头....
//     */
//    private ArrayList<DrawGraphBean> mGraphPathList = new ArrayList<>();
//    /**
//     * 涂鸦的路径
//     */
//    private ArrayList<DrawPathBean> mDoodlePathList = new ArrayList<>();
//    /**
//     * 马赛克路径
//     */
//    private ArrayList<DrawPathBean> mMosaicPathList = new ArrayList<>();

    private ArrayList<Object> revokeList = new ArrayList<>();  //用来替换上面的3个集合
    private ArrayList<Object> undoList = new ArrayList<>();  //记录撤销之后可还原的item

//    //撤销之后记录可还原的效果
//    /**
//     * 涂鸦的路径
//     */
//    private ArrayList<DrawPathBean> mUndoDoodlePathList = new ArrayList<>();
//    /**
//     * 马赛克路径
//     */
//    private ArrayList<DrawPathBean> mUndoMosaicPathList = new ArrayList<>();


    /**
     * 图形的当前操作模式
     */
    private Mode mGraphMode = Mode.NONE;
    /**
     * 当前选中的图形
     */
    private DrawGraphBean mCurrentGraphBean;
    /**
     * 是否点击到图形了
     */
    private boolean mIsClickOnGraph = false;

    private float mStartX, mStartY;
    private float mMoveX, mMoveY;

    /**
     * 区分点击和滑动
     */
    private float mDelaX, mDelaY;


    public DoodleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMode(mMode);
    }

    public DoodleView setCallBack(DoodleCallback callBack) {
        this.mCallBack = callBack;
        return this;
    }


    public boolean isEmpty() {
        return revokeList.isEmpty();
    }

    private Paint mTestPaint;


    private void initTestPaint() {
        if (null == mTestPaint) {
            mTestPaint = new Paint();
        } else {
            mTestPaint.reset();
        }
        mTestPaint.setAntiAlias(true);
        mTestPaint.setStyle(Paint.Style.FILL);
        mTestPaint.setColor(mPaintColor);
        mTestPaint.setAlpha(mAlpha);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawImp(canvas);
    }


    private void drawImp(Canvas canvas) {
        if (mViewWidth <= 0 || mViewHeight <= 0) {
            return;
        }

        // 画原始图
        if (mOriginBitmap != null) {
            canvas.drawBitmap(mOriginBitmap, 0, 0, mBitmapPaint);
        }

        if (bPaintSizeMode) {
            initTestPaint();
            drawTestPaint(canvas, mTestPaint);
        } else {
            {//保证绘制顺序
                int len = revokeList.size();
                for (int i = 0; i < len; i++) {
                    Object obj = revokeList.get(i);
                    if (obj instanceof DrawPathBean) { //绘制涂鸦
                        DrawPathBean pathBean = (DrawPathBean) obj;
                        if (Mode.DOODLE_MODE == pathBean.mode) {
                            canvas.drawPath(pathBean.path, pathBean.paint);
                        }
                    } else if (obj instanceof DrawGraphBean) { //绘制图形(矩形|箭头)
                        DrawGraphBean graphBean = (DrawGraphBean) obj;
                        if (graphBean.isPass) {
                            drawGraph(canvas, graphBean);
                        }
                    }
                }

                if (mTempPath != null && mTempPaint != null) {
                    canvas.drawPath(mTempPath, mTempPaint);
                }
                if (mTempGraphBean != null) {
                    drawGraph(canvas, mTempGraphBean);
                }
            }
        }
    }


    /**
     * 画某个图形
     *
     * @param canvas    canvas
     * @param graphBean graphBean
     */
    private void drawGraph(Canvas canvas, DrawGraphBean graphBean) {
        if (graphBean.isPass) {
            if (graphBean.type == GRAPH_TYPE.RECT) {
                // 矩形
                graphBean.paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(graphBean.startX, graphBean.startY, graphBean.endX, graphBean.endY, graphBean.paint);
            } else if (graphBean.type == GRAPH_TYPE.OVAL) {
                // 椭圆
                graphBean.paint.setStyle(Paint.Style.STROKE);
                canvas.drawOval(new RectF(graphBean.startX, graphBean.startY, graphBean.endX, graphBean.endY), graphBean.paint);
            } else if (graphBean.type == GRAPH_TYPE.ARROW) {
                // 箭头
                graphBean.paint.setStyle(Paint.Style.FILL);
                drawArrow(graphBean.startX, graphBean.startY, graphBean.endX, graphBean.endY, canvas, graphBean.paint, graphBean.nTriangleWidth);
            } else if (graphBean.type == GRAPH_TYPE.LINE) {
                // 直线
                graphBean.paint.setStyle(Paint.Style.FILL);
                canvas.drawLine(graphBean.startX, graphBean.startY, graphBean.endX, graphBean.endY, graphBean.paint);
            } else if (graphBean.type == GRAPH_TYPE.DIRECT_LINE) {
                // 垂直或水平的直线
                graphBean.paint.setStyle(Paint.Style.FILL);
                canvas.drawLine(graphBean.startX, graphBean.startY, graphBean.endX, graphBean.endY, graphBean.paint);
            }
        }
    }

    /**
     * 设置原始的截图
     *
     * @param originBitmap drawable
     */
    public void setOriginBitmap(@NonNull Bitmap originBitmap) {
        mOriginBitmap = originBitmap;
        initOriginBitmap();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mViewWidth = w;
            mViewHeight = h;

            initOriginBitmap();
        }
    }

    private void initOriginBitmap() {
        if (mOriginBitmap != null && mViewHeight > 0 && mViewWidth > 0) {
            mOriginBitmap = Bitmap.createScaledBitmap(mOriginBitmap, mViewWidth, mViewHeight, true);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

            mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMosaicPaint.setFilterBitmap(false);
            mMosaicPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            makeMosaicBitmap();

            mGraphRectPaint = new Paint();
            mGraphRectPaint.setAntiAlias(true);
            mGraphRectPaint.setColor(Color.RED);
            mGraphRectPaint.setStyle(Paint.Style.STROKE);
            mGraphRectPaint.setStrokeWidth(1);
            mGraphRectPaint.setStrokeCap(Paint.Cap.ROUND);
            mGraphRectPaint.setStrokeJoin(Paint.Join.ROUND);
            // 3.5实线，2.5空白
            mGraphRectPaint.setPathEffect(new DashPathEffect(new float[]{CoreUtils.dpToPixel(3.5f), CoreUtils.dpToPixel(2.5f)}, 0));

            mDotPaint = new Paint();
            mDotPaint.setAntiAlias(true);
            mDotPaint.setColor(Color.RED);
            mDotPaint.setStyle(Paint.Style.FILL);
            mDotPaint.setStrokeCap(Paint.Cap.ROUND);
            mDotPaint.setStrokeJoin(Paint.Join.ROUND);

            postInvalidate();
        }
    }


    private Paint setModePaint(Mode mode, int mAlpha, int mPaintColor, float mPaintWidth) {
        return Helper.initPaint(mode, mAlpha, mPaintColor, mPaintWidth);
    }

    private boolean hashGraphPath() {
        for (Object obj : revokeList) {
            if (obj instanceof DrawGraphBean) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            return false;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    /**
     * 切换未双指组件时，取消本次touch
     */
    public void cancleTouch() {
        mTempPath = null;
        mTempGraphBean = null;
        mTempMosaicPath = null;
        postInvalidate();
        if (null != mCallBack) {
            mCallBack.onDrawComplete(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.e(TAG, "onTouchEvent: " + event.getPointerCount() + " >" + event.getAction());
        bPaintSizeMode = false;
        if (!mIsEditable) {
            return super.onTouchEvent(event);
        }
        mMoveX = event.getX();
        mMoveY = event.getY();
        int action = event.getAction();
        int tmp = action & MotionEvent.ACTION_MASK;
        if (tmp == MotionEvent.ACTION_DOWN) {
//            Log.e(TAG, "onTouchEvent: downxxxxxxxxxxxxxxxxxxxxxx");
            mStartX = mMoveX;
            mStartY = mMoveY;
            mDelaX = 0;
            mDelaY = 0;
            // 正常的画图操作
            if (!mIsClickOnGraph) {
                touchDownNormalPath();
            } else if (mCurrentGraphBean != null && hashGraphPath()) {
                touchDownInitGraphOperate();
            }
            if (mCallBack != null) {
                mCallBack.onDrawStart();
            }
            return true;
        } else if (tmp == MotionEvent.ACTION_MOVE) {
//            Log.e(TAG, "onTouchEvent: ACTION_MOVE----------------->");
            mDelaX += Math.abs(mMoveX - mStartX);
            mDelaY += Math.abs(mMoveY - mStartY);
            if (!mIsClickOnGraph) {
                touchMoveNormalDraw();
            } else if (mCurrentGraphBean != null && hashGraphPath()) {
                touchMoveGraphOperate();
            }

            if (mCallBack != null) {
                mCallBack.onDrawing();
            }
            postInvalidate();
            return true;
        } else if (tmp == MotionEvent.ACTION_UP) {
//        } else if (tmp == MotionEvent.ACTION_UP || tmp == MotionEvent.ACTION_CANCEL) {
            // 非点击，正常Up
            if (!mIsClickOnGraph) {
                if (mMode == Mode.DOODLE_MODE) {
                    // 把path加到队列中
                    DrawPathBean pathBean = new DrawPathBean(mTempPath, mTempPaint, new IPaint(mPaintColor, mAlpha, mPaintWidth), Mode.DOODLE_MODE);
                    revokeList.add(pathBean);
                } else if (mMode == Mode.MOSAIC_MODE) {
                    // 把path加到队列中
                    DrawPathBean pathBean = new DrawPathBean(mTempMosaicPath, mTempMosaicPaint, new IPaint(mPaintColor, mAlpha, mPaintWidth), Mode.MOSAIC_MODE);
                    revokeList.add(pathBean);
                } else if (mMode == Mode.GRAPH_MODE) {
                    // 如果是图形，画完之后，可以立即编辑当前图形
                    revokeList.add(mTempGraphBean);
                }
                mTempPath = null;
                mTempPaint = null;
                mTempMosaicPath = null;
                mTempMosaicPaint = null;
                mTempGraphBean = null;
            }

            if (mIsClickOnGraph && mCurrentGraphBean != null) {
                mCurrentGraphBean.startPoint.x = mCurrentGraphBean.startX;
                mCurrentGraphBean.startPoint.y = mCurrentGraphBean.startY;
                mCurrentGraphBean.endPoint.x = mCurrentGraphBean.endX;
                mCurrentGraphBean.endPoint.y = mCurrentGraphBean.endY;
                mGraphMode = Mode.DRAG;
            } else {
                mGraphMode = Mode.NONE;
                mCurrentGraphBean = null;
            }

            if (mCallBack != null) {
                mCallBack.onDrawComplete(true);
                mCallBack.onRevertStateChanged(getCurrentPathSize(mMode));
            } else {
                postInvalidate();
            }
            return true;
        }
        return super.onTouchEvent(event);

    }

    /**
     * 按下时，初始化绘图参数
     */
    private void touchDownNormalPath() {
        if (mMode == Mode.GRAPH_MODE) {
            mTempPaint = setModePaint(mMode, mAlpha, mPaintColor, mPaintWidth);
            // 创建一个对象
            mTempGraphBean = new DrawGraphBean(mStartX, mStartY, mStartX, mStartY, mCurrentGraphType, mTempPaint, mPaintWidth, mAlpha);
        } else if (mMode == Mode.DOODLE_MODE) {
            // 设置对应mode的画笔
            mTempPaint = setModePaint(mMode, mAlpha, mPaintColor, mPaintWidth);
            mTempPath = new Path();
            mTempPath.moveTo(mStartX, mStartY);
        } else if (mMode == Mode.MOSAIC_MODE) {
            mTempMosaicPaint = setModePaint(mMode, mAlpha, mPaintColor, mPaintWidth);
            mTempMosaicPath = new Path();
            mTempMosaicPath.moveTo(mStartX, mStartY);
        }
    }

    /**
     * 按下时，初始化正在操作的图形
     */
    private void touchDownInitGraphOperate() {
        // 此时，在操作某个图形
        mCurrentGraphBean.rectFList.add(new RectF(mCurrentGraphBean.startPoint.x, mCurrentGraphBean.startPoint.y,
                mCurrentGraphBean.endPoint.x, mCurrentGraphBean.endPoint.y));
        mCurrentGraphBean.clickPoint.set(mMoveX, mMoveY);
        // 判断是否点击到了起点或者终点
        RectF startDotRect = new RectF(mCurrentGraphBean.startX - mDotRadius, mCurrentGraphBean.startY - mDotRadius,
                mCurrentGraphBean.startX + mDotRadius, mCurrentGraphBean.startY + mDotRadius);
        RectF endDotRect = new RectF(mCurrentGraphBean.endX - mDotRadius, mCurrentGraphBean.endY - mDotRadius,
                mCurrentGraphBean.endX + mDotRadius, mCurrentGraphBean.endY + mDotRadius);
        if (startDotRect.contains(mMoveX, mMoveY)) {
            mGraphMode = Mode.DRAG_START;
        } else if (endDotRect.contains(mMoveX, mMoveY)) {
            mGraphMode = Mode.DRAG_END;
        } else {
            mGraphMode = Mode.DRAG;
        }
    }

    /**
     * 移动时，绘制路径或者图形
     */
    private void touchMoveNormalDraw() {
        // 使用队列中最后一条path进行操作
        if (mMode == Mode.DOODLE_MODE) {
            mTempPath.lineTo(mMoveX, mMoveY);
        } else if (mMode == Mode.MOSAIC_MODE) {
            mTempMosaicPath.lineTo(mMoveX, mMoveY);
        } else if (mMode == Mode.GRAPH_MODE) {
            // 只操作暂时的图形对象
            if (mTempGraphBean != null) {
                // 只有移动了足够距离，才算合格的图形
                if (mDelaX > mGraphValidRange || mDelaY > mGraphValidRange) {
                    mTempGraphBean.isPass = true;
                    if (mTempGraphBean.type == GRAPH_TYPE.DIRECT_LINE) {
                        // 由于笔直的线的特殊性，需要特殊处理
                        float[] point = getDirectLineEndPoint(mTempGraphBean.startX, mTempGraphBean.startY, mMoveX, mMoveY);
                        mTempGraphBean.endX = point[0];
                        mTempGraphBean.endY = point[1];
                        mTempGraphBean.endPoint.x = point[0];
                        mTempGraphBean.endPoint.y = point[1];
                        // 此时的rectList应该只有一条数据
                        if (mTempGraphBean.rectFList.size() == 1) {
                            // 它对应的rect要么是水平的，要么是垂直的
                            RectF rectF = mTempGraphBean.rectFList.get(0);
                            if (mTempGraphBean.startY == mTempGraphBean.endY) {
                                // 水平的直线
                                rectF.left = mTempGraphBean.startX;
                                rectF.top = mTempGraphBean.startY - mDotRadius;
                                rectF.right = mTempGraphBean.endX;
                                rectF.bottom = mTempGraphBean.startY + mDotRadius;
                            } else {
                                // 垂直的直线
                                rectF.left = mTempGraphBean.startX - mDotRadius;
                                rectF.top = mTempGraphBean.startY;
                                rectF.right = mTempGraphBean.startX + mDotRadius;
                                rectF.bottom = mTempGraphBean.endY;
                            }
                        }
                    } else {
                        mTempGraphBean.endX = mMoveX;
                        mTempGraphBean.endY = mMoveY;
                        mTempGraphBean.endPoint.x = mMoveX;
                        mTempGraphBean.endPoint.y = mMoveY;
                        // 此时的rectList应该只有一条数据
                        if (mTempGraphBean.rectFList.size() == 1) {
                            mTempGraphBean.rectFList.get(0).right = mMoveX;
                            mTempGraphBean.rectFList.get(0).bottom = mMoveY;
                        }
                    }
                }
            }
        }
    }

    /**
     * 移动图形，包括缩放等
     */
    private void touchMoveGraphOperate() {
        if (mCurrentGraphBean != null) {
            float dx = mMoveX - mCurrentGraphBean.clickPoint.x;
            float dy = mMoveY - mCurrentGraphBean.clickPoint.y;
            // 如果是拖拽模式
            changeGraphRect(dx, dy);
        }
    }

    /**
     * 拖拽缩放图形的操作
     *
     * @param offsetX x偏移量
     * @param offsetY y偏移量
     */
    private void changeGraphRect(float offsetX, float offsetY) {
        if (mCurrentGraphBean != null) {
            int rectSize = mCurrentGraphBean.rectFList.size();
            if (rectSize > 0) {
                RectF tempRectF = mCurrentGraphBean.rectFList.get((rectSize - 1));
                if (mGraphMode == Mode.DRAG) {
                    mCurrentGraphBean.startX = mCurrentGraphBean.startPoint.x + offsetX;
                    mCurrentGraphBean.startY = mCurrentGraphBean.startPoint.y + offsetY;
                    mCurrentGraphBean.endX = mCurrentGraphBean.endPoint.x + offsetX;
                    mCurrentGraphBean.endY = mCurrentGraphBean.endPoint.y + offsetY;
                } else if (mGraphMode == Mode.DRAG_START) {
                    // 如果是拖动起始点
                    // 只需要变化起始点的坐标即可
                    if (mCurrentGraphBean.type == GRAPH_TYPE.DIRECT_LINE) {
                        // 如果是笔直线，只在该对应方向进行平移
                        if (mCurrentGraphBean.startX == mCurrentGraphBean.endX) {
                            // 垂直的直线,x不变，只变化y
                            mCurrentGraphBean.startY = mCurrentGraphBean.startPoint.y + offsetY;
                        } else {
                            // 水平的直线，y不变
                            mCurrentGraphBean.startX = mCurrentGraphBean.startPoint.x + offsetX;
                        }
                    } else {
                        mCurrentGraphBean.startX = mCurrentGraphBean.startPoint.x + offsetX;
                        mCurrentGraphBean.startY = mCurrentGraphBean.startPoint.y + offsetY;
                    }
                    Log.d(TAG, "拖动起始点");
                } else if (mGraphMode == Mode.DRAG_END) {
                    // 如果是拖动终点
                    // 只需要变化终点的坐标即可
                    if (mCurrentGraphBean.type == GRAPH_TYPE.DIRECT_LINE) {
                        // 如果是笔直线，只在该对应方向进行平移
                        if (mCurrentGraphBean.startX == mCurrentGraphBean.endX) {
                            // 垂直的直线,x不变，只变化y
                            mCurrentGraphBean.endY = mCurrentGraphBean.endPoint.y + offsetY;
                        } else {
                            // 水平的直线，y不变
                            mCurrentGraphBean.endX = mCurrentGraphBean.endPoint.x + offsetX;
                        }
                    } else {
                        mCurrentGraphBean.endX = mCurrentGraphBean.endPoint.x + offsetX;
                        mCurrentGraphBean.endY = mCurrentGraphBean.endPoint.y + offsetY;
                    }
                    Log.d(TAG, "拖动终点");
                }
                // 更新围绕的rect
                if (mCurrentGraphBean.type == GRAPH_TYPE.DIRECT_LINE) {
                    if (mCurrentGraphBean.startX == mCurrentGraphBean.endX) {
                        // 垂直的直线
                        tempRectF.left = mCurrentGraphBean.startX - mDotRadius;
                        tempRectF.top = mCurrentGraphBean.startY;
                        tempRectF.right = mCurrentGraphBean.startX + mDotRadius;
                        tempRectF.bottom = mCurrentGraphBean.endY;
                    } else {
                        // 水平的直线
                        tempRectF.left = mCurrentGraphBean.startX;
                        tempRectF.top = mCurrentGraphBean.startY - mDotRadius;
                        tempRectF.right = mCurrentGraphBean.endX;
                        tempRectF.bottom = mCurrentGraphBean.startY + mDotRadius;
                    }
                } else {
                    tempRectF.left = mCurrentGraphBean.startX;
                    tempRectF.top = mCurrentGraphBean.startY;
                    tempRectF.right = mCurrentGraphBean.endX;
                    tempRectF.bottom = mCurrentGraphBean.endY;
                }
                Log.d(TAG, "拖动图形rect");
            }
        }
    }

    /**
     * 撤销操作
     *
     * @return 撤销后剩余可以撤销的步骤
     */
    public void revoke() {
        // 撤销只针对当前模式的撤销，不是所有步骤的撤销
        if (revokeList.size() <= 0) {
            return;
        }
        Object tmp = revokeList.remove(revokeList.size() - 1);
        undoList.add(tmp);
        postInvalidate();

    }


    /**
     * 还原
     */
    public void undo() {
        if (undoList.size() > 0) {
            Object tmp = undoList.remove(undoList.size() - 1);
            revokeList.add(tmp);
            postInvalidate();
        }
    }

    public int getUndoSize() {
        return undoList.size();
    }

    public int getRevokeSize() {
        return revokeList.size();
    }

    /**
     * 获取指定模式下，是否可撤销
     *
     * @param mode mode
     * @return boolean
     */
    public boolean getCurrentPathSize(Mode mode) {
        return revokeList.size() > 0;
    }


    /**
     * 清楚正在操作的图形的焦点
     */
    public void clearGraphFocus() {
        mCurrentGraphBean = null;
        mIsClickOnGraph = false;
        mGraphMode = Mode.NONE;
        postInvalidate();
    }

    /**
     * 获取马赛克的bitmap
     */
    private Bitmap makeMosaicBitmap() {
        if (mMoasicBitmap != null) {
            return mMoasicBitmap;
        }

        int w = Math.round(mViewWidth / 16f);
        int h = Math.round(mViewHeight / 16f);

        if (mOriginBitmap != null) {
            // 先创建小图
            mMoasicBitmap = Bitmap.createScaledBitmap(mOriginBitmap, w, h, false);
            // 再把小图放大
            mMoasicBitmap = Bitmap.createScaledBitmap(mMoasicBitmap, mViewWidth, mViewHeight, false);
        }
        return mMoasicBitmap;
    }

    /**
     * 计算两点对应的角度
     *
     * @return float
     */
    public float getRotation(float startX, float startY, float endX, float endY) {
        float deltaX = startX - endX;
        float deltaY = startY - endY;
        // 计算坐标点相对于圆点所对应的弧度
        double radians = Math.atan2(deltaY, deltaX);
        // 把弧度转换成角度
        return (float) Math.toDegrees(radians);
    }

    /**
     * 设置所要画图形的种类
     *
     * @param graphType graphType
     */
    public void setGraphType(GRAPH_TYPE graphType) {
        // 设置模式前，先把焦点给clear一下
        clearGraphFocus();
        this.mMode = Mode.GRAPH_MODE;
        this.mCurrentGraphType = graphType;
        if (mCallBack != null) {
            mCallBack.onRevertStateChanged(getCurrentPathSize(mMode));
        }
    }

    private final float MAX_SCALE = 2.0f; //标记三角形结束点,箭头的等腰顶点 2X

    private final float SCALE_MIN_SCALE = 0.6f;

    /**
     * 画箭头
     */
    private void drawArrow(float sx, float sy, float ex, float ey, Canvas canvas, Paint paint, float triangleWidth) {
        float nWidth = triangleWidth;
        int nTriangleHeight = 60;
        float x = ex - sx;
        float y = ey - sy;
        double r = Math.sqrt(x * x + y * y);
        float zx = (float) (ex - (nTriangleHeight * x / r));
        float zy = (float) (ey - (nTriangleHeight * y / r));


        float xz = zx - sx;
        float yz = zy - sy;
        double zr = Math.sqrt(xz * xz + yz * yz);

        final int Tmp = 1;

        float mx = (float) (sx + (Tmp * x / r));
        float my = (float) (sy + (Tmp * y / r));

        float x_m = mx - sx;
        float y_m = my - sy;
        double mr = Math.sqrt(x_m * x_m + y_m * y_m);


        Path triangle = new Path();
        triangle.moveTo(sx, sy);
        triangle.lineTo((float) (mx + SCALE_MIN_SCALE * nWidth * y_m / mr), (float) (my - SCALE_MIN_SCALE * nWidth * x_m / mr));


        triangle.lineTo((float) (zx + nWidth * yz / zr), (float) (zy - nWidth * xz / zr));


        triangle.lineTo((float) (zx + nWidth * MAX_SCALE * yz / zr), (float) (zy - nWidth * MAX_SCALE * xz / zr));
        triangle.lineTo(ex, ey);
        triangle.lineTo((float) (zx - nWidth * MAX_SCALE * yz / zr), (float) (zy + nWidth * MAX_SCALE * xz / zr));
        triangle.lineTo((float) (zx - nWidth * yz / zr), (float) (zy + nWidth * xz / zr));


        triangle.lineTo((float) (mx - SCALE_MIN_SCALE * nWidth * y_m / mr), (float) (my + SCALE_MIN_SCALE * nWidth * x_m / mr));

        triangle.close();
        canvas.drawPath(triangle, paint);
    }

    /**
     * 画笔直笔直的直线
     */
    private float[] getDirectLineEndPoint(float sx, float sy, float ex, float ey) {
        float degrees = getRotation(sx, sy, ex, ey);
        float[] point = new float[2];
        // 根据角度画直线
        if ((-45 <= degrees && degrees <= 45) || degrees >= 135 || degrees <= -135) {
            // 往x轴的方向绘制，即y值不变
            point[0] = ex;
            point[1] = sy;
        } else {
            // 往y轴的方向绘制，即x值不变
            point[0] = sx;
            point[1] = ey;
        }
        return point;
    }

    /**
     * 设置画笔的颜色
     *
     * @param color 颜色
     */
    public void setPaintColor(int color) {
        this.mPaintColor = color;
        if (mTempPaint != null) {
            mTempPaint.setColor(mPaintColor);
        }
    }

    /**
     * 透明度
     *
     * @param alpha 0~1f   0 透明 ; 1 不透明
     */
    public void setAlpha(float alpha) {
        mAlpha = (int) (alpha * 255);
        if (mTempPaint != null) {
            mTempPaint.setAlpha(mAlpha);
        }
    }


    /**
     * 画笔宽度
     *
     * @param paintWidth
     */
    @Override
    public void setPaintWidth(float paintWidth) {
        super.setPaintWidth(paintWidth);
        if (mTempPaint != null) {
            mTempPaint.setStrokeWidth(paintWidth);
        }
        invalidate();
    }


    /**
     * 设置是否可编辑
     *
     * @param editable 能否编辑
     */
    public void setEditable(boolean editable) {
        this.mIsEditable = editable;
    }

    public void setMode(Mode mode) {
        // 设置模式前，先把焦点给clear一下
        clearGraphFocus();
        this.mMode = mode;
        if (mCallBack != null) {
            mCallBack.onRevertStateChanged(getCurrentPathSize(mMode));
        }
    }


    @Override
    public void recycle() {
        if (mOriginBitmap != null && !mOriginBitmap.isRecycled()) {
            mOriginBitmap.recycle();
            mOriginBitmap = null;
        }
        if (mMoasicBitmap != null && !mMoasicBitmap.isRecycled()) {
            mMoasicBitmap.recycle();
            mMoasicBitmap = null;
        }
        mCallBack = null;
        reset();
    }

    public void reset() {
        revokeList.clear();
        undoList.clear();
    }

    public Bitmap getBitmap() {
        //创建一个与该View相同大小的缓存区
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();//创建一个新的画布
        //在cacheCanvas上绘制cacheBitmap
        canvas.setBitmap(bitmap);
        drawImp(canvas);
        return bitmap;
    }

    private final int MHALF_SIZE = 200;//展示当前200x200方形内的内容

    public Rect getClipRect() {
        return getClipRect(mMoveX, mMoveY);
    }

    private Rect getClipRect(float x, float y) {
        int left = (int) Math.min(Math.max(0, x - MHALF_SIZE), getWidth() - MHALF_SIZE * 2);
        int top = (int) Math.min(Math.max(0, y - MHALF_SIZE), getHeight() - MHALF_SIZE * 2);
        return new Rect(left, top, left + MHALF_SIZE * 2, top + MHALF_SIZE * 2);
    }


    public void save(String filePath) {
        Bitmap cacheBitmap = getBitmap();
        try {
            BitmapUtils.saveBitmapToFile(cacheBitmap, true, 100, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
