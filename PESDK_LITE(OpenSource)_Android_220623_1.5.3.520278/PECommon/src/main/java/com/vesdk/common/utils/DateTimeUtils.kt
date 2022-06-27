package com.vesdk.common.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间日期及格式化工具
 */
object DateTimeUtils {

    /**
     * 时间格式
     */
    private const val DEFAULT_TIME_PATTERN = "yyyy-MM-dd hh:mm:ss"

    /**
     * 字符串
     */
    private val sBuilder = StringBuilder()

    /**
     * 格式
     */
    private val sFormatter = Formatter(sBuilder)

    /**
     * 秒数转换为时间格式化字符串
     */
    fun second2String(s: Float): String {
        return millisecond2String((s * 1000).toLong(), false)
    }

    /**
     * 毫秒数转换为时间格式化字符串 支持是否显示小时
     */
    fun millisecond2String(ms: Long, existsHours: Boolean = false): String {
        var timeMs = ms
        val bNegative = timeMs < 0 // 是否为负数
        if (bNegative) {
            timeMs = -timeMs
        }
        val totalSeconds = (timeMs / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        synchronized(sBuilder) {
            sBuilder.setLength(0)
            return try {
                if (hours > 0 || existsHours) {
                    sFormatter.format(
                            "%s%02d:%02d:%02d",
                            if (bNegative) "-" else "", hours, minutes, seconds
                    )
                            .toString()
                } else {
                    sFormatter.format(
                            "%s%02d:%02d", if (bNegative) "-" else "",
                            minutes, seconds
                    ).toString()
                }
            } catch (ex: Exception) {
                ""
            }
        }
    }

    /**
     * 毫秒数转换为时间格式化字符串 支持是否显示小时或毫秒
     *
     * @param ms            毫秒
     * @param existsHours       小时
     * @param existsMillisecond 毫秒
     * @param alignment         是否需要统计显示宽度，如为true时，5:4.5就为05:04.5
     */
    fun millisecond2String(
            ms: Long,
            existsHours: Boolean = false,
            exitsMin: Boolean = true,
            existsMillisecond: Boolean = false,
            alignment: Boolean = true
    ): String? {
        var timeMs = ms
        val bNegative = timeMs < 0 // 是否为负数
        if (bNegative) {
            timeMs = -timeMs
        }
        val totalSeconds = (timeMs / 1000).toInt() // 总计时间
        val millisecond = (timeMs % 1000).toInt() / 100 // 毫秒
        val seconds = totalSeconds % 60 // 秒
        val minutes = totalSeconds / 60 % 60 // 分
        val hours = totalSeconds / 3600 // 时
        synchronized(sBuilder) {
            sBuilder.setLength(0)
            return try {
                // 判断是否支持小时
                if (hours > 0 || existsHours) {
                    sFormatter.format(
                            if (alignment) "%s%02d:%02d:%02d" else "%s%d:%d:%d",
                            if (bNegative) "-" else "", hours, minutes, seconds
                    )
                            .toString()
                } else if (existsMillisecond) {
                    if (exitsMin) {
                        if (minutes > 0 || alignment) {
                            sFormatter.format(
                                    if (alignment) "%s%02d:%02d.%d" else "%s%d:%d.%d",
                                    if (bNegative) "- " else "", minutes, seconds,
                                    millisecond
                            ).toString()
                        } else {
                            sFormatter.format(
                                    "%s%d.%d",
                                    if (bNegative) "- " else "", seconds, millisecond
                            )
                                    .toString()
                        }
                    } else {
                        val sec = hours * 60 * 60 + minutes * 60 + seconds
                        sFormatter.format(
                                "%d.%d",
                                sec,
                                millisecond
                        )
                                .toString()
                    }
                } else {
                    sFormatter.format(
                            if (alignment) "%s%02d:%02d" else "%s%d:%d",
                            if (bNegative) "- " else "", minutes, seconds
                    ).toString()
                }
            } catch (ex: java.lang.Exception) {
                ""
            }
        }
    }

    /**
     * 获取时间 yyyy-MM-dd hh:mm:ss
     */
    fun getCurrent(time: Long): String {
        val sdf = SimpleDateFormat(DEFAULT_TIME_PATTERN, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT+8")
        return try {
            sdf.format(time)
        } catch (e: Exception) {
            sdf.format(System.currentTimeMillis())
        }
    }

    /**
     * 时间 yyyy-MM-dd hh:mm:ss
     */
    fun getCurrent(time: String): Long {
        val sim = SimpleDateFormat(DEFAULT_TIME_PATTERN, Locale.getDefault())
        return sim.parse(time)?.time ?: 0
    }

}