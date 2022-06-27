package com.pesdk.uisdk.fragment.main;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 点击时，允许遍历的层次范围
 */
@Retention(RetentionPolicy.CLASS)
public @interface IClickRange {
    int range_all = 0;//  （文字、叠加、画中画）
    int range_overlay = 1; //（文字、叠加）    在OverlayFragment中，不要响应画中画切换
    int range_none = -1; //不允许点击
}
