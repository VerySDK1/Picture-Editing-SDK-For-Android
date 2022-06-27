package com.vesdk.common.utils

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vecore.base.lib.utils.FileUtils
import com.vesdk.common.bean.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger
import java.security.SecureRandom

object CommonUtils {

    /**
     * 权限列表
     */
    private val permissionList = mutableListOf<Permission>()

    /**
     * 返回权限
     */
    suspend fun getPermission(context: Context, key: String) = withContext(Dispatchers.IO) {
        var result: Permission? = null
        if (permissionList.isEmpty()) {
            FileUtils.readTxtFile(context, "asset://permissions.json")?.let { permissions ->
                val list: MutableList<Permission>? = Gson()
                        .fromJson(permissions, object : TypeToken<MutableList<Permission>>() {}.type)
                list?.let {
                    if (it.size > 0) {
                        permissionList.addAll(it)
                    }
                }
            }
        }
        if (permissionList.isNotEmpty()) {
            for (info in permissionList) {
                if (key == info.key) {
                    result = info
                    break
                }
            }
        }
        result
    }

    /**
     * 获取key
     */
    @JvmStatic
    fun getKey(id: String, url: String): String {
        return "$id$url".hashCode().toString()
    }

    /**
     * 获取屏幕宽度
     * @param context 上下文
     * @return 宽度
     */
    @JvmStatic
    fun getWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    /**
     * 获取屏幕高度
     * @param context 上下文
     * @return 高度
     */
    @JvmStatic
    fun getHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

    @JvmStatic
    fun dip2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 文件名重命名
     *
     * @param oldFile 要修改的文件旧路径
     * @param newFile 新的文件路径
     * @return 成功返回true, 失败返回false
     */
    @JvmStatic
    fun renameFile(oldFile: String, newFile: String): Boolean {
        val old = File(oldFile)
        val new = File(newFile)
        if (new.exists()) {
            // 若在该目录下已经有一个文件和新文件名相同，则删除
            new.delete()
        }
        return old.renameTo(new)
    }

    /**
     * 随机
     */
    private val RANDOM = SecureRandom()

    /**
     * 获取随机字符串
     */
    @JvmStatic
    fun getRandomId(): String {
        return BigInteger(130, RANDOM).toString(32)
    }
}