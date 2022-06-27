package com.vesdk.engine.listener

import android.graphics.Bitmap
import android.graphics.PointF
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.vecore.gles.ExtTexture
import com.vecore.gles.GLES20Canvas
import com.vecore.gles.RawTexture
import com.vecore.listener.ExtraDrawFrameListener
import com.vecore.models.ExtraDrawFrame
import com.vecore.models.ExtraDrawMaskFilterConfig
import com.vecore.models.ExtraDrawMaskFrame
import com.vecore.recorder.api.IRecorderTextureCallBack
import com.vesdk.engine.bean.config.MLKitFaceConfig
import com.vesdk.engine.bean.config.MLKitSegmentationConfig
import com.vesdk.engine.face.engine.MLKitFace
import com.vesdk.engine.segmentation.engine.MLKitSegmentation
import java.nio.ByteBuffer

/**
 * 拍摄回调
 */
class CameraExtraListener(val faceListener: OnEngineFaceListener) : ExtraDrawFrameListener {

    /**
     * Canvas
     */
    private var mGLES20Canvas: GLES20Canvas? = null
    private var mRawTexture: RawTexture? = null
    private var mExtTexture: ExtTexture? = null
    private var mFrameBitmap: Bitmap? = null

    /**
     * 宽高
     */
    private var mWidth = 0
    private var mHeight = 0
    private var mZoomWidth = 0
    private var mZoomHeight = 0

    /**
     * 当前时间
     */
    private var mPts: Long = -1

    /**
     * 原始纹理和蒙版图片
     */
    private var mMaskFrame: ExtraDrawMaskFrame? = null
    private var mOriginalTexture: RawTexture? = null

    /**
     * 配置
     */
    private val config = ExtraDrawMaskFilterConfig()

    /**
     * 引擎
     */
    private val mFaceEngine = MLKitFace(MLKitFaceConfig())
    private val mSegmentationEngine = MLKitSegmentation(MLKitSegmentationConfig())

    /**
     * 人脸、分割
     */
    private var mIsFace = false
    private var mIsSegmentation = false

    /**
     * 处理中
     */
    private var mIsSegmentIng = false
    private var mIsFaceIng = false


    override fun onDrawFrame(frame: ExtraDrawFrame, pts: Long, extra: Any?): Long {
        //不需要分割
        if (!mIsFace && !mIsSegmentation) {
            return frame.textureId.toLong()
        }

        //初始化
        initDraw(frame)

        //缩放bitmap
        mFrameBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }

        //缩放的bitmap
        mFrameBitmap = getBitmap(frame, mZoomWidth, mZoomHeight)

        //人脸
        realTimeFace()

