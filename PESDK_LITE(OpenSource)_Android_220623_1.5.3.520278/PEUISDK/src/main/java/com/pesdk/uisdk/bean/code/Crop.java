package com.pesdk.uisdk.bean.code;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * 裁剪比例
 */
public interface Crop {

    int CROP_ORIGINAL = 0;//原始
    int CROP_FREE = 1;//自由
    int CROP_1 = 2;//1:1
    int CROP_169 = -1;//16:9
    int CROP_916 = -2;//9:16
    int CROP_43 = 3;
    int CROP_34 = 4;
    int CROP_45 = 5;
    int CROP_23 = 6;
    int CROP_32 = 7;
    int CROP_12 = 8;
    int CROP_21 = 9;
    int CROP_67 = 10;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CROP_ORIGINAL, CROP_FREE, CROP_1, CROP_169, CROP_916, CROP_43, CROP_34, CROP_45, CROP_23, CROP_32, CROP_12, CROP_21, CROP_67})
    @interface CropMode {

    }

}
