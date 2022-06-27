package com.vesdk.engine.face.engine

import android.graphics.Bitmap
import android.graphics.PointF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetector
import com.vesdk.engine.bean.EngineError
import com.vesdk.engine.bean.EngineException
import com.vesdk.engine.bean.base.BaseFaceConfig
import com.vesdk.engine.bean.config.MLKitFaceConfig
import com.vesdk.engine.face.FaceEngine
import com.vesdk.engine.listener.OnEngineFaceListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 人脸
 */
class MLKitFace(val config: MLKitFaceConfig) : FaceEngine() {

    /**
     * 检测器
     */
    private var detector: FaceDetector? = null

    override fun isSameConfig(config: BaseFaceConfig): Boolean {
        return this.config.isSameConfig(config)
    }

    override fun create() {
        release()
        detector = config.initDetector()
    }

    override fun release() {
        detector?.close()
        detector = null
    }

    override fun asyncProcess(bitmap: Bitmap, listener: OnEngineFaceListener) {
        detector?.let { engine ->
            engine.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { result ->
                    if (result.size > 0) {
                        val w = bitmap.width
                        val h = bitmap.height
                        listener.onSuccess(
                            getPointFList(result[0], w.toFloat(), h.toFloat()),
                            getFivePointF(result[0], w.toFloat(), h.toFloat()), w * 1.0f / h
                        )
                    } else {
                        listener.onFail(
                            EngineError.ERROR_CODE_IDENTIFY,
                            EngineError.ERROR_MSG_IDENTIFY
                        )
                    }
                }.addOnFailureListener { e ->
                    listener.onFail(e.hashCode(), e.toString())
                }
        } ?: kotlin.run {
            listener.onFail(EngineError.ERROR_CODE_INIT, EngineError.ERROR_MSG_INIT)
        }
    }

    override suspend fun asyncProcess(bitmap: Bitmap) =
        suspendCancellableCoroutine<Array<PointF?>?> { cont ->
            detector?.let { engine ->
                engine.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { result ->
                        if (result.size > 0) {
                            val w = bitmap.width
                            val h = bitmap.height
                            cont.resume(getPointFList(result[0], w.toFloat(), h.toFloat()))
                        } else {
                            cont.resumeWithException(
                                EngineException(
                                    EngineError.ERROR_CODE_IDENTIFY,
                                    EngineError.ERROR_MSG_IDENTIFY
                                )
                            )
                        }
                    }.addOnFailureListener { e ->
                        cont.resumeWithException(EngineException(e.hashCode(), e.toString()))
                    }
            } ?: kotlin.run {
                cont.resumeWithException(
                    EngineException(
                        EngineError.ERROR_CODE_INIT,
                        EngineError.ERROR_MSG_INIT
                    )
                )
            }
        }

    /**
     * 根据MLFace返回特定顺序的点 12个点
     * 由于图像是旋转了 所以需要把X --> 1 - x轴
     */
    private fun getPointFList(face: Face?, w: Float, h: Float): Array<PointF?>? {
        if (face == null) {
            return null
        }
        val point = FloatArray(24)
        var contour = face.getContour(FaceContour.FACE)
        var points: List<PointF>
        if (contour != null) {
            points = contour.points
            //0~2  => 26、18、10、
            var p = points[26]
            point[0] = p.x / w
            point[1] = p.y / h
            p = points[18]
            point[2] = p.x / w
            point[3] = p.y / h
            p = points[10]
            point[4] = p.x / w
            point[5] = p.y / h


            //6~11 =》 29、7、25、11、23、13
            p = points[29]
            point[12] = p.x / w
            point[13] = p.y / h
            p = points[7]
            point[14] = p.x / w
            point[15] = p.y / h
            p = points[25]
            point[16] = p.x / w
            point[17] = p.y / h
            p = points[11]
            point[18] = p.x / w
            point[19] = p.y / h
            p = points[22]
            point[20] = p.x / w
            point[21] = p.y / h
            p = points[14]
            point[22] = p.x / w
            point[23] = p.y / h
        }
        //3 =》 鼻梁
        contour = face.getContour(FaceContour.NOSE_BRIDGE)
        if (contour != null) {
            points = contour.points
            val p = points[1]
            point[6] = p.x / w
            point[7] = p.y / h
        }

        //4 => 左眼
        contour = face.getContour(FaceContour.LEFT_EYE)
        if (contour != null) {
            points = contour.points
            val p1 = points[4]
            val p2 = points[12]
            point[8] = (p1.x + p2.x) / 2 / w
            point[9] = (p1.y + p2.y) / 2 / h
        }

        //5 => 右眼
        contour = face.getContour(FaceContour.RIGHT_EYE)
        if (contour != null) {
            points = contour.points
            val p1 = points[4]
            val p2 = points[12]
            point[10] = (p1.x + p2.x) / 2 / w
            point[11] = (p1.y + p2.y) / 2 / h
        }
        val pointFS = arrayOfNulls<PointF>(12)
        var i = 0
        var j = 0
        while (i < point.size) {
            pointFS[j] = PointF(point[i++], point[i++])
            j++
        }
        return pointFS
    }

