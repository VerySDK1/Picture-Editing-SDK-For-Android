package com.vesdk.engine.segmentation

import android.graphics.Bitmap
import com.vecore.matting.MattingerFactory
import com.vesdk.engine.bean.base.BaseSegmentationConfig
import com.vesdk.engine.listener.OnEngineSegmentationListener

/**
 * 分割引擎
 */
abstract class SegmentationEngine {

    companion object {

        /**
         * 人像
         */
        const val MODEL_PORTRAIT = MattingerFactory.MattingerOption.PORTRAIT_MATTING

        /**
         * 天空
         */
        const val MODEL_SKY = MattingerFactory.MattingerOption.SKY_MATTING

        /**
         * 使用bitmap
         */
        const val MODE_USE_BITMAP_RESULT = 3

        /**
         * 使用原始数据
         */
        const val MODE_USE_RAW_BUFFER_RESULT = 4

    }

    /**
     * 配置相同
     */
    abstract fun isSameConfig(config: BaseSegmentationConfig): Boolean

    /**
     * 创建
     */
    abstract fun create()

    /**
     * 释放
     */
    abstract fun release()

    /**
     * 异步
     */
    abstract fun asyncProcess(bitmap: Bitmap, listener: OnEngineSegmentationListener)

    /**
     * 同步
     */
    abstract suspend fun syncProcess(bitmap: Bitmap): Bitmap

}