package com.pesdk.uisdk.widget.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;
import com.vecore.models.MaskObject;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

/**
 * 蒙版
 */
public class MaskMediaView extends View {

    private String TAG = "MaskMediaView";
    private Context mContext;
    private final int CORE_CIRCULAR_R = 28;
    private int mMode = 0;
    private final int NOTHING = 0; //无
    private final int LINEAR = 1; //线性
    private final int MIRROR = 2;//镜面
    private final int CIRCULAR = 3; //圆形
    private final int RECTANGLE = 4; //矩形
    private final int FIVE_POINTED_STAR = 5; //五角星
    private final int LOVE = 6; //爱心
    private final int FLOWER = 7; //花朵
    private final int HEXAGON = 8; //六边形
    private final int FIVE_POINTED_STAR_II = 9; //五角星II
    private final int MOON = 10; //月亮
    // 缩放比例区间
    private final float MAX_SCALE = 5.0f;
    private final float MIN_SCALE = 0.0f;
    // 父容器大小
//    private Point mParentSize;
    //视频中心坐标
//    private PointF mCenterPoint = new PointF(0, 0);
    //视频范围
    private RectF mRect;
    //绘制外矩形
    private RectF mOutRect = new RectF();
    //半径
    private float mRadius = 0;
    //左右半径
    private float mHorizontalRadius = 0;
    //上下半径
    private float mVerticalRadius = 0;
    //矩形圆角半径
    private float mFilletRadius = 0;
    //中心圆
    private RectF coreRecF = new RectF();
    //中心圆心中心坐标
    private PointF mCoreCenterPoint = new PointF(0, 0);
    //转换后传出中心点坐标
    private PointF mPoint = new PointF(0, 0);
    //转换的传出的圆角值
    private float mFillet;
    //转换后传出的旋转角度
    private float mRotate;
    //转换后的宽高比
    private float mWidth = 1.0f, mHeight = 1.0f;
    private float mOffsetX, mOffsetY;
    //初始旋转角度
    private float mInitRotateAngle;
    //旋转角度
    private float mRotateAngle;
    //缩放系数
    private float mDisf = 1.0f;

    //画笔
    private Paint mPaint = new Paint();
    private Paint mPaint1 = new Paint();

    private Bitmap mCornerBtn; //圆角
    private Bitmap mHeigetBtn; //上
    private Bitmap mWidthBtn;// 右
    private Bitmap mRotateBtn;//旋转 缩放

    private float mCornerBtnMovingDistance = 0;
    //是否绘制该按钮
    private boolean isCanvasCornerBtn = false;
    private boolean isCanvasHeigetBtn = false;
    private boolean isCanvasWidthBtn = false;
    private boolean isCanvasRotateBtn = true;
    //ture 旋转按钮 绘制在对角 false 绘制在正下方
    private boolean isDiagonal = false;

    //是否点击了该按钮
    private boolean isCornerBtn = false;
    private boolean isHeigetBtn = false;
    private boolean isWidthBtn = false;
    private boolean isRotateBtn = false;

    private RectF mCornerRectF = new RectF();
    private RectF mHeigetRectF = new RectF();
    private RectF mWidthRectF = new RectF();
    private RectF mRotateRectF = new RectF();
    // 旋转图形的矩阵
    private Matrix mMatrix = new Matrix();

    private Bitmap starBitmap;
    private Bitmap loveBitmap;

    //设置图案形状
    public void setmMode(int mode) {
        if (mMode != mode) {
            isInit = true;
            mMode = mode;
            mDisf = 1.0f;
            geTradius();
            invalidate();
        }
    }

    /**
     * @param mRotate 旋转角度  （顺时针方向）
     * @param rect    显示位置  旋转角度：0度时
     */
    public MaskMediaView(Context context, float mRotate, RectF rect) {
        this(context, null);
        isInit = true;
        this.mInitRotateAngle = mRotate;
        this.mRotateAngle = mRotate;
        mRect = new RectF(rect);
        mOffsetX = rect.left;
        mOffsetY = rect.top;
        mCoreCenterPoint.x = rect.centerX();
        mCoreCenterPoint.y = rect.centerY();
        geTradius();
    }

