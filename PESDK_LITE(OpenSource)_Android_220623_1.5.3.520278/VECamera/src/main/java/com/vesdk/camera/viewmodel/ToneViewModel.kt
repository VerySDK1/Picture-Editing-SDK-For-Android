package com.vesdk.camera.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vecore.models.VisualFilterConfig
import com.vesdk.camera.bean.IMediaParamImp
import java.util.*

/**
 *调色
 */
class ToneViewModel : ViewModel() {

    var mBrightness = Float.NaN
    var mContrast = Float.NaN
    var mSaturation = Float.NaN
    var mSharpen = Float.NaN
    var mWhite = Float.NaN
    var mVignette = Float.NaN
    var mTint = Float.NaN
    var mHighlight = Float.NaN
    var mLightSensation = Float.NaN
    var mFade = Float.NaN
    var mGraininess = Float.NaN
    var mShadow = Float.NaN

    var vignettId: Int = IMediaParamImp.NO_VIGNETTEDID
    var mTemperature = Float.NaN


    fun resetParam() {
        //全部重置
        mBrightness = Float.NaN
        mContrast = Float.NaN
        mSaturation = Float.NaN
        mSharpen = Float.NaN
        mVignette = Float.NaN
        mWhite = Float.NaN
        mTemperature = Float.NaN
        mTint = Float.NaN
        mHighlight = Float.NaN
        mShadow = Float.NaN
        mLightSensation = Float.NaN
        mFade = Float.NaN
        mGraininess = Float.NaN
        vignettId = IMediaParamImp.NO_VIGNETTEDID

        mLiveDataReset.postValue(null)
        mLiveDataResetState.postValue(false)

        apply()
    }


    /**
     * 虚拟图片调色
     */
    private fun getFilterList(mediaParamImp: IMediaParamImp): List<VisualFilterConfig> {
        val configs = ArrayList<VisualFilterConfig>()
        val defaultValue = Float.NaN
        var config = VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL)
        applyAdjustConfig(config, mediaParamImp)
        config.defaultValue = defaultValue
        configs.add(config)
        if (mediaParamImp.getVignetteId() != IMediaParamImp.NO_VIGNETTE_ID) {
            //暗角有效
            val vignetted = VisualFilterConfig(VisualFilterConfig.FILTER_ID_VIGNETTE)
            vignetted.setDefaultValue(mediaParamImp.getVignette())
            configs.add(vignetted)
        }
        return configs
    }


    private val mLiveDataTone = MutableLiveData<List<VisualFilterConfig>>()
    private val mLiveDataReset = MutableLiveData<Void>()
    private val mLiveDataResetState = MutableLiveData<Boolean>()

    fun getToneLiveData() = mLiveDataTone
    fun getToneResetLiveData() = mLiveDataReset
    fun getToneResetEnableLiveData() = mLiveDataResetState

    fun updateAdjustConfig() {
        mLiveDataResetState.postValue(true)
        apply()
    }

    private fun apply() {
        mLiveDataTone.postValue(getFilterList(save()))
    }

    /**
     * 保存当前编辑的参数
     */
    private fun save(): IMediaParamImp {
        var tmp = IMediaParamImp()
        tmp.setBrightness(mBrightness)
        tmp.setContrast(mContrast)
        tmp.setSaturation(mSaturation)
        tmp.setSharpen(mSharpen)
        tmp.setWhite(mWhite)
        tmp.setVignette(mVignette)
        tmp.setVignetteId(vignettId)
        tmp.setTemperature(mTemperature)
        tmp.setTintValue(mTint)
        tmp.setHighlightsValue(mHighlight)
        tmp.setShadowsValue(mShadow)
        tmp.setGraininess(mGraininess)
        tmp.setLightSensation(mLightSensation)
        tmp.setFade(mFade)
        return tmp
    }

    /**
     * 调色调色参数到基础滤镜
     *
     * @param config 基础滤镜
     * @param imp    调色参数
     * @return 带调色参数的滤镜
     */
    private fun applyAdjustConfig(config: VisualFilterConfig, imp: IMediaParamImp) {
        config.brightness = imp.getBrightness()
        config.contrast = imp.getContrast()
        config.saturation = imp.getSaturation()
        config.sharpen = imp.getSharpen()
        config.temperature = imp.getTemperature()
        config.tint = imp.getTintValue()
        config.highlights = imp.getHighlightsValue()
        config.shadows = imp.getShadowsValue()
        config.graininess = imp.getGraininess()
        config.lightSensation = imp.getLightSensation()
        config.fade = imp.getFade()
    }

}