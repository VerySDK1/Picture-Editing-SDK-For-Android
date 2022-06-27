package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;

/**
 *
 */
public interface SegmentationEngine {
    /**
     * 创建分割引擎
     *
     * @param mode      仅MattingEngine 时参数有效,  0 人像抠图; 1 天空抠图
     * @param binPath
     * @param paramPath
     */
    void createAnalyzer(int mode, String binPath, String paramPath);

    /**
     * 释放
     */
    void release();


    /**
     * 异步
     */
    void asyncProcess(Bitmap bitmap, OnSegmentationListener listener);

    /**
     * 同步
     */
    void syncProcess(Bitmap bitmap, OnSegmentationListener listener);

}