    /**
     * 五官
     */
    private fun getFivePointF(face: Face?, w: Float, h: Float): Array<PointF?>? {
        if (face == null) {
            return null
        }
        val pointFS = arrayOfNulls<PointF>(32)
        //左脸  29 26 25 21
        //右脸  7 10 11 15
        //鼻子 0 2
        //左眼 8 5 2 0 13 11
        //右眼 0 2 5 8 11 13
        //上嘴唇上 0 4 6 10 (下嘴唇下)3 5
        //下巴 18
        //上嘴唇下 4
        //下嘴唇上 4
        val faceContour = face.getContour(FaceContour.FACE)
        val noseContour = face.getContour(FaceContour.NOSE_BOTTOM)
        val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)
        val rightEyeContour = face.getContour(FaceContour.RIGHT_EYE)
        val upperLipTopContour = face.getContour(FaceContour.UPPER_LIP_TOP)
        val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM)
        val lowerLipTopContour = face.getContour(FaceContour.LOWER_LIP_TOP)
        val lowerLipBottomContour = face.getContour(FaceContour.LOWER_LIP_BOTTOM)
        if (faceContour != null
            && noseContour != null
            && leftEyeContour != null
            && rightEyeContour != null
            && upperLipTopContour != null
            && upperLipBottomContour != null
            && lowerLipTopContour != null
            && lowerLipBottomContour != null
        ) {
            val facePoints = faceContour.points
            val nosePoints = noseContour.points
            val leftEyePoints = leftEyeContour.points
            val rightEyePoints = rightEyeContour.points
            val upperLipTopPoints = upperLipTopContour.points
            val upperLipBottomPoints = upperLipBottomContour.points
            val lowerLipTopPoints = lowerLipTopContour.points
            val lowerLipBottomPoints = lowerLipBottomContour.points

            //脸
            var p = facePoints[29]
            pointFS[0] = PointF(p.x / w, p.y / h)
            p = facePoints[26]
            pointFS[1] = PointF(p.x / w, p.y / h)
            p = facePoints[25]
            pointFS[2] = PointF(p.x / w, p.y / h)
            p = facePoints[21]
            pointFS[3] = PointF(p.x / w, p.y / h)
            p = facePoints[7]
            pointFS[4] = PointF(p.x / w, p.y / h)
            p = facePoints[10]
            pointFS[5] = PointF(p.x / w, p.y / h)
            p = facePoints[11]
            pointFS[6] = PointF(p.x / w, p.y / h)
            p = facePoints[15]
            pointFS[7] = PointF(p.x / w, p.y / h)

            //鼻子
            p = nosePoints[0]
            pointFS[8] = PointF(p.x / w, p.y / h)
            p = nosePoints[2]
            pointFS[9] = PointF(p.x / w, p.y / h)

            //眼睛
            p = leftEyePoints[8]
            pointFS[10] = PointF(p.x / w, p.y / h)
            p = leftEyePoints[5]
            pointFS[11] = PointF(p.x / w, p.y / h)
            p = leftEyePoints[2]
            pointFS[12] = PointF(p.x / w, p.y / h)
            p = leftEyePoints[0]
            pointFS[13] = PointF(p.x / w, p.y / h)
            p = leftEyePoints[13]
            pointFS[14] = PointF(p.x / w, p.y / h)
            p = leftEyePoints[11]
            pointFS[15] = PointF(p.x / w, p.y / h)
            p = rightEyePoints[0]
            pointFS[16] = PointF(p.x / w, p.y / h)
            p = rightEyePoints[2]
            pointFS[17] = PointF(p.x / w, p.y / h)
            p = rightEyePoints[5]
            pointFS[18] = PointF(p.x / w, p.y / h)
            p = rightEyePoints[8]
            pointFS[19] = PointF(p.x / w, p.y / h)
            p = rightEyePoints[11]
            pointFS[20] = PointF(p.x / w, p.y / h)
            p = rightEyePoints[13]
            pointFS[21] = PointF(p.x / w, p.y / h)

            //嘴唇
            p = upperLipTopPoints[0]
            pointFS[22] = PointF(p.x / w, p.y / h)
            p = upperLipTopPoints[4]
            pointFS[23] = PointF(p.x / w, p.y / h)
            p = upperLipTopPoints[5]
            pointFS[24] = PointF(p.x / w, p.y / h)
            p = upperLipTopPoints[10]
            pointFS[25] = PointF(p.x / w, p.y / h)
            p = lowerLipBottomPoints[3]
            pointFS[26] = PointF(p.x / w, p.y / h)
            p = lowerLipBottomPoints[5]
            pointFS[27] = PointF(p.x / w, p.y / h)

            //下巴
            p = facePoints[18]
            pointFS[28] = PointF(p.x / w, p.y / h)

            //嘴唇
            p = upperLipBottomPoints[4]
            pointFS[29] = PointF(p.x / w, p.y / h)
            p = lowerLipTopPoints[4]
            pointFS[30] = PointF(p.x / w, p.y / h)
        }
        return pointFS
    }

}