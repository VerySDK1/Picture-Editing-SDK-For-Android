package com.pesdk.uisdk.beauty.bean;

import android.graphics.PointF;

/**
 *
 */
public class FaceEyeParam {
    public FaceEyeParam(float baseEyeDistance, PointF midPointF) {
        this.baseEyeDistance = baseEyeDistance;
        mMidPointF = midPointF;
    }

    public float getBaseEyeDistance() {
        return baseEyeDistance;
    }

    public PointF getMidPointF() {
        return mMidPointF;
    }

    float baseEyeDistance;
    PointF mMidPointF;
}
