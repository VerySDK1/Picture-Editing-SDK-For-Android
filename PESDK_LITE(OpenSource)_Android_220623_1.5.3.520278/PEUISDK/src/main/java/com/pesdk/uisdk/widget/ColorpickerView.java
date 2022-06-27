package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import androidx.annotation.Nullable;

/**
 *拾色器
 */
public class ColorpickerView extends View {

    private static final int RADIUS = 150;//半径
    private static final int RADIUS_SMALL = 10;//半径
    private static final int CURSOR = 30;//半径

    //需要取色的图片
    private Bitmap originalBitmap;
    //旋转角度
    private float mAngle = 0;
    private float mOffsetX, mOffsetY;
    //选择的颜色
    private int selectedPureColor = -1;
    //当前点的位置
    private float[] mCenter;
    private Point mPoint;
    private Matrix mMatrix;
    //画笔
    private Paint mCursorPaint, mColorPaint, mStrokePaint;
    //是否显示抠图
    private boolean mIsShowZoom = true;

    public ColorpickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorpickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCursorPaint = new Paint();
        mCursorPaint.setAntiAlias(true);
        mCursorPaint.setStrokeWidth(2);
        mCursorPaint.setColor(Color.BLACK);

        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(2);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setColor(Color.WHITE);

        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);

        mCenter = new float[2];
        mPoint = new Point();
        mMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制到中间
        if (originalBitmap != null && mIsShowZoom) {
            //选中颜色
            mColorPaint.setColor(selectedPureColor);
            canvas.drawCircle(mPoint.x, mPoint.y, RADIUS , mColorPaint);
            mColorPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
            canvas.drawCircle(mPoint.x, mPoint.y, RADIUS - 30, mColorPaint);
            mColorPaint.setXfermode(null);
            //描边
            canvas.drawCircle(mPoint.x, mPoint.y, RADIUS , mStrokePaint);
            canvas.drawCircle(mPoint.x, mPoint.y, RADIUS - 30, mStrokePaint);
            //绘制光标
            canvas.drawLine(mPoint.x, mPoint.y - RADIUS_SMALL, mPoint.x, mPoint.y - RADIUS_SMALL - CURSOR, mCursorPaint);
            canvas.drawLine(mPoint.x, mPoint.y + RADIUS_SMALL, mPoint.x, mPoint.y + RADIUS_SMALL + CURSOR, mCursorPaint);
            canvas.drawLine(mPoint.x  + RADIUS_SMALL, mPoint.y, mPoint.x  + RADIUS_SMALL + CURSOR, mPoint.y, mCursorPaint);
            canvas.drawLine(mPoint.x  - RADIUS_SMALL, mPoint.y, mPoint.x  - RADIUS_SMALL - CURSOR, mPoint.y, mCursorPaint);
        }
    }

    /**
     * 判断点是否在多边形内
     * @return
     */
    public boolean isCoordinatePoint(Point point, List<Point> list) {
        double x = point.x;
        double y = point.y;
        int isum, icount, index;
        double dLon1 = 0, dLon2 = 0, dLat1 = 0, dLat2 = 0, dLon;
        if (list.size() < 3) {
            return false;
        }
        isum = 0;
        icount = list.size();
        for (index = 0; index < icount - 1; index++) {
            if (index == icount - 1) {
                dLon1 = list.get(index).x;
                dLat1 = list.get(index).y;
                dLon2 = list.get(0).x;
                dLat2 = list.get(0).y;
            } else {
                dLon1 = list.get(index).x;
                dLat1 = list.get(index).y;
                dLon2 = list.get(index + 1).x;
                dLat2 = list.get(index + 1).y;
            }
            // 判断指定点的 纬度是否在 相邻两个点(不为同一点)的纬度之间
            if (((y >= dLat1) && (y < dLat2)) || ((y >= dLat2) && (y < dLat1))) {
                if (Math.abs(dLat1 - dLat2) > 0) {
                    dLon = dLon1 - ((dLon1 - dLon2) * (dLat1 - y)) / (dLat1 - dLat2);
                    if (dLon < x){
                        isum++;
                    }
                }
            }
        }
        if ((isum % 2) != 0) {
            return true;
        } else {
            return false;
        }
    }

    private int getColorFromBitmap(int x, int y) {
        if (originalBitmap != null) {
            return originalBitmap.getPixel(x, y);
        }
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        //限制 x y 的范围
        mMatrix.reset();
        mMatrix.setRotate(-mAngle, mCenter[0], mCenter[1]);
        float[] p = new float[]{x, y};
        float[] d = new float[2];
        mMatrix.mapPoints(d, p);
        int a = (int) Math.max(0, Math.min(originalBitmap.getWidth() - 1, d[0] - mOffsetX));
        int b = (int) Math.max(0, Math.min(originalBitmap.getHeight() - 1, d[1] - mOffsetY));
        selectedPureColor = getColorFromBitmap(a, b);
        //恢复回去
        a += mOffsetX;
        b += mOffsetY;
        p[0] = a;
        p[1] = b;
        mMatrix.reset();
        mMatrix.setRotate(mAngle, mCenter[0], mCenter[1]);
        mMatrix.mapPoints(d, p);
        //点位置
        mPoint.set((int) d[0], (int) d[1]);
        if (mListener != null) {
            mListener.onColorSelected(selectedPureColor);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mHandler.removeMessages(MSG_HIDE);
            mHandler.sendEmptyMessageDelayed(MSG_HIDE, 1000);
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mHandler.removeMessages(MSG_HIDE);
            mIsShowZoom = true;
        }
        invalidate();
        return true;
    }

    private static final int MSG_HIDE = 45;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == MSG_HIDE) {
                mIsShowZoom = false;
                invalidate();
            }
            return false;
        }
    });

    public void recycler() {
        mHandler.removeCallbacksAndMessages(null);
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.isRecycled();
        }
    }

    /**
     *  图片 旋转角度
     */
    public void setOriginalDrawable(Bitmap bitmap, float angle, RectF rectF, int w, int h) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        this.originalBitmap = Bitmap.createBitmap(bitmap);
        //得到图片的区域
        mCenter[0] = rectF.centerX() * w;
        mCenter[1] = rectF.centerY() * h;
        mPoint.set((int) mCenter[0], (int) mCenter[1]);

        mOffsetX = rectF.left * w;
        mOffsetY = rectF.top * h;

        mAngle = 360 - (angle % 360 + 360) % 360;
        invalidate();
    }

    private ColorListener mListener;

    public void setListener(ColorListener listener) {
        mIsShowZoom = true;
        mListener = listener;
    }

    public interface ColorListener {

        void onColorSelected(int color);

    }

}
