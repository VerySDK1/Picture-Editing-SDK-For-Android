package com.pesdk.album.uisdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.pesdk.album.uisdk.bean.AlbumItem
import com.pesdk.album.uisdk.bean.MediaDirectory
import com.pesdk.album.uisdk.viewmodel.source.AlbumSource

class GalleryViewModel : ViewModel() {

    private val albumSource = AlbumSource()

    /**
     * 选中目录
     */
    private val _selectedDirLiveData = MutableLiveData<MediaDirectory>()

    /**
     * 目录下的数据
     */
    val mediaListData = Transformations.switchMap(_selectedDirLiveData) {
        albumSource.getAllMedia(it.type, it.id)
    }


    /**
     * 数据
     */
    private val albumItemList = mutableListOf<AlbumItem>()

    /**
     * 刷新目录
     */
    fun freshMedia(type: Int, id: String = "0") {
        _selectedDirLiveData.value = MediaDirectory(type, id)
    }

    /**
     * 设置相册
     */
    fun setAlbumList(list: MutableList<AlbumItem>) {
        albumItemList.clear()
        albumItemList.addAll(list)
    }

    /**
     * 获取相册
     */
    fun getAlbumList(): MutableList<AlbumItem> {
        return albumItemList
    }

}