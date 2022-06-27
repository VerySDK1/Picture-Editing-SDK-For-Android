package com.pesdk.album.uisdk.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.vesdk.common.CommonInit.ROOT_NAME
import com.vesdk.common.utils.PathUtils
import java.io.File

object AlbumPathUtils {

    /**
     * 字体后缀
     */
    private const val SUFFIX_TTF = ".ttf"

    /**
     * 视频后缀
     */
    private const val SUFFIX_VIDEO = ".mp4"

    /**
     * 目录名
     */
    private const val DOWNLOAD = "download"
    private const val VIDEO = "video"
    private const val IMAGE = "image"
    private const val TTF = "ttf"

    /**
     * 目录
     */
    private var sRdRootPath: String? = null
    private var sRdDownLoadPath: String? = null
    private var sRdVideoPath: String? = null
    private var sRdImagePath: String? = null
    private var sRdTtfPath: String? = null

    fun initialize(context: Context, file: File? = null) {
        if (sRdRootPath != null) {
            return
        }
        var path = file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != path) {
            do {
                var checkRootPath = context.getExternalFilesDir(null)
                val absolutePath = PathUtils.getFilePath(path)
                val prefix = checkRootPath?.parent
                if (prefix != null) {
                    if (checkRootPath != null && absolutePath.startsWith(prefix)) {
                        //外置
                        break
                    }
                    checkRootPath = context.filesDir
                    if (checkRootPath != null && absolutePath.startsWith(prefix)) {
                        //内置
                        break
                    }
                }
                val hasReadPermission =
                        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                    throw IllegalAccessError("Can`t get WRITE_EXTERNAL_STORAGE permission. $absolutePath")
                }
            } while (false)
        }
        if (null == path) {
            path = PathUtils.getRootPath()
        }
        if (null == path) {
            path = context.getExternalFilesDir(ROOT_NAME)
        }
        if (path == null) {
            return
        }

        //根目录
        PathUtils.checkPath(path)
        sRdRootPath = PathUtils.getFilePath(path)

        //下载
        path = File(sRdRootPath, "$DOWNLOAD/")
        PathUtils.checkPath(path)
        sRdDownLoadPath = PathUtils.getFilePath(path)

        //视频
        path = File(sRdRootPath, "$VIDEO/")
        PathUtils.checkPath(path)
        sRdVideoPath = PathUtils.getFilePath(path)

        //图片
        path = File(sRdRootPath, "$IMAGE/")
        PathUtils.checkPath(path)
        sRdImagePath = PathUtils.getFilePath(path)

        //字体
        path = File(sRdRootPath, "$TTF/")
        PathUtils.checkPath(path)
        sRdTtfPath = PathUtils.getFilePath(path)

    }


    /*----------------------------------目录----------------------------------*/


    /**
     * 视频名字
     */
    fun getVideoPath(id: String): String {
        val hashCode = id.hashCode()
        return PathUtils.getFilePath(sRdVideoPath, "${hashCode}$SUFFIX_VIDEO")
    }

    /**
     * 图片地址
     */
    fun getImagePath(name: String): String {
        return PathUtils.getFilePath(sRdImagePath, name)
    }

    /**
     * 滤镜
     */
    fun getTtfPath(id: String): String {
        val hashCode = id.hashCode()
        return PathUtils.getFilePath(sRdTtfPath, "${hashCode}$SUFFIX_TTF")
    }


    /*----------------------------------其他----------------------------------*/

    /**
     * 判断文件是否下载
     */
    fun isDownload(path: String?): Boolean {
        path?.let {
            return PathUtils.fileExists(it)
        }
        return false
    }

}