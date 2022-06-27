package com.vesdk.camera.api

import com.vesdk.camera.bean.ResponseData
import com.vesdk.camera.bean.ResponseSort
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 网络数据
 */
interface NetworkApi {

    companion object {

        /**
         * 滤镜
         */
        const val TYPE_CLOUD_FILTER = "filter"


    }

    /**
     * 分类
     */
    @POST("api/v1/type/list")
    @FormUrlEncoded
    fun getSort(@FieldMap map: Map<String, String>): Call<ResponseSort>

    /**
     * 分类下的数据
     */
    @POST("api/v1/file/list")
    @FormUrlEncoded
    fun getData(@FieldMap map: Map<String, String>): Call<ResponseData>


}