package com.pesdk.album.uisdk.viewmodel.repository

import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.uisdk.api.ApiConfig
import com.pesdk.album.uisdk.api.MaterialLibraryApi
import com.pesdk.album.uisdk.api.RetrofitCreator
import com.pesdk.album.uisdk.api.TtfApi
import com.pesdk.api.ChangeLanguageHelper
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
    private val materialLibraryApi = RetrofitCreator.create<MaterialLibraryApi>()

    /**
     * 字体
     */
    private val ttfApi = RetrofitCreator.create<TtfApi>()

    /**
     * 素材库分类
     */
    suspend fun getMaterialSort(minVer: String) = RetrofitHelper.fire(materialLibraryApi) { api ->
        api.getMaterialLibrarySort(getSortMap(minVer))
    }

    /**
     * 素材库
     */
    suspend fun getMaterialData(category: String) = RetrofitHelper.fire(materialLibraryApi) { api ->
        api.getMaterialLibraryData(getMaterialMap(category))
    }

    /**
     * 字体
     */
    suspend fun getTtfData() = RetrofitHelper.fire(ttfApi) { api ->
        api.getTtfList(getTtfMap())
    }


    /**
     * 分类
     */
    private fun getSortMap(minVer: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map[APP_KEY] = AlbumSdkInit.APP_KEY
        map[VER] = AlbumSdkInit.getVersion()
        map[OS] = ANDROID
        map[LANG] =
                if (ChangeLanguageHelper.isZh(AlbumSdkInit.context)) LANGUAGE_CN else LANGUAGE_EN
        map[TYPE] = ApiConfig.TYPE_CLOUD_VIDEO//类别
        map[VER_MIN] = minVer//最小版本号
        return map
    }

    /**
     * 数据
     */
    private fun getMaterialMap(category: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map[APP_KEY] = AlbumSdkInit.APP_KEY
        map[VER] = AlbumSdkInit.getVersion()
        map[OS] = ANDROID
        map[LANG] =
                if (ChangeLanguageHelper.isZh(AlbumSdkInit.context)) LANGUAGE_CN else LANGUAGE_EN
        map[TYPE] = ApiConfig.TYPE_CLOUD_VIDEO//类别
        map[CATEGORY] = category//id
        return map
    }

    /**
     * 字体数据
     */
    private fun getTtfMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map[APP_KEY] = AlbumSdkInit.APP_KEY
        map[VER] = AlbumSdkInit.getVersion()
        map[OS] = ANDROID
        map[LANG] =
                if (ChangeLanguageHelper.isZh(AlbumSdkInit.context)) LANGUAGE_CN else LANGUAGE_EN
        map[TYPE] = ApiConfig.TYPE_FONT_2//类别
        return map
    }


}