    public void setRectData(float mRotate, RectF rect, MaskObject maskObject) {//
        isInit = true;
        mRect = new RectF(rect);
        mOffsetX = rect.left;
        mOffsetY = rect.top;
        if (maskObject != null) {
            //旋转角度
            this.mRotate = maskObject.getAngle();
            this.mInitRotateAngle = mRotate;
            this.mRotateAngle = mRotate - this.mRotate;
            //中心圆点
            getCoreCenterPoint(maskObject);
            setFingerPosition(null, mCoreCenterPoint.x, mCoreCenterPoint.y);
            //缩放比例
            mDisf = maskObject.getDisf();
            //宽高
            mWidth = maskObject.getSize().getWidth();
            mHeight = maskObject.getSize().getHeight();
            mHorizontalRadius = (mRect.width()) * mWidth / 2.0f;
            mVerticalRadius = (mRect.height()) * mHeight / 2.0f;
            //圆角值
            mFillet = maskObject.getCornerRadius();
            mFilletRadius = mFillet * (Math.min(mHorizontalRadius, mVerticalRadius) * mDisf);
            mDisf = 1.0f;
            if (mMode == LOVE || mMode == FIVE_POINTED_STAR) {
                mVerticalRadius = mHorizontalRadius;
            }
        } else {
            this.mInitRotateAngle = mRotate;
            this.mRotateAngle = mRotate;
            mCoreCenterPoint.x = rect.centerX();
            mCoreCenterPoint.y = rect.centerY();
            geTradius();
        }
        invalidate();
    }

