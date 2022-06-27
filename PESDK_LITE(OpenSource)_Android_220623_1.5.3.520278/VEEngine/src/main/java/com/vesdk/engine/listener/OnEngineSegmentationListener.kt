package com.vesdk.engine.listener

import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 * 分割
 */
interface OnEngineSegmentationListener {

    /**
     * 成功
     *
     * @param bitmap 分割
     */
    fun onSuccess(bitmap: Bitmap)

    /**
     * mlkit 成功
     *
     * @param buffer 分割原始buffer
     */
    fun onSuccess(buffer: ByteBuffer, width: Int, height: Int)

    /**
     * 失败
     *
     * @param msg 错误
     */
    fun onFail(code: Int, msg: String)


}