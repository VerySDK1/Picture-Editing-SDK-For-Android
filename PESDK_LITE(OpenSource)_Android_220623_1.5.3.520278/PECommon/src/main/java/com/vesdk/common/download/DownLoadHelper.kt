package com.vesdk.common.download

import android.text.TextUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okio.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.nio.channels.FileChannel
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * 下载工具类
 */
class DownLoadHelper(key: String, url: String, localPath: String, listener: DownloadListener) {

    /**
     * 下载信息
     */
    private val mDownInfo: DownloadInfo = DownloadInfo(key, url, localPath)

    /**
     * 下载进度
     */
    private val mDownloadListener: DownloadListener = listener

    /**
     * subscribe
     */
    private var mSubscribe: Disposable? = null

    /**
     * 下载
     */
    private var mService: DownLoadService? = null

    /**
     * 进度
     */
    private var mProgress = -1f

    /**
     * 开始下载
     */
    fun start() {
        if (TextUtils.isEmpty(mDownInfo.savePath)) {
            mDownloadListener.downloadFail(mDownInfo.key, "save path is none!")
            return
        }
        val interceptor = DownloadInterceptor(MyDownloadProgressListener())
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(8, TimeUnit.SECONDS)
        builder.addInterceptor(interceptor)
        val retrofit = Retrofit.Builder()
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(getBasUrl(mDownInfo.url))
            .build()
        if (mService == null) {
            mService = retrofit.create(DownLoadService::class.java)
            mDownInfo.service = mService
        } else {
            mService = mDownInfo.service
        }
        downLoad()
    }

    /**
     * 暂停下载
     */
    fun pause() {
        mSubscribe?.dispose()
    }

    /**
     * 继续下载
     */
    fun reStart() {
        downLoad()
    }

    /**
     * 开始下载
     */
    private fun downLoad() {
        mService?.let {
            mSubscribe = it.download("bytes=" + mDownInfo.readLength + "-", mDownInfo.url) //指定线程
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .retryWhen(RetryWhenNetworkException()) //读取下载写入文件，并把ResponseBody转成DownInfo
                .map(Function { responseBody: ResponseBody ->
                    try {
                        //写入文件
                        writeCache(responseBody, File(mDownInfo.savePath), mDownInfo)
                    } catch (e: IOException) {
                        mDownloadListener.downloadFail(mDownInfo.key, e.message ?: e.toString())
                    }
                    mDownInfo
                } as Function<ResponseBody, DownloadInfo>)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    /**
     * 写入文件
     */
    private fun writeCache(responseBody: ResponseBody, file: File, info: DownloadInfo) {
        val parentFile = file.parentFile
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs()
        }
        val allLength: Long = if (info.contentLength == 0L) {
            responseBody.contentLength()
        } else {
            info.contentLength
        }
        val channelOut: FileChannel
        val randomAccessFile = RandomAccessFile(file, "rwd")
        channelOut = randomAccessFile.channel
        val mappedBuffer = channelOut.map(
            FileChannel.MapMode.READ_WRITE,
            info.readLength, allLength - info.readLength
        )
        //https://www.cnblogs.com/yanduanduan/p/6046235.html
        val buffer = ByteArray(1024 * 10)
        var len: Int
        while (responseBody.byteStream().read(buffer).also { len = it } != -1) {
            mappedBuffer.put(buffer, 0, len)
        }
        responseBody.byteStream().close()
        channelOut?.close()
        randomAccessFile.close()
    }

    /**
     * 读取baseurl
     */
    private fun getBasUrl(url: String): String {
        var u = url
        var head = ""
        var index = url.indexOf("://")
        if (index != -1) {
            head = url.substring(0, index + 3)
            u = url.substring(index + 3)
        }
        index = u.indexOf("/")
        if (index != -1) {
            u = u.substring(0, index + 1)
        }
        return head + u
    }

    /**
     * 下载信息
     */
    internal class DownloadInfo(
        /**
         * key
         */
        val key: String,
        /**
         * 下载该文件的url
         */
        val url: String,
        /**
         * 存储位置
         */
        val savePath: String,
    ) {

        /**
         * 文件总长度
         */
        var contentLength: Long = 0

        /**
         * 下载长度
         */
        var readLength: Long = 0

        /**
         * 下载进度
         */
        var service: DownLoadService? = null

    }

    /**
     * 下载进度
     */
    internal inner class MyDownloadProgressListener : DownloadProgressListener {
        override fun progress(read: Long, contentLength: Long, done: Boolean) {
            // 该方法仍然是在子线程，如果想要调用进度回调，需要切换到主线程，否则的话，会在子线程更新UI，直接错误
            // 如果断电续传，重新请求的文件大小是从断点处到最后的大小，不是整个文件的大小，info中的存储的总长度是
            // 整个文件的大小，所以某一时刻总文件的大小可能会大于从某个断点处请求的文件的总大小。此时read的大小为
            // 之前读取的加上现在读取的
            var e = read
            if (mDownInfo.contentLength > contentLength) {
                e = read + (mDownInfo.contentLength - contentLength)
            } else {
                mDownInfo.contentLength = contentLength
            }
            mDownInfo.readLength = e
            Observable.just(1).observeOn(AndroidSchedulers.mainThread())

                .subscribe(object : Observer<Int> {

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(value: Int) {
                        if (!done) {
                            val progressNew = 100 * mDownInfo.readLength / mDownInfo.contentLength
                            if (progressNew > mProgress) {
                                mProgress = progressNew.toFloat()
                                mDownloadListener.downloadProgress(mDownInfo.key, mProgress)
                            }
                        } else {
                            mProgress = -1f
                            mDownloadListener.downloadCompleted(
                                mDownInfo.key,
                                mDownInfo.savePath
                            )
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}
                })
        }
    }

    /**
     * 下载进度拦截器
     * Created by ${R.js} on 2018/3/22.
     */
    internal class DownloadInterceptor(private val listener: DownloadProgressListener) :
        Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalResponse = chain.proceed(chain.request())
            return originalResponse.newBuilder()
                .body(DownloadResponseBody(originalResponse.body, listener))
                .build()
        }
    }

