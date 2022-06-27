package com.pesdk.album.uisdk.viewmodel.repository

import android.content.ContentResolver
import android.text.TextUtils
import com.pesdk.album.uisdk.bean.DirectoryInfo
import com.pesdk.album.uisdk.ui.activity.AlbumActivity.Companion.MIN_GALLERY_VIDEO_DURATION
import com.pesdk.album.uisdk.ui.activity.AlbumActivity.Companion.UN_SUPPORT_VIDEO
import com.vecore.base.gallery.IImage
import com.vecore.base.gallery.IVideo
import com.vecore.base.gallery.ImageManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object DirectoryRepository {

    /**
     * 获取所有的图片目录
     */
    suspend fun getPhotoList(resolver: ContentResolver): MutableList<DirectoryInfo> {
        return suspendCoroutine { continuation ->
            //所有的图片
            val ilpParam = ImageManager.allPhotos(true, true)
            val ilImages = ImageManager.makeImageList(resolver, ilpParam)
            val mutableList = mutableListOf<DirectoryInfo>()

            var dirImage: IImage? = null
            var allCount = 0
            ilImages?.let {
                for ((id, name) in it.bucketIds) {
                    val param = ImageManager.allPhotos(true)
                    param.mBucketId = id
                    val imageList = ImageManager.makeImageList(resolver, param) ?: continue
                    //计算数量
                    var count = 0
                    var iImage: IImage? = null
                    for (i in 0 until imageList.count) {
                        val image = imageList.getImageAt(i)
                        if (image.isValid) {
                            count++
                            iImage = image
                            if (dirImage == null) {
                                dirImage = image
                            }
                        }
                    }
                    if (count > 0) {
                        allCount += count
                        mutableList.add(DirectoryInfo(id, name, count.toString(), iImage))
                    }
                    imageList.close()
                }
                mutableList.add(0, DirectoryInfo("0", "", allCount.toString(), dirImage))
            }
            continuation.resume(mutableList)
        }
    }

    /**
     * 所有的视频目录
     */
    suspend fun getVideoList(resolver: ContentResolver): MutableList<DirectoryInfo> {
        return suspendCoroutine { continuation ->
            //所有的图片
            val ilpParam = ImageManager.allVideos(true, true)
            val mediaList = ImageManager.makeImageList(resolver, ilpParam)
            val mutableList = mutableListOf<DirectoryInfo>()

            var dirImage: IImage? = null
            var allCount = 0
            mediaList?.let {
                for ((id, name) in it.bucketIds) {
                    val param = ImageManager.allVideos(true, true)
                    param.mBucketId = id
                    val videoList = ImageManager.makeImageList(resolver, param) ?: continue

                    //计算数量
                    var count = 0
                    var iImage: IImage? = null
                    for (i in 0 until videoList.count) {
                        val video = videoList.getImageAt(i)
                        val dataPath = video.dataPath
                        //地址为null
                        if (TextUtils.isEmpty(dataPath) || !video.isValid) {
                            continue
                        }
                        if (video.id <= 0
                            || ((video as IVideo).duration < MIN_GALLERY_VIDEO_DURATION)
                            || UN_SUPPORT_VIDEO.any { suffix -> dataPath.endsWith(suffix) }
                        ) {
                            continue
                        }
                        count++
                        iImage = video
                        if (dirImage == null) {
                            dirImage = video
                        }
                    }
                    if (count > 0) {
                        allCount += count
                        mutableList.add(DirectoryInfo(id, name, count.toString(), iImage))
                    }
                    videoList.close()
                }
                mutableList.add(0, DirectoryInfo("0", "", allCount.toString(), dirImage))
            }
            continuation.resume(mutableList)
        }
    }

    /**
     * 所有的图片和视频目录
     */
    suspend fun getMediaList(resolver: ContentResolver): MutableList<DirectoryInfo> {
        return suspendCoroutine { continuation ->
            //所有的图片
            val ilpParam = ImageManager.allMedia(true, true)
            val mediaList = ImageManager.makeImageList(resolver, ilpParam)
            val mutableList = mutableListOf<DirectoryInfo>()

            var dirImage: IImage? = null
            var allCount = 0
            mediaList?.let {
                for ((id, name) in it.bucketIds) {
                    val param = ImageManager.allMedia(true, true)
                    param.mBucketId = id
                    val mediaBucketList = ImageManager.makeImageList(resolver, param) ?: continue
                    //计算数量
                    var count = 0
                    var iImage: IImage? = null
                    for (i in 0 until mediaBucketList.count) {
                        val media = mediaBucketList.getImageAt(i) ?: continue
                        if (media is IVideo) {
                            val dataPath = media.dataPath
                            //地址为null
                            if (TextUtils.isEmpty(dataPath) || !media.isValid) {
                                continue
                            }
                            if (media.id <= 0
                                || (media.duration < MIN_GALLERY_VIDEO_DURATION)
                                || UN_SUPPORT_VIDEO.any { suffix -> dataPath.endsWith(suffix) }
                            ) {
                                continue
                            }
                            count++
                            iImage = media
                            if (dirImage == null) {
                                dirImage = media
                            }
                        } else {
                            if (media.isValid) {
                                count++
                                iImage = media
                                if (dirImage == null) {
                                    dirImage = media
                                }
                            }
                        }
                    }
                    if (count > 0) {
                        allCount += count
                        mutableList.add(DirectoryInfo(id, name, count.toString(), iImage))
                    }
                    mediaBucketList.close()
                }
                mutableList.add(0, DirectoryInfo("0", "", allCount.toString(), dirImage))
            }
            continuation.resume(mutableList)
        }
    }

}


