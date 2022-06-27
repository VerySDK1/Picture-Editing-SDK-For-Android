package com.pesdk.album.uisdk.viewmodel.source

import androidx.lifecycle.liveData
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_DEFAULT
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_IMAGE_ONLY
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_VIDEO_ONLY
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.uisdk.bean.DirectoryInfo
import com.pesdk.album.uisdk.viewmodel.repository.DirectoryRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class DirectorySource {

    fun getAllDirectory(type: Int = ALBUM_SUPPORT_DEFAULT) = fire(Dispatchers.IO) {
        val resolver = AlbumSdkInit.context.contentResolver
        when (type) {
            ALBUM_SUPPORT_DEFAULT -> DirectoryRepository.getMediaList(resolver)
            ALBUM_SUPPORT_VIDEO_ONLY -> DirectoryRepository.getVideoList(resolver)
            ALBUM_SUPPORT_IMAGE_ONLY -> DirectoryRepository.getPhotoList(resolver)
            else -> DirectoryRepository.getPhotoList(resolver)
        }
    }

    private fun fire(content: CoroutineContext, block: suspend () -> MutableList<DirectoryInfo>) =
            liveData(content) {
                emit(block())
            }


}