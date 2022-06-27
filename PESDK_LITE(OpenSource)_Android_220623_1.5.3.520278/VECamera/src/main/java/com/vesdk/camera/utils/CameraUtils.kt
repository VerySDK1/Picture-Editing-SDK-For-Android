package com.vesdk.camera.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import com.vecore.utils.UriUtils
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.util.*

object CameraUtils {

    /**
     * 获取绝对路径
     */
    @JvmStatic
    fun getAbsolutePath(context: Context, pathOUri: String): String? {
        return if (pathOUri.startsWith(ContentResolver.SCHEME_CONTENT) || pathOUri.startsWith(
                        ContentResolver.SCHEME_FILE
                )
        ) {
            getAbsolutePath(
                    context,
                    Uri.parse(pathOUri)
            )
        } else pathOUri
    }

    /**
     * 根据Uri获取绝对路径
     */
    @JvmStatic
    fun getAbsolutePath(context: Context, uri: Uri): String? {
        return UriUtils.getUriLegacyPath(context, uri)
    }

    /**
     * 是否Uri
     */
    @JvmStatic
    fun isUri(uriString: String): Boolean {
        return !TextUtils.isEmpty(uriString) && (uriString.startsWith(ContentResolver.SCHEME_CONTENT) || uriString.startsWith(
                ContentResolver.SCHEME_FILE
        ))
    }

    /**
     * android 适配29 + 打开bitmap
     * @param uriString
     */
    @JvmStatic
    fun getFileDescriptor(context: Context, uriString: String): FileDescriptor? {
        try {
            if (isUri(uriString)) {
                //uri方式
                return context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")
                        ?.fileDescriptor
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取文件的 bitmap
     *
     * @param context
     * @param pathOUri
     * @param op
     * @return
     */
    @JvmStatic
    fun decodeFile(context: Context, pathOUri: String, op: BitmapFactory.Options? = null): Bitmap? {
        val fileDescriptor: FileDescriptor? = getFileDescriptor(context, pathOUri)
        return if (null != fileDescriptor) {
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, op)
        } else {
            BitmapFactory.decodeFile(pathOUri, op)
        }
    }




    /**
     * 时长单位的转换 秒->毫秒
     */
    @JvmStatic
    fun s2ms(s: Float): Int {
        return (s * 1000).toInt()
    }

    /**
     * 时长单位的转换 秒->毫秒
     */
    @JvmStatic
    fun ms2s(s: Int): Float {
        return s / 1000.0f
    }

    /**
     * 时长单位的转换 秒->毫秒
     */
    @JvmStatic
    fun ms2s(s: Long): Float {
        return s / 1000.0f
    }


    /**
     * 毫秒数转换为时间格式化字符串 existsHours支持是否显示小时,existsMs支持是否显示毫秒
     */
    @JvmStatic
    fun stringForTime(
            timeMs: Long,
            existsHours: Boolean = false,
            existsMs: Boolean = true
    ): String {
        val totalSeconds = (timeMs / 1000).toInt()
        var ms = (timeMs % 1000).toInt()
        ms /= 100
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val builder = StringBuilder()
        val formatter = Formatter(builder, Locale.getDefault())
        return if (hours > 0 || existsHours) {
            if (existsMs) {
                formatter.format(
                        "%02d:%02d:%02d.%1d", hours, minutes,
                        seconds, ms
                ).toString()
            } else {
                formatter.format(
                        "%02d:%02d:%02d", hours, minutes,
                        seconds
                ).toString()
            }
        } else {
            if (existsMs) {
                formatter
                        .format("%02d:%02d.%1d", minutes, seconds, ms)
                        .toString()
            } else {
                formatter.format("%02d:%02d", minutes, seconds)
                        .toString()
            }
        }
    }

}