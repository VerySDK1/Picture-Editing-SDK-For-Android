package com.vesdk.common.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.vesdk.common.CommonInit.ROOT_NAME
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

object PathUtils {


    /**
     * 目录
     */
    private var sRootPath: String? = null

    fun initialize(context: Context, file: File? = null) {

        var path = file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != path) {
            do {
                var checkRootPath = context.getExternalFilesDir(null)
                val absolutePath = getFilePath(path)
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
            path = context.getExternalFilesDir(ROOT_NAME)
        }
        if (path == null) {
            return
        }

        //根目录
        checkPath(path)
        sRootPath = getFilePath(path)
    }

    /**
     * 根目录
     */
    fun getRootPath(): File? {
        return sRootPath?.let { File(it) }
    }

    /*----------------------------------文件路径----------------------------------*/

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    fun getFolderSize(file: File): Long {
        var size = 0L
        val fileList = file.listFiles()
        if (fileList != null && fileList.isNotEmpty()) {
            for (f in fileList) {
                size += if (f.isDirectory) {
                    getFolderSize(f)
                } else {
                    f.length()
                }
            }
        }
        return size
    }

    /**
     * 是否包含
     */
    fun isContains(src: String, name: String): Boolean {
        return if (TextUtils.isEmpty(src) || TextUtils.isEmpty(name)) {
            false
        } else if (src.startsWith(".")) {
            false
        } else src.contains(name)
    }

    /**
     * 检查path，如不存在创建之<br></br>
     * 并检查此路径是否存在文件.nomedia,如没有创建之
     * @param excludeNoMediaFile 是否需要检查排除.nomedia文件
     */
    fun checkPath(path: File, excludeNoMediaFile: Boolean = false) {
        if (!path.exists()) {
            path.mkdirs()
        }
        val fNoMedia = File(path, ".nomedia")
        if (excludeNoMediaFile) {
            if (fNoMedia.exists()) {
                fNoMedia.delete()
            }
        } else {
            if (!fNoMedia.exists()) {
                try {
                    fNoMedia.createNewFile()
                } catch (ignored: IOException) {
                    ignored.printStackTrace()
                }
            }
        }
    }

    /**
     * 判断指定文件路径是否有效并存在
     */
    fun fileExists(strFilePath: String): Boolean {
        return File(strFilePath).exists()
    }

    /*----------------------------------文件路径----------------------------------*/
    /**
     * 获取目录路径   必须使用getCanonicalPath()，禁止使用getAbsolutePath()
     */
    fun getFilePath(dir: String?, name: String?): String {
        return if (dir == null || dir.isEmpty() || name == null || name.isEmpty()) {
            ""
        } else getFilePath(File(dir, name))
    }

    fun getFilePath(path: String): String {
        return getFilePath(File(path))
    }

    fun getFilePath(file: File?): String {
        file?.let {
            return try {
                it.canonicalPath
            } catch (e: IOException) {
                return ""
            }
        }
        return ""
    }

    /**
     * 安全目录
     */
    private fun getFilePathSecure(file: File): String? {
        val absPath: String = try {
            file.canonicalPath
        } catch (e: IOException) {
            return null
        }
        //安全目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isInSecureDir(Paths.get(absPath))) {
                return null
            }
        }
        return absPath
    }

    /**
     * 是否在安全目录中
     */
    private fun isInSecureDir(path: Path): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            path.startsWith(sRootPath)
        } else true
    }

}