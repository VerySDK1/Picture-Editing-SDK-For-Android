package com.pesdk.uisdk.bean.code;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * 抠图类型
 */
public interface Segment {
    int SEGMENT_PERSON = 1; //扣人像
    int SEGMENT_SKY = 2;//扣天空
    int NONE = 0; //不抠图

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SEGMENT_PERSON, SEGMENT_SKY})
    @interface Type {

    }
}
