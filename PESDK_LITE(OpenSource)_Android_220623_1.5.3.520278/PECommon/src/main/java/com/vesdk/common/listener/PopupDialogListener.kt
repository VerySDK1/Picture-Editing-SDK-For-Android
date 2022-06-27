package com.vesdk.common.listener

interface PopupDialogListener {
    /**
     * 确定
     */
    fun onDialogSure()

    /**
     * 取消
     */
    fun onDialogCancel()
}