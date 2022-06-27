package com.pesdk.album.uisdk.viewmodel.source

import androidx.lifecycle.liveData
import com.pesdk.album.uisdk.bean.TtfInfo
import com.pesdk.album.uisdk.viewmodel.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class TtfSource {

    /**
     * 数据
     */
    fun getTtfData() = fire(Dispatchers.IO) {
        val placeResponse = NetworkRepository.getTtfData()
        if (placeResponse.code == 0) {
            val places = placeResponse.dataList
            val ttfList = mutableListOf<TtfInfo>()
            for (data in places) {
                ttfList.add(TtfInfo(data))
            }
            Result.success(ttfList)
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