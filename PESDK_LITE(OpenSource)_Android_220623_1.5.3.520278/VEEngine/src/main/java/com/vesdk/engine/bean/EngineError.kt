package com.vesdk.engine.bean

import androidx.annotation.Keep

/**
 * 引擎错误
 */
@Keep
object EngineError {

    /**
     * 不存在模型
     */
    const val ERROR_MSG_MODEL = "model does not exist"
    const val ERROR_CODE_MODEL = -1

    /**
     * 未初始化
     */
    const val ERROR_MSG_INIT = "uninitialized"
    const val ERROR_CODE_INIT = 0

    /**
     * 未识别
     */
    const val ERROR_MSG_IDENTIFY = "未识别"
    const val ERROR_CODE_IDENTIFY = 1

}