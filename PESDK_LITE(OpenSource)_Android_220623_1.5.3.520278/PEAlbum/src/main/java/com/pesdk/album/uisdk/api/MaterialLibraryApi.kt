package com.pesdk.album.uisdk.api

import com.pesdk.album.uisdk.bean.ResponseData
import com.pesdk.album.uisdk.bean.ResponseSort
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 网络数据
 */
interface MaterialLibraryApi {

    /**
     * 分类
     */
    @POST("filemanage2/public/filemanage/file/typeData")
    @FormUrlEncoded
    fun getMaterialLibrarySort(@FieldMap map: Map<String, String>): Call<ResponseSort>

    /**
     * 分类下的数据
     */
    @POST("filemanage2/public/filemanage/file/appData")
    @FormUrlEncoded
    fun getMaterialLibraryData(@FieldMap map: Map<String, String>): Call<ResponseData>


}