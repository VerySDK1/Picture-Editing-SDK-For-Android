package com.pesdk.album.api.bean

import android.os.Parcelable
import com.pesdk.album.api.bean.MediaInfo
import kotlinx.android.parcel.Parcelize

/**
 * 预览信息
 */
@Parcelize
data class PreviewInfo(
        val mediaInfo: MediaInfo,
        var oldSelected: Boolean,
        var selected: Boolean
) : Parcelable
