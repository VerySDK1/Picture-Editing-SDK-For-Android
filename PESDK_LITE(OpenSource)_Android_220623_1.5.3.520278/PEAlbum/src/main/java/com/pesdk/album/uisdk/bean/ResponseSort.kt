package com.pesdk.album.uisdk.bean

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * 分类
 */
data class ResponseSort(
    val code: Int,
    val msg: String,
    @SerializedName("data") val sorts: List<Sort>
)

@Parcelize
data class Sort(
    val id: String,
    val utid: String,
    val name: String,
    @SerializedName("name_en") val nameEn: String,
    val appkey: String,
    val icon: String,
    @SerializedName("icon_checked") val iconChecked: String,
    @SerializedName("icon_unchecked") val iconUnChecked: String,
    val type: String,
    val updatetime: String,
    val classify: String,
    val version: String,
    val sort: String,
    val sycn: String,
    val count: String
) : Parcelable