package com.pesdk.album.uisdk.bean

import com.pesdk.album.uisdk.utils.AlbumPathUtils
import com.vesdk.common.download.BaseDownload
import com.vesdk.common.download.DownloadStatue

/**
 * 字体
 */
class TtfInfo(val networkData: NetworkData) : BaseDownload(networkData.file) {

    /**
     * 本地地址
     */
    var localPath = AlbumPathUtils.getTtfPath(networkData.id)

    init {
        downStatue = if (AlbumPathUtils.isDownload(localPath)) {
            DownloadStatue.DOWN_SUCCESS
        } else {
            DownloadStatue.DOWN_NONE
        }
    }

}