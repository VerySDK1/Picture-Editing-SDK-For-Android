package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.utils.CoreUtils;

/**
 * 字幕、贴纸 、画中画 等元素的item    统一样式 ： 选中、未选中 、在当前范围
 */
public class CheckSimpleView extends androidx.appcompat.widget.AppCompatImageView {

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;

    private Drawable mDrawableLoading;

    private boolean mReady;
    private boolean mSetupPending;

    private boolean isChecked;
    private boolean isBelong;//未选中 但是出于当前时间段内 显示红点
    private boolean isLoading = false;

    private int mIconWidth, mIconHeight;

    private boolean isDrawDots = true; // ture 绘制原点  false绘制半直线

    public CheckSimpleView(Context context) {
        super(context);
    }

    public CheckSimpleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckSimpleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setScaleType(SCALE_TYPE);
        initPaint(context.getResources());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.pesdk_CircleAnimationView, defStyle, 0);
        mBorderWidth = DEFAULT_BORDER_WIDTH;
        mBorderColor = DEFAULT_BORDER_COLOR;
        mDrawableLoading = a.getDrawable(R.styleable.pesdk_CircleAnimationView_loadingSrc);
        a.recycle();

        mIconWidth = (int) getResources().getDimension(R.dimen.dp_30);
        mIconHeight = (int) getResources().getDimension(R.dimen.dp_30);

        mReady = true;
        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    public void setLoading() {
        isLoading = true;
    }

    public void cancelLoading() {
        isLoading = false;
        invalidate();
    }

    public boolean isLoading() {
        return isLoading;
    }


    private int mLoadingLevel = 0;

    private Paint pBGPaint = new Paint();
    private Paint pEdPaint = new Paint();
    private Paint pDotsPaint = new Paint();

    private static final String TAG = "CheckSimpleView";
    private final int H = 10;
    private int centerX = CoreUtils.dpToPixel(2);

    private void initPaint(Resources res) {
        pBGPaint.setAntiAlias(true);
        pBGPaint.setColor(res.getColor(R.color.pesdk_transparent20_white));
        pEdPaint.setAntiAlias(true);
        pEdPaint.setColor(res.getColor(R.color.pesdk_main_press_color));
        pDotsPaint.setAntiAlias(true);
        pDotsPaint.setStrokeWidth(4);
        pDotsPaint.setColor(res.getColor(R.color.pesdk_main_press_color));
        pDotsPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable tmp = getDrawable();
        Rect rectShow = new Rect(0, 0, getWidth(), getHeight());

        if (isDrawDots) {
            canvas.drawRoundRect(new RectF(rectShow), centerX, centerX, pBGPaint);
        } else {
            if (isChecked) {
                canvas.drawRoundRect(new RectF(rectShow), centerX, centerX, pBGPaint);
            }
        }
        if (tmp != null) {
            mBitmap = getBitmapFromDrawable(tmp);
            if (mBitmap != null) {
                setupImp();
            }
        }

        canvas.drawRect(rectShow, mBitmapPaint);
        if (isLoading && mDrawableLoading != null) {
            int loadLeft = (mIconWidth - mDrawableLoading.getIntrinsicWidth()) / 2;
            int loadTop = (mIconHeight - mDrawableLoading.getIntrinsicHeight()) / 2;
            mDrawableLoading.setBounds(loadLeft, loadTop, loadLeft + mDrawableLoading.getIntrinsicWidth(),
                    loadTop + mDrawableLoading.getIntrinsicHeight());
            mDrawableLoading.draw(canvas);
            mDrawableLoading.setLevel(mLoadingLevel += 100);
        } else {
            if (isChecked()) { //选中
                if (isDrawDots) {
                    RectF rect = new RectF(3, 3, getWidth() - 3, getHeight() - 3);
                    canvas.drawRoundRect(rect, 8, 8, pDotsPaint);
                } else {
                    RectF rect = new RectF(0, getHeight() - H, getWidth(), getHeight());
                    float x = Math.min(rect.width() / 2, rect.height() / 2);
                    canvas.drawRoundRect(rect, x, x, pEdPaint);
                }

            } else if (isBelong) { //未选中 但是出于当前时间段内 显示红点

                if (isDrawDots) {
                    canvas.drawCircle(getWidth() - 18, 18, 2, pDotsPaint);// 小圆
                } else {
                    int width = (int) (getWidth() * 0.3f);
                    int left = (getWidth() - width) / 2;
                    RectF rect = new RectF(left, getHeight() - H, left + width, getHeight());
                    float x = Math.min(rect.width() / 2, rect.height() / 2);
                    canvas.drawRoundRect(rect, x, x, pEdPaint);
                }

            }
        }
    }

    public void setIsDrawDots(boolean isDrawDots) {
        this.isDrawDots = isDrawDots;
    }

    public void setChecked(boolean check) {
        isChecked = check;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setBelong(boolean belong) {
        isBelong = belong;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }


    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(mIconWidth, mIconHeight,
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, mIconWidth, mIconHeight);
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        setupImp();
        invalidate();
    }

    private void setupImp() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.reset();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        int height = mBitmap.getHeight();
        int width = mBitmap.getWidth();

        mBorderRect.set(0, 0, getWidth(), getHeight());
        mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth, mBorderRect.height() - mBorderWidth);

        updateShaderMatrix(width, height);
    }

    private void updateShaderMatrix(int bmpWidth, int bmpHeight) {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (bmpWidth * mDrawableRect.height() > mDrawableRect.width() * bmpHeight) {
            scale = mDrawableRect.height() / (float) bmpHeight;
            dx = (mDrawableRect.width() - bmpWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) bmpWidth;
            dy = (mDrawableRect.height() - bmpHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0) + mBorderWidth, (int) (dy + 0) + mBorderWidth);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

}