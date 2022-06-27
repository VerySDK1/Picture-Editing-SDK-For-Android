package com.vesdk.engine

import android.graphics.Bitmap
import com.pesdk.analyzer.ModelAssetHelper
import com.vecore.models.MediaObject
import com.vesdk.engine.bean.EngineError
import com.vesdk.engine.bean.EngineMedia
import com.vesdk.engine.bean.base.BaseSegmentationConfig
import com.vesdk.engine.bean.config.MLKitSegmentationConfig
import com.vesdk.engine.bean.config.MattingSegmentationConfig
import com.vesdk.engine.listener.*
import com.vesdk.engine.segmentation.SegmentationEngine
import com.vesdk.engine.segmentation.engine.MLKitSegmentation
import com.vesdk.engine.segmentation.engine.MattingSegmentation

/**
 * 引擎管理
 */
object EngineManager {

    /**
     * 建议的最大、最小的尺寸
     */
    const val MIN_BITMAP = 256

    /**
     * 分割引擎
     */
    private var mSegmentationEngine: SegmentationEngine? = null

    /**
     * 注册媒体
     */
    private val mMediaFrameList = mutableListOf<MediaExtraListener>()


    /**
     * 释放
     */
    fun release() {
        releaseSegmentation()
        //释放扩展
        releaseExtra()
    }

    /**
     * 释放分割
     */
    fun releaseSegmentation() {
        mSegmentationEngine?.release()
        mSegmentationEngine = null
    }

    /**
     * 释放扩展
     */
    fun releaseExtra() {
        //媒体
        for (media in mMediaFrameList) {
            media.release()
        }
        mMediaFrameList.clear()
    }


    /**
     * 注册媒体
     */
    fun extraMedia(
            media: MediaObject,
            segmentation: Boolean,
            config: BaseSegmentationConfig,
            listener: OnEngineExtraListener
    ) {
        //移除
        removeExtraMedia(media)
        val engineMedia = EngineMedia(media, segmentation)
        val extraListener = MediaExtraListener(engineMedia, config, listener)
        media.setExtraDrawListener(extraListener)
        mMediaFrameList.add(extraListener)
    }

    /**
     * 修改扩展
     */
    fun modifyExtra(media: MediaObject, segmentation: Boolean) {
        for (listener in mMediaFrameList) {
            if (listener.engine.media.id == media.id) {
                listener.engine.isSegmentation = segmentation
                media.setExtraDrawListener(listener)
                break
            }
        }
    }

    /**
     * 获取录制回调
     */
    fun getCameraExtraListener(face: OnEngineFaceListener): CameraExtraListener {
        return CameraExtraListener(face)
    }

    /**
     * 异步分割
     */
    fun asyncSegmentation(bitmap: Bitmap, mode: Int, listener: OnEngineSegmentationListener) {
        getMattingConfig(mode)?.let {
            initSegmentation(it)
            mSegmentationEngine?.asyncProcess(bitmap, listener)
        } ?: kotlin.run {
            listener.onFail(EngineError.ERROR_CODE_MODEL, EngineError.ERROR_MSG_MODEL)
        }
    }

    /**
     * 分割配置
     */
    fun getMattingConfig(mode: Int = SegmentationEngine.MODEL_SKY): BaseSegmentationConfig? {
        val modelAssetHelper = ModelAssetHelper()
        when (mode) {
            SegmentationEngine.MODEL_PORTRAIT -> {
                modelAssetHelper.checkModel(true)?.let {
                    return MattingSegmentationConfig(mode, it.localBin, it.localParam)
                }
            }
            SegmentationEngine.MODEL_SKY -> {
                modelAssetHelper.checkModel(false)?.let {
                    return MattingSegmentationConfig(mode, it.localBin, it.localParam)
                }
            }
            SegmentationEngine.MODE_USE_BITMAP_RESULT -> {
                return MLKitSegmentationConfig(mode)
            }
            SegmentationEngine.MODE_USE_RAW_BUFFER_RESULT -> {
                return MLKitSegmentationConfig(mode)
            }
        }
        return null
    }


    /**
     * 移出扩展
     */
    private fun removeExtraMedia(info: MediaObject) {
        for (listener in mMediaFrameList) {
            if (listener.engine.media.id == info.id) {
                //释放、移出
                listener.release()
                mMediaFrameList.remove(listener)
                break
            }
        }
    }

    /**
     * 初始化分割
     */
    private fun initSegmentation(config: BaseSegmentationConfig) {
        if (config is MattingSegmentationConfig) {
            mSegmentationEngine?.let { engine ->
                if (engine is MattingSegmentation) {
                    if (config.isSameConfig(engine.config)) {
                        return
                    }
                }
                mSegmentationEngine?.release()
            }
            mSegmentationEngine = MattingSegmentation(config)
            mSegmentationEngine?.create()
        } else if (config is MLKitSegmentationConfig) {
            mSegmentationEngine?.let { engine ->
                if (engine is MLKitSegmentation) {
                    if (config.isSameConfig(engine.config)) {
                        return
                    }
                }
                mSegmentationEngine?.release()
            }
            mSegmentationEngine = MLKitSegmentation(config)
            mSegmentationEngine?.create()
        }
    }

}