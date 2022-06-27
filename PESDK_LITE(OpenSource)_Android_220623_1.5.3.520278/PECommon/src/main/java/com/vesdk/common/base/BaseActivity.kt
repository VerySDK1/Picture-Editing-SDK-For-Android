package com.vesdk.common.base

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.pesdk.R
import com.pesdk.ui.dialog.LoadingDialog
import com.vesdk.common.bean.Permission
import com.vesdk.common.listener.PopupDialogListener
import com.vesdk.common.ui.dialog.OptionsDialog
import com.vesdk.common.ui.dialog.PermissionDialog
import com.vesdk.common.utils.LoadingUtils
import com.vesdk.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 * activity基类
 */
abstract class BaseActivity : com.pesdk.base.BaseActivity() {

    /**
     * 进度
     */
    private var mLoadingDialog: LoadingDialog? = null

    /**
     * 权限
     */
    private lateinit var mPermissionsContract: ActivityResultLauncher<Array<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        //注册合同
        initRegisterContract()
        //权限
        lifecycleScope.launch {
            initPermissions()?.let { list ->
                checkPermission(list)
            } ?: kotlin.run {
                //初始化
                init()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
        System.gc()
        System.runFinalization()
    }


    /**
     * 跳转
     */
    protected open fun initRegisterContract() {
        //权限
        mPermissionsContract =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                //授权
                var authorization = true
                //拒绝不在弹窗
                var isShowRationale = false
                for (map in it) {
                    if (!map.value) {
                        authorization = false
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, map.key)) {
                            isShowRationale = true
                        }
                    }
                }
                when {
                    authorization -> {
                        //取得权限
                        permissionsSuccess()
                    }
                    else -> {
                        onPermissionsFailAlert()
                    }
                }
            }
    }

    /**
     * 初始注册权限
     */
    protected open suspend fun initPermissions(): MutableList<Permission>? {
        return null
    }

    /**
     * 检查权限
     */
    protected fun checkPermission(list: MutableList<Permission>, tip: Boolean = false) {
        //权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()
            val tipList = mutableListOf<Permission>()
            for (item in list) {
                if (checkSelfPermission(item.key) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(item.key)
                    tipList.add(item)
                }
            }
            if (permissions.isNotEmpty()) {
                if (tip) {
                    onPermissionsAlert(tipList)
                } else {
                    mPermissionsContract.launch(permissions.toTypedArray())
                }
                return
            }
        }
        permissionsSuccess()
    }

    /**
     * 权限成功
     */
    protected open fun permissionsSuccess() {

    }

    /**
     * 权限取消
     */
    protected open fun permissionsCancel() {
        onToast(getString(R.string.common_permission_cancel))
        finish()
    }


    /**
     * 初始化
     */
    abstract fun init()

    /**
     * 设置布局
     */
    abstract fun getLayoutId(): Int


    /**
     * 提示信息
     */
    protected fun onToast(msg: String) {
        ToastUtils.show(this, msg)
    }

    /**
     * 提示信息
     */
    protected fun onToast(id: Int) {
        onToast(getString(id))
    }

    /**
     * 显示 loading
     */
    protected fun showLoading(
        msg: String = getString(R.string.common_loading),
        cancelable: Boolean = false,
        outside: Boolean = false
    ) {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingUtils.showLoading(this, msg, cancelable, outside)
        }
        mLoadingDialog?.let {
            it.showDialog()
            it.setMessage(msg)
        }
    }

    /**
     * 隐藏 loading
     */
    protected fun hideLoading() {
        mLoadingDialog?.close()
    }


    /**
     * 权限提示弹窗
     */
    private fun onPermissionsAlert(list: MutableList<Permission>) {
        lifecycleScope.launch {
            if (list.isEmpty()) {
                permissionsSuccess()
            } else {
                PermissionDialog.create(
                    this@BaseActivity, list,
                    object : PopupDialogListener {

                        override fun onDialogSure() {
                            val permissionList = mutableListOf<String>()
                            for (item in list) {
                                permissionList.add(item.key)
                            }
                            mPermissionsContract.launch(permissionList.toTypedArray())
                        }

                        override fun onDialogCancel() {
                            permissionsCancel()
                        }
                    },
                    onDismissListener = null
                ).show()
            }
        }
    }

    /**
     * 权限弹窗弹窗
     */
    private fun onPermissionsFailAlert() {
        OptionsDialog.create(
            this, getString(R.string.common_dialog_title),
            getString(R.string.common_dialog_message_permission),
            getString(R.string.common_authorization), getString(R.string.common_exit),
            cancelable = false,
            cancelTouch = false,
            listener = object : PopupDialogListener {

                override fun onDialogSure() {
                    actionPermissionSetting()
                }

                override fun onDialogCancel() {
                    finish()
                }

            },
            onDismissListener = null
        ).show()
    }

    /**
     * 权限设置
     */
    private fun actionPermissionSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
        finish()
    }

}