    /**
     * 下载body
     */
    internal class DownloadResponseBody(
        private val responseBody: ResponseBody?,
        private val listener: DownloadProgressListener
    ) : ResponseBody() {

        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return responseBody?.contentType()
        }

        override fun contentLength(): Long {
            return responseBody?.contentLength() ?: 0
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null && responseBody != null) {
                bufferedSource = source(responseBody.source()).buffer()
            }
            return bufferedSource!!
        }

        private fun source(source: Source): Source {

            return object : ForwardingSource(source) {
                var totalBytesRead = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    listener.progress(
                        totalBytesRead,
                        responseBody?.contentLength() ?: 0,
                        bytesRead == -1L
                    )
                    return bytesRead
                }
            }
        }
    }

    /**
     * retry条件
     */
    internal class RetryWhenNetworkException(
        private val count: Int = 3,
        private val delay: Long = 3000,
        private val increaseDelay: Long = 3000
    ) :
        Function<Observable<out Throwable?>, Observable<*>> {

        override fun apply(observable: Observable<out Throwable?>): Observable<*> {
            //压缩规则 合并后的结果是一个Observable<Wrapper>
            return observable.zipWith(Observable.range(1, count + 1),
                { throwable: Throwable?, index: Int -> Wrapper(throwable, index) })
                .flatMap { wrapper: Wrapper ->
                    //转换规则
                    if ((wrapper.throwable is ConnectException
                                || wrapper.throwable is SocketTimeoutException
                                || wrapper.throwable is TimeoutException)
                        && wrapper.index < count + 1
                    ) {
                        //如果超出重试次数也抛出错误，否则默认是会进入onCompleted
                        return@flatMap Observable.timer(
                            delay + (wrapper.index - 1) * increaseDelay,
                            TimeUnit.MILLISECONDS
                        )
                    }
                    Observable.error<Any?>(wrapper.throwable)
                }
        }

        private class Wrapper(val throwable: Throwable?, val index: Int)
    }

    /**
     * 下载监听
     */
    interface DownloadListener {

        /**
         * 进度
         *
         * @param progress 进度
         */
        fun downloadProgress(key: String, progress: Float)

        /**
         * 下载完成
         *
         * @param key      key
         * @param filePath 地址
         */
        fun downloadCompleted(key: String, filePath: String)

        /**
         * 下载失败
         *
         * @param msg 错误
         */
        fun downloadFail(key: String, msg: String)
    }

    /**
     * 下载的Service
     * Created by ${R.js} on 2018/3/22.
     */
    interface DownLoadService {
        /**
         * 下载
         *
         * @param start 从某个字节开始下载数据
         * @param url   文件下载的url
         * @return Observable
         * @Streaming 这个注解必须添加，否则文件全部写入内存，文件过大会造成内存溢出
         */
        @Streaming
        @GET
        fun download(@Header("RANGE") start: String, @Url url: String): Observable<ResponseBody>
    }

    /**
     * 下载进度
     */
    interface DownloadProgressListener {

        /**
         * 下载进度
         *
         * @param read          已下载长度
         * @param contentLength 总长度
         * @param done          是否下载完毕
         */
        fun progress(read: Long, contentLength: Long, done: Boolean)
    }

}