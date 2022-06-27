package com.pesdk.uisdk.widget.doodle.bean;

/**
 */
public class IPaint {
    public IPaint(int color, int alpha, float mPaintWidth) {
        this.color = color;
        this.mAlpha = alpha;
        this.mPaintWidth = mPaintWidth;
    }

    public int color;
    public int mAlpha;//0~255;
    public float mPaintWidth;
}
