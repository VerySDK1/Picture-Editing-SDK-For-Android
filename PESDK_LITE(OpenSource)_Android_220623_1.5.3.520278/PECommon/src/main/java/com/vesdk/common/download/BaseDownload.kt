package com.vesdk.common.download

/**
 * 需要下载
 */
abstract class BaseDownload(
    /**
     * 网络链接
     */
    val url: String,
    /**
     * 下载状态
     */
    var downStatue: DownloadStatue = DownloadStatue.DOWN_NONE,
    /**
     * 下载失败次数
     */
    var failNum: Int = 0,
    /**
     * 下载进度
     */
    var downloadProgress: Float = -1f
)  {

    /**
     * 是否可以下载
     */
    fun isCanDown(): Boolean {
        return url != "" && failNum < 3
    }

}