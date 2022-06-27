package com.vesdk.camera.bean

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * 数据
 */
@Keep
data class ResponseData(
        val code: Int,
        val msg: String,
        @SerializedName("data") val dataList: List<NetworkData>
)

/**
 * 数据
 */
@Keep
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
) : Parcelable
