package com.vesdk.camera.entry

import android.annotation.SuppressLint
import android.content.Context
import com.vecore.PECore
import com.vesdk.camera.helper.SdkHelper
import com.vesdk.camera.utils.CameraPathUtils
import com.vesdk.common.CommonInit
import com.vesdk.common.utils.ToastUtils

/**
 * sdk入口
 */
@SuppressLint("StaticFieldLeak")
object CameraSdkInit {

    /**
     * 是否初始化
     */
    private var isInitializer = false

    /**
     * 配置
     */
    private var mConfiguration = CameraConfig.Builder().get()

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
        licenseKey: String = "",
        callBack: CameraSdkCustomizeCallBack?
    ) {
        if (isInitializer) {
            return
        }
        isInitializer = true

        this.context = context.applicationContext
        APP_KEY = appKey
        APP_SECRET = appSecret

        //公共
        CommonInit.initialize(context)
        //初始化
        CameraPathUtils.initialize(context)

        //回调
        SdkHelper.initCallBack(callBack)
    }

    /**
     * 版本号
     */
    fun getVersion(): String {
        return PECore.getVersionCode().toString()
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
     * 设置配置
     */
    fun setCameraConfig(config: CameraConfig) {
        mConfiguration = config
    }

    /**
     * 获取配置
     */
    fun getCameraConfig() = mConfiguration


    /**
     * 所有的通过配置传递 不通过Intent
     */
    fun getCameraContracts(): CameraContracts? {
        return CameraContracts()
    }

}