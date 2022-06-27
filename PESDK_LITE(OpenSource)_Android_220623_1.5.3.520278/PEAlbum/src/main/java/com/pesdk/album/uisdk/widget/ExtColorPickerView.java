package com.pesdk.album.uisdk.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.album.R;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;

/**
 * 字幕选择Shader
 */
public class ExtColorPickerView extends View {

    private static final int PADDING = 20;

    private final Paint mPointPaint = new Paint();
    private final Paint mStokePaint = new Paint();
    protected int[] mColorArr;
    protected boolean mChangeLastStoke;
    private boolean mIsLandscape = false;
    private int mRadius = 0;
    /**
     * 无
     */
    private final Bitmap mBmpNoColor;
    private final Bitmap mBmpNoColorChecked;
    private final float mDensity;
    private int mColumnNum = 0;
    /**
     * 位置
     */
    private final ArrayList<Location> mLocationList = new ArrayList<>();
    /**
     * 是否用圆形图标
     */
    private boolean mDrawCircle;
    /**
     * 是否为空心图标
     */
    private boolean mDrawStrokeOnly;
    /**
     * 文字
     */
    private final boolean mTextEdit;
    /**
     * filter
     */
    private final PaintFlagsDrawFilter mFilter;

    /**
     * 临时
     */
    private final RectF mTempRectF = new RectF();

    public ExtColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPointPaint.setAntiAlias(true);
        mStokePaint.setAntiAlias(true);
        mStokePaint.setColor(Color.BLACK);
        mStokePaint.setStyle(Paint.Style.STROKE);
        mStokePaint.setStrokeWidth(mStokeWidth - 4);
        mDensity = CoreUtils.getPixelDensity();

        mBmpNoColor = BitmapFactory.decodeResource(context.getResources(), R.drawable.album_ic_none_n);
        mBmpNoColorChecked = BitmapFactory.decodeResource(context.getResources(), R.drawable.album_ic_none_p);

        @SuppressLint("CustomViewStyleable")
        TypedArray tA = context.obtainStyledAttributes(attrs, R.styleable.AlbumColorPicker);
        mTextEdit = tA.getBoolean(R.styleable.AlbumColorPicker_isTextEdit, false);
        mDrawCircle = tA.getBoolean(R.styleable.AlbumColorPicker_isDrawCircle, true);
        mDrawStrokeOnly = tA.getBoolean(R.styleable.AlbumColorPicker_isDrawStrokeOnly, false);
        tA.recycle();

        if (mTextEdit) {
            mColorArr = new int[]{Color.parseColor("#484848"), Color.parseColor("#FFFFFF"),
                    Color.parseColor("#e8ce6b"), Color.parseColor("#f9b73c"),
                    Color.parseColor("#e3573b"), Color.parseColor("#be213b"),
                    Color.parseColor("#00ffff"), Color.parseColor("#5da9cf"),
                    Color.parseColor("#0695b5"), Color.parseColor("#2791db"),
                    Color.parseColor("#3564b7"), Color.parseColor("#e9c930"),
                    Color.parseColor("#a6b45c"), Color.parseColor("#87a522"),
                    Color.parseColor("#32b16c"), Color.parseColor("#017e54"),
                    Color.parseColor("#fdbacc"), Color.parseColor("#ff5a85"),
                    Color.parseColor("#ca4f9b"), Color.parseColor("#71369a"),
                    Color.parseColor("#6720d4"), Color.parseColor("#164c6e"),
                    Color.parseColor("#9f9f9f"), Color.parseColor("#000000"),};
        } else {
            mColorArr = new int[]{Color.parseColor("#00000000"), Color.parseColor("#FFFFFF"),
                    Color.parseColor("#e8ce6b"), Color.parseColor("#f9b73c"),
                    Color.parseColor("#e3573b"), Color.parseColor("#be213b"),
                    Color.parseColor("#00ffff"), Color.parseColor("#5da9cf"),
                    Color.parseColor("#0695b5"), Color.parseColor("#2791db"),
                    Color.parseColor("#3564b7"), Color.parseColor("#e9c930"),
                    Color.parseColor("#a6b45c"), Color.parseColor("#87a522"),
                    Color.parseColor("#32b16c"), Color.parseColor("#017e54"),
                    Color.parseColor("#fdbacc"), Color.parseColor("#ff5a85"),
                    Color.parseColor("#ca4f9b"), Color.parseColor("#71369a"),
                    Color.parseColor("#6720d4"), Color.parseColor("#164c6e"),
                    Color.parseColor("#9f9f9f"), Color.parseColor("#484848"),};
        }
        mColorArr[mColorArr.length - 1] = Color.parseColor("#000000");

        mChangeLastStoke = true;

        mFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    private void initLocation() {
        mLocationList.clear();
        int height = getHeight();
        int width = getWidth() - 2 * PADDING;
        int itemWidth;
        int itemHeight;
        int colNum;
        int rowNum;
        if (mIsLandscape) {
            colNum = 12;
            rowNum = mColorArr.length / colNum;
            if (mColorArr.length % colNum != 0) {
                rowNum += 1;
            }
            if (mColumnNum != 0) {
                colNum = mColumnNum;
                rowNum = mColorArr.length / mColumnNum + 1;
            }
            itemWidth = width / colNum;
            itemHeight = (height - rowNum * PADDING) / rowNum;
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < colNum; j++) {
                    int centerx;
                    if (i % rowNum == 1) {
                        centerx = (int) (itemWidth * (j + 0.75)) + PADDING;

                    } else {
                        centerx = (int) (itemWidth * (j + 0.25)) + PADDING;
                    }
                    int y = (int) (itemHeight * (i + 0.5)) + PADDING;
                    mLocationList.add(new Location(centerx, y));
                }
            }
        } else {
            if (mColumnNum != 0) {
                colNum = mColumnNum;
            } else {
                colNum = 8;
            }
            rowNum = mColorArr.length / colNum;
            if (mColorArr.length % colNum != 0) {
                rowNum += 1;
            }
            itemWidth = width / colNum;
            itemHeight = (height - (rowNum - 1) * PADDING) / rowNum;
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < colNum; j++) {
                    int centerx = (int) (itemWidth * (j + 0.5)) + PADDING;
                    int y = (int) (itemHeight * (i + 0.5)) + PADDING;
                    mLocationList.add(new Location(centerx, y));
                }
            }
        }
        mRadius = Math.min(itemWidth, itemHeight);
        mRadius = mRadius / 2;
    }

    private void checkChangeStoke() {
        if (mChangeLastStoke && mCheckedId == (mColorArr.length - 1)) {
            mStokePaint.setColor(Color.WHITE);
        } else {
            mStokePaint.setColor(Color.BLACK);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initLocation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mFilter);
        int itemWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / 6 / 2;
        int radius = itemWidth / 2;
        int index;
        Location item;
        if (mDrawStrokeOnly) {
            mStokePaint.setStyle(Paint.Style.STROKE);
            mPointPaint.setStyle(Paint.Style.STROKE);
            mPointPaint.setStrokeWidth(CoreUtils.dpToPixel(2));
        }
        int colNum, rowNum;
        if (mIsLandscape) {
            radius = itemWidth / 4;
            colNum = 12;
            rowNum = mColorArr.length / colNum;
            if (mColorArr.length % colNum != 0) {
                rowNum += 1;
            }
            if (mColumnNum != 0) {
                colNum = mColumnNum;
                rowNum = mColorArr.length / mColumnNum + 1;
            }

            if (mDrawCircle) {
                for (int i = 0; i < rowNum; i++) {

                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        item = mLocationList.get(index);
                        mPointPaint.setColor(mColorArr[index]);
                        if (mCheckedId == index) {
                            // 画边框
                            canvas.drawCircle(item.px, item.py, radius
                                    + mStokeWidth, mPointPaint);
                            checkChangeStoke();
                            canvas.drawCircle(item.px, item.py, radius, mStokePaint);

                        } else {
                            canvas.drawCircle(item.px, item.py, radius, mPointPaint);
                        }

                    }
                }
            } else {
                for (int i = 0; i < rowNum; i++) {
                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        item = mLocationList.get(index);
                        mPointPaint.setColor(mColorArr[index]);
                        if (mCheckedId == index) {
                            // 画边框
                            int r = radius + mStokeWidth;
                            mTempRectF.set(item.px - r, item.py - r, item.px + r,
                                    item.py + r);
                            canvas.drawRoundRect(mTempRectF, 4, 4, mPointPaint);


                            mTempRectF.set(item.px - radius, item.py - radius,
                                    item.px + radius, item.py + radius);
                            checkChangeStoke();
                            canvas.drawRoundRect(mTempRectF, 4, 4, mStokePaint);

                        } else {
                            int ml = item.px - radius;
                            int mt = item.py - radius;
                            mTempRectF.set(ml, mt, ml + 2 * radius, mt + 2 * radius);
                            canvas.drawRoundRect(mTempRectF, 4, 4, mPointPaint);
                        }

                    }
                }
            }
        } else {
            if (mColumnNum != 0) {
                colNum = mColumnNum;
            } else {
                colNum = 8;
            }
            rowNum = mColorArr.length / colNum;
            if (mColorArr.length % colNum != 0) {
                rowNum += 1;
            }
            if (mDrawCircle) {
                for (int i = 0; i < rowNum; i++) {
                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        if (index >= mColorArr.length) {
                            break;
                        }
                        item = mLocationList.get(index);
                        mPointPaint.setColor(mColorArr[index]);
                        int py = item.py;
                        if (mDensity < 2.01) {
                            if (i == 0) {
                                py -= 8;
                            }
                            if (i == 2) {
                                py += 8;
                            }
                        }
                        if (i == 0 && j == 0 && !mTextEdit) {
                            if (mCheckedId == index) {
                                // 画边框
                                mTempRectF.set(item.px - radius - mStokeWidth, py - radius - mStokeWidth,
                                        item.px + radius + mStokeWidth, py + radius + mStokeWidth);
                                canvas.drawBitmap(mBmpNoColorChecked, null, mTempRectF, null);
                            } else {
                                mTempRectF.set(item.px - radius, py - radius, item.px + radius, py + radius);
                                canvas.drawBitmap(mBmpNoColor, null, mTempRectF, null);
                            }
                        } else {
                            if (mCheckedId == index) {
                                // 画边框
                                canvas.drawCircle(item.px, py, radius + mStokeWidth, mPointPaint);
                                checkChangeStoke();
                                canvas.drawCircle(item.px, py, radius, mStokePaint);
                            } else {
                                canvas.drawCircle(item.px, py, radius, mPointPaint);
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < rowNum; i++) {
                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        item = mLocationList.get(index);
                        if (index >= mColorArr.length) {
                            break;
                        }
                        mPointPaint.setColor(mColorArr[index]);
                        if (mCheckedId == index) {
                            // 画边框
                            int r = radius + mStokeWidth;
                            mTempRectF.set(item.px - r, item.py - r, item.px + r,
                                    item.py + r);
                            canvas.drawRoundRect(mTempRectF, 4, 4, mPointPaint);

                            mTempRectF.set(item.px - radius, item.py - radius,
                                    item.px + radius, item.py + radius);
                            checkChangeStoke();
                            canvas.drawRoundRect(mTempRectF, 4, 4, mStokePaint);
                        } else {
                            int ml = item.px - radius;
                            int mt = item.py - radius;
                            mTempRectF.set(ml, mt, ml + 2 * radius, mt + 2 * radius);
                            canvas.drawRoundRect(mTempRectF, 4, 4, mPointPaint);
                        }
                    }
                }
            }
        }
    }

    private int mCheckedId = 0;
    private final int mStokeWidth = 8;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onCheckId((int) event.getX(), (int) event.getY());
                break;
            default:
                break;
        }
        return false;
    }

    private void onCheckId(int x, int y) {
        int len = mLocationList.size();
        Location temp;
        for (int i = 0; i < len; i++) {
            temp = mLocationList.get(i);
            if (x > temp.px - mRadius - mStokeWidth
                    && x < temp.px + mRadius + mStokeWidth
                    && y > temp.py - mRadius - mStokeWidth
                    && y < temp.py + mRadius + mStokeWidth) {

                if (i != mCheckedId) {
                    mCheckedId = i;
                    invalidate();
                    if (null != mColorListener && mCheckedId < len) {
                        mColorListener.getColor(mColorArr[mCheckedId], mCheckedId);
                    }
                }

                break;
            }

        }

    }

    public void setColorArr(int[] mColorArr) {
        this.mColorArr = mColorArr;
    }

    public void setColumnNum(int num) {
        mColumnNum = num;
    }

    public void setLandscape(boolean island) {
        mIsLandscape = island;
    }

    /**
     * 还原到初始状态，一个都不选中
     */
    public void reset() {
        setCheckId(0);
    }

    public void clearChecked() {
        setCheckId(-1);
    }

    public int getCheckColor(int checkedId) {
        setCheckId(checkedId);
        return mColorArr[mCheckedId];

    }

    public void setCheckId(int position) {
        mCheckedId = position;
        invalidate();
    }

    public void setCheckColor(int checkColor) {
        for (int i = 0; i < mColorArr.length; i++) {
            if (mColorArr[i] == checkColor) {
                setCheckId(i);
                break;
            }
        }
    }

    /**
     * 被选中的颜色
     */
    public int getColor() {
        return mColorArr[mCheckedId];
    }

    public void setDrawCircle(boolean isDrawCircle) {
        mDrawCircle = isDrawCircle;
    }

    public void setDrawStrokeOnly(boolean isDrawStrokeOnly) {
        mDrawStrokeOnly = isDrawStrokeOnly;
    }

    private IColorListener mColorListener;

    public void setColorListener(IColorListener listener) {
        mColorListener = listener;
    }

    public interface IColorListener {

        void getColor(int color, int position);
    }

    private static class Location {
        int px;
        int py;

        Location(int x, int y) {
            this.px = x;
            this.py = y;
        }
    }

}
