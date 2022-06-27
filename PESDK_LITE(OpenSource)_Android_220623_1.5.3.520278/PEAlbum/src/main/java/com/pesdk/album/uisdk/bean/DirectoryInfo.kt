package com.pesdk.album.uisdk.bean

import com.vecore.base.gallery.IImage

/**
 * 目录信息
 */
data class DirectoryInfo(
    val id: String,
    var name: String?,
    val size: String,
    val image: IImage?
)
