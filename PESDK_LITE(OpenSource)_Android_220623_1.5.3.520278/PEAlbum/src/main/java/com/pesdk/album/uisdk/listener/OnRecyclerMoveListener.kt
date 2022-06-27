package com.pesdk.album.uisdk.listener

import androidx.recyclerview.widget.RecyclerView

interface OnRecyclerMoveListener {

    /**
     * 当拖拽的时候回调
     * 可以在此方法实现拖拽条目，并实现刷新效果
     * @param fromPosition 从什么位置拖拽
     * @param toPosition 到什么位置
     * @return  是否执行
     */
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    /**
     * 当条目被移除的回调
     */
    fun onItemRemove(position: Int): Boolean

    /**
     * 是否长按拖拽
     */
    val isLongPressDragEnabled: Boolean

    /**
     * 是否滑动
     */
    val isItemViewSwipeEnabled: Boolean

    /**
     * 结束
     */
    fun clearView(viewHolder: RecyclerView.ViewHolder?)

    /**
     * 开始
     */
    fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int)

}