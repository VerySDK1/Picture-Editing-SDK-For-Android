package com.pesdk.demo.utils

import android.annotation.SuppressLint
import android.content.Context
import com.pesdk.demo.R
import com.pesdk.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    /**
     * 获取时间
     */
    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    fun getDate(time: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        sdf.timeZone = TimeZone.getTimeZone("GMT+8")
        return try {
            sdf.format(time)
        } catch (e: Exception) {
            sdf.format(System.currentTimeMillis())
        }
    }


    /**
     * 获取时间
     */
    fun getDateString(time: Long): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        sdf.timeZone = TimeZone.getTimeZone("GMT+8")
        return try {
            sdf.format(time)
        } catch (e: java.lang.Exception) {
            sdf.format(System.currentTimeMillis())
        }
    }
    /**
     * 相差时间
     */
      fun getUpdateTime(context: Context, time: Long): String {
        val diff = System.currentTimeMillis() - time
        return if (diff > 1000 * 60 * 60 * 5 || time == 0L) {
            //大于5个小时
            context.getString( R.string.flow_update_time, DateUtils.longToDate(time))
        } else {
            val seconds = (diff / 1000 / 60 % 60).toInt()
            val hour = (diff / 1000 / 60 / 60).toInt()
            when {
                hour > 0 -> {
                    context.getString(R.string.flow_update_time_hour, hour)
                }
                seconds >= 1 -> {
                    context.getString(R.string.flow_update_time_seconds, seconds)
                }
                else -> {
                    context.getString(R.string.flow_update_time_now)
                }
            }
        }
    }


}