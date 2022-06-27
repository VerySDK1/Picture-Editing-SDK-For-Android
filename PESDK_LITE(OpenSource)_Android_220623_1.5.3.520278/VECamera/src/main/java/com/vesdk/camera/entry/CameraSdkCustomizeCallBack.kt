package com.vesdk.camera.entry

import androidx.activity.result.contract.ActivityResultContract

/**
 * sdk回调
 */
interface CameraSdkCustomizeCallBack {

    /**
     * 音乐
     */
    fun onActionMusic(): ActivityResultContract<Void, ArrayList<String>>?

}