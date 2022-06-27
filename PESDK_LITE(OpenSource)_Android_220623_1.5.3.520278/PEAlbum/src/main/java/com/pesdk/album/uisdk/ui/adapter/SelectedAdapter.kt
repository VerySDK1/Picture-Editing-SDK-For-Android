package com.pesdk.album.uisdk.ui.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.api.bean.MediaInfo
import com.pesdk.album.api.bean.MediaType
import com.pesdk.helper.loadImage
import com.pesdk.helper.loadImageCenterCrop
import com.vesdk.common.utils.DateTimeUtils

/**
 * 素材库列表
 */
class SelectedAdapter(list: MutableList<MediaInfo>) :
    BaseQuickAdapter<MediaInfo, BaseViewHolder>(R.layout.album_item_selected, list) {

    override fun convert(holder: BaseViewHolder, item: MediaInfo) {
        val position = holder.layoutPosition
        //封面
        holder.getView<ImageView>(R.id.ivIcon)
            .loadImageCenterCrop(item.path, R.drawable.pecom_ic_default)
        //序号
        holder.setText(R.id.tvMediaNum, (position + 1).toString())
        //类型
        val ivType = holder.getView<ImageView>(R.id.ivItemType)
        val tvDuration = holder.getView<TextView>(R.id.tvDuration)
        when (item.type) {
            MediaType.TYPE_WORD -> {
                ivType.loadImage(R.drawable.album_ic_text)
                tvDuration.visibility = GONE
            }
            MediaType.TYPE_IMAGE -> {
                ivType.loadImage(R.drawable.album_ic_image)
                tvDuration.visibility = GONE
            }
            MediaType.TYPE_VIDEO -> {
                ivType.loadImage(R.drawable.album_ic_video)
                tvDuration.visibility = if (item.duration > 0) VISIBLE else GONE
                tvDuration.text = DateTimeUtils.millisecond2String(item.duration)
            }
        }

    }

    /**
     * 设置拖拽
     */
    fun setDrag(holder: BaseViewHolder, boolean: Boolean) {
        //拖拽
        holder.setVisible(R.id.mask, boolean)
        holder.setVisible(R.id.ivDelete, !boolean)
    }
}