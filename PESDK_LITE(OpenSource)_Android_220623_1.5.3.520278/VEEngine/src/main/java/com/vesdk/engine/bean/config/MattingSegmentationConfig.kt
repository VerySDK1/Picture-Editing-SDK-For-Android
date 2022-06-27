package com.vesdk.engine.bean.config

import com.vecore.matting.Mattinger
import com.vecore.matting.MattingerFactory
import com.vesdk.engine.bean.base.BaseSegmentationConfig

class MattingSegmentationConfig(
    /**
     * 模式
     */
    var mode: Int,
    /**
     * 地址
     */
    var binPath: String,
    /**
     * 地址
     */
    var protoPath: String,
) : BaseSegmentationConfig() {

    fun initDetector(): Mattinger {
        val options = MattingerFactory.MattingerOption()
            .setModel(mode, binPath, protoPath)
        return MattingerFactory.getMattinger(options)
    }

    override fun isSameConfig(config: BaseSegmentationConfig): Boolean {
        if (config is MattingSegmentationConfig) {
            return mode == config.mode && binPath == config.binPath && protoPath == config.protoPath
        }
        return false
    }

}