package com.vesdk.engine.bean.base

/**
 * 人脸配置
 */
abstract class BaseFaceConfig {

    /**
     * 相同配置
     */
    abstract fun isSameConfig(config: BaseFaceConfig): Boolean

}