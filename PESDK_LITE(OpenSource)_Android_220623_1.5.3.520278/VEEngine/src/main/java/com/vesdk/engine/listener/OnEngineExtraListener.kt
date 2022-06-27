package com.vesdk.engine.listener

/**
 * 分割
 */
interface OnEngineExtraListener {

    /**
     * 首次加载
     */
    fun init()

    /**
     * 失败
     *
     * @param msg 错误
     */
    fun onFail(code: Int, msg: String)
}