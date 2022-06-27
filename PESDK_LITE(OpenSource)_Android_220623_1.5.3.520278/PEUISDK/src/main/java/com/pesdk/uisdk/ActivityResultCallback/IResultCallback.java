package com.pesdk.uisdk.ActivityResultCallback;

import android.graphics.RectF;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.vecore.models.PEImageObject;

/**
 * 回调onActivityResult
 */
public interface IResultCallback {
    /**
     * 增加当个layer
     */
    void addLayer(String path);


    /**
     * 编辑媒体 （裁剪|比例可能变化）
     */
    void onEditResult(RectF clipRectF, @Crop.CropMode int mode);

    /**
     * 美颜
     *
     * @param isAdd      true 新增；false 编辑
     * @param filterInfo
     * @param hairMedia 可能带有美发
     */
    void onBeautyResult(boolean isAdd, FilterInfo filterInfo,String hairMedia);

    /**
     * 抠图
     */
    void onSegmentResult(String maskPath);

    /**
     * 消除笔
     */
    void onEraseResult(PEImageObject imageObject);


    /**
     * 替换
     */
    void replaceLayer(String path);


}
