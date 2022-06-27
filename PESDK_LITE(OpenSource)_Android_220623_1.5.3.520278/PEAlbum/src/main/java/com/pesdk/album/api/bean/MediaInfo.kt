package com.pesdk.album.api.bean

import android.os.Parcelable
import com.pesdk.album.uisdk.utils.AlbumUtils
import kotlinx.android.parcel.Parcelize

/**
 * 媒体信息
 */
@Parcelize
data class MediaInfo(
    val path: String,
    val duration: Long,
    val type: MediaType = MediaType.TYPE_IMAGE
) : Parcelable {
    val id = AlbumUtils.randomId
}
