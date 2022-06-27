package com.pesdk.uisdk.edit.bean;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.vecore.models.EffectInfo;

import java.util.ArrayList;

/**
 * 数据
 */
public interface IParam {

    /**
     * 文字
     */
    ArrayList<WordInfoExt> getWordList();

    /**
     * 贴纸
     */
    ArrayList<StickerInfo> getStickerList();


    /**
     * 涂鸦
     */
    ArrayList<GraffitiInfo> getGraffitList();

    /**
     * 图层
     */
    ArrayList<CollageInfo> getCollageList();

    /**
     * 特效
     */
    ArrayList<EffectInfo> getEffectList();


    /**
     * 滤镜、调色、美颜
     */
    ArrayList<FilterInfo> getFilterList();


    /**
     * 消除笔、编辑、景深、背景
     */
    ExtImageInfo getExtImage();


    ArrayList<FrameInfo> getFrameList();

    /**
     * 叠加
     */
    ArrayList<CollageInfo> getOverLayList();


    /**
     * 普通滤镜
     */
    FilterInfo getFilter();

    /**
     * 调色
     */
    FilterInfo getAdjust();

    /**
     * 美颜
     */
    FilterInfo getBeauty();


    /**
     * 图片比例
     *
     * @return
     */
    @Crop.CropMode
    int getProportionMode();

    float getProportionValue();


}
