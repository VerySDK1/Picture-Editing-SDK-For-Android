package com.pesdk.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pesdk.R
import com.pesdk.helper.loadGif

/**
 * 加载等待
 */
class LoadingDialog(context: Context, themeResId: Int) : Dialog(context, themeResId) {

    /**
     * 进度 标题
     */
    private var mTvMessage: TextView? = null

    /**
     * 回调
     */
    private var mListener: OnLoadingListener? = null

    /**
     * 设置listener
     */
    fun setListener(listener: OnLoadingListener?) {
        mListener = listener
    }

    /**
     * 设置显示消息
     */
    private fun setMessageView(tvProgress: TextView?) {
        mTvMessage = tvProgress
    }

    /**
     * 显示
     */
    fun showDialog() {
        if (!isShowing) {
            show()
        }
    }

    /**
     * 关闭
     */
    fun close() {
        if (isShowing) {
            dismiss()
        }
    }

    /**
     * 消息
     */
    fun setMessage(msg: String) {
        mTvMessage?.text = msg
    }

    override fun onBackPressed() {
        mListener?.onCancel() ?: kotlin.run {
            super.onBackPressed()
        }
    }

    class Builder constructor(private val mContext: Context) {
        /**
         * 布局
         */
        private val mView: View

        /**
         * 是否可以点击外部取消
         */
        private var mIsCancelable = true
        private var mIsCanceledOnTouchOutside = true

        /**
         * 消息
         */
        private val mTvProgress: TextView

        /**
         * 取消
         */
        private val mIvCancel: ImageView?

        /**
         * 回调
         */
        private var mListener: OnLoadingListener? = null

        init {
            @SuppressLint("InflateParams")
            mView = LayoutInflater.from(mContext).inflate(R.layout.common_dialog_pro_loading, null)
            //等待
            val loading = mView.findViewById<ImageView>(R.id.ivLoading)
            loading.loadGif(R.drawable.common_animated)
            //消息
            mTvProgress = mView.findViewById(R.id.tvMsg)
            //返回
            mIvCancel = mView.findViewById(R.id.ivCancel)
            mIvCancel.setOnClickListener {
                mListener?.onCancel()
            }
        }

        fun setListener(listener: OnLoadingListener?): Builder {
            mListener = listener
            if (mListener != null && mIvCancel != null) {
                mIvCancel.visibility = View.VISIBLE
            }
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            mIsCancelable = cancelable
            return this
        }

        fun setCanceledOnTouchOutside(touchOutside: Boolean): Builder {
            mIsCanceledOnTouchOutside = touchOutside
            return this
        }

        fun create(): LoadingDialog {
            val menuDialog = LoadingDialog(mContext, R.style.common_dialog_style)
            menuDialog.setContentView(mView)
            menuDialog.setMessageView(mTvProgress)
            menuDialog.setListener(mListener)
            val window = menuDialog.window
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent)
                //window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER) // 此处可以设置dialog显示的位置
                window.setWindowAnimations(R.style.common_dialog_scale_anim) // 添加动画
            }
            menuDialog.setCanceledOnTouchOutside(mIsCanceledOnTouchOutside)
            menuDialog.setCancelable(mIsCancelable)
            return menuDialog
        }

    }

    interface OnLoadingListener {

        /**
         * 取消
         */
        fun onCancel()

    }
}