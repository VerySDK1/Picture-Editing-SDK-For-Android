package com.pesdk.uisdk.edit.listener;

import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.widget.edit.EditDragView;

/**
 */
public interface VideoKeyframeHandlerListener {

    /**
     * 新版字幕
     */
    void setWordExtProgress(int progress, EditDragView dragView, WordInfoExt info);

    /**
     * 设置贴纸
     */
    void setStickerProgress(int progress, EditDragView dragView, StickerInfo info);
}
