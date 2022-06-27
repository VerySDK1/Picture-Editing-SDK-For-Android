package com.vesdk.camera.entry

import android.os.Parcelable
import android.view.Gravity
import kotlinx.android.parcel.Parcelize

/**
 * sdk配置类
 */
@Parcelize
class CameraConfig() : Parcelable {

    private constructor(builder: Builder) : this() {
        this.baseUrl = builder.baseUrl

        this.firstShow = builder.firstShow

        this.cameraSupport = builder.cameraSupport

        this.limitMinTime = builder.limitMinTime
        this.limitMaxTime = builder.limitMaxTime

        this.multiShoot = builder.multiShoot
        this.insertGallery = builder.insertGallery
        this.artists = builder.artists
        this.relativePath = builder.relativePath
        this.mergeMedia = builder.mergeMedia

        this.hideBeauty = builder.hideBeauty
        this.hideFilter = builder.hideFilter
        this.hideMusic = builder.hideMusic
        this.hideSpeed = builder.hideSpeed

        this.enabledWatermark = builder.enabledWatermark
        this.watermarkPath = builder.watermarkPath
        this.watermarkGravity = builder.watermarkGravity
        this.watermarkAdjX = builder.watermarkAdjX
        this.watermarkAdjY = builder.watermarkAdjY

        this.keyFrameTime = builder.keyFrameTime
        this.isFaceFront = builder.isFaceFront
    }

    companion object {

        /**
         * 默认时间
         */
        const val DEFAULT_TIME = 0f

        /**
         * 拍照导入
         */
        const val CAMERA_SUPPORT_ALL = 0
        const val CAMERA_SUPPORT_PHOTO = 1
        const val CAMERA_SUPPORT_RECORDER = 2

        /**
         * 初始录制：视频 图片
         */
        const val FIRST_VIDEO = 0
        const val FIRST_IMAGE = 1

        /**
         * 录制关键帧:全关键帧、默认
         */
        const val KEY_FRAME_TIME_ALL = 0
        const val KEY_FRAME_TIME_DEFAULT = 1

    }

    /**
     * 基础
     */
    var baseUrl: String = ""

    /**
     * 初始默认显示
     */
    var firstShow = FIRST_VIDEO

    /**
     * 全部显示、拍照、录像
     */
    var cameraSupport = CAMERA_SUPPORT_ALL

    /**
     * 时间限制
     */
    var limitMinTime = DEFAULT_TIME
    var limitMaxTime = DEFAULT_TIME

    /**
     * 多次拍照
     */
    var multiShoot = false

    /**
     * 插入相册
     */
    var insertGallery = true
    var artists: String? = null
    var relativePath: String? = null

    /**
     * 合并视频
     */
    var mergeMedia = true

    /**
     * 隐藏美颜
     */
    var hideBeauty = false

    /**
     * 隐藏滤镜
     */
    var hideFilter = false

    /**
     * 隐藏音乐
     */
    var hideMusic = false

    /**
     * 隐藏变速
     */
    var hideSpeed = false

    /**
     * 开启水印
     */
    var enabledWatermark = true

    /**
     * 水印路径、位置、距离边框的绝对像素
     */
    var watermarkPath = ""
    var watermarkGravity = Gravity.START or Gravity.TOP
    var watermarkAdjX = 0
    var watermarkAdjY = 0

    /**
     * 视频录制关键帧间隔时间(秒),设置为0代表全关键帧,默认1
     */
    var keyFrameTime = KEY_FRAME_TIME_DEFAULT


    /**
     * 初始前置
     */
    var isFaceFront = true


    class Builder {
        /**
         * 基础
         */
        var baseUrl: String = ""

        /**
         * 初始默认显示
         */
        var firstShow = FIRST_VIDEO

        /**
         * 全部显示、拍照、录像
         */
        var cameraSupport = CAMERA_SUPPORT_ALL

        /**
         * 时间限制
         */
        var limitMinTime = DEFAULT_TIME
        var limitMaxTime = DEFAULT_TIME

