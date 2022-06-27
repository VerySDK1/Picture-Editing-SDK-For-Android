package com.pesdk.album.uisdk.viewmodel.repository

import android.content.ContentResolver
import android.text.TextUtils
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.uisdk.bean.AlbumItem
import com.pesdk.album.uisdk.ui.activity.AlbumActivity.Companion.MIN_GALLERY_VIDEO_DURATION
import com.pesdk.album.uisdk.ui.activity.AlbumActivity.Companion.UN_SUPPORT_VIDEO
import com.vecore.base.gallery.IVideo
import com.vecore.base.gallery.ImageManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AlbumRepository {

    /**
     * 获取所有的图片
     */
    suspend fun getPhotoList(
            resolver: ContentResolver,
            bucketId: String = "0"
    ): MutableList<AlbumItem> {
        return suspendCoroutine { continuation ->
            //所有的图片
            val ilpParam = ImageManager.allPhotos(true)
            if (bucketId != "0") {
                ilpParam.mBucketId = bucketId
            }
            val ilImages = ImageManager.makeImageList(resolver, ilpParam)
            val mutableList = mutableListOf<AlbumItem>()
            ilImages?.let {
                for (i in 0 until it.count) {
                    val image = ilImages.getImageAt(i)
                    mutableList.add(AlbumItem(image, 0, MediaType.TYPE_IMAGE))
                }
            }
            continuation.resume(mutableList)
        }
    }

    /**
     * 所有的视频
     */
    suspend fun getVideoList(
            resolver: ContentResolver,
            bucketId: String = "0"
    ): MutableList<AlbumItem> {
        return suspendCoroutine { continuation ->
            //所有的图片
            val ilpParam = ImageManager.allVideos(true, true)
            if (bucketId != "0") {
                ilpParam.mBucketId = bucketId
            }
            val ilVideos = ImageManager.makeImageList(resolver, ilpParam)
            val mutableList = mutableListOf<AlbumItem>()
            ilVideos?.let {
                for (i in 0 until it.count) {
                    val video = ilVideos.getImageAt(i)
                    val dataPath = video.dataPath
                    //地址为null
                    if (TextUtils.isEmpty(dataPath) || !video.isValid) {
                        continue
                    }
                    //时间和后缀限制
                    if (video.id <= 0
                            || ((video as IVideo).duration < MIN_GALLERY_VIDEO_DURATION)
                            || UN_SUPPORT_VIDEO.any { suffix -> dataPath.endsWith(suffix) }
                    ) {
                        continue
                    }
                    mutableList.add(
                            AlbumItem(video, video.duration, MediaType.TYPE_VIDEO)
                    )
                }
            }
            continuation.resume(mutableList)
        }
    }


    /**
     * 所有的图片和视频
     */
    suspend fun getMediaList(
            resolver: ContentResolver,
            bucketId: String = "0"
    ): MutableList<AlbumItem> {
        return suspendCoroutine { continuation ->
            //所有的图片
            val ilpParam = ImageManager.allMedia(true, true)
            if (bucketId != "0") {
                ilpParam.mBucketId = bucketId
            }
            val ilMedias = ImageManager.makeImageList(resolver, ilpParam)
            val mutableList = mutableListOf<AlbumItem>()
            ilMedias?.let {
                for (i in 0 until it.count) {
                    val media = ilMedias.getImageAt(i)
                    if (media is IVideo) {
                        val dataPath = media.dataPath
                        //地址为null
                        if (TextUtils.isEmpty(dataPath) || !media.isValid) {
                            continue
                        }
                        //时间和后缀限制
                        if (media.id <= 0
                                || (media.duration < MIN_GALLERY_VIDEO_DURATION)
                                || UN_SUPPORT_VIDEO.any { suffix -> dataPath.endsWith(suffix) }
                        ) {
                            continue
                        }
                        mutableList.add(
                                AlbumItem(media, media.duration, MediaType.TYPE_VIDEO)
                        )
                    } else {
                        mutableList.add(AlbumItem(media, 0, MediaType.TYPE_IMAGE))
                    }
                }
            }
            continuation.resume(mutableList)
        }
    }

}


