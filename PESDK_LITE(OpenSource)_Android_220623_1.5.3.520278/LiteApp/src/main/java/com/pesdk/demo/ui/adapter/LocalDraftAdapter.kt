package com.pesdk.demo.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.pesdk.api.IVirtualImageInfo
import com.pesdk.demo.R
import com.pesdk.demo.utils.Utils
import com.pesdk.utils.glide.GlideUtils

open class LocalDraftAdapter(private var requestManager: RequestManager) :
    RecyclerView.Adapter<LocalDraftAdapter.VH>() {

    private var mList = emptyList<IVirtualImageInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: List<IVirtualImageInfo>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item_draft_layout, parent, false)
        val vh = VH(itemView)
        val viewClickListener = ItemListener()
        vh.ivCover.setOnClickListener(viewClickListener)
        vh.ivCover.tag = viewClickListener
        return vh
    }

    fun getItem(position: Int): IVirtualImageInfo {
        return mList[position]
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        (holder.ivCover.tag as ItemListener).setPosition(position)
        val tmp = getItem(position)
        GlideUtils.setCover(requestManager, holder.ivCover, tmp.cover)
        holder.tvUpdate.text = Utils.getUpdateTime(holder.tvUpdate.context, tmp.updateTime)
        holder.tvCreate.text = Utils.getDateString(tmp.createTime)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivCover: ImageView = com.pesdk.uisdk.util.Utils.`$`(itemView, R.id.ivCover)
        var tvCreate: TextView = com.pesdk.uisdk.util.Utils.`$`(itemView, R.id.tvCreate)
        var tvUpdate: TextView = com.pesdk.uisdk.util.Utils.`$`(itemView, R.id.tvUpdate)

    }

    protected inner class ItemListener : OnMultiClickListener() {

        private var position = 0

        fun setPosition(position: Int) {
            this.position = position
        }

        override fun onSingleClick(view: View?) {
            mOnItemClickListener?.onItemClick(position, getItem(position))
        }
    }

    /**
     * 设置单击事件
     */
    fun setOnItemClickListener(listener: OnItemClickListener<IVirtualImageInfo>?) {
        mOnItemClickListener = listener
    }

    protected var mOnItemClickListener: OnItemClickListener<IVirtualImageInfo>? = null

}