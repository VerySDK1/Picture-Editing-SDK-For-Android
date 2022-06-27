package com.pesdk.album.uisdk.helper

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.pesdk.album.uisdk.listener.OnRecyclerMoveListener

class RecyclerItemTouchHelper(
    private val vertical: Boolean,
    val listener: OnRecyclerMoveListener
) : ItemTouchHelper.Callback() {

    /**
     * callback回调监听先调用的，用来判断是什么动作，比如判断方向（意思就是我要监听那个方向的拖到）
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val layoutManager = recyclerView.layoutManager
        val dragFlags: Int
        val swipeFlags: Int
        if (layoutManager is GridLayoutManager) {
            // 如果是Grid布局，则不能滑动，只能上下左右拖动
            dragFlags =
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            swipeFlags = 0
        } else {
            if (vertical) {
                dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                swipeFlags =
                    if (isItemViewSwipeEnabled) ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT else 0
            } else {
                dragFlags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                swipeFlags =
                    if (isItemViewSwipeEnabled) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
            }
        }
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    /**
     * 当移动的时候回调的方法
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        //adapter.notifyItemMoved(fromPosition，toPosition);改变拖拽item位置
        //不同type不允许移动
        return if (viewHolder.itemViewType != target.itemViewType) {
            false
        } else {
            listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        }
    }

    /**
     * 侧滑
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //监听侧滑
        // 1:删除数据；
        // 2：adapter.notifyItemRemove（position）
        listener.onItemRemove(viewHolder.adapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        listener.clearView(viewHolder)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        //actionState的取值有
        // ACTION_STATE_IDLE/ACTION_STATE_SWIPE/ACTION_STATE_DRAG
        listener.onSelectedChanged(viewHolder, actionState)
    }

    /**
     * 是否允许长按拖拽
     */
    override fun isLongPressDragEnabled(): Boolean {
        return listener.isLongPressDragEnabled
    }

    /**
     * item是否支持滑动
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return listener.isItemViewSwipeEnabled
    }

    /**
     * 重叠不易发生
     */
    override fun getBoundingBoxMargin(): Int {
        return 10
    }
}
