package com.pesdk.uisdk.bean;

import android.graphics.RectF;

/**
 * 证件照刚进入时，修正显示位置角度
 */
public class CardImportResult {

    public CardImportResult(RectF rectF, int angle) {
        mRectF = rectF;
        this.angle = angle;
    }

    public RectF getRectF() {
        return mRectF;
    }

    public int getAngle() {
        return angle;
    }

    private RectF mRectF;
    private int angle;

    @Override
    public String toString() {
        return "CardImportResult{" +
                "mRectF=" + mRectF +
                ", angle=" + angle +
                '}';
    }
}
