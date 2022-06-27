package com.vesdk.common.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pesdk.R
import com.vesdk.common.listener.PopupDialogListener
import com.vesdk.common.utils.CommonUtils.getWidth

/**
 * 选项弹窗
 */
class OptionsDialog private constructor(
    context: Context,
    cancelable: Boolean,
    canceledOnTouchOutside: Boolean
) : AlertDialog(context, R.style.common_dialog) {

    /**
     * 控件
     */
    private val mView: View
    private val mTvTitle: TextView
    private val mTvMsg: TextView
    private val mBtnSure: TextView
    private val mBtnCancel: TextView
    private val mLlBottom: LinearLayout
    private val mLine: View

    /**
     * 宽度
     */
    private val mWidth: Int = (getWidth(context) * 0.8).toInt()

    init {
        //点击返回
        setCancelable(cancelable)
        setCanceledOnTouchOutside(canceledOnTouchOutside)
        //布局
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams")
        mView = inflater.inflate(R.layout.common_dialog_options, null)
        mTvTitle = mView.findViewById(R.id.tvTitle)
        mTvMsg = mView.findViewById(R.id.tvMessage)
        mBtnSure = mView.findViewById(R.id.btnSure)
        mBtnCancel = mView.findViewById(R.id.btnCancel)
        mLlBottom = mView.findViewById(R.id.llBottom)
        mLine = mView.findViewById(R.id.common_dialog_vertical_line)
        mTvMsg.movementMethod = ScrollingMovementMethod.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT, 0f)
        setContentView(mView, params)
    }

    /**
     * 设置标题
     */
    private fun setDialogTitle(title: CharSequence?) {
        if (TextUtils.isEmpty(title)) {
            mTvTitle.visibility = View.GONE
        } else {
            mTvTitle.text = title
        }
    }

    /**
     * 设置消息
     */
    private fun setDialogMessage(msg: CharSequence?) {
        if (TextUtils.isEmpty(msg)) {
            mTvMsg.visibility = View.GONE
        } else {
            mTvMsg.text = msg
        }
    }

    /**
     * 确定取消按钮 显示和隐藏 whichButton 1确定 2取消
     */
    private fun setDialogButton(
        whichButton: Int,
        text: CharSequence?,
        listener: PopupDialogListener
    ) {
        when (whichButton) {
            STATUE_SURE -> {
                text?.run {
                    mBtnSure.text = text
                    mBtnSure.setOnClickListener {
                        listener.onDialogSure()
                        dismiss()
                    }
                } ?: run {
                    mBtnSure.visibility = View.GONE
                }
            }
            STATUE_CANCEL -> {
                text?.run {
                    mBtnCancel.text = text
                    mBtnCancel.setOnClickListener {
                        listener.onDialogCancel()
                        dismiss()
                    }
                } ?: run {
                    mBtnCancel.visibility = View.GONE
                }
            }
            else -> {
            }
        }
    }

    /**
     * 确定取消按钮 显示和隐藏
     */
    private fun setDialogButton(sure: String?, cancel: String?, listener: PopupDialogListener) {
        //全部为空 不显示
        if (TextUtils.isEmpty(sure) && TextUtils.isEmpty(cancel)) {
            mLlBottom.visibility = View.GONE
        } else {
            mLine.visibility =
                if (!TextUtils.isEmpty(sure) && !TextUtils.isEmpty(cancel)) View.VISIBLE else View.GONE
            //都不为空
            setDialogButton(STATUE_SURE, sure, listener)
            setDialogButton(STATUE_CANCEL, cancel, listener)
        }
    }

    companion object {
        private const val STATUE_SURE = 1
        private const val STATUE_CANCEL = 2

        /**
         * 获取对话框
         */
        @JvmStatic
        fun create(
            context: Context,
            title: String?,
            message: String?,
            sure: String?,
            cancel: String?,
            cancelable: Boolean,
            cancelTouch: Boolean,
            listener: PopupDialogListener,
            onDismissListener: DialogInterface.OnDismissListener?
        ): OptionsDialog {
            val dialog = OptionsDialog(context, cancelable, cancelTouch)
            onDismissListener?.let { dialog.setOnDismissListener(onDismissListener) }
            //设置标题信息
            dialog.setDialogTitle(title)
            dialog.setDialogMessage(message)
            //设置按钮
            dialog.setDialogButton(sure, cancel, listener)
            return dialog
        }

        /**
         * 获取对话框
         */
        @JvmStatic
        fun create(
            context: Context,
            title: String?,
            message: String?,
            cancelable: Boolean,
            cancelTouch: Boolean,
            listener: PopupDialogListener,
            onDismissListener: DialogInterface.OnDismissListener? = null
        ): OptionsDialog {
            val sure = context.resources.getString(R.string.common_sure)
            val cancel = context.resources.getString(R.string.common_cancel)
            return create(
                context, title, message, sure, cancel, cancelable, cancelTouch,
                listener = listener, onDismissListener = onDismissListener
            )
        }

        /**
         * 获取对话框
         */
        @JvmStatic
        fun create(
            context: Context,
            title: String?,
            message: String?,
            listener: PopupDialogListener,
        ): OptionsDialog {
            return create(
                context,
                title,
                message,
                cancelable = false,
                cancelTouch = false,
                listener = listener
            )
        }
    }

}