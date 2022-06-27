package com.pesdk.demo.ui.adapter;

/**
 *所有的RecycleView 单击回调
 */
public interface OnItemClickListener<T> {

    void onItemClick(int position, T item);
}
