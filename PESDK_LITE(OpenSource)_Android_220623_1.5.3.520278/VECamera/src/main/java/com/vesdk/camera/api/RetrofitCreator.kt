package com.vesdk.camera.api

import com.vesdk.camera.entry.CameraSdkInit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络
 */
object RetrofitCreator {

    /**
     * 网络
     */
    private fun getRetrofit(): Retrofit? {
        return if (CameraSdkInit.getCameraConfig().baseUrl == "") null else
            Retrofit.Builder()
                    .baseUrl(CameraSdkInit.getCameraConfig().baseUrl)
                    .client(
                            OkHttpClient.Builder()
                                    .readTimeout(30, TimeUnit.SECONDS)
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    .build()
                    )
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
    }


    fun <T> create(serviceClass: Class<T>): T? = getRetrofit()?.create(serviceClass)

    inline fun <reified T> create(): T? = create(T::class.java)


}