package com.pesdk.album.api

import android.annotation.SuppressLint
import android.content.Context
import com.pesdk.album.uisdk.helper.SdkHelper
import com.pesdk.album.uisdk.utils.AlbumPathUtils
import com.vecore.PECore
import com.vesdk.common.CommonInit
import com.vesdk.common.utils.ToastUtils

/**
 * sdk入口
 */
@SuppressLint("StaticFieldLeak")
object AlbumSdkInit {

    /**
     * 是否初始化
     */
    private var isInitializer = false

    /**
     * 配置
     */
    private var mConfiguration = AlbumConfig.Builder()
        //地址
        //界面 ALBUM_SUPPORT_DEFAULT / ALBUM_SUPPORT_VIDEO_ONLY / ALBUM_SUPPORT_IMAGE_ONLY
        .setAlbumSupport(AlbumConfig.ALBUM_SUPPORT_IMAGE_ONLY)
        //文字版
        .setHideText(true)
        //隐藏相机
        .setHideCamera(true)
        //隐藏预览
        .setHideEdit(false)
        //隐藏素材库
        .setHideMaterial(true)
        //隐藏拍照、录制切换
        .setHideCameraSwitch(true)
        //最小时间
        .setLimitMinTime(AlbumConfig.DEFAULT_TIME)
        //数量
        .setLimitNum(1, 1)
        //具体指定数量 setLimitNum二选一
        //.setLimitMedia(3, 2, 2)
        //首次显示 FIRST_ALL / FIRST_VIDEO / FIRST_IMAGE / FIRST_MATERIAL
        .setFirstShow(AlbumConfig.FIRST_ALL)
        .get()

    /**
     * 上下文
     */
    lateinit var context: Context

    /**
     * appKey
     */
    var APP_KEY: String = ""
    var APP_SECRET: String = ""


    /**
     * 初始化
     */
    fun init(
        context: Context,
        appKey: String = "",
        appSecret: String = "",
        callback: AlbumSdkCustomizeCallBack?
    ) {
        if (isInitializer) {
            return
        }
        isInitializer = true

        AlbumSdkInit.context = context.applicationContext
        APP_KEY = appKey
        APP_SECRET = appSecret

        //公共
        CommonInit.initialize(context)
        //初始化
        AlbumPathUtils.initialize(context)

        //回调
        SdkHelper.initCallBack(callback)
    }

    /**
     * 版本号
     */
    fun getVersion(): String {
        return PECore.getVersionCode().toString()
    }

    /**
     * 设置配置
     */
    fun setAlbumConfig(config: AlbumConfig) {
        mConfiguration = config
    }

    /**
     * 检查appKey是否无效
     */
    private fun appKeyIsInvalid(): Boolean {
        if (!isInitializer) {
            ToastUtils.show(context, "Sdk not initialized!")
            return false
        }
        return true
    }

    /**
     * 获取配置
     */
    fun getAlbumConfig() = mConfiguration

    /**
     * 所有的通过配置传递 不通过Intent
     */
    fun getAlbumContracts(): AlbumContracts? {
        return AlbumContracts()
    }

    /**
     * 模板相册
     */
    fun getAlbumTemplateContracts(): AlbumTemplateContract? {
        if (appKeyIsInvalid()) {
            return AlbumTemplateContract()
        }
        return null
    }

}