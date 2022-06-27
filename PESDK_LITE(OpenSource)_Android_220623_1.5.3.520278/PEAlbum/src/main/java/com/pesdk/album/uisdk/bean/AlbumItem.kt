package com.pesdk.album.uisdk.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.pesdk.album.api.bean.MediaType
import com.vecore.base.gallery.IImage

/**
 * 相册信息
 */
data class AlbumItem(
    val media: IImage?,
    val duration: Long = 0,
    val type: MediaType = MediaType.TYPE_IMAGE
) : MultiItemEntity {

    override val itemType = type.ordinal
}
