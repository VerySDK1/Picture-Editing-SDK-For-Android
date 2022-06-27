package com.pesdk.album.api

import androidx.activity.result.contract.ActivityResultContract

/**
 * 相册回调
 */
interface AlbumSdkCustomizeCallBack {

    /**
     * 相机
     */
    fun onActionCamera(): ActivityResultContract<Void, ArrayList<String>>?

}