package com.vesdk.camera.viewmodel.repository

import com.pesdk.api.ChangeLanguageHelper
import com.vesdk.camera.api.NetworkApi
import com.vesdk.camera.api.RetrofitCreator
import com.vesdk.camera.entry.CameraSdkInit
import com.vesdk.common.helper.RetrofitHelper

object NetworkRepository {

    private const val APP_KEY = "appkey"
    private const val VER = "ver"
    private const val OS = "os"
    private const val LANG = "lang"
    private const val TYPE = "type"
    private const val VER_MIN = "ver_min"
    private const val CATEGORY = "category"

    private const val ANDROID = "android"
    private const val LANGUAGE_EN = "en"
    private const val LANGUAGE_CN = "cn"

    /**
     * 素材库
     */
    private val networkApi = RetrofitCreator.create<NetworkApi>()


    /**
     * 素材库分类
     */
    suspend fun getSort(type: String, minVer: String) = RetrofitHelper.fire(networkApi) { api ->
        api.getSort(getSortMap(type, minVer))
    }

    /**
     * 素材具体分类下数据
     */
    suspend fun getCategoryData(type: String, category: String) = RetrofitHelper.fire(networkApi) { api ->
        api.getData(getFilterMap(type, category))
    }




    /**
     * 分类
     */
    private fun getSortMap(type: String, minVer: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map[APP_KEY] = CameraSdkInit.APP_KEY
        map[VER] = CameraSdkInit.getVersion()
        map[OS] = ANDROID
        map[LANG] =
                if (ChangeLanguageHelper.isZh(CameraSdkInit.context)) LANGUAGE_CN else LANGUAGE_EN
        map[TYPE] = type//类别
        map[VER_MIN] = minVer//最小版本号
        return map
    }

    /**
     * 数据
     */
    private fun getFilterMap(type: String, category: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map[APP_KEY] = CameraSdkInit.APP_KEY
        map[VER] = CameraSdkInit.getVersion()
        map[OS] = ANDROID
        map[LANG] =
                if (ChangeLanguageHelper.isZh(CameraSdkInit.context)) LANGUAGE_CN else LANGUAGE_EN
        map[TYPE] = type//类别
        map[CATEGORY] = category//id
        return map
    }

}