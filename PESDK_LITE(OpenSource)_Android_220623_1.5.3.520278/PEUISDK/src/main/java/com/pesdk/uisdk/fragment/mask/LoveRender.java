package com.pesdk.uisdk.fragment.mask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.pesdk.uisdk.R;
import com.vecore.BaseVirtual;

import androidx.appcompat.content.res.AppCompatResources;

/**
 * 爱心
 */
public class LoveRender extends MaskRender {

    private final RectF mLoveRectF = new RectF();
    private final Bitmap mLoveBitmap;

    public LoveRender(Context context) {
        super(context);
        mLoveBitmap = getDrawableBitmap(context, R.drawable.mask_svg_love);
    }

    @Override
    protected boolean isKeepRatio() {
        return true;
    }

    @Override
    protected void drawPattern(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            //计算出宽度
            BaseVirtual.SizeF size = mCurrentFrame.getSize();
            if (size.getWidth() != size.getHeight()) {
                mCurrentFrame.setSize(size.getWidth(), size.getWidth());
            }
            float width = size.getWidth() * mShowRectF.width() * mViewRectF.width() / 2;;
            float height = size.getHeight() * mShowRectF.height() * mViewRectF.height() / 2;
            float value = Math.min(width, height);
            mLoveRectF.set(mCenterPointF.x - value, mCenterPointF.y - value,
                    mCenterPointF.x + value, mCenterPointF.y + value);
            //绘制矩形
            canvas.drawBitmap(mLoveBitmap,null, mLoveRectF, mPaint);
        }
    }

    @Override
    protected void drawBtn(Canvas canvas) {
        if (!mViewRectF.isEmpty() && mCurrentFrame != null) {
            float diff = mBtnRotate.getWidth() * 0.5f;
            //旋转按钮
            mRotateRectF.set(mLoveRectF.centerX() - diff, mLoveRectF.bottom + RADIUS_MIN,
                    mLoveRectF.centerX() + diff, mLoveRectF.bottom + RADIUS_MIN + mBtnRotate.getHeight());
            canvas.drawBitmap(mBtnRotate, null, mRotateRectF, null);
        }
    }

    @Override
    protected boolean isRotateScale() {
        return true;
    }

    /**
     * id转成bitmap
     */
    private Bitmap getDrawableBitmap(Context context, int resId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //下边这行修改了，使用的是AppCompatImageView里用到的方法
            Drawable drawable = AppCompatResources.getDrawable(context,resId);
            if (drawable != null) {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        }
        return bitmap;
    }

}
