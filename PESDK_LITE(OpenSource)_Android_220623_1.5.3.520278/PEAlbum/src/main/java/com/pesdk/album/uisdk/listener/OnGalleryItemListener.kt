package com.pesdk.album.uisdk.listener

import com.pesdk.album.uisdk.bean.AlbumItem

/**
 * 相册点击
 */
interface OnGalleryItemListener {

    /**
     * 添加文字
     */
    fun onClickWord()

    /**
     * 添加
     */
    fun onClickAdd(position: Int, item: AlbumItem)

    /**
     * 点击右上角
     */
    fun onClickEdit(item: AlbumItem)


}