package com.vesdk.camera.ui.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.vesdk.camera.R
import com.pesdk.helper.loadImageCenterCrop

class RecorderVideoAdapter(list: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.camera_item_video, list) {
    
    override fun convert(holder: BaseViewHolder, item: String) {
        //封面
        holder.getView<ImageView>(R.id.ivCover)
            .loadImageCenterCrop(item, R.drawable.pecom_ic_default)
        //序号
        holder.setText(R.id.tvMediaNum, (holder.layoutPosition + 1).toString())
    }

}