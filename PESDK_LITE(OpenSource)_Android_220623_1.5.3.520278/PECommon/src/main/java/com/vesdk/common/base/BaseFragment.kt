package com.vesdk.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.pesdk.R
import com.pesdk.ui.dialog.LoadingDialog
import com.vesdk.common.utils.LoadingUtils
import com.vesdk.common.utils.ToastUtils

/**
 * 基类
 */
abstract class BaseFragment : Fragment() {

    /**
     * 进度
     */
    private var mLoadingDialog: LoadingDialog? = null

    /**
     * 布局
     */
    private var mRoot: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initArguments()
        }
        initRegisterContract()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mRoot = inflater.inflate(getLayoutId(), container, false)
        return mRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRoot?.let {
            init()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(requireContext()).clearMemory()
    }

    /**
     * 跳转
     */
    protected open fun initRegisterContract() {

    }

    /**
     * 初始化传入
     */
    protected open fun initArguments() {

    }

    /**
     * 返回
     */
    open fun onBackPressed(): Int {
        return -1
    }

    /**
     * 初始化
     */
    abstract fun init()

    /**
     * 布局id
     */
    abstract fun getLayoutId(): Int



    /**
     * 提示信息
     */
    protected fun onToast(msg: String) {
        context?.let {
            ToastUtils.show(it, msg)
        }
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
        val context = context
        if (mLoadingDialog == null && context != null) {
            mLoadingDialog = LoadingUtils.showLoading(context, msg, cancelable, outside)
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
}