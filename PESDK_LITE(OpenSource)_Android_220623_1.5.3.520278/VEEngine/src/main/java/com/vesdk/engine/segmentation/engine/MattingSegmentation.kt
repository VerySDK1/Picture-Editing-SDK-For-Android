package com.vesdk.engine.segmentation.engine

import android.graphics.Bitmap
import com.vecore.matting.InputImage
import com.vecore.matting.Mattinger
import com.vesdk.engine.bean.EngineError
import com.vesdk.engine.bean.EngineException
import com.vesdk.engine.bean.base.BaseSegmentationConfig
import com.vesdk.engine.bean.config.MattingSegmentationConfig
import com.vesdk.engine.listener.OnEngineSegmentationListener
import com.vesdk.engine.segmentation.SegmentationEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MattingSegmentation(val config: MattingSegmentationConfig) : SegmentationEngine() {

    /**
     * 检测器
     */
    private var mattinger: Mattinger? = null

    override fun isSameConfig(config: BaseSegmentationConfig): Boolean {
        return this.config.isSameConfig(config)
    }

    override fun create() {
        release()
        mattinger = config.initDetector()
    }

    override fun release() {
        mattinger?.close()
        mattinger = null
    }

    override fun asyncProcess(bitmap: Bitmap, listener: OnEngineSegmentationListener) {
        mattinger?.let { engine ->
            engine.process(InputImage.fromBitmap(bitmap))
                ?.addOnSuccessListener { result ->
                    if (result.data != null) {
                        val tmp = Bitmap.createBitmap(result.data)
                        result.data.recycle()
                        listener.onSuccess(tmp)
                    } else {
                        listener.onFail(
                            EngineError.ERROR_CODE_IDENTIFY,
                            EngineError.ERROR_MSG_IDENTIFY
                        )
                    }
                }?.addOnFailureListener { e ->
                    listener.onFail(e.hashCode(), e.toString())
                }
        } ?: kotlin.run {
            listener.onFail(EngineError.ERROR_CODE_INIT, EngineError.ERROR_MSG_INIT)
        }
    }

    override suspend fun syncProcess(bitmap: Bitmap) = suspendCancellableCoroutine<Bitmap> { cont ->
        mattinger?.let { engine ->
            engine.processSync(InputImage.fromBitmap(bitmap))?.let { result ->
                if (result.data != null) {
                    val tmp = Bitmap.createBitmap(result.data)
                    result.data.recycle()
                    cont.resume(tmp)
                } else {
                    cont.resumeWithException(
                        EngineException(
                            EngineError.ERROR_CODE_IDENTIFY,
                            EngineError.ERROR_MSG_IDENTIFY
                        )
                    )
                }
            } ?: kotlin.run {
                cont.resumeWithException(
                    EngineException(
                        EngineError.ERROR_CODE_IDENTIFY,
                        EngineError.ERROR_MSG_IDENTIFY
                    )
                )
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

}