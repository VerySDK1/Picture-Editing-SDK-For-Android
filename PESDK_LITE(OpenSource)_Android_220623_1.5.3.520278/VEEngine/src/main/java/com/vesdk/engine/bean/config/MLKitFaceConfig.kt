package com.vesdk.engine.bean.config

import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.vesdk.engine.bean.base.BaseFaceConfig

/**
 * 人脸识别配置
 */
class MLKitFaceConfig : BaseFaceConfig() {

    /**
     * 初始化检测器
     */
    fun initDetector(): FaceDetector {
        //①轮廓检测②特征点检测③分类④特征点检测和分类
        val options = FaceDetectorOptions.Builder()
            //FAST 快速模式    ACCURATE精准模式
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            //LANDMARK_MODE_NONE 不识别特征点   LANDMARK_MODE_ALL
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            //检测轮廓
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            //NO_CLASSIFICATIONS ALL_CLASSIFICATIONS 是否将人脸分类为“微笑”和“睁眼”等类别
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            //最小识别多大的脸部，与照片大小有关
            .setMinFaceSize(0.2f)
            //启用面部跟踪
            //.enableTracking()
            .build()
        return FaceDetection.getClient(options)
    }

    /**
     * 配置是否相同
     */
    override fun isSameConfig(config: BaseFaceConfig): Boolean {
        return true
    }

}