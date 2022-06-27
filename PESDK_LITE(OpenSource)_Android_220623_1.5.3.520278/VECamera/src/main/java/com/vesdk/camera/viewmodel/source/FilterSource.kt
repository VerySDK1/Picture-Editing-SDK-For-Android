package com.vesdk.camera.viewmodel.source

import androidx.lifecycle.liveData
import com.vesdk.camera.api.NetworkApi
import com.vesdk.camera.bean.FilterInfo
import com.vesdk.camera.viewmodel.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class FilterSource {

    /**
     * 分类
     */
    fun getFilterSort(minVer: String) = fire(Dispatchers.IO) {
        val placeResponse = NetworkRepository.getSort(NetworkApi.TYPE_CLOUD_FILTER, minVer)
        if (placeResponse.code == 0) {
            val places = placeResponse.sorts
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.code} and ${placeResponse.msg}"))
        }
    }

    /**
     * 数据
     */
    fun getFilterData(category: String) = fire(Dispatchers.IO) {
        val placeResponse = NetworkRepository.getCategoryData(NetworkApi.TYPE_CLOUD_FILTER, category)
        if (placeResponse.code == 0) {
            val places = placeResponse.dataList
            val filterList = mutableListOf<FilterInfo>()
            for (data in places) {
                filterList.add(FilterInfo(data))
            }
            Result.success(filterList)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.code} and ${placeResponse.msg}"))
        }
    }

    private fun <T> fire(content: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(content) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }

}