package com.vesdk.engine.listener

import com.vesdk.engine.bean.DetectedObject

/**
 * 检测
 */
interface OnEngineDetectionListener {

    /**
     * 成功
     * @param list 检测对象
     */
    fun onSuccess(list: MutableList<DetectedObject>)

    /**
     * 失败
     * @param msg 错误
     */
    fun onFail(code: Int, msg: String)
}