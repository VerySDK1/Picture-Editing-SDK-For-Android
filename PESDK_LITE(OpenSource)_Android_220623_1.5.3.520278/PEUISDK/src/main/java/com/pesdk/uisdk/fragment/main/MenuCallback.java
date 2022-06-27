package com.pesdk.uisdk.fragment.main;

import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;

/**
 * 主编辑的菜单功能
 */
public interface MenuCallback {

    void onAddLayer();

    void onSticker(StickerInfo stickerInfo);

    void onText(WordInfoExt editInfo);

    void onFilter();

    void onBeauty();

    void onBlur();

    /**
     * 比例
     */
    void onProportion();

    void onCrop();

    void onGraffiti();

    void onAdjust();

    void onWatermark();

    /**
     * 图层-调节
     */
    void onPip();

    void onErase();

    void onCanvas();

    void onSky();

    void onKoutu();

    /**
     * 叠加
     */
    void onOverlay();

    /**
     * 边框
     */
    void onFrame();

    @Deprecated
    void onMosaic();

    void onMask();

    void onDepth();


    /**
     * 左右镜像
     */
    void onMirrorLeftright();

    /**
     * 上下镜像
     */
    void onMirrorUpDown();

    /**
     * 替换
     */
    void onReplace();

    /**
     * 微调
     */
    void onRectAdjust();

    /**
     * 回到主样式
     */
    void onBack2Main();
}
