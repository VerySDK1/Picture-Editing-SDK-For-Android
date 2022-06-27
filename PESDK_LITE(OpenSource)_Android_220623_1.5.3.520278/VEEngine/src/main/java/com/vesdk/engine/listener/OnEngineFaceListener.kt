package com.vesdk.engine.listener

import android.graphics.PointF

/**
 * 人脸
 */
interface OnEngineFaceListener {

    /**
     * 成功
     * @param facePointF 人脸
     * @param fivePointF 五官
     * @param asp 比例
     */
    fun onSuccess(facePointF: Array<PointF?>?, fivePointF: Array<PointF?>?, asp: Float)

    /**
     * 失败
     * @param msg 错误
     */
    fun onFail(code: Int, msg: String)
}