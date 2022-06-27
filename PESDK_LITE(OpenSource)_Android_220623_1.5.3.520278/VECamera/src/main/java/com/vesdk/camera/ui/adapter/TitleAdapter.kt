package com.vesdk.camera.ui.adapter

import android.widget.CheckedTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.vecore.utils.Log
import com.vesdk.camera.R
import com.vesdk.camera.bean.Sort
import com.vesdk.common.helper.refreshItem

/**
 * 素材库标题列表
 */
class TitleAdapter(list: MutableList<Sort>?) :
        BaseQuickAdapter<Sort, BaseViewHolder>(R.layout.camera_item_title, list) {

    private var lastCheck = 0

    override fun convert(holder: BaseViewHolder, item: Sort) {
        val tvTitle = holder.getView<CheckedTextView>(R.id.tvTitle)
        tvTitle.isChecked = holder.layoutPosition == lastCheck
        tvTitle.text = item.name
    }

    val TAG = "TitleAdapter"

    /**
     * 设置选中
     */
    fun setCheck(position: Int) {
        if (position != lastCheck) {
            val oldChecked = lastCheck
            lastCheck = position
            refreshItem(oldChecked, lastCheck)
        }
    }

}