        //人像分割
        if (mIsSegmentation) {
            val sameTime = mPts == pts
            val segment = realTimeSegmentation(frame, sameTime)
            mPts = pts
            if (segment) {
                return mRawTexture?.id?.toLong() ?: frame.textureId.toLong()
            }
        }
        mPts = pts
        return frame.textureId.toLong()
    }

    /**
     * 实时处理背景分割
     */
    private fun realTimeSegmentation(frame: ExtraDrawFrame, sameTime: Boolean): Boolean {
        //未分割中...
        val first = mMaskFrame == null || mOriginalTexture == null
        if (first || !sameTime && !mIsSegmentIng) {
            mIsSegmentIng = true
            //原始
            val texture = RawTexture(mWidth, mHeight, false)
            mGLES20Canvas?.let { canvas ->
                canvas.beginRenderTarget(texture)
                Matrix.setIdentityM(frame.transforms, 0)
                canvas.drawTexture(mExtTexture, frame.transforms, 0, 0, mWidth, mHeight)
                canvas.endRenderTarget()
                canvas.deleteRecycledResources()
            }
            //分割
            mFrameBitmap?.let { frameBitmap ->
                //分割
                mSegmentationEngine.asyncProcess(
                    frameBitmap,
                    object : OnEngineSegmentationListener {

                        override fun onFail(code: Int, msg: String) {
                            mIsSegmentIng = false
                        }

                        override fun onSuccess(bitmap: Bitmap) {
                            mIsSegmentIng = false
                        }

                        override fun onSuccess(buffer: ByteBuffer, width: Int, height: Int) {
                            mMaskFrame = ExtraDrawMaskFrame(buffer, width, height)
                            mOriginalTexture = texture
                            mIsSegmentIng = false
                        }

                    })
            }
        }
        //绘制
        mGLES20Canvas?.let { canvas ->
            if (mMaskFrame != null && mOriginalTexture != null) {
                canvas.beginRenderTarget(mRawTexture)
                frame.textureId = mOriginalTexture?.id ?: 0
                frame.flags = 0
                frame.drawFrameWithMask(mMaskFrame, config)
                canvas.endRenderTarget()
                canvas.deleteRecycledResources()
                return true
            }
        }
        return false
    }

    /**
     * 实时处理瘦脸
     */
    private fun realTimeFace() {
        //处理中
        if (mIsFaceIng || !mIsFace) {
            return
        }
        mFrameBitmap?.let { frameBitmap ->
            mIsFaceIng = true
            mFaceEngine.asyncProcess(frameBitmap, object : OnEngineFaceListener {

                override fun onFail(code: Int, msg: String) {
                    faceListener.onFail(code, msg)
                    mIsFaceIng = false
                }

                override fun onSuccess(
                    facePointF: Array<PointF?>?,
                    fivePointF: Array<PointF?>?,
                    asp: Float
                ) {
                    faceListener.onSuccess(facePointF, fivePointF, asp)
                    mIsFaceIng = false
                }
            })
        }
    }

    /**
     * 获取bitmap
     */
    private fun getBitmap(frame: ExtraDrawFrame, w: Int, h: Int): Bitmap {
        val originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        frame.asBitmap(originalBitmap)
        return originalBitmap
    }

    /**
     * 初始化
     */
    private fun initDraw(frame: ExtraDrawFrame) {
        //按比例缩放 图片小一点相对处理时间快一点
        if (mZoomWidth <= 0 || mZoomHeight <= 0) {
            //旋转角度
            if (frame.degress % 180 == 90) {
                setSize(frame.height, frame.width)
            } else {
                setSize(frame.width, frame.height)
            }
            if (mWidth > mHeight) {
                mZoomWidth = MAX_SIDE
                mZoomHeight = (mZoomWidth * mHeight * 1.0f / mWidth).toInt()
            } else {
                mZoomHeight = MAX_SIDE
                mZoomWidth = (mZoomHeight * mWidth * 1.0f / mHeight).toInt()
            }
            if (mWidth < mZoomWidth) {
                mZoomWidth = mWidth
                mZoomHeight = mHeight
            }
        }

        //获取
        if (mGLES20Canvas == null) {
            //GLES20Canvas 创建必须在GL线程，即响应onDrawFrame时
            mGLES20Canvas = GLES20Canvas()
            mGLES20Canvas?.setSize(mWidth, mHeight)
            //FBO对应的纹理，同一个bitmap
            mRawTexture = RawTexture(mWidth, mHeight, false)
        }

        //纹理
        if (mExtTexture == null || mExtTexture?.id != frame.textureId) {
            mExtTexture = ExtTexture(
                mGLES20Canvas,
                if (frame.flags == IRecorderTextureCallBack.FLAG_OES_TEXTURE)
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES
                else
                    GLES20.GL_TEXTURE_2D, frame.textureId
            )
            GLES20.glFinish()
            //透明
            mExtTexture?.isOpaque = false
            //上下镜像
            mExtTexture?.setFlipperVertically(false)
        }
    }

    /**
     * 设置宽高
     */
    private fun setSize(a: Int, b: Int) {
        mWidth = a
        mHeight = b
    }


    /**
     * 设置人脸
     */
    fun setFace(face: Boolean) {
        mIsFace = face
    }

    /**
     * 设置分割
     */
    fun setSegmentation(segmentation: Boolean) {
        mIsSegmentation = segmentation
    }

    /**
     * 释放
     */
    fun release() {
        //停止
        mIsFace = false
        mIsSegmentation = false
        mIsSegmentIng = true
        mIsFaceIng = true
        //释放
        mFaceEngine.release()
        mSegmentationEngine.release()
        //canvas释放
        mExtTexture?.recycle()
        mExtTexture = null
        mRawTexture?.recycle()
        mRawTexture = null
        mGLES20Canvas = null
    }

    companion object {
        /**
         * 建议的最大边
         */
        const val MAX_SIDE = 256
    }

    /**
     * 录制
     */
    init {
        mFaceEngine.create()
        mSegmentationEngine.create()
    }

}