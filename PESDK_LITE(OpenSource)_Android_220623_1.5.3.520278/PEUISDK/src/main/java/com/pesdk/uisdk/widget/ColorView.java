package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import com.pesdk.uisdk.R;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 颜色选中
 */
public class ColorView extends View implements Checkable {

    private static final String TAG = "ColorView";

    private boolean bChecked = false;
    private Paint mPaint;
    private int color = Color.RED;
    private int color_white_border = Color.BLACK;
    private int colorP = Color.BLACK;
    private final int STROKE_WIDTH = 6;
    private static final int MODE_CIRCLE = 0;//圆形
    public static final int MODE_RECT = 1;//矩形

    public void setMode(int mode) {
        this.mMode = mode;
    }

    private int mMode = MODE_CIRCLE; //默认圆形

    public ColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        colorP = ContextCompat.getColor(context, R.color.pesdk_main_color);
        color_white_border = ContextCompat.getColor(context, R.color.pesdk_line_color);
    }

    @Override
    public void setChecked(boolean checked) {
        bChecked = checked;
        invalidate();
    }

    @Override
    public boolean isChecked() {
        return bChecked;
    }

    @Override
    public void toggle() {

    }


    public void setColor(int color) {
        this.color = color;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        int radius = (int) (Math.min(centerX, centerY) * 0.9f - STROKE_WIDTH - 2);
        if (isChecked()) {
            radius += STROKE_WIDTH / 2f;
        }
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        if (mMode == MODE_CIRCLE) {
            canvas.drawCircle(centerX, centerY, radius, mPaint);
            if (isChecked()) { //选中
                mPaint.reset();
                mPaint.setAntiAlias(true);
                mPaint.setColor(colorP);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(STROKE_WIDTH / 2);
                canvas.drawCircle(centerX, centerY, radius, mPaint);
            } else if (color == Color.WHITE) { //纯白看不见，绘制边框
                //画边框,纯白看不见
                mPaint.reset();
                mPaint.setAntiAlias(true);
                mPaint.setColor(color_white_border);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(centerX, centerY, radius, mPaint);
            }
        } else if (mMode == MODE_RECT) {
            RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            canvas.drawRoundRect(rectF, STROKE_WIDTH / 2f, STROKE_WIDTH / 2f, mPaint);

            if (isChecked()) { //选中
                mPaint.reset();
                mPaint.setAntiAlias(true);
                mPaint.setColor(colorP);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(STROKE_WIDTH / 2f);
                canvas.drawRoundRect(rectF, STROKE_WIDTH / 2f, STROKE_WIDTH / 2f, mPaint);

            } else if (color == Color.WHITE) { //纯白看不见，绘制边框
                //画边框,纯白看不见
                mPaint.reset();
                mPaint.setAntiAlias(true);
                mPaint.setColor(color_white_border);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRoundRect(rectF, STROKE_WIDTH / 2f, STROKE_WIDTH / 2f, mPaint);
            }
        }
    }
}
