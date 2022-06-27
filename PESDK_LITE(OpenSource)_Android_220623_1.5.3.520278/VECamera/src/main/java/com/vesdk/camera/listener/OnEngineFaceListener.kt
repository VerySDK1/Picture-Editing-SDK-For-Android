package com.vesdk.camera.listener

import android.graphics.PointF

/**
 * 人脸
 */
interface OnEngineFaceListener {

    /**
     * 成功
     */
    fun onSuccess(facePointF: Array<PointF?>, fivePointF: Array<PointF?>, asp: Float)

    /**
     * 失败
     */
    fun onFail(msg: String)

}