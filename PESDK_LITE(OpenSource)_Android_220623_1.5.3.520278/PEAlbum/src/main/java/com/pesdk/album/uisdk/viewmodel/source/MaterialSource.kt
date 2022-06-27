package com.pesdk.album.uisdk.viewmodel.source

import androidx.lifecycle.liveData
import com.pesdk.album.uisdk.bean.MaterialInfo
import com.pesdk.album.uisdk.viewmodel.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * 素材库
 */
class MaterialSource {

    /**
     * 分类
     */
    fun getMaterialSort(minVer: String) = fire(Dispatchers.IO) {
        val placeResponse = NetworkRepository.getMaterialSort(minVer)
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
    fun getMaterialData(category: String) = fire(Dispatchers.IO) {
        val placeResponse = NetworkRepository.getMaterialData(category)
        if (placeResponse.code == 0) {
            val places = placeResponse.dataList
            val materialList = mutableListOf<MaterialInfo>()
            for (data in places) {
                materialList.add(MaterialInfo(data))
            }
            Result.success(materialList)
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