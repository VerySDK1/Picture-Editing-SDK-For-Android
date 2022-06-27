package com.pesdk.uisdk.beauty.bean;

import android.graphics.PointF;

/**
 * 衣服与人脸的坐标关系
 * <p>
 * https://github.com/alibaba/MNNKit/blob/master/doc/FaceDetection_CN.md
 * 1.衣服的宽度约为两耳之间宽度(31[x]-1[x])的2倍、衣服的Rect.top 约与 下巴点(16[y])持平
 */
public class FaceClothesParam {
    public FaceClothesParam(float clothesWidth, PointF jawPointF) {
        mClothesWidth = clothesWidth;
        this.jawPointF = jawPointF;
    }

    public float getClothesWidth() {
        return mClothesWidth;
    }

    public PointF getJawPointF() {
        return jawPointF;
    }

    private   float mClothesWidth; //两肩宽
    private  PointF jawPointF; //下巴坐标
}
