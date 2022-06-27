package com.pesdk.album.uisdk.api

import android.text.TextUtils
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.api.AlbumSdkInit.APP_KEY
import okhttp3.*
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
        return if (AlbumSdkInit.getAlbumConfig().baseUrl == "") null else
            Retrofit.Builder()
                    .baseUrl(AlbumSdkInit.getAlbumConfig().baseUrl)
                    .client(
                            OkHttpClient.Builder()
                                    .readTimeout(30, TimeUnit.SECONDS)
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    //.addInterceptor(AppKeyInterceptor())
                                    .build()
                    )
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
    }

    fun <T> create(serviceClass: Class<T>): T? = getRetrofit()?.create(serviceClass)

    inline fun <reified T> create(): T? = create(T::class.java)

//
//    /**
//     * 公告参数
//     */
//    class AppKeyInterceptor : Interceptor {
//
//        override fun intercept(chain: Interceptor.Chain): Response {
//            //获取请求
//            var request: Request = chain.request()
//
//            //如果是post重新拼接 get直接添加  表单
//            if (TextUtils.equals(request.method(), "POST")) {
//                val bodyBuilder = FormBody.Builder()
//                var body: FormBody? = request.body() as FormBody?
//                body?.let {
//                    for (i in 0 until it.size()) {
//                        bodyBuilder.addEncoded(it.encodedName(i), it.encodedValue(i))
//                    }
//                }
//                body = bodyBuilder
//                        .addEncoded("appkey", APP_KEY)
//                        .addEncoded("ver", "148")
//                        .addEncoded("lang", "cn")
//                        .build();
//                request = request.newBuilder().patch(body).build()
//            } else if (TextUtils.equals(request.method(), "GET")) {
//                val url: HttpUrl = request.url()
//                val newUrl = url.newBuilder()
//                        .addEncodedQueryParameter("appkey", APP_KEY)
//                        .addEncodedQueryParameter("ver", "148")
//                        .build()
//                request = request.newBuilder().url(newUrl).build()
//            }
//            return chain.proceed(request)
//        }
//
//    }


}