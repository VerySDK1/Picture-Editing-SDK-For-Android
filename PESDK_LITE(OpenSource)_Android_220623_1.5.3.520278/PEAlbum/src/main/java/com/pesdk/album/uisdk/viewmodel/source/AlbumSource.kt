package com.pesdk.album.uisdk.viewmodel.source

import androidx.lifecycle.liveData
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_DEFAULT
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_IMAGE_ONLY
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_VIDEO_ONLY
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.uisdk.bean.AlbumItem
import com.pesdk.album.uisdk.viewmodel.repository.AlbumRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class AlbumSource {

    /**
     * 获取目录下所有的媒体
     */
    fun getAllMedia(statue: Int = ALBUM_SUPPORT_DEFAULT, id: String) =
            fire(Dispatchers.IO) {
                val resolver = AlbumSdkInit.context.contentResolver
                when (statue) {
                    ALBUM_SUPPORT_DEFAULT -> AlbumRepository.getMediaList(resolver, id)
                    ALBUM_SUPPORT_VIDEO_ONLY -> AlbumRepository.getVideoList(resolver, id)
                    ALBUM_SUPPORT_IMAGE_ONLY -> AlbumRepository.getPhotoList(resolver, id)
                    else -> AlbumRepository.getPhotoList(resolver, id)
                }
            }

    private fun fire(content: CoroutineContext, block: suspend () -> MutableList<AlbumItem>) =
            liveData(content) {
                emit(block())
            }

}