package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.FrameInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.TimeArray;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.HashMap;
import java.util.Map.Entry;

import androidx.core.content.ContextCompat;

/**
 * 单点拖动实现旋转和伸缩
 */

public class SinglePointRotate extends View {

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
    private final float MAX_SCALE = AppConfig.MAX_SCALE;
    private final float MIN_SCALE = AppConfig.MIN_SCALE;

    private final int MSG_CHANG_FRAME = 125;
    private static final int MSG_ANGLE = 126;

    private final int spId = "aatext_sample".hashCode();
    public int shadowColor = 0;
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
    private Matrix mMatrix, tempMatrix;


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
     * 原始图片，需要操作的图片
     */
    private Bitmap mOriginalBitmap;
    /**
     * 原始图片,的备份
     */
    private Bitmap mOriginalBackupBitmap;

    /**
     * 删除图片
     */
    private Bitmap mDeleteBitmap;

    /**
     * 可以控制图片旋转伸缩的图片
     */
    private Bitmap mContralBitmap;

    /**
     * 画刷
     */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 图片中心坐标
     */
    private Point mImageCenterPoint = new Point(0, 0);
    /**
     * 第一次进入
     */
    private boolean bFirstIn;
    private boolean isUserWriting;
    /**
     * 旋转角度
     */
    private float mRotateAngle;

    /**
     * 缩放系数 = mZoomFactor
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
     * 删除图片中心坐标
     */
    private Point mDeleteImageCenterPoint;

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

    private float mTextSize = 28;

    private StyleInfo mStyleInfo;
    private int duration = 1000;// ms
    private int progress = 0, mItemDelayMillis = 0;

    /**
     * 字符串
     */
    private String mText = "";
    private DisplayMetrics display;
    /**
     * 字符串画刷
     */
    private TextPaint mTextPaint = new TextPaint();
    private Point mParentSize;
    private boolean drawControl = false; // 设置是否支持拖拽(编辑模式下、预览模式下的区别)

    /**
     * 是否可以随意拖动
     *
     * @param isControl
     */
    public void setControl(boolean isControl) {
        isRunning = false;
        mhandler.removeCallbacks(runnable);
        drawControl = isControl;
        invalidate();
    }

    private int textcolor;

    private boolean bExport = false;

    public SinglePointRotate(Context context, float mRotate, float mdisf, Point mend, Point center, StyleInfo styleInfo, String bgPath) {
        this(context, mRotate, "", Color.WHITE, "", mdisf, mend, center, 10, Color.WHITE, styleInfo, bgPath, false);

    }

