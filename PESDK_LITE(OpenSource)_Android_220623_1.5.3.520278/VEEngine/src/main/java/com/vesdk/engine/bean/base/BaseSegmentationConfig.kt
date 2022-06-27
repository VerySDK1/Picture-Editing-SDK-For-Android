package com.vesdk.engine.bean.base

/**
 * 分割配置
 */
abstract class BaseSegmentationConfig {

    /**
     * 相同配置
     */
    abstract fun isSameConfig(config: BaseSegmentationConfig): Boolean

}