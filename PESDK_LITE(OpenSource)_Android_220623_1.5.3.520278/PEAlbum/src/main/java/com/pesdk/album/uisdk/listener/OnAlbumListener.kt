package com.pesdk.album.uisdk.listener

import com.pesdk.album.api.bean.MediaInfo

/**
 * 相册
 */
interface OnAlbumListener {

    /**
     * 预览
     */
    fun onEdit(mediaInfo: MediaInfo, selected: Boolean)

    /**
     * 选中菜单
     */
    fun selectMenu(type: Int)

}