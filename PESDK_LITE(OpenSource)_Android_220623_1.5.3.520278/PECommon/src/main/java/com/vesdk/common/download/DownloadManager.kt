package com.vesdk.common.download

import android.content.Context
import com.pesdk.R
import com.vesdk.common.utils.NetWorkUtils
import com.vesdk.common.utils.ToastUtils

/**
 * 下载
 */
object DownloadManager {

    /**
     * 最大同时下载数量
     */
    private const val MAX_DOWN = 10

    /**
     * 下载
     */
    private val mDownloadList = mutableMapOf<String, DownLoadHelper>()

    /**
     * 下载
     */
    fun addDownload(
            key: String,
            url: String,
            localPath: String,
            listener: DownLoadHelper.DownloadListener
    ): DownLoadHelper {
        val down = DownLoadHelper(key, url, localPath, object : DownLoadHelper.DownloadListener {
            override fun downloadProgress(key: String, progress: Float) {
                listener.downloadProgress(key, progress)
            }

            override fun downloadCompleted(key: String, filePath: String) {
                listener.downloadCompleted(key, filePath)
                mDownloadList.remove(key)
            }

            override fun downloadFail(key: String, msg: String) {
                listener.downloadFail(key, msg)
                mDownloadList.remove(key)
            }

        })
        mDownloadList[key] = down
        return down
    }

    /**
     * 移除
     */
    fun remove(key: String) {
        mDownloadList.remove(key)
    }

    /**
     * 关闭所有的
     */
    fun closeAll() {
        for (download in mDownloadList.values) {
            download.pause()
        }
        mDownloadList.clear()
    }

    /**
     * 能否下载
     */
    fun isCanDownload(context: Context, download: BaseDownload, key: String): Boolean {
        //网络
        if (!NetWorkUtils.isConnected(context)) {
            ToastUtils.show(context, context.getString(R.string.common_check_network))
            return false
        }
        //能否下载
        if (!download.isCanDown()) {
            ToastUtils.show(context, context.getString(R.string.common_many_failures_url_none))
            return false
        }
        //失败次数
        if (mDownloadList.size >= MAX_DOWN) {
            ToastUtils.show(context, context.getString(R.string.common_many_downloads_currently))
            return false
        }
        //存在
        if (mDownloadList.containsKey(key)) {
            ToastUtils.show(context, context.getString(R.string.common_downloading))
            return false
        }
        return true
    }

}