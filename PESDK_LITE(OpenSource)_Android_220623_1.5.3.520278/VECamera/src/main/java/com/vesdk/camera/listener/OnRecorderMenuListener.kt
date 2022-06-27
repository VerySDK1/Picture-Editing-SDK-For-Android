package com.vesdk.camera.listener

/**
 * 录制菜单
 */
interface OnRecorderMenuListener {

    /**
     * 成功
     */
    fun onNext()

    /**
     * 关闭
     */
    fun onCancel()

    /**
     * 录制
     */
    fun onRecorder(photo: Boolean)

    /**
     * 音乐
     */
    fun onMusic()

    /**
     * 滤镜
     */
    fun onFilter()

    /**
     * 美颜
     */
    fun onBeauty()

}