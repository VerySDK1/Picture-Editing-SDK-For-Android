package com.pesdk.uisdk.listener;

/**
 *所有的RecycleView 单击回调
 */
public interface OnItemClickListener<T> {

    void onItemClick(int position, T item);
}
