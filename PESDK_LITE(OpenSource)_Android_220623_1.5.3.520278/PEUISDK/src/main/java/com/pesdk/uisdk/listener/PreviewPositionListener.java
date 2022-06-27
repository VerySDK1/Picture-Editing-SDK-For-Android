package com.pesdk.uisdk.listener;

/**
 * 播放器进度
 */
public interface PreviewPositionListener {

    /**
     * 响应获取到编辑器预览进度
     */
    void onGetPosition(int position);

}
