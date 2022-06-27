package com.pesdk.album.uisdk.api

import com.pesdk.album.uisdk.bean.ResponseData
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TtfApi {

    /**
     * 字体
     */
    @POST("filemanage2/public/filemanage/file/appData")
    @FormUrlEncoded
    fun getTtfList(@FieldMap map: Map<String, String>): Call<ResponseData>


}
