package com.pesdk.album.uisdk.bean

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * 数据
 */
data class ResponseData(
    val code: Int,
    val msg: String,
    @SerializedName("data") val dataList: List<NetworkData>
)

/**
 * 数据
 */
@Parcelize
data class NetworkData(
    val id: String,
    val ufid: String,
    val name: String,
    val file: String,
    val cover: String,
    val suffix: String,
    val updatetime: String,
    val width: String,
    val height: String,
    val video: String,
    val sort: String,
    val version: String
) : Parcelable {
    constructor(name: String) : this(
        "0", "0", name, "", "", "", "",
        "", "", "0", "", ""
    )
}
