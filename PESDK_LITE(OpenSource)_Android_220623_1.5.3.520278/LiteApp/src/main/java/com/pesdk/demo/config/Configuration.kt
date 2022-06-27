package com.pesdk.demo.config

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Environment
import android.view.Gravity
import com.pesdk.album.api.AlbumConfig
import com.pesdk.api.manager.ExportConfiguration
import com.pesdk.api.manager.UIConfiguration
import com.pesdk.demo.AppImp.Companion.SPACE_NAME
import com.pesdk.demo.utils.Utils
import com.vecore.models.Watermark
import com.vesdk.camera.entry.CameraConfig
import java.io.File

/**
 * 配置
 */
class Configuration {

    companion object {

        /**
         * 素材服务器地址
         * api/v1/ ==> type/list 、 file/list
         */
        private const val BASE_URL = "http://pesystem.56show.com/"

        /**
         * 云草稿上传地址
         * api/v1/backup/ ==> create/update/delete/list
         */
        private const val BASE_UPLOAD_URL = "http://pesystem.56show.com/"

        /**
         * 用户uid
         */
        private const val UUID = "PESdkDemo"

        /**
         * 艺术家
         */
        private const val artist = "PESdkDemo"

        /**
         * 输出图片的最小边
         */
        private const val MIN_SIDE = 960

        /**
         * 测试用水印图片
         */
        private const val EDIT_WATERMARK_PATH: String = "asset://watermark.png"

    }

    fun getRelativePath(): String {
        return Environment.DIRECTORY_DCIM + "/" + SPACE_NAME
    }

    /**
     * 图片编辑导出
     */
    @SuppressLint("RtlHardcoded")
    fun initExportConfiguration(): ExportConfiguration {
        val exportConfiguration = ExportConfiguration.Builder()
            // 设置保存路径  //此参数仅在29 以下的设备有效
            .setSavePath(
                File(
                    Environment.getExternalStorageDirectory(),
                    getRelativePath()
                ).absolutePath
            )
            // https://www.jianshu.com/p/b3595fc1f9be
            .saveToAlbum(true) //导出后是否由uisdk层保存到相册  false 时且29+， 文件输出到 Android/data/***/**.jpg
            // //saveToAlbum(true)时有效， 文件输出到DCIM/pe.     适配target android 29+
            .setRelativePath(artist, getRelativePath())
            .setMinSide(MIN_SIDE)

        exportConfiguration.setWatermarkGravity(Gravity.RIGHT or Gravity.BOTTOM) //水印位置
        exportConfiguration.setWatermarkShowMode(Watermark.MODE_DEFAULT) //水印拉伸模式

        exportConfiguration.useCustomExportGuide(true)// 推荐：此方式可自定义修改导出前的弹窗提示

        if (1 == 1) {  //水印：方式1
            exportConfiguration.setWatermarkPath(EDIT_WATERMARK_PATH) //水印
                .setAdj(5, 5) //距离
                .setWatermarkShowMode(Watermark.MODE_DEFAULT)// 设置水印显示模式
        } else {  //水印：方式2  (文字水印)
            // 设置是否使用文字水印（使用文字水印，将不再显示图片水印）
            exportConfiguration.enableTextWatermark(true)
                // 设置文字水印内容（开启文字水印才生效）
                .setTextWatermarkContent(Utils.getDate(System.currentTimeMillis())) //水印内容
                // 设置文字水印大小（开启文字水印才生效）
                .setTextWatermarkSize(10)
                // 设置文字水印颜色（开启文字水印才生效）
                .setTextWatermarkColor(Color.WHITE)
                // 设置文字水印阴影颜色（开启文字水印才生效）
                .setTextWatermarkShadowColor(Color.BLACK)
        }
        return exportConfiguration.get()
    }

    /**
     * 图片编辑UI
     */
    fun initUIConfiguration(): UIConfiguration {
        return UIConfiguration.Builder()
            .setBaseUrl(BASE_URL)
            .get()
    }

    /**
     * 相册
     */
    fun initAlbumConfig(): AlbumConfig {
        return AlbumConfig.Builder()
            //地址 素材等网络资源
            .setBaseUrl("http://d.56show.com/")
            //界面 ALBUM_SUPPORT_DEFAULT / ALBUM_SUPPORT_VIDEO_ONLY / ALBUM_SUPPORT_IMAGE_ONLY
            .setAlbumSupport(AlbumConfig.ALBUM_SUPPORT_IMAGE_ONLY)
            //文字版
            .setHideText(true)
            //隐藏相机
            .setHideCamera(false)
            //隐藏预览
            .setHideEdit(false)
            //隐藏素材库
            .setHideMaterial(true)
            //隐藏拍照、录制切换
            .setHideCameraSwitch(true)
            //最小时间
            .setLimitMinTime(AlbumConfig.DEFAULT_TIME)
            //数量
            .setLimitNum(1, 1)
            //具体指定数量 setLimitNum二选一
            //.setLimitMedia(3, 2, 2)
            //首次显示 FIRST_ALL / FIRST_VIDEO / FIRST_IMAGE / FIRST_MATERIAL
            .setFirstShow(AlbumConfig.FIRST_ALL)
            .get()
    }


    /**
     * 相机
     */
    fun initCameraConfig(): CameraConfig {
        return CameraConfig.Builder()
            //地址
            .setBaseUrl(BASE_URL)
            //全部显示、拍照、录像
            .setCameraSupport(CameraConfig.CAMERA_SUPPORT_PHOTO)
            //初始默认显示
            .setFirst(CameraConfig.FIRST_VIDEO)
            //时间限制
            .setLimitTime(0f, 0f)
            //插入相册
            .setInsertGallery(true, artist, getRelativePath())
            //多次拍照
            .setMultiSHoot(true)
            //合并视频
            .setMergeMedia(false)
            //隐藏美颜
            .setHideBeauty(false)
            //隐藏滤镜
            .setHideFilter(false)
            //隐藏音乐
            .setHideMusic(true)
            //隐藏变速
            .setHideSpeed(true)
            //开启水印
            .enabledWatermark(true)
            //水印位置
            .setWatermark("")
            //关键帧
            .setKeyFrameTime(1)
            //帧率
            .setFrameRate(24)
            //前置
            .setFaceFront(true)
            .get()
    }

}