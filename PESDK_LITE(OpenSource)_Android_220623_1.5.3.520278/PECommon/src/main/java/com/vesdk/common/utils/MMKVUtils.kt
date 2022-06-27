package com.vesdk.common.utils

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV

object MMKVUtils {

    private var mmkv: MMKV = MMKV.defaultMMKV()

    /**
     * 移除某个key对
     */
    fun removeKey(key: String) {
        mmkv.removeValueForKey(key)
    }

    /**
     * 清除所有key
     */
    fun clearAll() {
        mmkv.clearAll()
    }

    /**
     * 从sp中迁移到mmvk
     */
    fun sp2Mmkv(context: Context, name: String) {
        val old = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        // 迁移旧数据
        mmkv.importFromSharedPreferences(old)
        // 清空旧数据
        old.edit().clear().apply()
    }

    /**
     * 加密
     */
    fun encryption(password: String?) {
        //加密
        mmkv.reKey(password)
    }

    /**
     * 切换
     */
    fun cutover(id: String?, mode: Int = MMKV.SINGLE_PROCESS_MODE) {
        mmkv = if (id == null) {
            MMKV.defaultMMKV()
        } else {
            MMKV.mmkvWithID(id, mode)
        }
    }


    fun encode(key: String, obj: Any): Boolean {
        val encode: Boolean
        when (obj) {
            is String -> {
                encode = mmkv.encode(key, obj)
            }
            is Int -> {
                encode = mmkv.encode(key, obj)
            }
            is Boolean -> {
                encode = mmkv.encode(key, obj)
            }
            is Float -> {
                encode = mmkv.encode(key, obj)
            }
            is Long -> {
                encode = mmkv.encode(key, obj)
            }
            is Double -> {
                encode = mmkv.encode(key, obj)
            }
            is ByteArray -> {
                encode = mmkv.encode(key, obj)
            }
            else -> {
                encode = mmkv.encode(key, obj.toString())
            }
        }
        return encode
    }

    fun encodeSet(key: String, sets: Set<String?>): Boolean {
        return mmkv.encode(key, sets)
    }

    fun encodeParcelable(key: String, obj: Parcelable): Boolean {
        return mmkv.encode(key, obj)
    }


    fun decodeInt(key: String, defaultValue: Int = 0): Int {
        return mmkv.decodeInt(key, defaultValue)
    }

    fun decodeDouble(key: String, defaultValue: Double = 0.0): Double {
        return mmkv.decodeDouble(key, defaultValue)
    }

    fun decodeLong(key: String, defaultValue: Long = 0L): Long {
        return mmkv.decodeLong(key, defaultValue)
    }

    fun decodeBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return mmkv.decodeBool(key, defaultValue)
    }

    fun decodeFloat(key: String, defaultValue: Float = 0f): Float {
        return mmkv.decodeFloat(key, defaultValue)
    }

    fun decodeBytes(key: String): ByteArray {
        return mmkv.decodeBytes(key)!!
    }

    fun decodeStringDef(key: String, defaultValue: String = ""): String {
        return mmkv.decodeString(key, defaultValue).toString()
    }

    fun decodeStringSet(key: String): Set<String> {
        return mmkv.decodeStringSet(key, emptySet()) as Set<String>
    }

    fun <T : Parcelable?> decodeParcelable(key: String, tClass: Class<T>?): T? {
        return mmkv.decodeParcelable(key, tClass)
    }

}