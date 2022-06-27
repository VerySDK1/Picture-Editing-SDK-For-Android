package com.pesdk.album.uisdk.bean

import com.pesdk.album.uisdk.utils.AlbumPathUtils
import com.vesdk.common.download.BaseDownload
import com.vesdk.common.download.DownloadStatue

/**
 * 素材
 */
class MaterialInfo (val networkData: NetworkData) : BaseDownload(networkData.file) {

    /**
     * 本地地址
     */
    var localPath = AlbumPathUtils.getVideoPath(networkData.id)

    init {
        downStatue = if (AlbumPathUtils.isDownload(localPath)) {
            DownloadStatue.DOWN_SUCCESS
        } else {
            DownloadStatue.DOWN_NONE
        }
    }

}