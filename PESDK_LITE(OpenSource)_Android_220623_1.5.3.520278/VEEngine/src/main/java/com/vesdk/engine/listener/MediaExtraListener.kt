package com.vesdk.engine.listener

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.vecore.gles.BitmapTexture
import com.vecore.gles.ExtTexture
import com.vecore.gles.GLES20Canvas
import com.vecore.gles.RawTexture
import com.vecore.listener.ExtraDrawFrameListener
import com.vecore.models.ExtraDrawFrame
import com.vecore.models.MediaType
import com.vecore.recorder.api.IRecorderTextureCallBack
import com.vesdk.engine.EngineManager
import com.vesdk.engine.bean.base.BaseSegmentationConfig
import com.vesdk.engine.bean.EngineException
import com.vesdk.engine.bean.EngineMedia
import com.vesdk.engine.bean.config.MattingSegmentationConfig
import com.vesdk.engine.segmentation.SegmentationEngine
import com.vesdk.engine.segmentation.engine.MattingSegmentation
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 扩展
 */
class MediaExtraListener(
    /**
         * 媒体
         */
        val engine: EngineMedia,
    /**
         * 分割配置
         */
        val config: BaseSegmentationConfig,
    /**
         * 回调
         */
        val listener: OnEngineExtraListener
) : ExtraDrawFrameListener {

    /**
     * Canvas
     */
    private var mGLES20Canvas: GLES20Canvas? = null
    private var mRawTexture: RawTexture? = null
    private var mExtTexture: ExtTexture? = null
    private var mByteBuffer: ByteBuffer? = null

    /**
     * 宽高
     */
    private var mWidth = 0
    private var mHeight = 0

    /**
     * 缩放
     */
    private var mZoomWidth = 0
    private var mZoomHeight = 0

    /**
     * 角度
     */
    private var mDegree = 0

    /**
     * 原始图片和蒙版图片
     */
    private var mMaskBitmap: Bitmap? = null
    private var mOriginalTexture: RawTexture? = null

    /**
     * 当前时间 避免一直重复 瘦脸大眼设置时会回调
     */
    private var mPts: Long = -1

    /**
     * 处理中
     */
    private var mIsSegmentIng = false

    /**
     * 同步
     */
    private val mIsSync = true

    /**
     * 下次分割清理旧的mask
     */
    private var mLastMask: Bitmap? = null

    private var mIsInit = true

    /**
     * 图片
     */
    private val mIsImage: Boolean = engine.media.mediaType == MediaType.MEDIA_IMAGE_TYPE

    /**
     * 分割引擎
     */
    private lateinit var mSegmentationEngine: SegmentationEngine

    /**
     * 绘制
     */
    override fun onDrawFrame(drawFrame: ExtraDrawFrame?, pts: Long, extra: Any?): Long {
        val id = drawFrame?.let { frame ->
            //判断是否需要分割
            if (engine.isSegmentation) {
                //绘制
                initDraw(frame)

                //分割
                val segment = realTimeSegmentation(frame, mPts == pts)

                //时间
                mPts = pts
                if (segment) {
                    mRawTexture?.id?.toLong() ?: frame.textureId.toLong()
                } else frame.textureId.toLong()
            } else {
                frame.textureId.toLong()
            }
        } ?: kotlin.run {
            0
        }
        if (mIsInit) {
            listener.init()
            mIsInit = false
        }
        return id
    }

    /**
     * 初始化
     */
    private fun initDraw(frame: ExtraDrawFrame) {
        //按比例缩放 图片小一点相对处理时间快一点
        if (mZoomWidth <= 0 || mZoomHeight <= 0) {
            //旋转角度
            mDegree = if (frame.degress % 180 == 90) {
                setSize(frame.height, frame.width)
                (frame.degress + 180) % 360
            } else {
                setSize(frame.width, frame.height)
                frame.degress
            }
            if (mWidth > mHeight) {
                mZoomHeight = EngineManager.MIN_BITMAP
                mZoomWidth = (mZoomHeight * mWidth * 1.0f / mHeight).toInt()
            } else {
                mZoomWidth = EngineManager.MIN_BITMAP
                mZoomHeight = (mZoomWidth * mHeight * 1.0f / mWidth).toInt()
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

        //从 textureId获取画面处理 然后在分割 在处理
        if (mByteBuffer == null) {
            mByteBuffer = ByteBuffer
                    .allocateDirect(4 * mWidth * mHeight)
                    .order(ByteOrder.LITTLE_ENDIAN)
        }

        //纹理
        if (mExtTexture?.id != frame.textureId) {
            mExtTexture = ExtTexture(mGLES20Canvas, if (frame.flags == IRecorderTextureCallBack.FLAG_OES_TEXTURE) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D, frame.textureId)
            GLES20.glFinish()
            mExtTexture?.isOpaque = false //透明
            mExtTexture?.setFlipperVertically(false) //上下镜像
        }
    }

    /**
     * 实时处理背景分割
     */
    private fun realTimeSegmentation(frame: ExtraDrawFrame, same: Boolean): Boolean {
        //同一时间 没有初始化 bitmap为null
        if ((!same && !mIsImage) || mOriginalTexture == null) {
            //导出或者未分割中
            if (mIsSync || !mIsSegmentIng) {
                mIsSegmentIng = true
                //缩放bitmap
                val frameBitmap = getBitmap(frame, mZoomWidth, mZoomHeight)

                //原始
                val texture = RawTexture(mWidth, mHeight, false)
                mGLES20Canvas?.beginRenderTarget(texture)
                GLES20Canvas.rotateTextureMatrix(frame.transforms, mDegree)
                mGLES20Canvas?.drawTexture(mExtTexture, frame.transforms, 0, 0, mWidth, mHeight)
                mGLES20Canvas?.endRenderTarget()
                mGLES20Canvas?.deleteRecycledResources()

                mLastMask = mMaskBitmap
                //同步抓取
                if (mIsSync) {
                    runBlocking {
                        try {
                            val mask = mSegmentationEngine.syncProcess(frameBitmap)
                            mMaskBitmap = mask
                            mOriginalTexture = texture
                        } catch (e: EngineException) {
                            listener.onFail(e.code, e.msg)
                        }
                        mIsSegmentIng = false
                    }
                    mIsSegmentIng = false
                } else {
                    mSegmentationEngine.asyncProcess(frameBitmap, object : OnEngineSegmentationListener {

                        override fun onSuccess(bitmap: Bitmap) {
                            val refresh = mMaskBitmap == null
                            mMaskBitmap = bitmap
                            mOriginalTexture = texture
                            mIsSegmentIng = false
                            if (refresh) {
                                engine.media.refresh()
                            }
                        }

                        override fun onSuccess(buffer: ByteBuffer, width: Int, height: Int) {
                            mIsSegmentIng = false
                        }

                        override fun onFail(code: Int, msg: String) {
                            listener.onFail(code, msg)
                            mIsSegmentIng = false
                        }
                    })
                }
            }
        }
        //绘制
        if (mMaskBitmap != null && mOriginalTexture != null) {
            mGLES20Canvas?.let { canvas ->
                canvas.beginRenderTarget(mRawTexture)
                GLES20Canvas.rotateTextureMatrix(frame.transforms, frame.degress)
                //黑白图（可缩小的尺寸），但与原图比例一致，不然错位
                val maskTexture = BitmapTexture(mMaskBitmap)
                maskTexture.setFlipperVertically(false)
                //黑白图(dst)rgba* 原始图(src)rgba + 黑白图rgba* 0
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                canvas.drawTexture(maskTexture, frame.transforms, 0, 0, mWidth, mHeight)

                //原始图
                GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO)
                canvas.drawTexture(mOriginalTexture, frame.transforms, 0, 0, mWidth, mHeight)
                canvas.endRenderTarget()
                canvas.deleteRecycledResources()
                maskTexture.recycle()
            }
            recycleLastMask()
            return true
        }
        return false
    }

    /**
     * 获取bitmap
     */
    private fun getBitmap(transforms: FloatArray, w: Int, h: Int): Bitmap {
        val originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mGLES20Canvas?.let { canvas ->
            canvas.beginRenderTarget(mRawTexture)
            GLES20Canvas.rotateTextureMatrix(transforms, mDegree)
            canvas.drawTexture(mExtTexture, transforms, 0, 0, w, h)
            mByteBuffer?.clear()
            GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mByteBuffer)
            GLES20.glFinish()
            mByteBuffer?.rewind()
            originalBitmap.copyPixelsFromBuffer(mByteBuffer)
            canvas.endRenderTarget()
            canvas.deleteRecycledResources()
            mByteBuffer?.clear()
        }
        return originalBitmap
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
     * 清理mask
     */
    private fun recycleLastMask() {
        mLastMask?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        mLastMask = null
    }

    /**
     * 设置宽高
     */
    private fun setSize(a: Int, b: Int) {
        mWidth = a
        mHeight = b
    }

    /**
     * 释放
     */
    fun release() {
        mExtTexture?.recycle()
        mExtTexture = null

        mByteBuffer?.clear()
        mByteBuffer = null

        mRawTexture?.recycle()
        mRawTexture = null

        mGLES20Canvas = null
        mIsSegmentIng = true

        mMaskBitmap?.recycle()
        mMaskBitmap = null

        mSegmentationEngine.release()

        recycleLastMask()
    }

    init {
        if (config is MattingSegmentationConfig) {
            mSegmentationEngine = MattingSegmentation(config)
            mSegmentationEngine.create()
        }
    }
}