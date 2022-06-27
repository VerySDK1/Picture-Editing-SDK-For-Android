package com.vesdk.engine.segmentation.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.Segmenter
import com.vesdk.engine.bean.EngineError
import com.vesdk.engine.bean.EngineException
import com.vesdk.engine.bean.base.BaseSegmentationConfig
import com.vesdk.engine.bean.config.MLKitSegmentationConfig
import com.vesdk.engine.listener.OnEngineSegmentationListener
import com.vesdk.engine.segmentation.SegmentationEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * mlkit
 */
class MLKitSegmentation(val config: MLKitSegmentationConfig) : SegmentationEngine() {

    /**
     * 检测器
     */
    private var segmentation: Segmenter? = null

    override fun isSameConfig(config: BaseSegmentationConfig): Boolean {
        return this.config.isSameConfig(config)
    }

    override fun create() {
        segmentation = config.initDetector()
    }

    override fun release() {
        segmentation?.close()
        segmentation = null
    }

    override fun asyncProcess(bitmap: Bitmap, listener: OnEngineSegmentationListener) {
        val kitConfig = config
        segmentation?.let { engine ->
            engine.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { result ->
                    if (kitConfig.mode == MODE_USE_RAW_BUFFER_RESULT) {
                        listener.onSuccess(result.buffer, result.width, result.height)
                    } else {
                        listener.onSuccess(maskColorsFromByteBuffer(result))
                    }
                }.addOnFailureListener { e ->
                    listener.onFail(e.hashCode(), e.toString())
                }
        } ?: kotlin.run {
            listener.onFail(EngineError.ERROR_CODE_INIT, EngineError.ERROR_MSG_INIT)
        }
    }

    override suspend fun syncProcess(bitmap: Bitmap) = suspendCancellableCoroutine<Bitmap> { cont ->
        segmentation?.let { engine ->
            engine.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { result ->
                    cont.resume(maskColorsFromByteBuffer(result))
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
     * mask Bitmap
     */
    private fun maskColorsFromByteBuffer(segmentationMask: SegmentationMask): Bitmap {
        val buffer = segmentationMask.buffer
        val maskWidth = segmentationMask.width
        val maskHeight = segmentationMask.height
        val colors = IntArray(maskWidth * maskHeight)
        for (i in 0 until maskWidth * maskHeight) {
            colors[i] = Color.argb((255 * buffer.float).toInt(), 255, 255, 255)

            //背景的可能性
//                float backgroundLikelihood = 1 - buffer.getFloat();
//                if (backgroundLikelihood > 0.9) {
//                    colors[i] = Color.argb(0, 0, 0, 0);
//                } else if (backgroundLikelihood > 0.2) {
//                    // Linear interpolation to make sure when backgroundLikelihood is 0.2, the alpha is 0 and
//                    // when backgroundLikelihood is 0.9, the alpha is 128.
//                    // +0.5 to round the float value to the nearest int.
//                    int alpha = (int) (182.9 * backgroundLikelihood - 36.6 + 0.5);
//                    colors[i] = Color.argb(128 - alpha, 0, 0, 0);
//                } else {
//                    colors[i] = Color.argb(255, 255, 255, 255);
//                }
        }
        return Bitmap.createBitmap(colors, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
    }

}