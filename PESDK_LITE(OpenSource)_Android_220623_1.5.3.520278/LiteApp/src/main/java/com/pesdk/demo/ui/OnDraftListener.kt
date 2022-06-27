package com.pesdk.demo.ui

import com.pesdk.uisdk.fragment.BaseFragment


/**
 * 草稿
 */
interface OnDraftListener {

    /**
     * 返回
     */
    fun onCancel(fragment: BaseFragment)

    /**
     * 编辑
     */
    fun onClickFlow(id: Long)

    /**
     * 备份
     */
    fun onClickBackup()

    /**
     * 导出
     */
    fun onClickExport()

}