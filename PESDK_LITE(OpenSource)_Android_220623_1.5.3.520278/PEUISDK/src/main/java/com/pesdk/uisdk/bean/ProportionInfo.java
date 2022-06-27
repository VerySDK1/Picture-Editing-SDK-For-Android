package com.pesdk.uisdk.bean;

import com.pesdk.uisdk.bean.code.Crop;

/**
 *
 */
public class ProportionInfo {

    public int getProportionMode() {
        return mProportionMode;
    }


    public float getProportionValue() {
        return mProportionValue;
    }


    //记录临时变量: 手动设置比例
    @Crop.CropMode
    private int mProportionMode = Crop.CROP_FREE;

    private float mProportionValue = -1;

    public ProportionInfo(@Crop.CropMode int proportionMode, float proportionValue) {
        mProportionMode = proportionMode;
        mProportionValue = proportionValue;
    }

    public void setProportionMode(@Crop.CropMode int proportionMode, float proportionValue) {
        mProportionMode = proportionMode;
        mProportionValue = proportionValue;
    }
}
