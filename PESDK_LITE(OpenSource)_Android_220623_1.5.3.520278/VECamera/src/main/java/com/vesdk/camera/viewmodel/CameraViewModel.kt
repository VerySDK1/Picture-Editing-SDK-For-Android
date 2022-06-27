package com.vesdk.camera.viewmodel

import android.graphics.PointF
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vecore.BaseVirtual
import com.vecore.Music
import com.vecore.base.lib.utils.FileUtils
import com.vecore.models.MediaObject
import com.vecore.models.VideoConfig
import com.vecore.models.VisualFilterConfig
import java.io.File
import java.util.*

/**
 * 录制文件
 */
class CameraViewModel : ViewModel() {

    //*****************************外部监听**************************

    /**
     * 录制文件
     */
    private val recorderListLiveData = MutableLiveData<String>()

    /**
     * 音乐
     */
    private val musicLiveData = MutableLiveData<String>()

    /**
     * 录制时间
     */
    private val screenDurationLiveData = MutableLiveData<String>()

    /**
     * 美颜
     */
    private val beautyLiveData = MutableLiveData<String>()

    /**
     * 滤镜
     */
    private val filterLiveData = MutableLiveData<String>()

    fun getRecorderFileLiveData() = recorderListLiveData

    fun getMusicLiveData() = musicLiveData

    fun getDurationLiveData() = screenDurationLiveData

    fun getBeautyLiveData() = beautyLiveData

    fun getFilterLiveData() = filterLiveData


    //*******************************************************

    /**
     * 录制列表
     */
    private val _recorderList = mutableListOf<String>()

    /**
     * 已录制总时间
     */
    private var _recordTotalTime = 0f

    /**
     * 当前录制时间和FPS
     */
    private var _recorderTime = ""
    private var _recorderFps = 0

    /**
     * 音乐
     */
    private var _music: Music? = null

    /**
     * 滤镜
     */
    private var _filterConfig: VisualFilterConfig? = null

    /**
     * 美颜
     */
    private var _beauty = VisualFilterConfig.SkinBeauty()

    /**
     * 五官
     */
    private var _faceAdjustment = VisualFilterConfig.FaceAdjustment()


    /**
     * 美颜
     */
    fun getBeauty(): VisualFilterConfig.SkinBeauty {
        return _beauty
    }

    /**
     * 五官
     */
    fun getFace(): VisualFilterConfig.FaceAdjustment {
        return _faceAdjustment
    }

    /**
     * 存在五官
     */
    fun isFace(): Boolean {
        return _faceAdjustment.bigEyes > 0 || _faceAdjustment.faceLift > 0
    }

    /**
     * 设置点
     */
    fun setFacePoint(facePointF: Array<PointF?>?) {
        _faceAdjustment.facePoints = facePointF
        freshBeauty()
    }

    /**
     * 重置
     */
    fun resetBeauty() {
        _faceAdjustment.resetParams()
        _faceAdjustment.bigEyes = 0f
        _faceAdjustment.faceLift = 0f
        _beauty.resetParams()
        _beauty.whitening = 0f
        _beauty.ruddy = 0f
        _beauty.defaultValue = 0f
        freshBeauty()
    }

    /**
     * 美颜
     */
    fun freshBeauty() {
        this.beautyLiveData.postValue("")
    }


    /**
     * 返回滤镜
     */
    fun getFilter(): VisualFilterConfig? {
        return _filterConfig
    }

    /**
     * 设置滤镜
     */
    fun setFilter(config: VisualFilterConfig?) {
        _filterConfig = config
        filterLiveData.postValue("")
    }


    /**
     * 添加音乐
     */
    fun addMusicPath(path: String) {
        _music = BaseVirtual.createMusic(path)
        musicLiveData.postValue("")
    }

    /**
     * 删除音乐
     */
    fun clearMusic() {
        _music = null
        musicLiveData.postValue("")
    }

    /**
     * 音乐名字
     */
    fun getMusicName(): String {
        return _music?.let {
            File(it.musicPath).name
        } ?: ""
    }

    /**
     * 音乐
     */
    fun getMusic(): Music? {
        return _music
    }


    /**
     * 获取时间
     */
    fun getRecorderTime(): String {
        return _recorderTime
    }

    /**
     * 获取FPS
     */
    fun getRecorderFps(): String {
        return String.format(Locale.CHINA, "%d fps", _recorderFps)
    }

    /**
     * 当前录制
     */
    fun screenDuration(msg: String, recordFPS: Int) {
        _recorderTime = msg
        _recorderFps = recordFPS
        screenDurationLiveData.postValue("")
    }

    /**
     * 录制时间
     */
    fun getRecorderTotalTime(): Float {
        return _recordTotalTime
    }

    /**
     * 获取录制
     */
    fun getRecorderList(): MutableList<String> {
        return _recorderList
    }


    /**
     * 增加录制
     */
    fun addCameraPath(path: String) {
        //录制列表
        _recorderList.add(path)

        //录制时间
        val vcRecord = VideoConfig()
        val duration = BaseVirtual.getMediaInfo(path, vcRecord)
        _recordTotalTime += duration

        //监听
        recorderListLiveData.postValue("")
    }

    /**
     * 删除所有的
     */
    fun clearAll() {
        //删除
        for (file in _recorderList) {
            FileUtils.deleteAll(file)
        }
        _recorderList.clear()
        _recordTotalTime = 0f

        //监听
        recorderListLiveData.postValue("")
    }

    /**
     * 删除录制
     */
    fun deleteCameraPath(position: Int) {
        if (position < 0 || position >= _recorderList.size) {
            return
        }
        val removeAt = _recorderList.removeAt(position)
        _recordTotalTime -= MediaObject(removeAt).duration
        FileUtils.deleteAll(removeAt)

        //监听
        recorderListLiveData.postValue("")
    }

}