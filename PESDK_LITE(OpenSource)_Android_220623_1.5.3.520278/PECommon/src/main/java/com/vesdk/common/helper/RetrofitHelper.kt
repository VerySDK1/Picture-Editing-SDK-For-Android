package com.vesdk.common.helper

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object RetrofitHelper {

    /**
     * 未设置网络地址
     */
    suspend fun <T, M> fire(api: M?, block: (api: M) -> Call<T>): T {
        return api?.let {
            block(it).await()
        } ?: kotlin.run {
            throw NullPointerException("Url is null")
        }
    }

    /**
     * 等待
     */
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    response.body()?.let {
                        continuation.resume(it)
                    } ?: kotlin.run {
                        continuation.resumeWithException(RuntimeException("response body is null"))
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}