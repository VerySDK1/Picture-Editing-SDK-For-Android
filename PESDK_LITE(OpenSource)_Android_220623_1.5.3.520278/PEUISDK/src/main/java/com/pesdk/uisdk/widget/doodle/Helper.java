package com.pesdk.uisdk.widget.doodle;

import android.graphics.Paint;

import com.pesdk.uisdk.widget.doodle.bean.Mode;

/**
 *
 */
public class Helper {
    public static Paint initPaint(Mode mode, int alpha, int color, float paintWidth) {
        if (mode == Mode.DOODLE_MODE) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            paint.setAlpha(alpha);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(paintWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            return paint;
        } else if (mode == Mode.MOSAIC_MODE) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(paintWidth * 2);
            paint.setAlpha(alpha);
            return paint;
        } else if (mode == Mode.GRAPH_MODE) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            paint.setStrokeWidth(paintWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setAlpha(alpha);
            return paint;
        }
        return null;
    }
}
