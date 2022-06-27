package com.pesdk.album.uisdk.ui.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.uisdk.bean.DirectoryInfo
import com.pesdk.helper.loadImageCenterCrop
import com.vesdk.common.helper.refreshItem
import kotlin.math.max

/**
 * 目录
 */
class DirectoryAdapter(private val directoryList: MutableList<DirectoryInfo>) :
    BaseQuickAdapter<DirectoryInfo, BaseViewHolder>(R.layout.album_item_directory, directoryList) {

    private var mLastCheck = 0

    override fun convert(holder: BaseViewHolder, item: DirectoryInfo) {
        //封面
        holder.getView<ImageView>(R.id.ivIcon)
            .loadImageCenterCrop(item.image?.dataPath, R.drawable.pecom_ic_default)
        holder.setText(R.id.tvName, item.name)
        holder.setText(R.id.tvSize, item.size)
        holder.setVisible(R.id.ivSelected, mLastCheck == holder.layoutPosition)
    }

    /**
     * 设置选中
     */
    fun setCheck(name: String?) {
        val oldCheck = mLastCheck
        mLastCheck = max(directoryList.indexOfLast { name == it.name }, 0)
        refreshItem(oldCheck, mLastCheck)
    }

}