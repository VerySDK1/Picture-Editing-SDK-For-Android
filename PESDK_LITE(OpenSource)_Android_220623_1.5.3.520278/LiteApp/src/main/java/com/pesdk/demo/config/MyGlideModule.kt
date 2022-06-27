package com.pesdk.demo.config

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.vecore.base.lib.utils.FileUtils
import java.io.File

/**
 * 配置Glide缓存目录
 */
@GlideModule
class MyGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        val maxMemory = Runtime.getRuntime().maxMemory().toInt() //获取系统分配给应用的总内存大小 512M
        var memoryCacheSize = 8000000.coerceAtMost(maxMemory / 10) //设置图片内存缓存占用1/10   8M
        //设置内存缓存大小
        builder.setMemoryCache(LruResourceCache(memoryCacheSize.toLong()))

        //设置BitmapPool缓存内存大小
        memoryCacheSize = 3000000 //3M
        builder.setBitmapPool(LruBitmapPool(memoryCacheSize.toLong()))
        //自定义缓存目录，磁盘缓存给150M 另外一种设置缓存方式
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, GLIDE_CACHE_DIR, 150 * 1024 * 1024))
        builder.setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565))
        //需要透明UI单独处理
        //builder.setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_ARGB_8888))
    }

    companion object {

        private const val GLIDE_CACHE_DIR = "GlideCache"

        /**
         * glide缓存size
         */
        fun getGlideCacheSize(context: Context?): Long {
            return context?.let {
                FileUtils.getFolderSize(File(it.cacheDir, GLIDE_CACHE_DIR))
            } ?: 0
        }
    }
}