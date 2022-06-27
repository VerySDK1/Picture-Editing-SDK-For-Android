package com.vesdk.camera.listener

import android.view.MotionEvent

/**
 * 相机
 */
interface OnCameraViewListener {

    /**
     * 向左切换
     */
    fun onSwitchFilterToLeft()

    /**
     * 向右切换
     */
    fun onSwitchFilterToRight()

    /**
     * 单击
     */
    fun onSingleTapUp(e: MotionEvent?)

    /**
     * 双击
     */
    fun onDoubleTap(e: MotionEvent?)

    /**
     * 即将开始切换滤镜(准备同时绘制两个滤镜)
     *
     * @param leftToRight      true 从左往右，否则从右往左;
     * @param filterProportion 左边滤镜所占百分比
     */
    fun onFilterChangeStart(leftToRight: Boolean, filterProportion: Double)

    /**
     * 左右实时滑动滤镜
     *
     * @param leftToRight       true 从左往右，否则从右往左;
     * @param mFilterProportion 左边滤镜所占百分比
     */
    fun onFilterChanging(leftToRight: Boolean, mFilterProportion: Double)

    /**
     * 滑动滤镜结束(绘制完整的单个滤镜)
     */
    fun onFilterChangeEnd()

    /**
     * 手势离开取消滤镜
     *
     * @param leftToRight       true 从左往右，否则从右往左;
     * @param mFilterProportion 左边滤镜所占百分比
     */
    fun onFilterCanceling(leftToRight: Boolean, mFilterProportion: Double)

    /**
     * 取消切换滤镜
     */
    fun onFilterChangeCanceled()

}