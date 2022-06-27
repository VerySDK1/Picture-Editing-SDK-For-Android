package com.pesdk.album.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlbumConfig() : Parcelable {

    private constructor(builder: Builder) : this() {
        this.baseUrl = builder.baseUrl

        this.albumSupport = builder.albumSupport

        this.hideText = builder.hideText
        this.hideCamera = builder.hideCamera
        this.hideEdit = builder.hideEdit
        this.hideMaterial = builder.hideMaterial
        this.hideCameraSwitch = builder.hideCameraSwitch

        this.limitMinTime = builder.limitMinTime

        this.limitMaxNum = builder.limitMaxNum
        this.limitMinNum = builder.limitMinNum

        this.limitMediaNum = builder.limitMediaNum
        this.limitVideoNum = builder.limitVideoNum
        this.limitImageNum = builder.limitImageNum

        this.firstShow = builder.firstShow
    }

    companion object {
        /**
         * 默认设置
         */
        const val DEFAULT_VALUE = 0

        /**
         * 默认时间
         */
        const val DEFAULT_TIME = 0f

        /**
         * 相册支持格式：默认，同时支持图片和视频
         */
        const val ALBUM_SUPPORT_DEFAULT = 0

        /**
         * 相册支持格式：仅支持视频
         */
        const val ALBUM_SUPPORT_VIDEO_ONLY = 1

        /**
         * 相册支持格式：仅支持图片
         */
        const val ALBUM_SUPPORT_IMAGE_ONLY = 2

        /**
         * 初始显示：默认 全部
         */
        const val FIRST_ALL = 0

        /**
         * 初始显示：视频
         */
        const val FIRST_VIDEO = 1

        /**
         * 初始显示：图片
         */
        const val FIRST_IMAGE = 2

        /**
         * 素材库
         */
        const val FIRST_MATERIAL = 3

    }

    /**
     * 基础
     */
    var baseUrl: String = ""

    /**
     * 相册界面
     */
    var albumSupport = ALBUM_SUPPORT_DEFAULT

    /**
     * 显示文字版
     */
    var hideText = false

    /**
     * 相机
     */
    var hideCamera = false

    /**
     * 隐藏预览
     */
    var hideEdit = false

    /**
     * 隐藏素材库
     */
    var hideMaterial = false

    /**
     * 拍照、录制切换隐藏
     */
    var hideCameraSwitch = false

    /**
     * 最小时间限制
     */
    var limitMinTime = DEFAULT_TIME


    /**
     * 媒体数量 DEFAULT_VALUE不限制
     */
    var limitMaxNum = DEFAULT_VALUE
    var limitMinNum = DEFAULT_VALUE

    /**
     * 指定媒体数量
     */
    var limitMediaNum = DEFAULT_VALUE
    var limitVideoNum = DEFAULT_VALUE
    var limitImageNum = DEFAULT_VALUE

    /**
     * 初始显示
     */
    var firstShow = FIRST_ALL


    class Builder {
        /**
         * 基础地址
         */
        var baseUrl: String = ""

        /**
         * 相册界面类型
         */
        var albumSupport = ALBUM_SUPPORT_DEFAULT

        /**
         * 显示文字版
         */
        var hideText = false

        /**
         * 相机
         */
        var hideCamera = false

        /**
         * 隐藏 编辑(预览)
         */
        var hideEdit = false

        /**
         * 隐藏素材库
         */
        var hideMaterial = false

        /**
         * 拍照、录制切换隐藏
         */
        var hideCameraSwitch = false

        /**
         * 最小时间限制 s
         */
        var limitMinTime = DEFAULT_TIME

        /**
         * 媒体数量 DEFAULT_VALUE不限制
         */
        var limitMaxNum = DEFAULT_VALUE
        var limitMinNum = DEFAULT_VALUE

        /**
         * 指定媒体数量
         */
        var limitMediaNum = DEFAULT_VALUE
        var limitVideoNum = DEFAULT_VALUE
        var limitImageNum = DEFAULT_VALUE

        /**
         * 初始显示
         */
        var firstShow = FIRST_ALL


        /**
         * 初始显示
         */
        fun setFirstShow(first: Int): Builder {
            firstShow = first
            return this
        }

        /**
         * 选择数量限制 setLimitNum二选一
         */
        fun setLimitMedia(media: Int, video: Int, image: Int): Builder {
            limitMediaNum = 0.coerceAtLeast(media)
            limitVideoNum = 0.coerceAtLeast(video)
            limitImageNum = 0.coerceAtLeast(image)
            limitMaxNum = DEFAULT_VALUE
            limitMinNum = DEFAULT_VALUE
            return this
        }

        /**
         * 选择数量限制
         */
        fun setLimitNum(min: Int, max: Int): Builder {
            limitMaxNum = 0.coerceAtLeast(min.coerceAtLeast(max))
            limitMinNum = 0.coerceAtLeast(max.coerceAtMost(min))
            limitMediaNum = DEFAULT_VALUE
            limitVideoNum = DEFAULT_VALUE
            limitImageNum = DEFAULT_VALUE
            return this
        }

        /**
         * 最小时间限制
         */
        fun setLimitMinTime(time: Float): Builder {
            limitMinTime = time
            return this
        }

        /**
         * 隐藏拍照、录制切换
         */
        fun setHideCameraSwitch(hide: Boolean): Builder {
            hideCameraSwitch = hide
            return this
        }

        /**
         * 隐藏素材库
         */
        fun setHideMaterial(hide: Boolean): Builder {
            hideMaterial = hide
            return this
        }

        /**
         * 隐藏预览
         */
        fun setHideEdit(hide: Boolean): Builder {
            hideEdit = hide
            return this
        }

        /**
         * 隐藏相机
         */
        fun setHideCamera(hide: Boolean): Builder {
            hideCamera = hide
            return this
        }

        /**
         * 隐藏文字版
         */
        fun setHideText(hide: Boolean): Builder {
            hideText = hide
            return this
        }

        /**
         * 相册类型
         */
        fun setAlbumSupport(support: Int): Builder {
            albumSupport = support
            return this
        }

        /**
         * 网络地址
         */
        fun setBaseUrl(url: String): Builder {
            baseUrl = url
            return this
        }

        fun get(): AlbumConfig {
            return AlbumConfig(this)
        }

    }

}