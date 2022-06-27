package com.pesdk.demo.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pesdk.demo.R
import com.pesdk.demo.ui.adapter.DialogOptionAdapter
import com.vecore.annotation.Keep
import java.util.*

/**
 * 导出
 */
@Keep
class SelectDialog private constructor(context: Context, id: Int) : Dialog(context, id) {

    class Builder constructor(private val mContext: Context) {

        @SuppressLint("InflateParams")
        private val mView: View =
            LayoutInflater.from(mContext).inflate(R.layout.flow_dialog_select, null)

        /**
         * listener
         */
        private var mListener: OnClickSelectListener? = null

        /**
         * 是否可以点击外部取消
         */
        private var mIsCancelable = true
        private var mIsCanceledOnTouchOutside = true

        /**
         * Adapter
         */
        private val mAdapter: DialogOptionAdapter
        private val mArrayList = ArrayList<SelectOption>()

        fun setOption(option: MutableList<SelectOption>): Builder {
            mArrayList.clear()
            mArrayList.addAll(option)
            mAdapter.notifyDataSetChanged()
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

        fun setListener(listener: OnClickSelectListener): Builder {
            mListener = listener
            return this
        }

        fun create(): SelectDialog {
            return SelectDialog(mContext, R.style.flow_dialog_style).apply {
                setContentView(mView)
                window?.apply {
                    setBackgroundDrawableResource(android.R.color.transparent)
                    setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setGravity(Gravity.BOTTOM) // 此处可以设置dialog显示的位置
                    setWindowAnimations(R.style.flow_dialog_anim_bottom) // 添加动画
                }
                setCanceledOnTouchOutside(mIsCanceledOnTouchOutside)
                setCancelable(mIsCancelable)
            }
        }

        init {
            val rvOption: RecyclerView = mView.findViewById(R.id.rv_option)
            rvOption.layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            mAdapter = DialogOptionAdapter(mArrayList)
            rvOption.adapter = mAdapter
            mAdapter.setOnItemClickListener { adapter, view, position ->
                mListener?.onSelect(position)
            }
        }
    }

    interface OnClickSelectListener {
        /**
         * 选项1
         */
        fun onSelect(index: Int)
    }

    /**
     * 选项
     */
    class SelectOption(var name: String, var drawable: Drawable)
}