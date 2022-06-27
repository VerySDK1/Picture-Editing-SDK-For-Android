package com.vesdk.camera.listener

import com.vesdk.camera.bean.FilterInfo
import com.vesdk.camera.bean.Sort

/**
 * 滤镜列表
 */
interface OnFilterItemListener {

    /**
     * 选择滤镜
     */
    fun onFilter(sort: Sort, filterInfo: FilterInfo, position: Int)

}