package com.vesdk.common.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pesdk.R
import com.vesdk.common.bean.Permission
import com.vesdk.common.listener.PopupDialogListener
import com.vesdk.common.ui.adapter.PermissionAdapter
import com.vesdk.common.utils.CommonUtils.getWidth

/**
 * 权限弹窗
 */
class PermissionDialog private constructor(context: Context)
    : AlertDialog(context, R.style.common_dialog) {

    /**
     * 控件
     */
    private val mView: View
    private val mBtnSure: TextView
    private val mBtnCancel: TextView

    /**
     * 列表
     */
    private val permissionList = mutableListOf<Permission>()
    private var mAdapter: PermissionAdapter

    /**
     * 宽度
     */
    private val mWidth = (getWidth(context) * 0.8).toInt()

    /**
     * 创建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT, 0f)
        setContentView(mView, params)
    }

    /**
     * 设置消息
     */
    private fun setPermission(permissions: MutableList<Permission>) {
        permissionList.clear()
        if (permissions.isNotEmpty()) {
            permissionList.addAll(permissions)
            mAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 回调
     */
    private fun setDialogListener(listener: PopupDialogListener) {
        mBtnCancel.setOnClickListener {
            dismiss()
            listener.onDialogCancel()
        }
        mBtnSure.setOnClickListener {
            dismiss()
            listener.onDialogSure()
        }
    }

    init {
        //点击返回
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        //布局
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams")
        mView = inflater.inflate(R.layout.common_dialog_permission, null)
        mBtnSure = mView.findViewById(R.id.btnSure)
        mBtnCancel = mView.findViewById(R.id.btnCancel)
        mBtnSure.text = context.getText(R.string.common_sure)
        mBtnCancel.text = context.getText(R.string.common_cancel)
        mView.findViewById<TextView>(R.id.tvPermission).text = context.getText(R.string.common_permission_title)

        mAdapter = PermissionAdapter(permissionList)
        mView.findViewById<RecyclerView>(R.id.rvData).apply {
            layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
            setHasFixedSize(true)
        }
    }

    companion object {

        /**
         * 获取对话框
         */
        @JvmStatic
        fun create(
                context: Context,
                permissions: MutableList<Permission>,
                listener: PopupDialogListener,
                onDismissListener: DialogInterface.OnDismissListener?
        ): PermissionDialog {
            val dialog = PermissionDialog(context)
            onDismissListener?.let { dialog.setOnDismissListener(onDismissListener) }
            //设置权限
            dialog.setPermission(permissions)
            //回调
            dialog.setDialogListener(listener)
            return dialog
        }

    }
}