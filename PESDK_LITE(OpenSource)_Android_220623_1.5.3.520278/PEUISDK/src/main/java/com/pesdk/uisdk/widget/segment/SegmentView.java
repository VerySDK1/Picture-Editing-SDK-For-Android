package com.pesdk.uisdk.widget.segment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.pesdk.uisdk.analyzer.ExtraPreviewFrameListener;
import com.pesdk.uisdk.widget.BaseSizeView;
import com.pesdk.uisdk.widget.doodle.Helper;
import com.pesdk.uisdk.widget.doodle.bean.IPaint;
import com.pesdk.uisdk.widget.doodle.bean.Mode;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.MediaObject;
import com.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * 手动抠图组件
 */
public class SegmentView extends BaseSizeView {

    private static final String TAG = "SegmentView";
    private int nMaskColor;
    private final int nAlpha = 128; //预览时mask颜色值
    private final int nRed = 151;
    private final int nGreen = 00;
    private final int nBlue = 255;

    public SegmentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        nMaskColor = Color.argb(nAlpha, nRed, nGreen, nBlue);
        mMaskPaint = initPaint(nMaskColor);
        mRubberPaint = initRubberPaint();


        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(2);
    }

    private Bitmap mSrc;

    public void setBaseMedia(String baseMedia) {
        ThreadPoolUtils.executeEx(() -> {
            try {
                mSrc = MiscUtils.getBitmapByMedia(new MediaObject(baseMedia), 1080);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        });
    }


    public void setBaseMediaFile(String baseMedia) {
        ThreadPoolUtils.executeEx(() -> {
            mSrc = BitmapFactory.decodeFile(baseMedia);
        });
    }


    public void setMask(Bitmap mask) {
        initPreviewMask(mask);
        postInvalidate();
    }


    /**
     * 将bitmap中的某种颜色值替换成新的颜色
     */
    private void initPreviewMask(Bitmap bmp) {
        Bitmap previewMask = bmp.copy(Bitmap.Config.ARGB_8888, true);
        //循环获得bitmap所有像素点
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //获得Bitmap 图片中每一个点的color颜色值
                //将需要填充的颜色值如果不是
                //在这说明一下 如果color 是全透明 或者全黑 返回值为 0
                //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000
                //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了
                int color = bmp.getPixel(j, i);
                //将颜色值存在一个数组中 方便后面修改
                if (color != 0) { //非纯透明
                    if (-1 != color) { //非纯白
                        int alpha = (color & 0xff000000) >>> 24;
                        {
//                            https://www.tpyyes.com/tool/rgb.html
//                            https://blog.csdn.net/qq_31332467/article/details/74838617
//                        int red = (color & 0x00ff0000) >> 16;
//                        int green = (color & 0x0000ff00) >> 8;
//                        int blue = (color & 0x000000ff);
//                        Log.e(TAG, "replaceBitmapColor: (" + j + "*" + i + ") " + color + "  _-----  " + alpha + "  _" + red + "," + green + "," + blue);
//                            9700ff
                            //nMaskColor 透明值为128
//                            151,0,255
                        }
                        previewMask.setPixel(j, i, Color.argb(alpha * nAlpha / 255, nRed, nGreen, nBlue));  //将白色替换成透明色
                    } else {
                        previewMask.setPixel(j, i, nMaskColor);  //将白色替换成透明色
                    }
                }
            }
        }
        mRevokeList.add(new DrawPathBean(bmp, previewMask));
    }

    /**
     * mask画笔
     */
    private Paint initPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);//抗锯齿
        paint.setDither(true);//消除拉动，使画面圓滑
        paint.setStyle(Paint.Style.STROKE);
        //设置画笔为空心，否则会是首尾连起来多边形内一块为透明。
        paint.setStrokeJoin(Paint.Join.ROUND); //结合方式，平滑
        paint.setStrokeCap(Paint.Cap.ROUND);  //圆头
        paint.setStrokeWidth(mPaintWidth);//设置空心边框宽
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        return paint;
    }

    private Paint initRubberPaint() {
        Paint paint = initPaint(Color.WHITE);
        paint.setAlpha(0);  //设置透明度为0
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        return paint;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (bPaintSizeMode) {
            initTestPaint();
            drawTestPaint(canvas, mTestPaint);
        } else {
            drawImp(canvas, nMaskColor, false, false, enableShowRevokeList);
        }
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
        mTestPaint.setAlpha(100);
        mTestPaint.setColor(nMaskColor);
    }

    private Path mPath, mRubberPath;
    private float preX = 0, preY = 0;


    /**
     * 获取完整大图再裁剪显示，可优化性能
     */
    private Bitmap createBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bitmap);
        if (isIDCard) {
            cv.drawColor(nMaskColor);
        }
        cv.drawBitmap(mSrc, null, new Rect(0, 0, getWidth(), getHeight()), null); //底图
        drawImp(cv, nMaskColor, false, false, true);
        return bitmap;
    }


    private final int MHALF_SIZE = 150;//展示当前300x300方形内的内容

    private Rect getClipRect(float x, float y) {
        int left = (int) Math.min(Math.max(0, x - MHALF_SIZE), getWidth() - MHALF_SIZE * 2);
        int top = (int) Math.min(Math.max(0, y - MHALF_SIZE), getHeight() - MHALF_SIZE * 2);
        return new Rect(left, top, left + MHALF_SIZE * 2, top + MHALF_SIZE * 2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mEnableRubber) {
                    mRubberPath = new Path();
                    mRubberPath.moveTo(event.getX(), event.getY());
                } else {
                    if (null == mPath) {
                        mPath = new Path();
                    }
                    mPath.moveTo(event.getX(), event.getY());
                }
                preX = event.getX();
                preY = event.getY();
                mCallback.startTouch(createBitmap(), getClipRect(preX, preY));
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mEnableRubber) {
                    mRubberPath.quadTo(preX, preY, event.getX(), event.getY());
                } else {
                    mPath.quadTo(preX, preY, event.getX(), event.getY());
                }
                preX = event.getX();
                preY = event.getY();
                mCallback.moveTouch(createBitmap(), getClipRect(preX, preY));
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mEnableRubber) {
                    mRevokeList.add(new DrawPathBean(mRubberPath, true, initRubberPaint(), new IPaint(nMaskColor, 255, mPaintWidth), Mode.DOODLE_MODE));
                    mRubberPath = null;
                } else {
                    mRevokeList.add(new DrawPathBean(mPath, false, initPaint(nMaskColor), new IPaint(nMaskColor, 255, mPaintWidth), Mode.DOODLE_MODE));
                    mPath = null;
                }
                mCallback.endTouch(null != mPath || null != mRubberPath || mRevokeList.size() > 0);
                break;
            default:
                break;

        }

        return true;
    }


    /**
     * 画笔宽度
     */
    @Override
    public void setPaintWidth(float paintWidth) {
        super.setPaintWidth(paintWidth);
        mPaintWidth = paintWidth;
        mMaskPaint.setStrokeWidth(mPaintWidth);
        mRubberPaint.setStrokeWidth(mPaintWidth);
        invalidate();
    }


    private boolean mEnableRubber = false; //false 普通涂抹; true 橡皮擦

    public void enableRubber(boolean enable) {
        mEnableRubber = enable;
        invalidate();
    }

    public void setEnableShowRevokeList(boolean enableShowRevokeList) {
        this.enableShowRevokeList = enableShowRevokeList;
    }

    private boolean enableShowRevokeList = false;

    /**
     * 把遮罩换成纯白的不透明区域
     */
    public Bitmap save() {
        return save(false);
    }

    /**
     * @param newpaint true 证件照mask (用户涂抹的区域更改为透明)
     * @return
     */
    public Bitmap save(boolean newpaint) {
        Bitmap mask = null;
        for (DrawPathBean bean : mRevokeList) {
            if (null != bean.mMask) {
                mask = bean.mMask;
                break;
            }
        }
        if (mRevokeList.size() == 0 && null == mask) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        drawImp(cv, Color.WHITE, true, newpaint, enableShowRevokeList);
        if (isIDCard) { //证件照
            return bmp;
        } else {
            Bitmap dst;
            if (null != mask) {
                dst = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888); //等比例输出
            } else { //与ExtraPreviewFrameListener中的缩放一致
                float asp = getWidth() * 1f / getHeight();
                int tmp = ExtraPreviewFrameListener.MIN_BITMAP;
                if (asp > 1) {
                    dst = Bitmap.createBitmap((int) (tmp * asp), tmp, Bitmap.Config.ARGB_8888);
                } else {
                    dst = Bitmap.createBitmap(tmp, (int) (tmp / asp), Bitmap.Config.ARGB_8888);
                }
            }
            cv = new Canvas(dst);
            cv.drawBitmap(bmp, null, new Rect(0, 0, dst.getWidth(), dst.getHeight()), null);
            bmp.recycle();
            return dst;
        }
    }

    private Paint mMaskPaint;
    private Paint mRubberPaint;
    private Paint mCirclePaint;

    public void setIDCard(boolean IDCard) {
        isIDCard = IDCard;
    }

    private boolean isIDCard = false; //true 证件照

    /**
     * @param cv
     * @param maskColor      mask对应的遮罩颜色,预览与输出时不一致（输出时为纯白色）
     * @param save           最终的导出
     * @param newPaint       证件照
     * @param showRevokeList 是否显示可撤销列表的内容
     */
    private void drawImp(Canvas cv, int maskColor, boolean save, boolean newPaint, boolean showRevokeList) {
        int saveLayer = cv.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
//        Log.e(TAG, "drawImp: " + maskColor + " " + mBaseMaskPath + " " + mMaskList.size());
        for (DrawPathBean bean : mRevokeList) {
            if (null != bean.mMask) {
                if (save) {//导出
                    cv.drawBitmap(bean.mMask, null, new Rect(0, 0, getWidth(), getHeight()), null);
                } else {
                    if (null != bean.mPreviewMask) { //预览
                        cv.drawBitmap(bean.mPreviewMask, null, new Rect(0, 0, getWidth(), getHeight()), null);
                    }
                }
                break;
            }
        }
        for (DrawPathBean bean : mRevokeList) { //手动抠图
            if (null != bean.mMask) {
                continue;
            }
            if (Mode.DOODLE_MODE == bean.mode) {
                if (isIDCard) { //导出时应用所有涂抹，预览时仅保留最后一次涂抹
                    if (newPaint && null != bean.mIPaint) { //兼容证件照导出mask
                        Paint paint = setModePaint(bean.mode, bean.mIPaint.mAlpha, bean.mIPaint.color, bean.mIPaint.mPaintWidth);
                        paint.setAlpha(0);  //设置透明度为0
                        paint.setColor(Color.WHITE);
                        cv.drawPath(bean.path, paint);
                    } else if (showRevokeList) { //证件照：touchUp ->媒体刷新成功这段时间显示revoke列表，防止界面闪烁| 拖动时放大
                        if (null != bean.mIPaint) {//nMaskColor:画笔颜色
                            Paint paint = setModePaint(bean.mode, bean.mIPaint.mAlpha, nMaskColor, bean.mIPaint.mPaintWidth); //强制修改列表中的颜色，与背景色一致
                            cv.drawPath(bean.path, paint);
                        }
                    }
                } else {
                    if (!bean.isRubber) {
                        bean.paint.setColor(maskColor);
                        cv.drawPath(bean.path, bean.paint);
                    } else {
                        cv.drawPath(bean.path, bean.paint);
                    }
                }
            }
        }

        if (null != mPath) {
            mMaskPaint.setColor(maskColor);
            cv.drawPath(mPath, mMaskPaint);
        }

        if (mEnableRubber) {
            if (null != mRubberPath) {
                cv.drawPath(mRubberPath, mRubberPaint);
            }
        }


        if (null != mPath || null != mRubberPath) {
            cv.drawCircle(preX, preY, mPaintWidth / 2, mCirclePaint);
        }
        cv.restoreToCount(saveLayer);
    }

    private Paint setModePaint(Mode mode, int alpha, int color, float paintWidth) {
        return Helper.initPaint(mode, alpha, color, paintWidth);
    }


    public void reset() {
        mPath = null;
        mRevokeList.clear();
        undoList.clear();
        mRubberPath = null;
        postInvalidate();
    }


    public List<SegmentView.DrawPathBean> getRevokeList() {
        return mRevokeList;
    }

    @Override
    public void recycle() {
        if (null != mSrc) {
            mSrc.recycle();
            mSrc = null;
        }
        reset();
    }


    private List<DrawPathBean> mRevokeList = new ArrayList<>(); //mask 手动微调
    private List<DrawPathBean> undoList = new ArrayList<>(); //mask 手动微调

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Callback mCallback;

    public void setMaskColor(int color) {
        nMaskColor = color;
        invalidate();
    }


    public static interface Callback {


        void startTouch(Bitmap bitmap, Rect rect);

        void moveTouch(Bitmap bitmap, Rect rect);

        void endTouch(boolean hasMask);


    }


    /**
     * 撤销操作
     *
     * @return 撤销后剩余可以撤销的步骤
     */
    public void revoke() {
        // 撤销只针对当前模式的撤销，不是所有步骤的撤销
        if (mRevokeList.size() <= 0) {
            return;
        }
        DrawPathBean tmp = mRevokeList.remove(mRevokeList.size() - 1);
        undoList.add(tmp);
        postInvalidate();

    }


    /**
     * 还原
     */
    public void undo() {
        if (undoList.size() > 0) {
            DrawPathBean tmp = undoList.remove(undoList.size() - 1);
            mRevokeList.add(tmp);
            postInvalidate();
        }
    }


    /**
     * 涂鸦
     */
    public class DrawPathBean {

        public Path path;
        public boolean isRubber = false;//true 橡皮擦; false 手动抠图
        public Paint paint;
        public IPaint mIPaint;
        public Mode mode;

        public DrawPathBean(Path path, boolean isRubber, Paint paint, IPaint iPaint, Mode mode) {
            this.path = path;
            this.isRubber = isRubber;
            this.paint = paint;
            this.mIPaint = iPaint;
            this.mode = mode;
        }


        public Bitmap mMask; //自动抠图
        public Bitmap mPreviewMask;

        /**
         * 智能抠图
         *
         * @param mask
         * @param previewMask
         */
        public DrawPathBean(Bitmap mask, Bitmap previewMask) {
            mMask = mask;
            mPreviewMask = previewMask;
        }


    }
}
