package com.pesdk.demo.helper

import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.demo.config.Configuration
import com.vesdk.camera.entry.CameraSdkCustomizeCallBack

/**
 * 相机自定义回调
 */
class CameraCustomizeCallBack : CameraSdkCustomizeCallBack {

    /**
     * 配置
     */
    private var mConfiguration: Configuration = Configuration()

    /**
     * 音乐
     */
    override fun onActionMusic(): ActivityResultContract<Void, ArrayList<String>>? {
        return null
    }

}