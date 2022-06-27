package com.vesdk.camera.listener

import android.graphics.Bitmap

/**
 * 分割
 */
interface OnEngineSegmentationListener {

    /**
     * 成功
     */
    fun onSuccess(bitmap: Bitmap)

    /**
     * 失败
     */
    fun onFail(msg: String)

}