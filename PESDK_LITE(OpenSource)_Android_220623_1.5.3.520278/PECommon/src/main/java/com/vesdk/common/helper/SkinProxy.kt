package com.vesdk.common.helper

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate.*
import com.vesdk.common.listener.SkinApi

/**
 * 实现类
 */
object SkinProxy : SkinApi {

    /**
     * 自动
     */
    private const val THEME_SYSTEM = "system"

    /**
     * 晚上
     */
    private const val THEME_NIGHT = "night"

    /**
     * 白天
     */
    private const val THEME_DAY = "day"


    /**
     * 初始化
     */
    override fun init(context: Context) {
        when (MMKVHelper.getAppSKinName()) {
            THEME_DAY -> {
                loadSkinDay()
            }
            THEME_NIGHT -> {
                loadSkinNight()
            }
            else -> {
                //跟随系统
                loadSkinDefault()
            }
        }
    }

    /**
     * 晚上
     */
    override fun loadSkinNight() {
        MMKVHelper.saveAppSKinName(THEME_NIGHT)
        setDefaultNightMode(MODE_NIGHT_YES)
    }

    /**
     * 白天
     */
    override fun loadSkinDay() {
        MMKVHelper.saveAppSKinName(THEME_DAY)
        setDefaultNightMode(MODE_NIGHT_NO)
    }

    /**
     * 跟随系统
     */
    override fun loadSkinDefault() {
        MMKVHelper.saveAppSKinName(THEME_SYSTEM)
        setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * 黑色文字
     */
    fun isDark(context: Context): Boolean {
        return !getDarkModeStatus(context)
    }

    /**
     * 检查当前系统是否已开启暗黑模式
     */
    private fun getDarkModeStatus(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

}