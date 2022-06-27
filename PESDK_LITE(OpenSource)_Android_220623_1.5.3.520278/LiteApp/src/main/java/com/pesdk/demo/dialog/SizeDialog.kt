package com.pesdk.demo.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.pesdk.demo.R
import com.vecore.base.lib.utils.CoreUtils

/**
 * 导出大小
 */
class SizeDialog private constructor(context: Context, id: Int) : Dialog(context, id) {

    class Builder @SuppressLint("InflateParams") constructor(private val mContext: Context) {

        @SuppressLint("InflateParams")
        private val mView: View = LayoutInflater.from(mContext).inflate(R.layout.dialog_size, null)

        private var mListener: OnClickSizeListener? = null

        private fun initView() {

            mView.findViewById<View>(R.id.btn480).setOnClickListener { v: View? ->
                mListener?.onMinSide(480)
            }
            mView.findViewById<View>(R.id.btn720).setOnClickListener { v: View? ->
                mListener?.onMinSide(720)
            }
            mView.findViewById<View>(R.id.btn1080).setOnClickListener { v: View? ->
                mListener?.onMinSide(1080)
            }
            mView.findViewById<View>(R.id.btn2k).setOnClickListener { v: View? ->
                mListener?.onMinSide(2160)
            }

            //取消
            mView.findViewById<View>(R.id.btnClose).setOnClickListener { v: View? ->
                mListener?.onCancel()
            }
        }

        fun setListener(listener: OnClickSizeListener?): Builder {
            mListener = listener
            return this
        }

        fun create(): SizeDialog {
            val menuDialog = SizeDialog(mContext, R.style.dialog_style)
            menuDialog.setContentView(mView)
            menuDialog.window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout((CoreUtils.getMetrics().widthPixels * 0.8).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.CENTER_VERTICAL) // 此处可以设置dialog显示的位置
                setWindowAnimations(R.style.dialog_anim) // 添加动画
            }
            menuDialog.setCanceledOnTouchOutside(false)
            menuDialog.setCancelable(false)
            return menuDialog
        }

        init {
            initView()
        }
    }

    interface OnClickSizeListener {
        /**
         * 设置确定
         */
        fun onMinSide(side: Int)

        /**
         * 取消
         */
        fun onCancel()
    }

}