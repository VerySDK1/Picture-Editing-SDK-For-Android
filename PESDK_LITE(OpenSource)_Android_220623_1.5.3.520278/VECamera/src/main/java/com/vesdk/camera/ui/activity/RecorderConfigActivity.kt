package com.vesdk.camera.ui.activity

import android.content.Context
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import com.vesdk.camera.R
import com.vesdk.camera.utils.CameraConfiguration
import com.vesdk.common.base.BaseActivity
import com.vesdk.common.helper.bindExtra
import kotlinx.android.synthetic.main.camera_activity_record_config.*
import kotlin.math.min

/**
 * 录制配置
 */
class RecorderConfigActivity : BaseActivity() {

    companion object {

        /**
         * 录制
         */
        private const val PARAM_RECORD = "_isRecord"

        @JvmStatic
        fun newInstance(context: Context, isRecord: Boolean): Intent {
            val intent = Intent(
                context,
                RecorderConfigActivity::class.java
            )
            intent.putExtra(PARAM_RECORD, isRecord)
            return intent
        }

        /**
         * 推荐0~100 过大会爆音
         */
        private const val MIN_MIC_FACTOR = 0
        private const val MAX_MIC_FACTOR = 500
        private const val MAX_FACTOR = MAX_MIC_FACTOR - MIN_MIC_FACTOR

        /**
         * 码率
         */
        private const val MIN_BITRATE_FACTOR = 400
        private const val MAX_BITRATE_FACTOR = 3000
        private const val MAX_BITRATE = MAX_BITRATE_FACTOR - MIN_BITRATE_FACTOR
    }

    /**
     * 录制
     */
    private val mIsRecord: Boolean by bindExtra(PARAM_RECORD, false)

    override fun init() {
        btnLeft.setOnClickListener { onBackPressed() }

        //录制
        recordParam.visibility = if (mIsRecord) VISIBLE else GONE

        //麦克风
        sbMicFactor.max = MAX_FACTOR
        sbMicFactor.setMinValue(MIN_MIC_FACTOR)
        sbMicFactor.progress = CameraConfiguration.recordMicFactor - MIN_MIC_FACTOR

        //码率
        sbBitrateBar.max = MAX_BITRATE
        sbBitrateBar.setMinValue(MIN_BITRATE_FACTOR)
        sbBitrateBar.progress = CameraConfiguration.recorderBitrate - MIN_BITRATE_FACTOR

        //录制大小
        when (CameraConfiguration.recorderSizeMode) {
            0 -> {
                rbSize360.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.camera_config_rb_p,
                    0
                )
                recordSizeRG.check(R.id.rbSize360)
            }
            1 -> {
                rbSize480.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.camera_config_rb_p,
                    0
                )
                recordSizeRG.check(R.id.rbSize480)
            }
            2 -> {
                rbSize720.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.camera_config_rb_p,
                    0
                )
                recordSizeRG.check(R.id.rbSize720)
            }
            else -> {
                rbSize1080.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.camera_config_rb_p,
                    0
                )
                recordSizeRG.check(R.id.rbSize1080)
            }
        }

        //选中
        recordSizeRG.setOnCheckedChangeListener { _, checkedId ->
            rbSize360.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            rbSize480.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            rbSize720.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            rbSize1080.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            when (checkedId) {
                R.id.rbSize360 -> {
                    sbBitrateBar.progress = 0
                    rbSize360.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.camera_config_rb_p,
                        0
                    )
                }
                R.id.rbSize480 -> {
                    sbBitrateBar.progress = 850 - MIN_BITRATE_FACTOR
                    rbSize480.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.camera_config_rb_p,
                        0
                    )
                }
                R.id.rbSize720 -> {
                    sbBitrateBar.progress = 1800 - MIN_BITRATE_FACTOR
                    rbSize720.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.camera_config_rb_p,
                        0
                    )
                }
                R.id.rbSize1080 -> {
                    sbBitrateBar.progress = MAX_BITRATE
                    rbSize1080.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.camera_config_rb_p,
                        0
                    )
                }
            }
        }
    }

    /**
     * 保存配置参数
     */
    private fun saveConfig() {
        //码率
        val bitRate = min(MAX_BITRATE_FACTOR, MIN_BITRATE_FACTOR + sbBitrateBar.progress)
        //音量
        val volume = min(MAX_MIC_FACTOR, MIN_MIC_FACTOR + sbMicFactor.progress)
        //分辨率
        val sizeMode = when (recordSizeRG.checkedRadioButtonId) {
            R.id.rbSize360 -> 0
            R.id.rbSize480 -> 1
            R.id.rbSize720 -> 2
            R.id.rbSize1080 -> 3
            else -> 3
        }

        //保存
        CameraConfiguration.saveRecorderConfig(bitRate, sizeMode, volume)
    }

    override fun onBackPressed() {
        saveConfig()
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    override fun getLayoutId(): Int {
        return R.layout.camera_activity_record_config
    }
}