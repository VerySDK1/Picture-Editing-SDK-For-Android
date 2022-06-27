package com.pesdk.uisdk.widget.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.vecore.base.lib.utils.CoreUtils;

import androidx.annotation.Nullable;

/**
 * 拖拽组件的中间线
 *
 * @create 2019/11/29
 */
public class DragBorderLineView extends View {
    private Paint mPaint;

    public DragBorderLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(6);
        mLinePx = CoreUtils.dpToPixel(25);
    }


    private int mLinePx = 20; //线的长度
    //画上下的中线
    private boolean bDrawVerCenterLine = false;
    //画左右的中线
    private boolean bDrawHorCenterLine = false;

    @Override
    protected void onDraw(Canvas canvas) {
        if (bDrawVerCenterLine) {
            float centerY = getHeight() / 2;
            canvas.drawLine(0, centerY, mLinePx, centerY, mPaint);
            canvas.drawLine(getWidth() - mLinePx, centerY, getWidth(), centerY, mPaint);
        }
        if (bDrawHorCenterLine) {
            float centerX = getWidth() / 2;
            canvas.drawLine(centerX, 0, centerX, mLinePx, mPaint);
            canvas.drawLine(centerX, getHeight() - mLinePx, centerX, getHeight(), mPaint);
        }
    }

    public void drawVerLine(boolean drawVerLine) {
        bDrawVerCenterLine = drawVerLine;
        invalidate();
    }

    public void drawHorLine(boolean drawHorLine) {
        bDrawHorCenterLine = drawHorLine;
        invalidate();
    }
}
