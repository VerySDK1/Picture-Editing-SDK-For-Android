package com.vesdk.camera.utils

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import com.vesdk.camera.entry.CameraSdkInit
import com.vesdk.common.utils.PathUtils
import java.io.File

/**
 * 检测SD的状态和大小的类
 */
object CheckSDSizeUtils {

    /**
     * 判断SD是否存在
     */
    @JvmStatic
    fun existSDCard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 查看SD卡总容量（单位MB）
     */
    @JvmStatic
    fun getSDAllSize(path: String): Long {
        var rootPath: String? = path
        if (TextUtils.isEmpty(rootPath)) {
            // 取得SD卡文件路径
            rootPath = PathUtils.getFilePath(if (SDK_INT < Build.VERSION_CODES.P) {
                Environment.getExternalStorageDirectory()
            } else {
                CameraSdkInit.context.getExternalFilesDir(null)
            })
        }
        return rootPath?.let {
            val sf = StatFs(it)
            // 获取单个数据块的大小(Byte)
            val blockSize = sf.blockSizeLong
            // 获取所有数据块数
            val allBlocks = sf.blockCountLong
            // 返回SD卡大小
            // return allBlocks * blockSize; //单位Byte
            // return (allBlocks * blockSize)/1024; //单位KB
            // 单位MB
            allBlocks * blockSize / 1024 / 1024
        } ?: kotlin.run { 0 }
    }

    /**
     * 查看SD卡剩余容量（单位MB）
     */
    @JvmStatic
    fun getSDFreeSize(path: String): Long {
        var rootPath: String? = path
        if (TextUtils.isEmpty(rootPath)) {
            // 取得SD卡文件路径
            rootPath = PathUtils.getFilePath(if (SDK_INT < Build.VERSION_CODES.P) {
                Environment.getExternalStorageDirectory()
            } else {
                CameraSdkInit.context.getExternalFilesDir(null)
            })
        }
        return rootPath?.let {
            if (File(rootPath).canWrite()) {
                val sf = StatFs(rootPath)
                // 获取单个数据块的大小(Byte)
                val blockSize = sf.blockSizeLong
                // 空闲的数据块的数量
                val freeBlocks = sf.blockCountLong
                // 返回SD卡空闲大小
                // return freeBlocks * blockSize; //单位Byte
                // return (freeBlocks * blockSize)/1024; //单位KB
                // 单位MB
                freeBlocks * blockSize / 1024 / 1024
            } else {
                0
            }
        } ?: kotlin.run { 0 }
    }

    /**
     * 获取SD剩余容量是否不下于当前比例
     *
     * @param size 当前小于容量值（单位MB）
     */
    @JvmStatic
    fun isThanCurrentSize(rootPath: String, size: Int): Boolean {
        return getSDFreeSize(rootPath) > size
    }

    /**
     * 获取SD剩余容量是否不下于200MB
     */
    @JvmStatic
    fun isThanCurrentSize(rootPath: String): Boolean {
        return getSDFreeSize(rootPath) > 200
    }
}