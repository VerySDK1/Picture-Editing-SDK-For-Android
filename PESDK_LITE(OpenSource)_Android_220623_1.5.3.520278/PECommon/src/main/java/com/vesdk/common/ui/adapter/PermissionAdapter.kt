package com.vesdk.common.ui.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.vesdk.common.bean.Permission
import com.pesdk.helper.loadImage
import com.pesdk.R

/**
 * 权限
 */
class PermissionAdapter(list: MutableList<Permission>) :
        BaseQuickAdapter<Permission, BaseViewHolder>(R.layout.common_item_permission, list) {

    override fun convert(holder: BaseViewHolder, item: Permission) {
        with(holder) {
            if (item.icon == 0) {
                setGone(R.id.ivIcon, true)
                setGone(R.id.tvTitle, false)
                if (item.title.isNotEmpty()) {
                    setText(R.id.tvTitle, item.title.substring(0, 1))
                } else {
                    setText(R.id.tvTitle, "")
                }
            } else {
                setGone(R.id.ivIcon, false)
                setGone(R.id.tvTitle, true)
                getView<ImageView>(R.id.ivIcon).loadImage(item.icon)
            }
            setText(R.id.tvPermission, item.title)
            setText(R.id.tvMemo, item.memo)
        }
    }

}