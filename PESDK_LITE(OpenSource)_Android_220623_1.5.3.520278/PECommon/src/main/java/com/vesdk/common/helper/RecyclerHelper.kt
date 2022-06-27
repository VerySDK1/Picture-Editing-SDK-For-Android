package com.vesdk.common.helper

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 刷新
 */
fun <T> BaseQuickAdapter<T, BaseViewHolder>.refreshItem(vararg position: Int) {
    for (i in position) {
        if (i > -1) {
            this.notifyItemChanged(i)
        }
    }
}

/**
 * 列表初始化
 */
fun <VH : RecyclerView.ViewHolder> RecyclerView.init(
    adapter: RecyclerView.Adapter<VH>,
    manager: RecyclerView.LayoutManager
) {
    //布局管理
    layoutManager = manager
    this.adapter = adapter
    //固定
    setHasFixedSize(true)
    //动画
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
}

/**
 * 列表初始化
 */
fun <VH : RecyclerView.ViewHolder> RecyclerView.init(
    adapter: RecyclerView.Adapter<VH>,
    context: Context,
    orientation: Int
) {
    //布局管理
    layoutManager = WrapContentLinearLayoutManager(context, orientation, false)
    this.adapter = adapter
    //固定
    setHasFixedSize(true)
    //动画
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
}