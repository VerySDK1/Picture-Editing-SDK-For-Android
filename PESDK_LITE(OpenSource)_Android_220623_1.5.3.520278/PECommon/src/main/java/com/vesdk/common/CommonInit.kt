package com.vesdk.common

import android.content.Context
import android.content.pm.ApplicationInfo
import com.tencent.mmkv.MMKV
import com.vesdk.common.helper.LOG_ENABLE
import com.vesdk.common.helper.SkinProxy
import com.vesdk.common.utils.PathUtils
import java.io.File

/**
 * 初始化
 */
object CommonInit {

    /**
     * 是否初始化
     */
    private var isInitializer = false

    /**
     * 名字
     */
    const val ROOT_NAME = "pe"

    /**
     * 初始化
     */
    @JvmStatic
    fun initialize(context: Context, file: File? = null) {
        if (isInitializer) {
            return
        }
        isInitializer = true

        //MMKV
        MMKV.initialize(context)
        //皮肤
        SkinProxy.init(context)
        //路径
        if (file == null) {
            PathUtils.initialize(context, context.getExternalFilesDir(ROOT_NAME))
        } else {
            PathUtils.initialize(context, file)
        }
        //日志
        LOG_ENABLE = isApkInDebug(context)
    }

    /**
     * 判断签名是debug签名还是release签名
     */
    private fun isApkInDebug(context: Context): Boolean {
        return try {
            val info = context.applicationInfo
            info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: Exception) {
            false
        }
    }

}