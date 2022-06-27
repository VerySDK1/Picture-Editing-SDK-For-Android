package com.vesdk.common.utils

import android.content.Context
import com.pesdk.ui.dialog.LoadingDialog

/**
 * 等待
 */
object LoadingUtils {

    /**
     * 显示 loading
     */
    fun showLoading(
        context: Context,
        msg: String,
        cancelable: Boolean = false,
        outside: Boolean = false,
        listener: LoadingDialog.OnLoadingListener? = null
    ): LoadingDialog {
        val dialog = with(LoadingDialog.Builder(context)) {
            setCancelable(cancelable)
            setCanceledOnTouchOutside(outside)
            setListener(listener)
            create()
        }
        dialog.showDialog()
        dialog.setMessage(msg)
        return dialog
    }

    /**
     * 隐藏 loading
     */
    fun hideLoading(loadingDialog: LoadingDialog?) {
        loadingDialog?.close()
    }

}