package com.pesdk.demo.ui.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.demo.R
import com.pesdk.demo.ui.SelectDialog

/**
 * 弹窗选项
 */
class DialogOptionAdapter(data: MutableList<SelectDialog.SelectOption>) :
    BaseQuickAdapter<SelectDialog.SelectOption, BaseViewHolder>(R.layout.flow_item_dialog_option, data) {

    override fun convert(holder: BaseViewHolder, item: SelectDialog.SelectOption) {
        val view = holder.getView<TextView>(R.id.tvSelect)
        view.text = item.name
        view.setCompoundDrawables(item.drawable, null, null, null)
    }

}