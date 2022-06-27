package com.vesdk.engine.face

import android.graphics.Bitmap
import android.graphics.PointF
import com.vesdk.engine.bean.base.BaseFaceConfig
import com.vesdk.engine.listener.OnEngineFaceListener

/**
 * 人脸引擎
 */
abstract class FaceEngine {

    /**
     * 配置相同
     */
    abstract fun isSameConfig(config: BaseFaceConfig): Boolean

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
    abstract fun asyncProcess(bitmap: Bitmap, listener: OnEngineFaceListener)

    /**
     * 同步
     */
    abstract suspend fun asyncProcess(bitmap: Bitmap): Array<PointF?>?

}