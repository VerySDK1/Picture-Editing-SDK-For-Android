package com.pesdk.album.uisdk.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.vecore.utils.UriUtils
import java.math.BigInteger
import java.security.SecureRandom

/**
 */
object AlbumUtils {
    /**
     * 随机
     */
    private val RANDOM = SecureRandom()

    /**
     * 获取随机字符串
     */
    val randomId: String
        get() = BigInteger(130, RANDOM).toString(32)

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

    @JvmStatic
    fun isUri(uriString: String): Boolean {
        return UriUtils.isUri(uriString)
    }

    /**
     * 根据Uri获取绝对路径
     */
    @JvmStatic
    fun getAbsolutePath(context: Context, uri: Uri): String? {
        return UriUtils.getUriLegacyPath(context, uri)
    }

}