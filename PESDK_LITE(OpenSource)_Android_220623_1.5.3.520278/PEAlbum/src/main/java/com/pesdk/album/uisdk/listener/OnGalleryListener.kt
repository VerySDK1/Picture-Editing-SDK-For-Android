package com.pesdk.album.uisdk.listener

import com.pesdk.album.api.bean.MediaInfo

/**
 * 素材库添加
 */
interface OnGalleryListener {

    /**
     * 显示文字版
     * @param type 当前显示的类型
     * @return 是否显示文字版
     */
    fun isText(type: Int): Boolean

    /**
     * 显示编辑
     * @return 是否显示编辑
     */
    fun isEdit(): Boolean

    /**
     * 添加
     * @param mediaInfo 选中媒体
     */
    fun onAddAlbum(mediaInfo: MediaInfo): Int

    /**
     * 预览
     */
    fun onEdit(mediaInfo: MediaInfo, selected: Boolean)

    /**
     * 添加文字
     */
    fun onAddText()

}