    /**
     * init初始化有完整信息的字幕
     *
     * @param context
     * @param mRotate
     * @param text
     * @param textColor
     * @param ttfLocal
     * @param zoomFactor
     * @param parentSize
     * @param center
     * @param textSize
     * @param shadowColor
     * @param styleInfo
     * @param bgPath
     */
    private SinglePointRotate(Context context, float mRotate, String text,
                              int textColor, String ttfLocal, float zoomFactor, Point parentSize,
                              Point center, int textSize, int shadowColor, StyleInfo styleInfo,
                              String bgPath, boolean export) {
        super(context);
        mContext = context;
        bExport = export;
        display = CoreUtils.getMetrics();
        this.shadowColor = shadowColor;
        this.mParentSize = parentSize;
        this.mRotateAngle = mRotate;
        // 初始化要操作的原始图片， 先将图片引入到工程中
        this.disf = zoomFactor;
        this.textcolor = textColor;
        this.mTextPaint.setTextSize(textSize);
        this.mTextPaint.setAntiAlias(true);
        // 消除锯齿
        this.mPaint.setAntiAlias(true);
        // 初始化删除图片
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.pesdk_ic_drag_delete);
        // 初始化控制图片
        mContralBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.pesdk_ic_drag_controller);
        // 删除图片的半宽
        this.mOutLayoutImageWidth = mDeleteBitmap.getWidth() / 2;
        this.mOutLayoutImageHeight = mDeleteBitmap.getHeight() / 2;
        this.drawFrame(bgPath);
        this.setTTFLocal(ttfLocal, false);
        setCenter(center);
        setStyleInfo(false, styleInfo, 1000, false, disf);
        this.isUserWriting = false;
        setInputText(text);
        this.isUserWriting = true;
        setDisf(zoomFactor);

        // 设置画笔的宽度
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);
        //设置阴影
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mPaint.setShadowLayer(2, 2, 2, ContextCompat.getColor(context, R.color.pesdk_drag_shadow_color));
    }


    public void setCenter(Point center) {
        if (!center.equals(mImageCenterPoint.x, mImageCenterPoint.y)) {
            this.mImageCenterPoint = center;
        }
    }

    public void updateCenter(Point center) {
        this.mImageCenterPoint = center;
        invaView(true);
    }

    public void setFirstIn(boolean bFirstin) {
        bFirstIn = bFirstin;
    }


    public void setRotate(float mRotate) {
        if (mRotateAngle != mRotate) {
            mRotateAngle = mRotate;
        }
    }

    public void setStyleInfo(boolean isMain, StyleInfo si, int nduration, boolean drawFrame, float mdisf) {

        duration = nduration;
        mStyleInfo = si;
        if (!TextUtils.isEmpty(mStyleInfo.tFont)) {
//            String localttf = TTFData.getInstance().quweryOne(mStyleInfo.tFont);
//            if (null != localttf) {
//                setTTFLocal(localttf, false);
//            }
        }
        if (isMain) {
            isRunning = false;
            mhandler.removeCallbacks(runnable);
            textcolor = mStyleInfo.getTextDefaultColor();
            if (mStyleInfo.shadow) {
                this.shadowColor = mStyleInfo.strokeColor;
            } else {
                this.shadowColor = 0;
            }
        }
//        Log.e(TAG, "setStyleInfo: " + disf + ">>" + mdisf);
        disf = mdisf;
        progress = 0;
        if (mStyleInfo.st == CommonStyleUtils.STYPE.special) {
            invaView(drawFrame);
            mItemDelayMillis = mStyleInfo.getFrameDuration();
//            Log.e(TAG, "setStyleInfo: " + mStyleInfo.n +"   "+ bExport);
            if (drawFrame) {
                if (bExport && !TextUtils.isEmpty(mStyleInfo.filterPng)) {
                    mhandler.obtainMessage(MSG_CHANG_FRAME,
                            mStyleInfo.filterPng).sendToTarget();
                } else {
                    mhandler.obtainMessage(MSG_CHANG_FRAME,
                            mStyleInfo.frameArray.valueAt(0).pic).sendToTarget();
                }
            }
        } else if (mStyleInfo.frameArray.size() > 0) {
            if (mStyleInfo.lashen) {
                drawFrame(mStyleInfo.frameArray.valueAt(0).pic);
            } else {
                setImageStyle(mStyleInfo.frameArray.valueAt(0).pic, drawFrame);
            }
        }

    }

    private String TAG = SinglePointRotate.class.getName();
    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            mhandler.removeCallbacks(this);
            progress += mItemDelayMillis;

            if (null != mStyleInfo) {
                int itemTime = mItemDelayMillis;
                int np = (int) (progress);
                int tdu = np;
                int timeArrayCount = mStyleInfo.timeArrays.size();
//                Log.e(TAG, "runnable" + timeArrayCount + "............." + np + "...mStyleInfo.du." + mStyleInfo.du);
                if (timeArrayCount == 2) {
                    int headdu = mStyleInfo.timeArrays.get(0).getDuration();
                    if (np > headdu) { // 循环后面部分
                        TimeArray loopArray = mStyleInfo.timeArrays.get(1);
                        int loopdu = loopArray.getDuration();
                        int j = 0;
                        int item = 0;
                        while (true) {
                            item = np - loopdu * j;
                            if (item <= loopdu) {
                                break;
                            } else {
                                j++;
                            }
                        }
                        tdu = item + headdu;
                        if (tdu >= mStyleInfo.du)
                            tdu = tdu % mStyleInfo.du;
                    }

                } else if (timeArrayCount == 3) {
                    int headdu = mStyleInfo.timeArrays.get(0).getDuration();
                    TimeArray lastArray = mStyleInfo.timeArrays.get(2);
                    if (np > headdu) { // 循环后面部分

                        int mLastP = (int) (duration - lastArray.getDuration());
                        if (np > mLastP) { // 末尾部分
                            int off = np - mLastP;
                            tdu = lastArray.getBegin() + off;
                        } else { // 循环中间部分
                            TimeArray loopArray = mStyleInfo.timeArrays.get(1);
                            int loopdu = loopArray.getDuration();
                            int j = 0;
                            int item = 0;
                            np = np - headdu;
                            while (true) {
                                item = np - loopdu * j;
                                if (item <= loopdu) {
                                    break;
                                } else {
                                    j++;
                                }
                            }
                            tdu = item + headdu;
                        }

                    }
                } else {
                    if (np > mStyleInfo.du) {
                        int j = 1;
                        int item = 0;
                        while (true) {
                            item = np - mStyleInfo.du * j;
                            if (item <= mStyleInfo.du) {
                                break;
                            } else {
                                j++;
                            }
                        }
                        tdu = item;
                    }
                }

                FrameInfo st = CommonStyleUtils.search(tdu, mStyleInfo.getFrameDuration(), mStyleInfo.frameArray, mStyleInfo.timeArrays, true, 0);
                if (null != st) {
                    mhandler.removeMessages(MSG_CHANG_FRAME);
                    if (bExport && !TextUtils.isEmpty(mStyleInfo.filter)) {
                        mhandler.obtainMessage(MSG_CHANG_FRAME, mStyleInfo.filter).sendToTarget();
                    } else {
                        mhandler.obtainMessage(MSG_CHANG_FRAME, st.pic).sendToTarget();
                    }
                }
            }
        }
    };

    public void previewSpecailByUserEdit() {
        progress = 0;
        mhandler.removeMessages(MSG_CHANG_FRAME);
        if (null != mStyleInfo.frameArray && mStyleInfo.frameArray.size() > 0) {
            mhandler.obtainMessage(MSG_CHANG_FRAME, mStyleInfo.frameArray.valueAt(0).pic).sendToTarget();
        }
    }

    private boolean isRunning = false;

    public void onPasue() {
        mhandler.removeCallbacks(runnable);
    }

    public void onResume() {
        if (isRunning) {
            mhandler.removeCallbacks(runnable);
            mhandler.post(runnable);
        }
    }

    private Handler mhandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_CHANG_FRAME:
                    setImageStyle((String) msg.obj, true);
                    mhandler.removeCallbacks(runnable);
                    isRunning = true;
                    if (mStyleInfo.needWhileDraw()) {
                        mhandler.postDelayed(runnable, mItemDelayMillis);
                    }
                    break;
                case MSG_ANGLE:
                    mCorrectAngle = false;
                    break;
                default:
                    break;
            }
        }
    };

    private String lastFramePic = "";

    /**
     * 画单独的一帧的画面
     *
     * @param picPath
     */
    private void drawFrame(String picPath) {
        if (!TextUtils.equals(lastFramePic, picPath)) {
            lastFramePic = picPath;
            if (null != mOriginalBitmap && !mOriginalBitmap.isRecycled()) {
                mOriginalBitmap.recycle();
                mOriginalBitmap = null;
                if (null != mOriginalBackupBitmap
                        && !mOriginalBackupBitmap.isRecycled()) {
                    mOriginalBackupBitmap.recycle();
                    mOriginalBackupBitmap = null;
                }
            }
            if (!TextUtils.isEmpty(picPath)) {
                mOriginalBitmap = BitmapFactory.decodeFile(picPath);
            } else {
                Log.e(TAG, "drawFrame pic is null");
            }
            if (null == mOriginalBitmap) {
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
                        .drawableToBitmap(gd, 400, 60);
            }
            mOriginalBackupBitmap = Bitmap.createBitmap(mOriginalBitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(R.color.red);
//        super.onDraw(canvas);
        mOnDraw(canvas);
    }

    /**
     * 是否只是横向拉升 (部分字幕,第一个字幕、自拍 等）
     *
     * @return
     */
    private boolean isLaShen() {
        return (mStyleInfo.lashen);
    }

    private Point mTemp = new Point(); // 减少获取字体大小
    private HashMap<Long, Bitmap> maps = new HashMap<>(),
            mapWords = new HashMap<>();

    private void mOnDraw(Canvas canvas) {
        clearSomeBitmap();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawColor(Color.CYAN);// 测试用。查看区域
        // 画图片的包围框 ，顺时针画
        if (drawControl) {
            if (mStyleInfo.pid != spId) {
                canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y,
                        mPaint);
                canvas.drawLine(mPoint2.x, mPoint2.y, mPoint3.x, mPoint3.y,
                        mPaint);
                canvas.drawLine(mPoint3.x, mPoint3.y, mPoint4.x, mPoint4.y,
                        mPaint);
                canvas.drawLine(mPoint4.x, mPoint4.y, mPoint1.x, mPoint1.y,
                        mPaint);
            }
        }
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
            Log.e("mOriginalBitmap...", "no has mOriginalBitmap.... ");
            return;
        }
        int bwidth, bheight;
        boolean isLaShen = isLaShen();
        if (isLaShen) {
            bwidth = mOriginalBitmap.getWidth();
            bheight = mOriginalBitmap.getHeight();
        } else {
            //非横向拉升的字幕，以json定义的宽高为准
            bwidth = (int) mStyleInfo.w;
            bheight = (int) mStyleInfo.h;
        }


//        Log.e(TAG, "mOnDraw: " + mStyleInfo.mlocalpath + "   " + bwidth + "*" + bheight + "  >>>" + mStyleInfo.w + "*" + mStyleInfo.h + "  >" + isLaShen + " " +
//                " mOriginalBitmap:" + mOriginalBitmap.getWidth() + "*" + mOriginalBitmap.getHeight());
        if (bwidth <= 0 || bheight <= 0) {
            Log.e(TAG, "mOnDraw:  size  error ：" + bwidth + "*" + bheight + "  mStyleInfo:" + mStyleInfo);
            return;
        }

        Bitmap newb = Bitmap.createBitmap(bwidth, bheight, Config.ARGB_8888);

        Canvas canvasTmp = new Canvas();

        canvasTmp.setBitmap(newb);
        canvasTmp.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        canvasTmp.drawColor(Color.BLUE);
        canvasTmp.drawBitmap(mOriginalBitmap, null, new Rect(0, 0, bwidth, bheight), null);
        RectF rectF = mStyleInfo.getTextRectF();
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

            setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                    (mImageCenterPoint.x - mRotatedImageWidth / 2),
                    (mImageCenterPoint.y - mRotatedImageHeight / 2));

            canvas.drawBitmap(newb, mMatrix, null);
            maps.put(System.currentTimeMillis(), newb);
            if (!TextUtils.isEmpty(mText)) {
                if (!mStyleInfo.lashen) {

                    /**
                     * 新增部分 画text
                     */
                    int mpa = 5;
                    mleft = (int) (mleft * disf) - mpa;
                    mright = (int) (mright * disf) + mpa;
                    mtop = (int) (mtop * disf) - mpa;
                    mbottom = (int) (mbottom * disf) + mpa;
                    int padding = 0;

                    // 左上角的坐标
                    Point mp12 = new Point(padding, padding);

                    // 右上角的坐标
                    Point mp22 = new Point(mright - mleft - padding, padding);

                    int mH = mbottom - mtop - padding;
                    // 右下角坐标
                    Point mp32 = new Point(mp22.x, mH);

                    // 左下角坐标
                    Point mp42 = new Point(padding, mH);

                    // mPaint.setColor(Color.BLACK);
                    int wi = mright - mleft;
                    int mhei = mbottom - mtop;

                    Bitmap mword = Bitmap.createBitmap(wi, mhei,
                            Config.ARGB_8888);

                    Canvas bword = new Canvas();
                    bword.setBitmap(mword);
                    centerPoint = intersects(mp42, mp22, mp12, mp32);

                    int wwidth = mp22.x - mp12.x;
                    int wheight = mp32.y - mp22.y;
                    int topy = mpa + mp12.y;
                    if (mStyleInfo.pid != spId) {

                        if (!mTemp.equals(mp42.x, mp42.y)) {
                            mTemp.set(mp42.x, mp42.y);
                            getTSize(wwidth, wheight);
                        }
                        boolean bStroke = false;
                        if (mStyleInfo.st == CommonStyleUtils.STYPE.special) {
                            if (mStyleInfo.strokeWidth > 0) {// && mStyleInfo.strokeColor !=
                                // textcolor
                                mTextPaint.setColor(mStyleInfo.strokeColor);
                                mTextPaint.setStrokeWidth(mStyleInfo.strokeWidth);
                                mTextPaint.setStyle(Style.FILL_AND_STROKE); // 描边种类
                                mTextPaint.setFakeBoldText(true); // 外层text采用粗体
                                StaticLayout myStaticLayout = new StaticLayout(
                                        mText, mTextPaint, wwidth,
                                        Alignment.ALIGN_CENTER, 1.0f, 0.0f,
                                        false);
                                bword.translate(
                                        0,
                                        (wheight - myStaticLayout.getHeight()) / 2);
                                myStaticLayout.draw(bword);
                                bStroke = true;
                            }
                        } else {
                            if (shadowColor != 0) {
                                {
                                    mTextPaint.setColor(shadowColor);
                                    mTextPaint.setStrokeWidth(2);
                                    mTextPaint.setFakeBoldText(true); // 外层text采用粗体
                                    mTextPaint.setStyle(Style.FILL_AND_STROKE); // 描边种类
                                    StaticLayout myStaticLayout = new StaticLayout(
                                            mText, mTextPaint, wwidth,
                                            Alignment.ALIGN_CENTER, 1.0f, 0.0f,
                                            false);
                                    bword.translate(0,
                                            (wheight - myStaticLayout
                                                    .getHeight()) / 2);
                                    myStaticLayout.draw(bword);
                                    bStroke = true;
                                }
                            }
                        }
                        mTextPaint.setColor(textcolor);
                        mTextPaint.setStrokeWidth(0);
                        mTextPaint.setStyle(Style.FILL); // 描边种类
                        mTextPaint.setFakeBoldText(false); // 外层text采用粗体

                        StaticLayout myStaticLayout = new StaticLayout(mText,
                                mTextPaint, wwidth, Alignment.ALIGN_CENTER,
                                1.0f, 0.0f, false);
                        if (!bStroke) {
                            bword.translate(0,
                                    (wheight - myStaticLayout.getHeight()) / 2);
                        }

                        myStaticLayout.draw(bword);

                    } else {
                        if (!mTemp.equals(mp42.x, mp42.y)) {
                            mTemp.set(mp42.x, mp42.y);
                            mTextSize = drawSingle(mText, mTextPaint, wwidth,
                                    wheight);
                        }
                        mTextPaint.setTextSize(mTextSize);
                        FontMetrics fm = mTextPaint.getFontMetrics();

                        float tHeight = (Math.abs(fm.ascent) + Math
                                .abs(fm.descent));

                        float by = 0.5f + (Math.abs(fm.descent) / tHeight);
                        int baseY = (int) (wheight * by);

                        int centerY = wheight / 2; // 区域的中心点

                        int bCenterY = (int) (baseY + Math.abs(fm.descent) - tHeight / 2); // 计算text在编辑区域的中心点

                        int bx = (int) (centerPoint.x - (mTextPaint
                                .measureText(mText) / 2));
                        int basey = (topy + baseY - (bCenterY - centerY));
                        if (shadowColor != 0) {
                            if (shadowColor != textcolor) {
                                mTextPaint.setColor(shadowColor);
                                mTextPaint.setStrokeWidth(2);
                                mTextPaint.setStyle(Style.FILL_AND_STROKE); // 描边种类
                                mTextPaint.setFakeBoldText(true); // 外层text采用粗体
                                bword.drawText(mText, bx, basey, mTextPaint); //
                                // 向上移动
                            }

                        } else {
                            if (null != mStyleInfo && mStyleInfo.strokeWidth > 0
                                    && mStyleInfo.strokeColor != textcolor) {
                                mTextPaint.setColor(mStyleInfo.strokeColor);
                                mTextPaint.setStrokeWidth(mStyleInfo.strokeWidth);
                                mTextPaint.setStyle(Style.FILL_AND_STROKE); // 描边种类
                                mTextPaint.setFakeBoldText(true); // 外层text采用粗体
                                bword.drawText(mText, bx, basey, mTextPaint); //
                            }

                        }

                        mTextPaint.setColor(textcolor);
                        mTextPaint.setStrokeWidth(0);
                        mTextPaint.setStyle(Style.FILL_AND_STROKE); // 描边种类
                        mTextPaint.setFakeBoldText(false); // 外层text采用粗体
                        bword.drawText(mText, bx, basey, mTextPaint); // 向上移动

                    }

                    bword.save();

                    tempMatrix = new Matrix();

                    tempMatrix.setScale(1f, 1f);

                    // 设置移动
                    tempMatrix.postTranslate(dx + mOutLayoutImageWidth + mleft,
                            dy + mOutLayoutImageHeight + mtop);
                    // 设置旋转比例
                    tempMatrix.postRotate(mRotateAngle % 360, (getWidth() / 2),
                            (getHeight() / 2));

                    canvas.drawBitmap(mword, tempMatrix, mPaint);

                    mapWords.put(System.currentTimeMillis(), mword);
                }
            }

        }

        if (drawControl) { // 只有在调节字幕界面。画控制器
            if (null != mContralBitmap && !mContralBitmap.isRecycled())
                // 画控制图片
                canvas.drawBitmap(mContralBitmap, mContralImageCenterPoint.x
                        - mOutLayoutImageWidth, mContralImageCenterPoint.y
                        - mOutLayoutImageHeight, mPaint);
            if (null != mDeleteBitmap && !mDeleteBitmap.isRecycled())
                canvas.drawBitmap(mDeleteBitmap, mDeleteImageCenterPoint.x
                        - mOutLayoutImageWidth, mDeleteImageCenterPoint.y
                        - mOutLayoutImageHeight, mPaint);
        }

    }


    private void getTSize(int wwidth, int wheight) {
        int temp = (int) (mTextSize + 100);
        StaticLayout myStaticLayout = null;
        while (temp > 3) {
            mTextPaint.setTextSize(temp);
            myStaticLayout = new StaticLayout(mText, mTextPaint, wwidth,
                    Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            if (myStaticLayout.getHeight() > wheight) {
                temp -= 2;
            } else {
                break;
            }
        }
        mTextSize = temp;

    }

    private void clearSomeBitmap() {
        if (maps.size() > 0) {
            for (Entry<Long, Bitmap> item : maps.entrySet()) {
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
        if (mapWords.size() > 0) {
            for (Entry<Long, Bitmap> item : mapWords.entrySet()) {
                Bitmap b = item.getValue();
                if (null != b) {
                    if (!b.isRecycled()) {
                        b.recycle();
                    }
                    b = null;
                }
                mapWords.remove(item.getKey());
            }
            mapWords.clear();
        }

    }

    /**
     * 单行显示
     *
     * @param str
     * @param p
     * @param width
     * @return
     */
    private int drawSingle(String str, Paint p, int width, int height) {
        int i = 150;
        this.mTextPaint.setAntiAlias(true);
        FontMetrics fm;
        while (i > 3) {
            mTextPaint.setTextSize(i);
            fm = mTextPaint.getFontMetrics();

            int theight = (int) (Math.abs(fm.ascent) + Math.abs(fm.descent));
            if ((mTextPaint.measureText(str) + 10 < width && (theight < height))
                    || i <= 3) {
                break;
            } else {
                i -= 3;
            }

        }

        return i;
    }

    /**
     * @param path
     */
    public void save(String path) {
        Bitmap bm = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        mOnDraw(canvas);
        try {
            BitmapUtils.saveBitmapToFile(bm, 100, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bm.recycle();
    }

    public String getText() {
        return mText;
    }

    public float getTextSize() {
        if (null != mTextPaint) {
            try {
                return mTextPaint.getTextSize();
            } catch (Exception e) {
                e.printStackTrace();
                return 28;
            }
        } else
            return 28;
    }

    //是否修正角度
    private boolean mCorrectAngle = false;
    //双指缩放开始之间的距离和与X轴角度
    private double mStartLen;
    private float mStartAngle = 0;
    //是否点击、是否是两个手指
    private boolean mDown = false;
    private boolean mTwoPoint = false;
    //临时记录点下时的角度和倍数
    private float mTempAngle = 0;
    private float mTempDisf = 1.0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawControl) {
            int pointerNum = event.getPointerCount();
            //两个手指  缩放模式
            if (pointerNum == 2) {
                int re = event.getAction();
                if ((re & MotionEvent.ACTION_MASK)
                        == MotionEvent.ACTION_POINTER_DOWN) {
                    //点击
                    mDown = true;
                    mCorrectAngle = false;
                    mTempDisf = disf;
                    mTempAngle = mRotateAngle;
                    mStartLen = getDistance(event);
                    mStartAngle = getDeg(event);
                    mTwoPoint = true;
                } else if ((re & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                        || re == MotionEvent.ACTION_UP || re == MotionEvent.ACTION_CANCEL) {
                    //拿起
                    mDown = false;
                    onRectChanged();
                    return false;
                } else if (re == MotionEvent.ACTION_MOVE) {
                    //移动
                    double endLen = getDistance(event);
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
                                mhandler.removeMessages(MSG_ANGLE);
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
                            mhandler.removeMessages(MSG_ANGLE);
                            mCorrectAngle = false;
                        }
                        //如果角度 相差5  是90整数倍
                        if (!mCorrectAngle && Math.abs(mRotateAngle % 90) >= 5 && Math.abs(mRotateAngle % 90) <= 85
                                && (Math.abs(angle % 90) < 5 || Math.abs(angle % 90) > 85)) {
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
                            mhandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                            mAPoint.set(-1000, -1000);
                            // 设置图片参数
                            setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                    angle, disf);
                        }
                        if (!mCorrectAngle) {
                            // 设置图片参数
                            setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                    angle, disf);
                        }
                    }
                }
            } else {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTwoPoint = false;
                        mCorrectAngle = false;
                        mTempAngle = mRotateAngle;
                        // 先获取点击坐标吗？
                        mAPoint.set(event.getX() + mImageViewLeft, event.getY()
                                + mImageViewTop);
                        // 先判断用户点击的是哪个按钮（图片）, 如果是2，表示要旋转和伸缩图片
                        int checkPosition = getClickPosition((int) event.getX(),
                                (int) event.getY());
                        if (checkPosition == 0) {// onClick(this)

                            if (null != listener) {
                                listener.onClick(this);
                            }
                        }
                        if (drawControl) {
                            if (checkPosition == 1) {
                                if (null != ViewListener) {
                                    ViewListener.onDelete(SinglePointRotate.this);
                                }
                            } else if (checkPosition == 2) {
                                // 设置操作模式为移动缩放模式
                                mDefautMode = ZOOM_ROTATE;
                            } else {
                                // 设置操作模式为拖动模式
                                mDefautMode = DRAG;
                            }
                        } else {
                            return false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mTwoPoint) {
                            // 如果为移动缩放模式
                            mBPoint.set(event.getX() + mImageViewLeft, event.getY()
                                    + mImageViewTop);
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
                                        mhandler.removeMessages(MSG_ANGLE);
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
                                    mhandler.removeMessages(MSG_ANGLE);
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
                                    Utils.onVibrator(mContext);
                                    mCorrectAngle = true;
                                    mhandler.sendEmptyMessageDelayed(MSG_ANGLE, 500);
                                    // 设置图片参数
                                    setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                            mTempAngle, disf);
                                }
                                if (!mCorrectAngle) {
                                    // 设置图片参数
                                    setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                            mTempAngle, disf);
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
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mhandler.removeMessages(MSG_ANGLE);
                        // 设置操作模式为什么都不做
                        mDefautMode = NONE;
                        onRectChanged();
                        break;
                }
            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (null != listener) {
                    listener.onClick(this);
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 用于计算两个两个手指之间形成的直线与x轴的夹角
     *
     * @return
     */
    private float getDeg(MotionEvent event) {
        Point point1 = new Point((int) event.getX(0), (int) event.getY(0));
        Point point2 = new Point((int) event.getX(1), (int) event.getY(1));
        float x = point2.x - point1.x;
        float y = point2.y - point1.y;
        return (float) (Math.atan2(y, x) * 180 / Math.PI);
    }

    /**
     * 计算两点之间的距离 缩放
     */
    private double getDistance(MotionEvent event) {
        int xlen = Math.abs((int) event.getX(event.getPointerId(0)) - (int) event.getX(event.getPointerId(1)));
        int ylen = Math.abs((int) event.getY(event.getPointerId(0)) - (int) event.getY(event.getPointerId(1)));
        return Math.sqrt(xlen * xlen + ylen * ylen);
    }

    public void setOnClickListener(onClickListener _listener) {
        listener = _listener;
    }

    private onClickListener listener;

    public interface onClickListener {

        public void onClick(SinglePointRotate view);
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
        this.layout(mImageViewLeft, mImageViewTop, mImageViewLeft
                + mImageViewWidth, mImageViewTop + mImageViewHeight);

    }

    private void setCenterPoint(Point c) {
        mImageCenterPoint = c;
        setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                (mImageCenterPoint.x - mRotatedImageWidth / 2),
                (mImageCenterPoint.y - mRotatedImageHeight / 2));

    }

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
            if (isLaShen()) {
                sbmpW = (int) (mOriginalBitmap.getWidth() * zoomFactor);
                sbmpH = (int) (mOriginalBitmap.getHeight() * zoomFactor);
            } else {
                sbmpW = (int) (mStyleInfo.w * zoomFactor);
                sbmpH = (int) (mStyleInfo.h * zoomFactor);
            }
//            Log.e(TAG, "setImageViewParams: zoomFactor:" + zoomFactor + " isLaShen:" + isLaShen() + " mStyleInfo:" + mStyleInfo.w + "*" + mStyleInfo.h + " sbmpW:" + sbmpW + "*" + sbmpH);
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

        int tdx = dx + mOutLayoutImageWidth;
        int tdy = dy + mOutLayoutImageHeight;
        mMatrix.postTranslate(tdx, tdy);

//        Log.e(TAG, "setImageViewParams: " + mRotatedImageWidth + "*" + mRotatedImageHeight + " " +
//                " mImageCenterPoint:" + mImageCenterPoint.x + "*" + mImageCenterPoint.y + " trans: " + tdx + "*" + tdy);

        // 设置小图片的宽高
        setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                (mImageCenterPoint.x - mRotatedImageWidth / 2),
                (mImageCenterPoint.y - mRotatedImageHeight / 2));
    }

    private Point rotateCenterPoint;

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
        mDeleteImageCenterPoint = mPoint1;
        mContralImageCenterPoint = mPoint3;
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
        double num = (sp4.y - sp3.y) * (sp3.x - sp1.x) - (sp4.x - sp3.x)
                * (sp3.y - sp1.y);
        double denom = (sp4.y - sp3.y) * (sp2.x - sp1.x) - (sp4.x - sp3.x)
                * (sp2.y - sp1.y);
        localPoint.x = (int) (sp1.x + (sp2.x - sp1.x) * num / denom);
        localPoint.y = (int) (sp1.y + (sp2.y - sp1.y) * num / denom);
        return localPoint;
    }

    /**
     * 是否点中2个图标， 1点中 delete图片 ；2点中 control图片； 0 没有点中
     */
    private int getClickPosition(int x, int y) {
        int xx = x;
        int yy = y;
        int kk1 = ((xx - mDeleteImageCenterPoint.x)
                * (xx - mDeleteImageCenterPoint.x) + (yy - mDeleteImageCenterPoint.y)
                * (yy - mDeleteImageCenterPoint.y));
        int kk2 = ((xx - mContralImageCenterPoint.x)
                * (xx - mContralImageCenterPoint.x) + (yy - mContralImageCenterPoint.y)
                * (yy - mContralImageCenterPoint.y));

        if (kk1 < mOutLayoutImageWidth * mOutLayoutImageWidth) {
            return 1;
        } else if (kk2 < mOutLayoutImageWidth * mOutLayoutImageWidth) {
            return 2;
        }
        return 0;
    }

    /**
     * 旋转顶点坐标
     *
     * @param rotateCenterPoint 围绕该点进行旋转
     * @param sourcePoint
     * @param angle
     * @return
     */
    private Point rotatePoint(Point rotateCenterPoint, Point sourcePoint,
                              float angle) {

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

    /**
     * 设置显示样式
     */
    public void setImageStyle(StyleInfo info) {
        mStyleInfo = info;
        mTextPaint.setColor(mStyleInfo.getTextDefaultColor());
        if (TextUtils.isEmpty(mText)) {
            mText = mStyleInfo.getHint();
        }
        invaView(true);
    }

    /**
     * 设置显示样式
     */
    public void setImageStyle(String picPath, boolean minvalidate) {
        drawFrame(picPath);
        invaView(minvalidate);
    }

    /**
     * 刷新图片信息的配置，缩放比
     */
    private void invaView(boolean minvalidate) {
        setImageViewParams(mOriginalBitmap, mImageCenterPoint, mRotateAngle, disf);
        if (minvalidate)
            invalidate();
    }

    /**
     * 设置放大程度
     *
     * @param mdisf
     */
    public void setDisf(float mdisf) {
        if (mdisf <= MIN_SCALE) {
            mdisf = MIN_SCALE;
        } else if (mdisf >= MAX_SCALE) {
            if (mStyleInfo.pid != spId) {
                mdisf = MAX_SCALE;
            }
        }

        if (disf != mdisf) {
            disf = mdisf;
            invaView(true);
        }
    }

    /**
     * 根据文字组合背景图
     */
    private void setSameParamWithText() {
        setSameParamWithText(false);
    }

    private void setSameParamWithText(boolean isWriting) {
        // 根据屏幕最大宽度获取文字最大绘制宽度 display.widthPixels mDeleteBitmap mContralBitmap
        int newtTop = mStyleInfo.tTop, newtButtom = mStyleInfo.tButtom;
        if (mStyleInfo.tHeight > 40) {
            newtTop = mStyleInfo.tTop + (mStyleInfo.tHeight - 40) / 2;
            newtButtom = mStyleInfo.tButtom
                    + (mStyleInfo.tHeight - 40 - (mStyleInfo.tHeight - 40) / 2);
        }
        int nMaxTextDrawWidth = mParentSize.x - mStyleInfo.tLeft - mStyleInfo.tRight
                - mDeleteBitmap.getWidth() / 2 - mContralBitmap.getWidth() / 2;

        nMaxTextDrawWidth = (int) (nMaxTextDrawWidth / disf);
        // 获取原图宽\高
//        int bwidth = (int) mStyleInfo.w;
//        int bheight = (int) mStyleInfo.h;
        int bwidth = mOriginalBackupBitmap.getWidth();
        int bheight = mOriginalBackupBitmap.getHeight();

//        Log.e(TAG, "setSameParamWithText: " + mOriginalBackupBitmap.getWidth() + "*" + mOriginalBackupBitmap.getHeight());
        // 获取文字原始可写入区域的高度
        int nSourceDrawTextWidth = bwidth - mStyleInfo.tLeft - mStyleInfo.tRight;
        int nSourceDrawTextHeight = bheight - newtTop - newtButtom;
        int nNewDrawTextHeight = 0;
        // 如果可写区域的预留的高度太小，自动加大
        if (nSourceDrawTextHeight < 33) {
            nNewDrawTextHeight = 33;
        }

        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(mTextPaint.getTypeface());
        textPaint.setColor(textcolor);
        textPaint.setStrokeWidth(0);
        textPaint.setStyle(Style.FILL);
        textPaint.setFakeBoldText(false); // 外层text采用粗体
        // 有描边
        if (shadowColor != 0) {
            if (shadowColor != textcolor) {
                textPaint.setColor(shadowColor);
                textPaint.setStyle(Style.STROKE);
                textPaint.setStrokeWidth(mStyleInfo.strokeWidth);
                textPaint.setFakeBoldText(true); // 外层text采用粗体
            }
        }

        // 通过图片预留的文字区域高度，计算合适的字体大小 获取使用的字体后实际的占用高度
        Rect bounds = new Rect();
        getDrawTextSize(textPaint,
                nNewDrawTextHeight == 0 ? nSourceDrawTextHeight
                        : nNewDrawTextHeight, bounds);

        HashMap<String, String> Parma = new HashMap<String, String>();
        int n = 1, start = 0, end = 0;
        for (int i = 1; i <= getText().length(); i++) {
            Rect rt = new Rect();
            textPaint.getTextBounds(getText(), end, i, rt);
            if (rt.width() > nMaxTextDrawWidth) {

                end = i - 1;
                Parma.put("" + n, getText().substring(start, end));
                start = end;
                i--;
                n++;
            }
            if (i == getText().length())
                Parma.put("" + n, getText().substring(start));
        }


        // 从多行文字中找占用最宽的一行
        int newwidth = 0;
        Rect LineRect = new Rect();
        for (int i = 0; i < Parma.size(); i++) {
            String strTempLine = Parma.get(Integer.toString(i + 1));
            if (strTempLine == null)
                strTempLine = "";
            textPaint.getTextBounds(strTempLine, 0, strTempLine.length(),
                    LineRect);
            if (LineRect.width() > newwidth) {
                newwidth = LineRect.width();
            }
        }
        int addWPix = 0;
        int addHPix = 0;
        if (mStyleInfo.onlyone) {
            Rect onlyOneLineRect = new Rect();
            textPaint.getTextBounds(getText(), 0, getText().length(),
                    onlyOneLineRect);
            newwidth = onlyOneLineRect.width();

            addWPix = newwidth > nSourceDrawTextWidth ? newwidth
                    - nSourceDrawTextWidth : 0;
            addHPix = onlyOneLineRect.height() > nSourceDrawTextHeight ? onlyOneLineRect
                    .height() - nSourceDrawTextHeight
                    : 0;

        } else {
            addWPix = newwidth > nSourceDrawTextWidth ? newwidth
                    - nSourceDrawTextWidth : 0;
            addHPix = bounds.height() * n > nSourceDrawTextHeight ? bounds
                    .height() * n - nSourceDrawTextHeight : 0;
        }


        // 创建一个原图
        Bitmap newb = Bitmap.createBitmap(bwidth, bheight, Config.ARGB_8888);
        Canvas canvasTmp = new Canvas();
//        canvasTmp.drawColor(Color.BLUE);
        canvasTmp.setBitmap(newb);
        canvasTmp.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        canvasTmp.drawBitmap(mOriginalBackupBitmap, new Rect(0, 0, mOriginalBackupBitmap.getWidth(), mOriginalBackupBitmap.getHeight()), new Rect(0, 0, bwidth, bheight), null);
//        canvasTmp.drawBitmap(mOriginalBackupBitmap, 0, 0, null);

        // 把原图按从走到右从上倒下依次分割成9张小图，用于拉伸 分割的的1 3 7 9 小图分别处于四个边角，不需要拉伸处理
        Bitmap bitmap1 = Bitmap.createBitmap(newb, 0, 0, (int) (mStyleInfo.left),
                (int) (mStyleInfo.top));
        Bitmap bitmap2 = Bitmap.createBitmap(newb, (int) (mStyleInfo.left), 0, bwidth
                - (int) (mStyleInfo.left) - (int) (mStyleInfo.right), (int) (mStyleInfo.top));
        Bitmap bitmap3 = Bitmap.createBitmap(newb, bwidth - (int) (mStyleInfo.right),
                0, (int) (mStyleInfo.right), (int) (mStyleInfo.top));
        Bitmap bitmap4 = Bitmap.createBitmap(newb, 0, (int) (mStyleInfo.top),
                (int) (mStyleInfo.left), bheight - (int) (mStyleInfo.top)
                        - (int) (mStyleInfo.buttom));
        Bitmap bitmap5 = Bitmap.createBitmap(newb, (int) (mStyleInfo.left),
                (int) (mStyleInfo.top), bwidth - (int) (mStyleInfo.left) - (int) (mStyleInfo.right),
                bheight - (int) (mStyleInfo.top) - (int) (mStyleInfo.buttom));
        Bitmap bitmap6 = Bitmap.createBitmap(newb, bwidth - (int) (mStyleInfo.right),
                (int) (mStyleInfo.top), (int) (mStyleInfo.right), bheight - (int) (mStyleInfo.top)
                        - (int) (mStyleInfo.buttom));
        Bitmap bitmap7 = Bitmap.createBitmap(newb, 0, bheight
                - (int) (mStyleInfo.buttom), (int) (mStyleInfo.left), (int) (mStyleInfo.buttom));
        Bitmap bitmap8 = Bitmap.createBitmap(newb, (int) (mStyleInfo.left), bheight
                - (int) (mStyleInfo.buttom), bwidth - (int) (mStyleInfo.left)
                - (int) (mStyleInfo.right), (int) (mStyleInfo.buttom));
        Bitmap bitmap9 = Bitmap.createBitmap(newb, bwidth - (int) (mStyleInfo.right),
                bheight - (int) (mStyleInfo.buttom), (int) (mStyleInfo.right),
                (int) (mStyleInfo.buttom));
        // 释放canvasTmp 和 newb
        canvasTmp = null;
        newb.recycle();

        // 新计算bitmap2
        int wnum = (bitmap2.getWidth() + addWPix) / bitmap2.getWidth() + 1;
        int hnum = (bitmap2.getHeight()) / bitmap2.getHeight();
        int width = bitmap2.getWidth() + addWPix;
        int height = bitmap2.getHeight();
        Bitmap newbitmap2 = Bitmap
                .createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas2 = new Canvas(newbitmap2);
        for (int i = 0; i < wnum; i++) {
            canvas2.drawBitmap(bitmap2, i * bitmap2.getWidth(), 0, null);
        }
        canvas2 = null;
        bitmap2.recycle();
        bitmap2 = newbitmap2;// 新计算bitmap2

        // 新计算bitmap4
        wnum = (bitmap4.getWidth()) / bitmap4.getWidth();
        hnum = (bitmap4.getHeight() + addHPix) / bitmap4.getHeight() + 1;
        width = bitmap4.getWidth();
        height = bitmap4.getHeight() + addHPix;
        Bitmap newbitmap4 = Bitmap
                .createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas4 = new Canvas(newbitmap4);
        for (int i = 0; i < wnum; i++) {
            for (int j = 0; j < hnum; j++) {
                canvas4.drawBitmap(bitmap4, i * bitmap4.getWidth(),
                        j * bitmap4.getHeight(), null);
            }
        }
        canvas4 = null;
        bitmap4.recycle();
        bitmap4 = newbitmap4;// 新计算bitmap4

        // 新计算bitmap6
        wnum = (bitmap6.getWidth()) / bitmap6.getWidth() + 1;
        hnum = (bitmap6.getHeight() + addHPix) / bitmap6.getHeight() + 1;
        width = bitmap6.getWidth();
        height = bitmap6.getHeight() + addHPix;
        Bitmap newbitmap6 = Bitmap
                .createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas6 = new Canvas(newbitmap6);
        for (int i = 0; i < wnum; i++) {
            for (int j = 0; j < hnum; j++) {
                canvas6.drawBitmap(bitmap6, i * bitmap6.getWidth(),
                        j * bitmap6.getHeight(), null);
            }
        }
        canvas6 = null;
        bitmap6.recycle();
        bitmap6 = newbitmap6;// 新计算bitmap6

        // 新计算bitmap5
        wnum = (bitmap5.getWidth() + addWPix) / bitmap5.getWidth() + 1;
        hnum = (bitmap5.getHeight() + addHPix) / bitmap5.getHeight() + 1;
        width = bitmap5.getWidth() + addWPix;
        height = bitmap5.getHeight() + addHPix;
        Bitmap newbitmap5 = Bitmap
                .createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas5 = new Canvas(newbitmap5);
        for (int i = 0; i < wnum; i++) {
            for (int j = 0; j < hnum; j++) {
                canvas5.drawBitmap(bitmap5, i * bitmap5.getWidth(),
                        j * bitmap5.getHeight(), null);
            }
        }
        canvas5 = null;
        bitmap5.recycle();
        bitmap5 = newbitmap5;// 新计算bitmap5

        // 新计算bitmap8
        wnum = (bitmap8.getWidth() + addWPix) / bitmap8.getWidth() + 1;
        hnum = (bitmap8.getHeight()) / bitmap8.getHeight();
        width = bitmap8.getWidth() + addWPix;
        height = bitmap8.getHeight();
        Bitmap newbitmap8 = Bitmap
                .createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas8 = new Canvas(newbitmap8);
        for (int i = 0; i < wnum; i++) {
            canvas8.drawBitmap(bitmap8, i * bitmap8.getWidth(), 0, null);
        }
        canvas8 = null;
        bitmap8.recycle();
        bitmap8 = newbitmap8;// 新计算bitmap8

        // 9张组合成一张新的背景Bitmap
        int newWidth = bitmap1.getWidth() + bitmap2.getWidth()
                + bitmap3.getWidth();
        int newHeight = bitmap1.getHeight() + bitmap4.getHeight()
                + bitmap7.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight,
                Config.ARGB_8888);
        Canvas newcanvas = new Canvas(newBitmap);

        {
            newcanvas.drawBitmap(bitmap5, bitmap1.getWidth(),
                    bitmap1.getHeight(), null);
            newcanvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null);
            newcanvas.drawBitmap(bitmap4, 0, bitmap1.getHeight(), null);
            newcanvas.drawBitmap(bitmap6,
                    bitmap1.getWidth() + bitmap2.getWidth(),
                    bitmap1.getHeight(), null);
            newcanvas.drawBitmap(bitmap8, bitmap1.getWidth(),
                    bitmap1.getHeight() + bitmap4.getHeight(), null);
            newcanvas.drawBitmap(bitmap1, 0, 0, null);
            newcanvas.drawBitmap(bitmap3,
                    bitmap1.getWidth() + bitmap2.getWidth(), 0, null);
            newcanvas.drawBitmap(bitmap7, 0,
                    bitmap1.getHeight() + bitmap4.getHeight(), null);
            newcanvas.drawBitmap(bitmap9,
                    bitmap1.getWidth() + bitmap2.getWidth(),
                    bitmap1.getHeight() + bitmap4.getHeight(), null);
        }

        // 绘制文字
        newWidth = newBitmap.getWidth();
        newHeight = newBitmap.getHeight();
        // 文字可绘制的矩形区域
        Rect TextRect = new Rect();
        TextRect.left = 0 + mStyleInfo.tLeft;
        TextRect.top = 0 + newtTop;
        TextRect.right = newWidth - mStyleInfo.tRight;
        TextRect.bottom = newHeight - newtButtom;
        // 最后加上文字的Bitmap


        Canvas TextCanvas = new Canvas(newBitmap);
        TextCanvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));


        FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = TextRect.top + offY * 2 + 4;

        if (mStyleInfo.onlyone) {

            String strTempLine = getText();
            if (strTempLine == null)
                strTempLine = "";
            textPaint.getTextBounds(strTempLine, 0, strTempLine.length(),
                    LineRect);
            int moveLeft = (newwidth > nSourceDrawTextWidth ? newwidth
                    : nSourceDrawTextWidth) / 2 - LineRect.width() / 2;

            // 有描边
            if (shadowColor != 0) {
                if (shadowColor != textcolor) {
                    textPaint.setColor(shadowColor);
                    textPaint.setStrokeWidth(mStyleInfo.strokeWidth);
                    textPaint.setStyle(Style.STROKE);
                    textPaint.setFakeBoldText(true); // 外层text采用粗体
                    TextCanvas.drawText(strTempLine, TextRect.left + moveLeft,
                            newY, textPaint);
                    // TextCanvas.drawText(strTempLine, TextRect.left, newY,
                    // textPaint);
                }
            }
            textPaint.setColor(textcolor);
            textPaint.setStrokeWidth(0);
            textPaint.setStyle(Style.FILL);
            textPaint.setFakeBoldText(false); // 外层text采用粗体
            TextCanvas.drawText(strTempLine, TextRect.left + moveLeft, newY,
                    textPaint);
            // TextCanvas.drawText(strTempLine, TextRect.left, newY, textPaint);
        } else {
            for (int i = 0; i < Parma.size(); i++) {
                String strTempLine = Parma.get(Integer.toString(i + 1));
                if (strTempLine == null)
                    strTempLine = "";
                textPaint.getTextBounds(strTempLine, 0, strTempLine.length(),
                        LineRect);
                int moveLeft = (newwidth > nSourceDrawTextWidth ? newwidth
                        : nSourceDrawTextWidth) / 2 - LineRect.width() / 2;

                // 有描边
                if (shadowColor != 0) {
                    if (shadowColor != textcolor) {
                        textPaint.setColor(shadowColor);
                        textPaint.setStrokeWidth(mStyleInfo.strokeWidth);
                        textPaint.setStyle(Style.STROKE);
                        textPaint.setFakeBoldText(true); // 外层text采用粗体
                        TextCanvas.drawText(strTempLine, TextRect.left
                                        + moveLeft, newY + i * bounds.height(),
                                textPaint);
                    }
                }
                textPaint.setColor(textcolor);
                textPaint.setStrokeWidth(0);
                textPaint.setStyle(Style.FILL);
                textPaint.setFakeBoldText(false); // 外层text采用粗体
                TextCanvas.drawText(strTempLine, TextRect.left + moveLeft, newY
                        + i * bounds.height(), textPaint);
            }
        }
        newcanvas = null;
        TextCanvas = null;

        mOriginalBitmap = Bitmap.createBitmap(newBitmap);
        bitmap1.recycle();
        bitmap2.recycle();
        bitmap3.recycle();
        bitmap4.recycle();
        bitmap5.recycle();
        bitmap6.recycle();
        bitmap7.recycle();
        bitmap8.recycle();
        bitmap9.recycle();
        // textBitmap.removeSelf();
        newBitmap.recycle();

        calculateImagePosition(0, 0, ((int) (newWidth * disf)),
                ((int) (newHeight * disf)), mRotateAngle);
        if (bFirstIn) {
            // 计算中心点
            mImageCenterPoint.y = (int) (this.mParentSize.y * mStyleInfo.centerxy[1]);
            mImageCenterPoint.x = (int) (this.mParentSize.x * mStyleInfo.centerxy[0] + mRotatedImageWidth / 2);
            bFirstIn = false;
        }
        if (isWriting)// 1.用户自己手动改动文字的时候2.用户切换不同字幕选项的时候 isWriting为true
        {
            int nMark = 5;
            if (mRotatedImageWidth / 2 > mImageCenterPoint.x)// 左边超出范围
            {
                mImageCenterPoint.x = mRotatedImageWidth / 2;
            }
            if (mRotatedImageHeight / 2 > mImageCenterPoint.y)// 上边超出范围
            {
                mImageCenterPoint.y = mRotatedImageHeight / 2 + nMark;
            }
            if (!mStyleInfo.onlyone)// 多行
            {
                if ((mImageCenterPoint.x + mRotatedImageWidth / 2) > this.mParentSize.x)// 右边超出范围
                {
                    mImageCenterPoint.x = this.mParentSize.x - mRotatedImageWidth / 2
                            - nMark;
                }
                if ((mImageCenterPoint.y + mRotatedImageHeight / 2) > this.mParentSize.y)// 下边超出范围
                {
                    mImageCenterPoint.y = this.mParentSize.y - mRotatedImageHeight / 2
                            - nMark;
                }
            } else// 单行
            {
                if ((mImageCenterPoint.x - mRotatedImageWidth / 2) > this.mParentSize.x)// 右边超出范围
                {
                    // if (mRotatedImageWidth/2 > this.mParentSize.x)
                    // mImageCenterPoint.x = mRotatedImageWidth/2-nMark;
                    // if (mRotatedImageHeight/2 > this.mParentSize.y)
                    // mImageCenterPoint.y = mRotatedImageHeight/2-nMark;
                }
            }
        }

        setImageViewParams(mOriginalBitmap, mImageCenterPoint, mRotateAngle,
                disf);
    }

    private float getDrawTextSize(TextPaint textPaint, int nDrawTextHeight,
                                  Rect bounds) {
        float temp = 0;
        int nTestDrawTextHeight = 0;
        String strTest = "旁j";
        while (true) {
            textPaint.setTextSize(temp);
            // Rect bounds = new Rect();
            textPaint.getTextBounds(strTest, 0, strTest.length(), bounds);
            nTestDrawTextHeight = bounds.height();
            if (nTestDrawTextHeight > nDrawTextHeight) {
                temp--;
                break;
            } else {
                temp++;
            }
        }
        textPaint.setTextSize(temp);
        textPaint.getTextBounds(strTest, 0, strTest.length(), bounds);
        mTextSize = temp;
        return temp;
    }

    /**
     * 设置显示的字符串
     *
     * @param text
     */
    public void setInputText(String text) {
        this.mText = text;
        mTemp.set(0, 0);
        if (mStyleInfo.pid == spId) {
            mTextPaint.setTextSize(24f);
            int mwidth = (int) mTextPaint.measureText(text);

            int mRectWidth = (int) (mOriginalBitmap.getWidth() * mStyleInfo.getTextRectF().width()); // 原始图片可填充字幕的区域的宽度

            int mscaWidth = (int) (mRectWidth * disf);

            float targetDisf = disf;

            int twidth = mwidth + 50;

            if (mscaWidth <= twidth) {
                // 放大disf
                float canMaxDisf = (float) ((display.widthPixels - 100.0) / mRectWidth);
                int targetWidth = mscaWidth;
                while (targetWidth < twidth) {
                    targetDisf += 0.2f;
                    if (targetDisf >= canMaxDisf) {
                        targetDisf = canMaxDisf;
                        break;
                    }
                    targetWidth = (int) (mRectWidth * targetDisf);

                }
                setDisf(targetDisf);
            }
        } else if (mStyleInfo.lashen) {
            setSameParamWithText(isUserWriting);
        }


        this.invalidate();

    }

    /**
     * 设置字符串颜色
     *
     * @param textColor
     */
    public void setInputTextColor(int textColor) {
        this.textcolor = textColor;
        if (mStyleInfo.lashen) {
            setSameParamWithText();
        }
        this.invalidate();
    }

    /**
     * 设置字体的shadow
     *
     * @param shadow
     */
    public void setShadowColor(int shadow) {
        // 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)
        setShadowColor(shadow, true);
    }

    private void setShadowColor(int shadow, boolean minvalidate) {
        // 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)

        if (shadowColor != shadow) {
            shadowColor = shadow;
        }
        if (mStyleInfo.lashen) {
            setSameParamWithText();
        }
        if (minvalidate)
            invalidate();
    }

    public float getRotateAngle() {
        return mRotateAngle;
    }

    private String ttfLocal = null;

    /**
     * 设置字体样式。楷体，宋体
     *
     * @param mttfLocal
     * @param postInvalidateEnable
     */

    private void setTTFLocal(String mttfLocal, boolean postInvalidateEnable) {
        mTemp.set(0, 0);
        try {
            this.ttfLocal = mttfLocal;
            Typeface tf = null;
//            Typeface tf = TTFUtils.gettfs(mttfLocal);
            if (null != tf) {
                this.mTextPaint.setTypeface(tf);
            } else {
                this.ttfLocal = null;
                this.mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }

            if (postInvalidateEnable) {
                if (mStyleInfo.lashen) {
                    setSameParamWithText();
                }
                this.invalidate();
            }
        } catch (Exception e) {
            this.ttfLocal = null;
            this.setDefualtTtf(postInvalidateEnable);
            e.printStackTrace();
        }
    }

    public void setTTFLocal(String mttfLocal) {
        setTTFLocal(mttfLocal, true);
    }

    /**
     * 系统默认的字体
     *
     * @param postInvalidateEnable
     */
    public void setDefualtTtf(boolean postInvalidateEnable) {

        this.ttfLocal = null;
        this.mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT,
                Typeface.NORMAL));
        if (postInvalidateEnable) {
            if (mStyleInfo.lashen) {
                setSameParamWithText();
            }
            this.invalidate();
        }

    }

    public String getTTFlocal() {
        return ttfLocal;
    }

    /**
     * 获取字体颜色
     *
     * @return
     */
    public int getTextColor() {
        return textcolor;
    }

    public Point getCenter() {
        return mImageCenterPoint;
    }

    public float getDisf() {
        return disf;
    }


    public int getShadowColor() {
        return shadowColor;
    }

    /**
     * 释放资源
     */
    public void recycle() {
        isRunning = false;
        mhandler.removeCallbacks(runnable);
        recycleCommonBmps();
        if (null != mTextPaint) {
            mTextPaint.reset();
        }
        mhandler.removeCallbacksAndMessages(null);

    }

    /**
     * 释放自动手动公共的bmp
     */
    private void recycleCommonBmps() {
        clearSomeBitmap();
    }

    private ViewListener ViewListener;

    /**
     * 删除图标的监听
     *
     * @param listener
     */
    public void setViewListener(ViewListener listener) {
        ViewListener = listener;
    }

    public interface ViewListener {
        /**
         * 删除当前字幕
         */
        void onDelete(SinglePointRotate single);

        /**
         * 位置发生改变
         */
        void onRectChanged(SinglePointRotate single);
    }

    private void onRectChanged() {
        if (null != ViewListener) {
            mhandler.postDelayed(() -> ViewListener.onRectChanged(this), 150);

        }
    }


    /**
     * 获取未旋转时，图片的显示位置  （当前角度矩阵信息，逆向旋转同大小的角度，再获取4个顶点的坐标）
     */

    public RectF getOriginalRect() {
//        Log.e(TAG, "getOriginalRect: " + this + "   src:" + mPoint1 + "  " + mPoint2 + " " + mPoint3 + "  >" + mPoint4);

        Matrix tmp = createMatrix();
        float[] arr1 = new float[2];
        float[] arr2 = new float[2];
        float[] arr3 = new float[2];
        float[] arr4 = new float[2];
        tmp.mapPoints(arr1, new float[]{mPoint1.x, mPoint1.y});
        tmp.mapPoints(arr2, new float[]{mPoint2.x, mPoint2.y});
        tmp.mapPoints(arr3, new float[]{mPoint3.x, mPoint3.y});
        tmp.mapPoints(arr4, new float[]{mPoint4.x, mPoint4.y});
//        Log.e(TAG, "getOriginalRect: " + Arrays.toString(arr1) + "  " + Arrays.toString(arr2) + "  " + Arrays.toString(arr3) + "  >" + Arrays.toString(arr4));
        RectF rect = new RectF(arr1[0], arr1[1], arr3[0], arr3[1]);

        float centerX = getLeft() + getWidth() / 2;
        float centerY = getTop() + getHeight() / 2;
        tmp.reset();

        float halfW = rect.width() / 2.0f;
        float halfH = rect.height() / 2.0f;
        return new RectF((int) (centerX - halfW), (int) (centerY - halfH), (int) (centerX + halfW), (int) (centerY + halfH));
    }

    private Matrix createMatrix() {
        Matrix tempMatrix = new Matrix();
        tempMatrix.setScale(1f, 1f);
        // 设置逆向的旋转角度
        tempMatrix.postRotate(-(mRotateAngle % 360), (getWidth() / 2), (getHeight() / 2));
        return tempMatrix;
    }
}
