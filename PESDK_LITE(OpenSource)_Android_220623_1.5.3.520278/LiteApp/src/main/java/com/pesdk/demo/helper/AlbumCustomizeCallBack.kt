package com.pesdk.demo.helper

import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.api.AlbumSdkCustomizeCallBack
import com.pesdk.demo.config.Configuration
import com.vesdk.camera.entry.CameraSdkInit

/**
 * 相册自定义回调
 */
class AlbumCustomizeCallBack : AlbumSdkCustomizeCallBack {

    /**
     * 配置
     */
    private var mConfiguration: Configuration = Configuration()

    /**
     * 相机
     */
    override fun onActionCamera(): ActivityResultContract<Void, ArrayList<String>>? {
        CameraSdkInit.setCameraConfig(mConfiguration.initCameraConfig())
        return CameraSdkInit.getCameraContracts()
    }

}