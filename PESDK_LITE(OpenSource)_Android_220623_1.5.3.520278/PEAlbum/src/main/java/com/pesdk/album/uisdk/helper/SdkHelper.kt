package com.pesdk.album.uisdk.helper

import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.api.AlbumSdkCustomizeCallBack

/**
 * 回调
 */
object SdkHelper {

    private var mAlbumSdkCallBack: AlbumSdkCustomizeCallBack? = null

    fun initCallBack(callBack: AlbumSdkCustomizeCallBack?) {
        mAlbumSdkCallBack = callBack
    }

    /**
     * 获取相册
     */
    fun getCameraContracts(): ActivityResultContract<Void, ArrayList<String>>? {
        return mAlbumSdkCallBack?.onActionCamera()
    }

}