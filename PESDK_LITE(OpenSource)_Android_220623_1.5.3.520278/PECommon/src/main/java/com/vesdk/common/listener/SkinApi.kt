package com.vesdk.common.listener

import android.content.Context

/**
 * 主题
 */
interface SkinApi {
    /**
     * 初始化
     */
    fun init(context: Context)

    /**
     * 加载暗黑
     */
    fun loadSkinNight()

    /**
     * 亮色
     */
    fun loadSkinDay()

    /**
     * 跟随系统
     */
    fun loadSkinDefault()
}