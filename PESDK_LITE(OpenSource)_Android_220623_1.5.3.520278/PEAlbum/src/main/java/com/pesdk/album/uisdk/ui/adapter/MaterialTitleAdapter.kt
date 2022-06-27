package com.pesdk.album.uisdk.ui.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.uisdk.bean.Sort
import com.vesdk.common.helper.refreshItem

/**
 * 素材库标题列表
 */
class MaterialTitleAdapter(list: MutableList<Sort>?) :
    BaseQuickAdapter<Sort, BaseViewHolder>(R.layout.album_item_material_title, list) {

    private var lastCheck = 0

    override fun convert(holder: BaseViewHolder, item: Sort) {
        val tvTitle = holder.getView<TextView>(R.id.tvTitle)
        tvTitle.isPressed = holder.layoutPosition == lastCheck
        tvTitle.text = item.name
    }

    /**
     * 设置选中
     */
    fun setCheck(position: Int) {
        if (position != lastCheck) {
            val oldCheck = lastCheck;
            lastCheck = position
            refreshItem(oldCheck, lastCheck)
        }
    }

}