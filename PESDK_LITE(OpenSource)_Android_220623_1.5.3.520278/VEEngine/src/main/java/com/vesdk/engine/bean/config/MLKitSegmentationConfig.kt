package com.vesdk.engine.bean.config

import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.vesdk.engine.bean.base.BaseSegmentationConfig
import com.vesdk.engine.segmentation.SegmentationEngine.Companion.MODE_USE_RAW_BUFFER_RESULT

class MLKitSegmentationConfig(
        /**
         * 模式
         */
        var mode: Int = MODE_USE_RAW_BUFFER_RESULT
) : BaseSegmentationConfig() {

    fun initDetector(): Segmenter {
        val options = SelfieSegmenterOptions.Builder()
                //SINGLE_IMAGE_MODE 不会分析先前帧  STREAM_MODE会分析先前帧
                .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
                .enableRawSizeMask()
                .build()
        return Segmentation.getClient(options)
    }

    override fun isSameConfig(config: BaseSegmentationConfig): Boolean {
        if (config is MLKitSegmentationConfig) {
            return mode == config.mode
        }
        return false
    }

}