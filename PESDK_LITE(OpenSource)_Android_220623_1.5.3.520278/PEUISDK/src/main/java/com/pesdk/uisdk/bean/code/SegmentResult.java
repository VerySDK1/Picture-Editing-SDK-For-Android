package com.pesdk.uisdk.bean.code;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * 抠图结果
 */
public interface SegmentResult {
    int AI_SUCCESS = 1; //有ai
    int AI_FAILED = -1;//无ai
    int None = 0;//尚未检测

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({None, AI_SUCCESS, AI_FAILED})
    @interface reuslt {

    }
}
