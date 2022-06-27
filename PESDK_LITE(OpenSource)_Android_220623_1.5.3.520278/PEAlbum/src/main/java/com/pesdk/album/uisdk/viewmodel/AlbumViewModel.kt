package com.pesdk.album.uisdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.pesdk.album.api.bean.MediaInfo
import com.pesdk.album.uisdk.bean.DirectoryInfo
import com.pesdk.album.uisdk.bean.MediaDirectory
import com.pesdk.album.uisdk.viewmodel.source.DirectorySource
import java.util.*

/**
 * 媒体
 */
class AlbumViewModel : ViewModel() {

    private val directorySource = DirectorySource()


    /**
     * 刷新目录监听
     */
    private val _dirLiveData = MutableLiveData<Int>()

    /**
     * 剪同款
     */
    private var _isTemplate = false;

    /*-------------------------外部监听------------------------*/

    /**
     * 选中
     */
    val selectedLiveData = MutableLiveData<String>()

    /**
     * 目录列表
     */
    val directoryListData = Transformations.switchMap(_dirLiveData) {
        directorySource.getAllDirectory(it)
    }

    /**
     * 选中目录
     */
    val selectedDir = MutableLiveData<MediaDirectory>()


    /*-------------------------数据监听------------------------*/


    /**
     * 选中
     */
    private val selectedList = mutableListOf<MediaInfo>()

    /**
     * 目录
     */
    private val directoryList = mutableListOf<DirectoryInfo>()


    /**
     * 添加选中媒体
     */
    fun addSelectMedia(mediaInfo: MediaInfo) {
        if (!_isTemplate) {
            selectedList.add(mediaInfo)
        }
        selectedLiveData.value = mediaInfo.path
    }

    /**
     * 移除选中媒体
     */
    fun deleteSelectMedia(mediaInfo: MediaInfo?) {
        if (!_isTemplate && mediaInfo != null) {
            selectedList.remove(mediaInfo)
        }
        selectedLiveData.value = ""
    }

    /**
     * 返回选中媒体
     */
    fun getSelectedList(): MutableList<MediaInfo> {
        return selectedList
    }

    /**
     * 交换
     */
    fun swapSelectedList(fromPosition: Int, toPosition: Int) {
        Collections.swap(selectedList, fromPosition, toPosition)
    }


    /**
     * 刷新目录
     */
    fun freshDirectory(type: Int) {
        _dirLiveData.value = type
    }

    /**
     * 设置目录
     */
    fun setDirectoryList(list: MutableList<DirectoryInfo>) {
        directoryList.clear()
        directoryList.addAll(list)
    }

    /**
     * 获取目录列表
     */
    fun getDirectoryList(): MutableList<DirectoryInfo> {
        return directoryList
    }


    /**
     * 刷新选中目录
     */
    fun freshSelectedDirectory(dir: MediaDirectory) {
        selectedDir.postValue(dir)
    }

    /**
     * 设置剪同款
     */
    fun setTemplate(template: Boolean = true) {
        _isTemplate = template
    }

}