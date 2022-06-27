package com.vesdk.camera.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.vecore.models.VisualFilterConfig
import com.vesdk.camera.R
import com.vesdk.camera.bean.IMediaParamImp
import com.vesdk.camera.viewmodel.ToneViewModel
import com.vesdk.common.base.BaseFragment
import kotlinx.android.synthetic.main.camera_fragment_tone_layout.*
import java.text.DecimalFormat

/**
 *调色
 */
class ToneFragment : BaseFragment(), View.OnClickListener {

    val TAG = "ToneFragment"

    companion object {
        @JvmStatic
        fun newInstance() = ToneFragment()
    }

    private val mToneViewModel by lazy { ViewModelProvider(requireActivity()).get(ToneViewModel::class.java) }

    private val brightness = 0
    private val contrast = 1
    private val saturation = 2
    private val sharpen = 3
    private val vignette = 5
    private val temperature = 6
    private val tone = 7
    private val highlight = 8
    private val shadow = 9
    private val lightSensation = 10
    private val fade = 11
    private val graininess = 12
    private val reset = 100
    private var mStatus = reset //当前选中的项目


    //双击恢复
    private val TIMEOUT = 400 //双击间四百毫秒延时

    private var mLastTime: Long = 0


    override fun init() {
        btn_brightness.setOnClickListener(this)
        btn_contrast.setOnClickListener(this)
        btn_hightlight.setOnClickListener(this)
        btn_guanggan.setOnClickListener(this)
        btn_shadow.setOnClickListener(this)
        btn_hese.setOnClickListener(this)
        btn_saturation.setOnClickListener(this)
        btn_temperature.setOnClickListener(this)
        btn_tone.setOnClickListener(this)
        btn_keli.setOnClickListener(this)
        btn_sharpen.setOnClickListener(this)
        btn_vignette.setOnClickListener(this)

        context?.let {
            mRange.setBGColor(ContextCompat.getColor(it, R.color.transparent_white50))
            mRange.setProgressColor(Color.WHITE)
        }
        mRange.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var textid = R.string.pesdk_filter_brightness
            var value = 0f
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    value = 1.0f * progress / seekBar.max
                    textid = setFeaturesValue(value)
                    onConfigChange()
                }
                tv_progress!!.text = getText(textid).toString() + " " + decimalFormat.format(value.toDouble())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (mStatus == reset || mStatus == brightness) { //首次拖动,啥都未选中时
                    mStatus = brightness
                    textid = R.string.pesdk_filter_brightness
                    btn_brightness.setChecked(true)
                }
                mRange.setChangedByHand(true)
                textid = setFeaturesValue(seekBar.progress * 1.0f / seekBar.max)
                tv_progress!!.visibility = View.VISIBLE
                tv_progress.text = getText(textid).toString() + " " + decimalFormat.format(value.toDouble())
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                value = 1.0f * seekBar.progress / seekBar.max
                textid = setFeaturesValue(value)
                tv_progress!!.visibility = View.INVISIBLE
            }
        })


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mToneViewModel.getToneResetLiveData().observe(viewLifecycleOwner) {
            initValue()
        }
    }

    private val decimalFormat = DecimalFormat("0.00") //构造方法的字符格式这里如果小数不足2位,会以0补足.


    //返回当前选中的项目 返回当前的名字
    private fun setFeaturesValue(value: Float): Int {
        return when (mStatus) {
            brightness -> {
                mToneViewModel.mBrightness = 2 * value - 1.0f
                R.string.pesdk_filter_brightness
            }
            contrast -> {
                mToneViewModel.mContrast = value * 4
                R.string.pesdk_filter_contrast
            }
            saturation -> {
                mToneViewModel.mSaturation = value * 2
                R.string.pesdk_filter_saturation
            }
            sharpen -> {
                mToneViewModel.mSharpen = value
                R.string.pesdk_filter_sharpness
            }
            temperature -> {
                mToneViewModel.mTemperature = value * 2 - 1.0f
                R.string.pesdk_filter_temperature
            }
            tone -> {
                mToneViewModel.mTint = value * 2 - 1.0f
                R.string.pesdk_filter_tone
            }
            vignette -> {
                fixVignette(value)
                R.string.pesdk_filter_vignette
            }
            highlight -> {
                mToneViewModel.mHighlight = value
                R.string.pesdk_filter_highlight
            }
            shadow -> {
                mToneViewModel.mShadow = value
                R.string.pesdk_filter_shadow
            }
            graininess -> {
                mToneViewModel.mGraininess = value
                R.string.pesdk_filter_keli
            }
            lightSensation -> {
                mToneViewModel.mLightSensation = value * 2 - 1.0f
                R.string.pesdk_filter_guanggan
            }
            fade -> {
                mToneViewModel.mFade = value
                R.string.pesdk_filter_hese
            }
            else -> {
                R.string.pesdk_filter_unknow
            }
        }
    }

    //暗角
    private fun fixVignette(value: Float) {
        mToneViewModel.mVignette = value
        mToneViewModel.vignettId = if (value > 0 && value <= 1) {
            VisualFilterConfig.FILTER_ID_VIGNETTE
        } else {
            IMediaParamImp.NO_VIGNETTEDID
        }
    }

    override fun onClick(v: View) {
        onClickImp(v.id)
    }


    private fun initValue() {
        when (mStatus) {
            brightness -> {
                mStatus = reset
                onClickImp(btn_brightness)
                btn_brightness.isChecked = true
            }
            contrast -> {
                mStatus = reset
                onClickImp(btn_contrast)
                btn_contrast.isChecked = true
            }
            saturation -> {
                mStatus = reset
                onClickImp(btn_saturation)
                btn_saturation.isChecked = true
            }
            sharpen -> {
                mStatus = reset
                onClickImp(btn_sharpen)
                btn_sharpen.isChecked = true
            }
            temperature -> {
                mStatus = reset
                onClickImp(btn_temperature)
                btn_temperature.isChecked = true
            }
            tone -> {
                mStatus = reset
                onClickImp(btn_tone)
                btn_tone.isChecked = true
            }
            vignette -> {
                mStatus = reset
                onClickImp(btn_vignette)
                btn_vignette.isChecked = true
            }
            highlight -> {
                mStatus = reset
                onClickImp(btn_hightlight)
                btn_hightlight.isChecked = true
            }
            shadow -> {
                mStatus = reset
                onClickImp(btn_shadow)
                btn_shadow.isChecked = true
            }
            graininess -> {
                mStatus = reset
                onClick(btn_keli)
                btn_keli.isChecked = true
            }
            lightSensation -> {
                mStatus = reset
                onClick(btn_guanggan)
                btn_guanggan.isChecked = true
            }
            fade -> {
                mStatus = reset
                onClick(btn_hese)
                btn_hese.isChecked = true
            }
        }

    }

    private fun onClickImp(view: View) {
        onClickImp(view.id)
    }

    private fun onClickImp(viewId: Int) {
        if (viewId == R.id.btn_brightness) {
            //亮度
            if (mStatus == brightness) {
                if (mToneViewModel.mBrightness.isNaN() || mToneViewModel.mBrightness == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mBrightness = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_brightness))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = brightness
            mRange.setDefaultValue(50)
            var fv = mRange.max * (mToneViewModel.mBrightness - (-1.0f)) / 2.0f
            if (fv.isNaN()) {
                fv = mRange.max / 2.0f
                mRange.setChangedByHand(false)
            } else {
                mRange.setChangedByHand(true)
            }
            mRange.progress = fv.toInt()
        } else if (viewId == R.id.btn_contrast) {
            //对比度
            if (mStatus == contrast) {
                if (mToneViewModel.mContrast.isNaN() || mToneViewModel.mContrast.toDouble() == 1.0) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mContrast = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_contrast))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = contrast
            mRange.setDefaultValue(25)
            if (mToneViewModel.mContrast.isNaN()) {
                //    VisualFilterConfig中对比度默认为1.0f(区间：0~4.0f)
                mRange.setChangedByHand(false)
                mRange.progress = mRange.max / 4
            } else {
                mRange.setChangedByHand(true)
                mRange.progress = (mRange.max * mToneViewModel.mContrast / 4).toInt()
            }
        } else if (viewId == R.id.btn_saturation) {
            //饱和度
            if (mStatus == saturation) {
                if (mToneViewModel.mSaturation.isNaN() || mToneViewModel.mSaturation.toDouble() == 1.0) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mSaturation = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_saturation))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = saturation
            mRange.setDefaultValue(50)
            if (mToneViewModel.mSaturation.isNaN()) {
                //    VisualFilterConfig中饱和度默认为1.0f(区间：0~2.0f)
                mRange.setChangedByHand(false)
                mRange.progress = (mRange.max * 1 / 2).toInt()
            } else {
                mRange.setChangedByHand(true)
                mRange.progress = (mRange.max * mToneViewModel.mSaturation / 2).toInt()
            }
        } else if (viewId == R.id.btn_sharpen) {
            //锐化
            if (mStatus == sharpen) {
                if (mToneViewModel.mSharpen.isNaN() || mToneViewModel.mSharpen == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mSharpen = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_sharpness))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = sharpen
            mRange.setDefaultValue(0)
            if (mToneViewModel.mSharpen.isNaN()) {
                mRange.setChangedByHand(false)
            } else {
                mRange.setChangedByHand(true)
            }
            mRange.progress = (mRange.max * mToneViewModel.mSharpen).toInt()
        } else if (viewId == R.id.btn_temperature) {  //色温
            if (mStatus == temperature) {
                if (mToneViewModel.mTemperature.isNaN() || mToneViewModel.mTemperature == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mTemperature = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_temperature))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = temperature
            mRange.setDefaultValue(50)
            var fv: Float = mRange.max * (mToneViewModel.mTemperature - -1.0f) / 2.0f
            if (fv.isNaN()) {
                fv = mRange.max / 2.0f
                mRange.setChangedByHand(false)
            } else {
                mRange.setChangedByHand(true)
            }
            mRange.progress = fv.toInt()
        } else if (viewId == R.id.btn_vignette) { //暗角
            if (mStatus == vignette) {
                if (mToneViewModel.mVignette.isNaN() || mToneViewModel.mVignette == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.vignettId = IMediaParamImp.NO_VIGNETTEDID
                    mToneViewModel.mVignette = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_vignette))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = vignette
            mRange.setDefaultValue(0)
            if (IMediaParamImp.NO_VIGNETTEDID != mToneViewModel.vignettId) {
                mRange.setChangedByHand(true)
            } else {
                mRange.setChangedByHand(false)
            }
            mRange.progress = (mRange.max * mToneViewModel.mVignette).toInt()
        } else if (viewId == R.id.btn_shadow) { //阴影
            if (mStatus == shadow) {
                if (mToneViewModel.mShadow.isNaN() || mToneViewModel.mShadow == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mShadow = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_shadow))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = shadow
            mRange.setDefaultValue(0)
            mRange.setChangedByHand(!mToneViewModel.mShadow.isNaN())
            mRange.progress = (mRange.max * mToneViewModel.mShadow).toInt()
        } else if (viewId == R.id.btn_hightlight) {  //高光
            if (mStatus == highlight) {
                if (mToneViewModel.mHighlight.isNaN() || mToneViewModel.mHighlight == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mHighlight = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_highlight))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = highlight
            mRange.setDefaultValue(0)
            mRange.setChangedByHand(!mToneViewModel.mHighlight.isNaN())
            mRange.progress = (mRange.max * mToneViewModel.mHighlight).toInt()
        } else if (viewId == R.id.btn_guanggan) {  //光感
            if (mStatus == lightSensation) {
                if (mToneViewModel.mLightSensation.isNaN() || mToneViewModel.mLightSensation == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mLightSensation = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_guanggan))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = lightSensation
            mRange.setDefaultValue(50)
            var fv: Float = mRange.max * (mToneViewModel.mLightSensation - -1.0f) / 2.0f
            if (fv.isNaN()) {
                fv = mRange.max / 2.0f
                mRange.setChangedByHand(false)
            } else {
                mRange.setChangedByHand(true)
            }
            mRange.progress = fv.toInt()
        } else if (viewId == R.id.btn_hese) {  //褐色
            if (mStatus == fade) {
                if (mToneViewModel.mFade.isNaN() || mToneViewModel.mFade == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mFade = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_hese))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = fade
            mRange.setDefaultValue(0)
            mRange.setChangedByHand(!mToneViewModel.mFade.isNaN())
            mRange.progress = (mRange.max * mToneViewModel.mFade).toInt()
        } else if (viewId == R.id.btn_keli) {
            if (mStatus == graininess) {
                if (mToneViewModel.mGraininess.isNaN() || mToneViewModel.mGraininess == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mGraininess = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_keli))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = graininess
            mRange.setDefaultValue(0)
            mRange.setChangedByHand(!mToneViewModel.mGraininess.isNaN())
            mRange.progress = (mRange.max * mToneViewModel.mGraininess).toInt()
        } else if (viewId == R.id.btn_tone) {  //色调
            if (mStatus == tone) {
                if (mToneViewModel.mTint.isNaN() || mToneViewModel.mTint == 0f) {
                    return
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mToneViewModel.mTint = Float.NaN
                    onConfigChange()
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_tone))
                } else {
                    mLastTime = System.currentTimeMillis()
                    return
                }
            }
            mLastTime = System.currentTimeMillis()
            mStatus = tone
            mRange.setDefaultValue(50)
            var fv: Float = mRange.max * (mToneViewModel.mTint - -1.0f) / 2.0f
            if (fv.isNaN()) {
                fv = mRange.max / 2.0f
                mRange.setChangedByHand(false)
            } else {
                mRange.setChangedByHand(true)
            }
            mRange.progress = fv.toInt()
        }
    }

    /**
     * 新的调色参数
     */
    private fun onConfigChange() {
        mToneViewModel.updateAdjustConfig()
    }


    /**
     * 保存当前编辑的参数
     */
    override fun getLayoutId(): Int {
        return R.layout.camera_fragment_tone_layout
    }
}