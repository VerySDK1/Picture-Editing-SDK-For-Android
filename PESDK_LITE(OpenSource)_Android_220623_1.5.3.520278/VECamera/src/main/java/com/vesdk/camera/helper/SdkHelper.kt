package com.vesdk.camera.helper

import androidx.activity.result.contract.ActivityResultContract
import com.vesdk.camera.entry.CameraSdkCustomizeCallBack

/**
 * 回调
 */
object SdkHelper {

    private var mCameraSdkCallBack: CameraSdkCustomizeCallBack? = null

    fun initCallBack(callBack: CameraSdkCustomizeCallBack?) {
        mCameraSdkCallBack = callBack
    }

    /**
     * 获取音乐
     */
    fun getMusicContracts(): ActivityResultContract<Void, ArrayList<String>>? {
        return mCameraSdkCallBack?.onActionMusic()
    }

}