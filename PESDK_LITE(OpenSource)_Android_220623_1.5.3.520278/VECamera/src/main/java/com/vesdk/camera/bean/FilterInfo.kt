package com.vesdk.camera.bean

import com.vesdk.camera.utils.CameraPathUtils
import com.vesdk.common.download.BaseDownload
import com.vesdk.common.download.DownloadStatue

/**
 * 滤镜
 */
class FilterInfo(val networkData: NetworkData) : BaseDownload(networkData.file) {

    /**
     * 本地地址
     */
    var localPath: String = if (networkData.file.contains("zip")) CameraPathUtils.getFilterZipPath(networkData.id) else CameraPathUtils.getFilterPath(networkData.id)

    init {
        downStatue = if (CameraPathUtils.isDownload(localPath)) {
            DownloadStatue.DOWN_SUCCESS
        } else {
            DownloadStatue.DOWN_NONE
        }
    }

}