    public MaskMediaView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskMediaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels;
        imageHeight = metrics.heightPixels;
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);

        mPaint1.setAntiAlias(true);
        mPaint1.setColor(Color.RED);
        mPaint1.setStyle(Paint.Style.STROKE);
        mPaint1.setStrokeWidth(4);

        // 初始化控制图片
        mCornerBtn = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_vepub_thumb_corner);
        mHeigetBtn = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_vepub_thumb_height);
        mWidthBtn = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_width);
        mRotateBtn = BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_vepub_thumb_rotate);


        starBitmap = getDrawableBitmap(mContext, R.drawable.pesdk_vepub_ic_star);//BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_vepub_ic_star);
        loveBitmap = getDrawableBitmap(mContext, R.drawable.pesdk_ic_love);//BitmapFactory.decodeResource(getResources(), R.drawable.pesdk_vepub_ic_star);

    }

    public static Bitmap getDrawableBitmap(final Context context, int resId) {
        final Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//下边这行修改了，使用的是AppCompatImageView里用到的方法
            Drawable drawable = AppCompatResources.getDrawable(context, resId);
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        }
        return bitmap;
    }

    private boolean isInit = false;
    int imageWidth;
    int imageHeight;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mMode != 0) {
            mMatrix.reset();
            mMatrix.postRotate(mRotateAngle, mCoreCenterPoint.x, mCoreCenterPoint.y);
            canvas.save();
            canvas.setMatrix(mMatrix);
            //设置中心圆和矩形位置
            coreRecF.set(mCoreCenterPoint.x - CORE_CIRCULAR_R, mCoreCenterPoint.y - CORE_CIRCULAR_R, mCoreCenterPoint.x + CORE_CIRCULAR_R, mCoreCenterPoint.y + CORE_CIRCULAR_R);
            mOutRect.set(mCoreCenterPoint.x - mHorizontalRadius * mDisf, mCoreCenterPoint.y - mVerticalRadius * mDisf, mCoreCenterPoint.x + mHorizontalRadius * mDisf, mCoreCenterPoint.y + mVerticalRadius * mDisf);
            if (mOutRect.right <= mOutRect.left) {
                mOutRect.right = mOutRect.left = mCoreCenterPoint.x;
            }
            if (mOutRect.bottom <= mOutRect.top) {
                mOutRect.bottom = mOutRect.top = mCoreCenterPoint.y;
            }
            //绘制中心圆
            canvas.drawArc(coreRecF, 0, 360, false, mPaint);
            canvasPattern(canvas);
            canvasBtn(canvas);
            canvas.restore();
        } else {
            isInit = false;
            setIsCanasBtn();
            mPoint.x = 0;
            mPoint.y = 0;
            mRotate = 0;
            mDisf = 1.0f;
            mFillet = 0;
            mWidth = 1.0f;
            mHeight = 1.0f;
            mTouchListener.onRectChange(mPoint, mRotate, mDisf, mFillet, mWidth, mHeight);
        }

        if (isInit) {
            setOnClick();
            isInit = false;
        }
    }

    //绘制图案
    private void canvasPattern(Canvas canvas) {
        if (mMode == NOTHING) {

        }
        if (mMode == LINEAR) {
            setIsCanasBtn();
            isDiagonal = true;

            canvas.drawLine(-imageHeight, mCoreCenterPoint.y, mCoreCenterPoint.x - CORE_CIRCULAR_R, mCoreCenterPoint.y, mPaint);
            canvas.drawLine(mCoreCenterPoint.x + CORE_CIRCULAR_R, mCoreCenterPoint.y, imageHeight, mCoreCenterPoint.y, mPaint);
        }
        if (mMode == MIRROR) {
            setIsCanasBtn();
            isDiagonal = true;
            canvas.drawLine(-imageHeight, mOutRect.top, imageHeight, mOutRect.top, mPaint);
            canvas.drawLine(-imageHeight, mOutRect.bottom, imageHeight, mOutRect.bottom, mPaint);
//            canvas.drawRoundRect(mOutRect , mFilletRadius,mFilletRadius,mPaint1);
        }
        if (mMode == CIRCULAR) {
            isCanvasCornerBtn = false;
            isCanvasHeigetBtn = true;
            isCanvasWidthBtn = true;
            isDiagonal = false;
            canvas.drawArc(mOutRect, 0, 360, false, mPaint);
        }
        if (mMode == RECTANGLE) {
            isCanvasCornerBtn = true;
            isCanvasHeigetBtn = true;
            isCanvasWidthBtn = true;
            isDiagonal = true;
            canvas.drawRoundRect(mOutRect, mFilletRadius, mFilletRadius, mPaint);
        }
        if (mMode == FIVE_POINTED_STAR) {
            setIsCanasBtn();
            if (starBitmap != null && !starBitmap.isRecycled()) {
//                canvas.drawRoundRect(mOutRect , mFilletRadius,mFilletRadius,mPaint1);
                canvas.drawBitmap(starBitmap, null, mOutRect, mPaint);
            }

        }
        if (mMode == LOVE) {
            setIsCanasBtn();
            if (starBitmap != null && !starBitmap.isRecycled()) {
//                canvas.drawRoundRect(mOutRect , mFilletRadius,mFilletRadius,mPaint1);
                canvas.drawBitmap(loveBitmap, null, mOutRect, mPaint);
            }
        }
        if (mMode == FLOWER) {
            setIsCanasBtn();
        }
        if (mMode == HEXAGON) {
            setIsCanasBtn();
        }
        if (mMode == FIVE_POINTED_STAR_II) {
            setIsCanasBtn();
        }
        if (mMode == MOON) {
            setIsCanasBtn();
        }
    }

    private void setIsCanasBtn() {
        isCanvasCornerBtn = false;
        isCanvasHeigetBtn = false;
        isCanvasWidthBtn = false;
        isDiagonal = false;
    }

    //绘制按钮
    private void canvasBtn(Canvas canvas) {
        if (isCanvasCornerBtn) { //圆角
            if ((mOutRect.left - mCornerBtn.getWidth() * 1.5 + mCornerBtnMovingDistance) > (mOutRect.left - mCornerBtn.getWidth() * 1.5)) {
                mCornerBtnMovingDistance = 0;
            }
            if (Math.abs(mCornerBtnMovingDistance) >= (Math.min(mHorizontalRadius, mVerticalRadius))) {
                mCornerBtnMovingDistance = -Math.min(mHorizontalRadius, mVerticalRadius);
            }
            mCornerRectF.set((float) (mOutRect.left - mCornerBtn.getWidth() * 1.5 + mCornerBtnMovingDistance), (float) (mOutRect.top - mCornerBtn.getHeight() * 1.5 + mCornerBtnMovingDistance), (float) (mOutRect.left - mCornerBtn.getWidth() * 0.5 + mCornerBtnMovingDistance), (float) (mOutRect.top - mCornerBtn.getHeight() * 0.5 + mCornerBtnMovingDistance));
            canvas.drawBitmap(mCornerBtn, null, mCornerRectF, null);
        }
        if (isCanvasHeigetBtn) {//上
            mHeigetRectF.set((float) (mOutRect.centerX() - mHeigetBtn.getWidth() * 0.5), (float) (mOutRect.top - mHeigetBtn.getWidth() * 1.5), (float) (mOutRect.centerX() + mHeigetBtn.getWidth() * 0.5), (float) (mOutRect.top - mHeigetBtn.getWidth() * 0.5));
            canvas.drawBitmap(mHeigetBtn, null, mHeigetRectF, null);
        }
        if (isCanvasWidthBtn) { //右
            mWidthRectF.set((float) (mOutRect.right + mWidthBtn.getWidth() * 0.5), (float) (mOutRect.centerY() - mWidthBtn.getWidth() * 0.5), (float) (mOutRect.right + mWidthBtn.getWidth() * 1.5), (float) (mOutRect.centerY() + mWidthBtn.getWidth() * 0.5));
            canvas.drawBitmap(mWidthBtn, null, mWidthRectF, null);
        }
//        if(isCanvasRotateBtn && isDiagonal){ // 旋转 缩放  isDiagonal  ture 旋转按钮 绘制在对角 false 绘制在正下方
//            mRotateRectF.set((float) (mOutRect.right + mRotateBtn.getWidth()*0.5), (float) (mOutRect.bottom  +mRotateBtn.getWidth()*0.5),(float) (mOutRect.right +mRotateBtn.getWidth()*1.5), (float) (mOutRect.bottom  + mRotateBtn.getWidth()*1.5));
//            canvas.drawBitmap(mRotateBtn , null ,mRotateRectF , null );
//        }else {
//            mRotateRectF.set((float) (mOutRect.centerX()  - mRotateBtn.getWidth()*0.5), (float) (mOutRect.bottom  + mRotateBtn.getWidth()*0.5),(float) (mOutRect.centerX()  +mRotateBtn.getWidth()*0.5), (float) (mOutRect.bottom + mRotateBtn.getWidth()*1.5));
//            canvas.drawBitmap(mRotateBtn , null ,mRotateRectF , null );
//        }
        if (isCanvasRotateBtn) {
            if (mMode == LINEAR) {
                mRotateRectF.set((float) (mCoreCenterPoint.x - mRotateBtn.getWidth() * 0.5), (float) (mCoreCenterPoint.y + mRotateBtn.getWidth() * 2.5)
                        , (float) (mCoreCenterPoint.x + mRotateBtn.getWidth() * 0.5), (float) (mCoreCenterPoint.y + mRotateBtn.getWidth() * 3.5)); //+ mOutRect.centerX()/4
            } else {
                mRotateRectF.set((float) (mOutRect.centerX() - mRotateBtn.getWidth() * 0.5), (float) (mOutRect.bottom + mRotateBtn.getWidth() * 0.5)
                        , (float) (mOutRect.centerX() + mRotateBtn.getWidth() * 0.5), (float) (mOutRect.bottom + mRotateBtn.getWidth() * 1.5));
            }
            canvas.drawBitmap(mRotateBtn, null, mRotateRectF, null);
        }
    }

    //ture 单指  false 双指
    private boolean isSingle = false;
    //单击时 手指点下去时的 x y
    private float mDownX = 0;
    private float mDownY = 0;
    //记录手指在 x y上的移动距离
    private float mMoveX = 0;
    private float mMoveY = 0;
    //旋转之后转换点击点坐标
    float[] p;
    float[] d;
    //临时记录圆角按钮移动的距离
    private float mTempCornerBtnMovingDistance = 0;
    //临时记录点下时的角度和倍数
    private float mTempAngle = 0;
    private float mTempDisf = 1.0f;
    private float mTempnewL = 0;
    //临时记录上 右半径
    private float mTempVerticalRadius = 0.0f;
    private float mTempHorizontalRadius = 0.0f;
    //临时记录中心圆心中心坐标
    private PointF mTempCoreCenterPoint = new PointF(0, 0);
    //记录手指点下时手指的位置
    private PointF mTempPoint = new PointF(0, 0);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerNum = event.getPointerCount();
        if (pointerNum == 2) {
            int re = event.getAction();
            if ((re & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                //点击
                isSingle = false;

            } else if ((re & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                    || re == MotionEvent.ACTION_UP || re == MotionEvent.ACTION_CANCEL) {
                //拿起

            }
            if (re == MotionEvent.ACTION_MOVE) {
                //移动

            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isSingle = true;
                    //记录点击时的半径
                    mTempVerticalRadius = mVerticalRadius;
                    mTempHorizontalRadius = mHorizontalRadius;
                    //记录点击时圆角按钮移动的距离
                    mTempCornerBtnMovingDistance = mCornerBtnMovingDistance;
                    //记录点击时中心圆 中心坐标
                    mTempCoreCenterPoint.x = mCoreCenterPoint.x;
                    mTempCoreCenterPoint.y = mCoreCenterPoint.y;
                    //记录点击时的旋转角度
                    mTempAngle = mRotateAngle;
                    //记录点击时的缩放倍数
                    mTempDisf = mDisf;
                    mMoveX = 0;
                    mMoveY = 0;
                    //记录手指点下去时的 x y
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mTempnewL = (float) Math.sqrt((mDownX - mOutRect.centerX() - mRotateBtn.getWidth() * 1.5) * (mDownX - mOutRect.centerX() - mRotateBtn.getWidth() * 1.5) +
                            (event.getY() - mOutRect.centerY() - mRotateBtn.getWidth() * 1.5) * (mDownY - mOutRect.centerY() - mRotateBtn.getWidth() * 1.5));
                    mMatrix.reset();
                    mMatrix.postRotate(-mRotateAngle, mCoreCenterPoint.x, mCoreCenterPoint.y);
                    p = new float[]{mDownX, mDownY};
                    d = new float[2];
                    mMatrix.mapPoints(d, p);
                    //判断点击的位置
                    isCornerBtn = mCornerRectF.contains(d[0], d[1]);// 圆角
                    isHeigetBtn = mHeigetRectF.contains(d[0], d[1]);// 上
                    isWidthBtn = mWidthRectF.contains(d[0], d[1]);// 右
                    isRotateBtn = mRotateRectF.contains(d[0], d[1]);// 旋转 缩放
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mMatrix.reset();
                    mMatrix.postRotate(-mRotateAngle, mCoreCenterPoint.x, mCoreCenterPoint.y);
                    float[] p = new float[]{event.getX(), event.getY()};
                    float[] d = new float[2];
                    mMatrix.mapPoints(d, p);
                    mMoveX = d[0] - this.d[0];
                    mMoveY = d[1] - this.d[1];
                    if (isSingle) {
                        if (isCornerBtn && mMode != LOVE && mMode != FIVE_POINTED_STAR) {
                            mCornerBtnMovingDistance = mTempCornerBtnMovingDistance + mMoveX;
                            if (Math.abs(mCornerBtnMovingDistance) >= (Math.min(mHorizontalRadius, mVerticalRadius))) {
                                if (mMoveX > 0) {
                                    mCornerBtnMovingDistance = Math.min(mHorizontalRadius, mVerticalRadius);
                                } else {
                                    mCornerBtnMovingDistance = -Math.min(mHorizontalRadius, mVerticalRadius);
                                }
                            }
                            if ((mOutRect.left - mCornerRectF.left - mCornerBtn.getWidth() * 1.5) >= 0) {
                                mFilletRadius = (float) Math.sqrt((mOutRect.left - mCornerRectF.left - mCornerBtn.getWidth() * 1.5) * (mOutRect.left - mCornerRectF.left - mCornerBtn.getWidth() * 1.5)
                                        + (mOutRect.top - mCornerRectF.top - mCornerBtn.getHeight() * 1.5) * (mOutRect.top - mCornerRectF.top - mCornerBtn.getHeight() * 1.5));
                            }
                        } else if (isHeigetBtn && mMode != LOVE && mMode != FIVE_POINTED_STAR) {
                            mVerticalRadius = mTempVerticalRadius - mMoveY;
                        } else if (isWidthBtn && mMode != LOVE && mMode != FIVE_POINTED_STAR) {
                            mHorizontalRadius = mTempHorizontalRadius + mMoveX;

                        } else if (isRotateBtn) {
                            if (mMode != LINEAR && mMoveY != 0 && mMoveX != 0) {//mMode !=MIRROR&&
                                //缩放
                                int tmpW = 0, tmpH;
                                tmpW = (int) (mRect.right - mRect.left) / 2;
                                tmpH = (int) (mRect.bottom - mRect.top) / 2;
                                float realL = (float) Math.sqrt((float) (tmpW * tmpW + tmpH * tmpH));
                                float newL = (float) Math.sqrt((event.getX() - mOutRect.centerX() - mRotateBtn.getWidth() * 1.5) * (event.getX() - mOutRect.centerX() - mRotateBtn.getWidth() * 1.5) +
                                        (event.getY() - mOutRect.centerY() - mRotateBtn.getWidth() * 1.5) * (event.getY() - mOutRect.centerY() - mRotateBtn.getWidth() * 1.5));
//                                if(mMoveX!= 0 && mMoveY != 0){
//                                    float newDisf = newL*1.0f /realL;
//                                    mDisf = newDisf ;
                                mDisf = mTempDisf * (newL * 1.0f / mTempnewL);
//                                }
                                if (mDisf <= MIN_SCALE) {
                                    mDisf = MIN_SCALE;
                                } else if (mDisf >= MAX_SCALE) {
                                    mDisf = MAX_SCALE;
                                }
                            }
                            //计算旋转角度
                            double a = spacing(mOutRect.centerX(), mOutRect.centerY(), mDownX, mDownY);
                            double b = spacing(event.getX(), event.getY(), mDownX, mDownY);
                            double c = spacing(mOutRect.centerX(), mOutRect.centerY(), event.getX(), event.getY());
                            double cosb = (a * a + c * c - b * b) / (2 * a * c);
                            if (cosb > 1) {
                                cosb = 1f;
                            }
                            double radian = Math.acos(cosb);
                            float newDegree = (float) radianToDegree(radian);
                            //center -> proMove的向量， 我们使用PointF来实现
                            PointF centerToProMove = new PointF((mDownX - mOutRect.centerX()), (mDownY - mOutRect.centerY()));
                            //center -> curMove 的向量
                            PointF centerToCurMove = new PointF((event.getX() - mOutRect.centerX()), (event.getY() - mOutRect.centerY()));
                            //向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
                            float result = centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;
                            if (result < 0) {
                                newDegree = -newDegree;
                            }
                            mRotateAngle = mTempAngle + newDegree;
                            mRotateAngle = mRotateAngle % 360;
//                             updateRotateAndScale(mMoveX ,mMoveY);
                        } else {
                            float x = mTempCoreCenterPoint.x + event.getX() - mDownX;
                            float y = mTempCoreCenterPoint.y + event.getY() - mDownY;
                            setFingerPosition(event, x, y);
                        }
                        invalidate();
                    }
                    setOnClick();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void setOnClick() {
        if (mTouchListener != null) {
            mMatrix.reset();
            mMatrix.setRotate(-mInitRotateAngle, mRect.centerX(), mRect.centerY());
            float[] ps = new float[]{mCoreCenterPoint.x, mCoreCenterPoint.y};
            float[] ds = new float[2];
            mMatrix.mapPoints(ds, ps);
            mPoint.x = (ds[0] - mRect.left) * 2.0f / (mRect.right - mRect.left) - 1;
            mPoint.y = (ds[1] - mRect.top) * 2.0f / (mRect.bottom - mRect.top) - 1;
            if (mPoint.x > 1) {
                mPoint.x = 1;
            }
            if (mPoint.x < -1) {
                mPoint.x = -1;
            }
            if (mPoint.y > 1) {
                mPoint.y = 1;
            }
            if (mPoint.y < -1) {
                mPoint.y = -1;
            }
            mFillet = mFilletRadius * 1.0f / (Math.min(mHorizontalRadius, mVerticalRadius) * mDisf);
            if (mFillet > 1) {
                mFillet = 1.0f;
            }
            mRotate = -mRotateAngle + mInitRotateAngle;
            mWidth = (mOutRect.left - mOutRect.right) * 1.0f / (mRect.left - mRect.right);
            mHeight = (mOutRect.bottom - mOutRect.top) * 1.0f / (mRect.bottom - mRect.top);
            if (mMode == MIRROR) {
                mWidth = mHeight;
            }
            if (mMode == FIVE_POINTED_STAR || mMode == LOVE) {
                if (mHeight < mWidth) {
                    mHeight = mWidth;
                } else {
                    mWidth = mHeight;
                }
            }
            mTouchListener.onRectChange(mPoint, mRotate, mDisf, mFillet, mWidth, mHeight);
        }
    }

    //重新获取矩形外框中心点坐标
    private void getCoreCenterPoint(MaskObject maskObject) {
        float x = (maskObject.getCenter().x + 1.0f) * (mRect.width()) / 2.0f + mRect.left;
        float y = (maskObject.getCenter().y + 1.0f) * (mRect.height()) / 2.0f + mRect.top;
        mMatrix.reset();
        mMatrix.setRotate(mInitRotateAngle, mRect.centerX(), mRect.centerY());
        float[] ps = new float[]{x, y};
        float[] ds = new float[2];
        mMatrix.mapPoints(ds, ps);
        mCoreCenterPoint.x = ds[0];
        mCoreCenterPoint.y = ds[1];
    }

    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mOutRect.centerX();
        float c_y = mOutRect.centerY();

//        float x = mRotateRectF.centerX();
//        float y = mRotateRectF.centerY();
        float x = mOutRect.right;
        float y = mOutRect.bottom;

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;
        mRotateAngle += angle;
        mRotateAngle = mRotateAngle % 360;
    }

    //设置小圆中心点范围
    private void setFingerPosition(MotionEvent event, float x, float y) {
        //限制 x y 的范围
        mMatrix.reset();
        mMatrix.setRotate(-mInitRotateAngle, mRect.centerX(), mRect.centerY());
        float[] p = new float[]{x, y};
        float[] d = new float[2];
        mMatrix.mapPoints(d, p);

        int a = (int) Math.max(0, Math.min(mRect.right - mRect.left - 1, d[0] - mOffsetX));
        int b = (int) Math.max(0, Math.min(mRect.bottom - mRect.top - 1, d[1] - mOffsetY));

        //恢复回去
        a += mOffsetX;
        b += mOffsetY;
        p[0] = a;
        p[1] = b;
        mMatrix.reset();
        mMatrix.setRotate(mInitRotateAngle, mRect.centerX(), mRect.centerY());
        mMatrix.mapPoints(d, p);
        //点位置
        mCoreCenterPoint.x = d[0];
        mCoreCenterPoint.y = d[1];
    }


    private void geTradius() {
        if (mRect != null) {
            if ((mRect.right - mRect.left) > (mRect.bottom - mRect.top)) {
                mRadius = (mRect.bottom - mRect.top) / 2;
            } else {
                mRadius = (mRect.right - mRect.left) / 2;
            }
            mHorizontalRadius = mRadius;
            mVerticalRadius = mRadius;
        }

    }


    /**
     * 计算两点之间的距离 缩放
     */
    private double getDistance(MotionEvent event) {
        int xlen = Math.abs((int) event.getX(event.getPointerId(0)) - (int) event.getX(event.getPointerId(1)));
        int ylen = Math.abs((int) event.getY(event.getPointerId(0)) - (int) event.getY(event.getPointerId(1)));
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
     * 两点的距离
     */
    private double spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }

    /**
     * 弧度换算成角度
     *
     * @return
     */

    public static double radianToDegree(double radian) {

        return radian * 180 / Math.PI;

    }

    /**
     * 绘制彩色多边形或星形
     *
     * @param canvas Canvas画布
     * @param paint  Paint画笔
     * @param radius 外接圆半径
     * @param count  外顶点数
     * @param isStar 是否为星形
     */
    private void drawStar(Canvas canvas, Paint paint, float radius, int count, boolean isStar) {
        canvas.translate(radius, radius);
        if ((!isStar) && count < 3) {
            canvas.translate(-radius, -radius);
            return;
        }
        if (isStar && count < 3) {
            canvas.translate(-radius, -radius);
            return;
        }
        canvas.rotate(-90);

        Path path = new Path();
        float inerRadius = count % 2 == 0 ? (radius * (cos(360 / count / 2) - sin(360 / count / 2) * sin(90 - 360 / count) / cos(90 - 360 / count)))
                : (radius * sin(360 / count / 4) / sin(180 - 360 / count / 2 - 360 / count / 4));
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                path.moveTo(radius * cos(360 / count * i) + mCoreCenterPoint.x, radius * sin(360 / count * i) + mCoreCenterPoint.y);
            } else {
                path.lineTo(radius * cos(360 / count * i) + mCoreCenterPoint.x, radius * sin(360 / count * i) + mCoreCenterPoint.y);
            }
            if (isStar) {
                path.lineTo(inerRadius * cos(360 / count * i + 360 / count / 2) + mCoreCenterPoint.x, inerRadius * sin(360 / count * i + 360 / count / 2) + mCoreCenterPoint.y);
            }
        }
        path.close();
        canvas.drawPath(path, paint);
        canvas.rotate(90);
        canvas.translate(-radius, -radius);
    }

    /**
     * Math.sin的参数为弧度，使用起来不方便，重新封装一个根据角度求sin的方法
     *
     * @param num 角度
     * @return
     */
    float sin(int num) {
        return (float) Math.sin(num * Math.PI / 180);
    }

    /**
     * 与sin同理
     */
    float cos(int num) {
        return (float) Math.cos(num * Math.PI / 180);
    }

    public void setTouchListener(OnTouchListener touchListener) {
        mTouchListener = touchListener;
    }

    private OnTouchListener mTouchListener;

    public interface OnTouchListener {
        /**
         * @param point 中心坐标
         * @param disf  缩放系数
         * @return fillet  圆角
         */
        void onRectChange(PointF point, float rotate, float disf, float fillet, float width, float height);
    }
}
