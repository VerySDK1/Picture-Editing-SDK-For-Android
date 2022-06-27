package com.vesdk.engine.bean

import com.vecore.models.MediaObject

/**
 * 引擎媒体
 */
class EngineMedia(
        /**
         * 媒体
         */
        val media: MediaObject,
        /**
         * 分割
         */
        var isSegmentation: Boolean = false
)