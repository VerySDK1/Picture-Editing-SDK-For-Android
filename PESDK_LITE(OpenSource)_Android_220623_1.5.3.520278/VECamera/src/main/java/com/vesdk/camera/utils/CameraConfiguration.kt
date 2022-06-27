package com.vesdk.camera.utils

import androidx.annotation.IntDef
import com.vecore.BaseVirtual
import com.vecore.base.lib.utils.CoreUtils
import com.vesdk.common.utils.MMKVUtils
import kotlin.math.max

/**
 * 应用持久化配置
 */
object CameraConfiguration {

    /**
     * 全屏 ...
     */
    const val RECORD_ASP_0 = 0
    const val RECORD_ASP_1 = 1
    const val RECORD_ASP_34 = 2
    const val RECORD_ASP_43 = 3
    const val RECORD_ASP_916 = 4
    const val RECORD_ASP_169 = 5

    /**
     * 键值
     */
    private const val RECORDER_BITRATE = "recorder_bitrate"
    private const val RECORDER_MIC_FACTOR = "recorder_mic_factor"
    private const val RECORDER_SIZE = "recorder_size"

    /**
     * 保存录制配置
     */
    @JvmStatic
    fun saveRecorderConfig(bitrate: Int, sizeMode: Int, volume: Int) {
        MMKVUtils.encode(RECORDER_BITRATE, bitrate)
        MMKVUtils.encode(RECORDER_SIZE, sizeMode)
        MMKVUtils.encode(RECORDER_MIC_FACTOR, volume)
    }

    /**
     * 录制输出码率
     */
    @JvmStatic
    val recorderBitrate: Int
        get() = max(400, MMKVUtils.decodeInt(RECORDER_BITRATE, 1800))

    /**
     * 麦克风音量
     */
    @JvmStatic
    val recordMicFactor: Int
        get() = max(0, MMKVUtils.decodeInt(RECORDER_MIC_FACTOR, 100))

    /**
     * 预览size
     */
    @JvmStatic
    val recorderSizeMode: Int
        get() = MMKVUtils.decodeInt(RECORDER_SIZE, 2)

    /**
     * 录制大小
     */
    fun getRecorderSize(@RecordAsp type: Int = RECORD_ASP_916): BaseVirtual.Size {
        val mode = recorderSizeMode
        val size = BaseVirtual.Size(0, 0)
        val displayMetrics = CoreUtils.getMetrics()
        val displayAsp = displayMetrics.widthPixels / (displayMetrics.heightPixels + 0.0f)
        when (mode) {
            0 -> {
                when (type) {
                    RECORD_ASP_1 -> {
                        size[360] = 360
                    }
                    RECORD_ASP_34 -> {
                        size[360] = 480
                    }
                    RECORD_ASP_43 -> {
                        size[480] = 360
                    }
                    RECORD_ASP_169 -> {
                        size[640] = 360
                    }
                    RECORD_ASP_0 -> {
                        size[360] = (360 / displayAsp).toInt()
                    }
                    else -> {
                        size[360] = 640
                    }
                }
            }
            1 -> {
                when (type) {
                    RECORD_ASP_1 -> {
                        size[480] = 480
                    }
                    RECORD_ASP_34 -> {
                        size[480] = 640
                    }
                    RECORD_ASP_43 -> {
                        size[640] = 480
                    }
                    RECORD_ASP_169 -> {
                        size[(480 * (16 / 9f)).toInt()] = 480
                    }
                    RECORD_ASP_0 -> {
                        size[480] = (480 / displayAsp).toInt()
                    }
                    else -> {
                        size[480] = 854
                    }
                }
            }
            3 -> {
                when (type) {
                    RECORD_ASP_1 -> {
                        size[1080] = 1080
                    }
                    RECORD_ASP_34 -> {
                        size[1080] = 1440
                    }
                    RECORD_ASP_43 -> {
                        size[1440] = 1080
                    }
                    RECORD_ASP_169 -> {
                        size[1920] = 1080
                    }
                    RECORD_ASP_0 -> {
                        size[1080] = (1080 / displayAsp).toInt()
                    }
                    else -> {
                        size[1080] = 1920
                    }
                }
            }
            else -> {
                when (type) {
                    RECORD_ASP_1 -> {
                        size[720] = 720
                    }
                    RECORD_ASP_34 -> {
                        size[720] = 960
                    }
                    RECORD_ASP_43 -> {
                        size[960] = 720
                    }
                    RECORD_ASP_169 -> {
                        size[1280] = 720
                    }
                    RECORD_ASP_0 -> {
                        size[720] = (720 / displayAsp).toInt()
                    }
                    else -> {
                        size[720] = 1280
                    }
                }
            }
        }
        return size
    }


    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        RECORD_ASP_0,
        RECORD_ASP_1,
        RECORD_ASP_34,
        RECORD_ASP_43,
        RECORD_ASP_916,
        RECORD_ASP_169
    )
    internal annotation class RecordAsp
}