        /**
         * 多次拍照
         */
        var multiShoot = false

        /**
         * 插入相册
         */
        var insertGallery = true
        var artists: String? = null
        var relativePath: String? = null

        /**
         * 合并视频
         */
        var mergeMedia = true

        /**
         * 隐藏美颜
         */
        var hideBeauty = false

        /**
         * 隐藏滤镜
         */
        var hideFilter = false

        /**
         * 隐藏音乐
         */
        var hideMusic = false

        /**
         * 隐藏变速
         */
        var hideSpeed = false

        /**
         * 开启水印
         */
        var enabledWatermark = true

        /**
         * 水印路径、位置、距离边框的绝对像素
         */
        var watermarkPath = ""
        var watermarkGravity = Gravity.START or Gravity.TOP
        var watermarkAdjX = 0
        var watermarkAdjY = 0

        /**
         * 视频录制关键帧间隔时间(秒),设置为0代表全关键帧,默认1
         */
        var keyFrameTime = KEY_FRAME_TIME_DEFAULT


        /**
         * 初始前置
         */
        var isFaceFront = true


        /**
         * 前置
         */
        fun setFaceFront(front: Boolean): Builder {
            isFaceFront = front
            return this
        }


        /**
         * 帧率
         */
        fun setFrameRate(frame: Int): Builder {
            keyFrameTime = frame
            return this
        }

        /**
         * 录制关键帧
         */
        fun setKeyFrameTime(key: Int): Builder {
            keyFrameTime = key
            return this
        }

        /**
         * 水印位置
         */
        fun setWatermark(
                path: String,
                gravity: Int = Gravity.START or Gravity.TOP,
                adjX: Int = 0,
                adjY: Int = 0
        ): Builder {
            watermarkPath = path
            watermarkGravity = gravity
            watermarkAdjX = adjX
            watermarkAdjY = adjY
            return this
        }

        /**
         * 开启水印
         */
        fun enabledWatermark(enabledWatermark: Boolean): Builder {
            this.enabledWatermark = enabledWatermark
            return this
        }

        /**
         * 隐藏变速
         */
        fun setHideSpeed(hideSpeed: Boolean): Builder {
            this.hideSpeed = hideSpeed
            return this
        }

        /**
         * 隐藏音乐
         */
        fun setHideMusic(hideMusic: Boolean): Builder {
            this.hideMusic = hideMusic
            return this
        }

        /**
         * 隐藏滤镜
         */
        fun setHideFilter(hideFilter: Boolean): Builder {
            this.hideFilter = hideFilter
            return this
        }

        /**
         * 隐藏美颜
         */
        fun setHideBeauty(hideBeauty: Boolean): Builder {
            this.hideBeauty = hideBeauty
            return this
        }

        /**
         * 合并视频
         */
        fun setMergeMedia(mergeMedia: Boolean): Builder {
            this.mergeMedia = mergeMedia
            return this
        }

        /**
         * 多次拍照
         */
        fun setMultiSHoot(multiShoot: Boolean): Builder {
            this.multiShoot = multiShoot
            return this
        }

        /**
         * 插入相册
         */
        fun setInsertGallery(insertGallery: Boolean, artists: String, relativePath: String): Builder {
            this.insertGallery = insertGallery
            this.artists = artists
            this.relativePath = relativePath
            return this
        }

        /**
         * 时间限制
         */
        fun setLimitTime(min: Float, max: Float): Builder {
            limitMinTime = min
            limitMaxTime = max
            return this
        }

        /**
         * 初始默认显示
         */
        fun setFirst(first: Int): Builder {
            firstShow = first
            return this
        }

        /**
         * 全部显示、拍照、录像
         */
        fun setCameraSupport(support: Int): Builder {
            cameraSupport = support
            return this
        }

        /**
         * 网络地址
         */
        fun setBaseUrl(url: String): Builder {
            baseUrl = url
            return this
        }

        fun get(): CameraConfig {
            return CameraConfig(this)
        }

    }

}