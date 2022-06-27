package com.pesdk.uisdk.ui.home.segment.listener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.text.TextUtils;

import com.pesdk.uisdk.analyzer.internal.ResultListener;
import com.pesdk.uisdk.analyzer.internal.SegmentationEngine;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.vecore.gles.BitmapTexture;
import com.vecore.gles.ExtTexture;
import com.vecore.gles.GLES20Canvas;
import com.vecore.gles.RawTexture;
import com.vecore.listener.ExtraDrawFrameListener;
import com.vecore.models.ExtraDrawFrame;
import com.vecore.models.PEImageObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.pesdk.uisdk.analyzer.ExtraPreviewFrameListener.MIN_BITMAP;
import static com.vecore.recorder.api.IRecorderTextureCallBack.FLAG_OES_TEXTURE;

/**
 * 预览回调
 */
public class PreviewFrameListener implements ExtraDrawFrameListener {
    private static final String TAG = "PreviewFrameListener";


    /**
     * 媒体
     */
    private final PEImageObject mPEImageObject; //主元素
    private ImageOb mImageOb;

    /**
     * Canvas
     */
    private GLES20Canvas mGLES20Canvas;
    private RawTexture mRawTexture;
    private ExtTexture mExtTexture;
    private ByteBuffer mByteBuffer;
    /**
     * 宽高
     */
    private int mWidth;
    private int mHeight;
    private int mZoomWidth;//缩放
    private int mZoomHeight;//缩放
    /**
     * 角度
     */
    private int mDegree;

    /**
     * 当前时间 避免一直重复 瘦脸大眼设置时会回调
     */
    private long mPts = -1;

    /**
     * id 画中画区分需要
     */
    private int mId;


    /**
     * 原始图片和蒙版图片
     */
    private Bitmap mMaskBitmap;
    private RawTexture mOriginalTexture;

    /**
     * 处理中
     */
    private boolean mIsSegmentIng = false;//分割
    private boolean mIsFaceIng = false;//脸部

    /**
     * 导出 可能需要同步
     */
    private boolean mIsExport = false;

    private SegmentationEngine mSegmentationEngine;

    /**
     * 媒体
     */
    public PreviewFrameListener(PEImageObject mediaObject, SegmentationEngine segmentationEngine) {
        mPEImageObject = mediaObject;
        mSegmentationEngine = segmentationEngine;
    }

//    /**
//     * 图册
//     */
//    public PreviewFrameListener(PEImageObject peImageObject, int id, SegmentationEngine segmentationEngine) {
//        this.mPEImageObject = peImageObject;
//        this.mId = id;
//        mSegmentationEngine = segmentationEngine;
//    }


    @Override
    public long onDrawFrame(ExtraDrawFrame frame, long pts, Object extra) {
        if (frame == null) {
            return 0;
        }
        if (null != mPEImageObject) {
            mImageOb = (ImageOb) mPEImageObject.getTag();
        }
        if (mImageOb == null) {
            return frame.textureId;
        }

        //按比例缩放 图片小一点相对处理时间快一点
        if (mZoomWidth <= 0 || mZoomHeight <= 0) {
            //旋转角度
            if (frame.degress % 180 == 90) {
                setSize(frame.height, frame.width);
                mDegree = (frame.degress + 180) % 360;
            } else {
                setSize(frame.width, frame.height);
                mDegree = frame.degress;
            }

            if (mWidth > mHeight) {
                mZoomHeight = MIN_BITMAP;
                mZoomWidth = (int) (mZoomHeight * mWidth * 1.0f / mHeight);
            } else {
                mZoomWidth = MIN_BITMAP;
                mZoomHeight = (int) (mZoomWidth * mHeight * 1.0f / mWidth);
            }
            if (mWidth < mZoomWidth) {
                mZoomWidth = mWidth;
                mZoomHeight = mHeight;
            }
        }

        //获取
        if (mGLES20Canvas == null) {
            //GLES20Canvas 创建必须在GL线程，即响应onDrawFrame时
            mGLES20Canvas = new GLES20Canvas();
            mGLES20Canvas.setSize(mWidth, mHeight);
            //FBO对应的纹理，同一个bitmap
            mRawTexture = new RawTexture(mWidth, mHeight, false);
        }

        //从 textureId获取画面处理 然后在分割 在处理
        if (mByteBuffer == null) {
            mByteBuffer = ByteBuffer.allocateDirect(4 * mWidth * mHeight)
                    .order(ByteOrder.LITTLE_ENDIAN);
        }

        //纹理
        if (mExtTexture == null || mExtTexture.getId() != frame.textureId) {
            mExtTexture = new ExtTexture(mGLES20Canvas, frame.flags == FLAG_OES_TEXTURE ?
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D, frame.textureId);
            GLES20.glFinish();
            mExtTexture.setOpaque(false);//透明
            mExtTexture.setFlipperVertically(false);//上下镜像
        }

        //瘦脸
//        if (mPts != pts) {
//            realTimeFace(frame.transforms);
//        }

        //人像分割
        boolean segment = realTimeSegmentation(frame, mPts == pts);
        mPts = pts;
        if (segment) {
            return mRawTexture.getId();
        }

        return frame.textureId;
    }

    /**
     * 强制刷新
     */
    public void force() {
        mPts = -1;
    }

    private Bitmap mLastMask; //下次分割清理旧的mask

    private boolean isSegmented = false; //是否已经抠图过

    /**
     * 实时处理背景分割
     */
    private boolean realTimeSegmentation(ExtraDrawFrame frame, boolean same) {
//        Log.e(TAG, "realTimeSegmentation: " + mVideoOb.isSegment() + " " + same + " " + mPts + " isSegmented:" + isSegmented + " " + mMaskBitmap);
        if (mImageOb.isSegment()) {
            //同一时间 没有初始化 bitmap为null
            if (!same || !isSegmented || mOriginalTexture == null) {
                //导出或者未分割中
                if (mIsExport || !mIsSegmentIng) {
                    mIsSegmentIng = true;
                    //缩放bitmap
                    Bitmap mFrameBitmap = getBitmap(frame.transforms, mZoomWidth, mZoomHeight);

                    //原始
                    RawTexture texture = new RawTexture(mWidth, mHeight, false);
                    mGLES20Canvas.beginRenderTarget(texture);
                    GLES20Canvas.rotateTextureMatrix(frame.transforms, mDegree);
                    mGLES20Canvas.drawTexture(mExtTexture, frame.transforms, 0, 0, mWidth, mHeight);
                    mGLES20Canvas.endRenderTarget();
                    mGLES20Canvas.deleteRecycledResources();
                    mLastMask = mMaskBitmap;
                    mOriginalTexture = texture;
                    if (!TextUtils.isEmpty(mImageOb.getMaskPath())) { //已经存在抠图文件
                        mMaskBitmap = BitmapFactory.decodeFile(mImageOb.getMaskPath());
                    } else {
                        mMaskBitmap = null;
                    }

//                    //同步抓取mask
//                    SegmentRunnable runnable = new SegmentRunnable(texture, mFrameBitmap, (original, mask) -> {
//                        mOriginalTexture = original;
//                        if (null != mask && Util.hasAlpha(mask)) { //有人像|天空
//                            mMaskBitmap = mask;
//                            mFrameBitmap.recycle();
//                            if (null != mResultListener) {
//                                mResultListener.onResult(true);
//                            }
//                        } else { //抠图失败：未找到人脸|天空
//                            if (null != mResultListener) {
//                                mResultListener.onResult(false);
//                            }
//                        }
//                    }, mSegmentationEngine);
//                    ThreadPoolUtils.execute(runnable);
//                    synchronized (runnable.lock) {
//                        try {
//                            runnable.lock.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    mIsSegmentIng = false;
                    isSegmented = true;
                }
            }
            //绘制
            if (mMaskBitmap != null && mOriginalTexture != null && null != mGLES20Canvas) {
                mGLES20Canvas.beginRenderTarget(mRawTexture);
                GLES20Canvas.rotateTextureMatrix(frame.transforms, frame.degress);
                //黑白图（可缩小的尺寸），但与原图比例一致，不然错位
                BitmapTexture maskTexture = new BitmapTexture(mMaskBitmap);
                maskTexture.setFlipperVertically(false);
                //黑白图(dst)rgba* 原始图(src)rgba + 黑白图rgba* 0
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                mGLES20Canvas.drawTexture(maskTexture, frame.transforms, 0, 0, mWidth, mHeight);

                //原始图
                GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
                mGLES20Canvas.drawTexture(mOriginalTexture, frame.transforms, 0, 0, mWidth, mHeight);

                mGLES20Canvas.endRenderTarget();
                mGLES20Canvas.deleteRecycledResources();
                maskTexture.recycle();
                recycleLastMask();
                return true;
            }
        }
        return false;
    }


    /**
     * 获取bitmap
     */
    private Bitmap getBitmap(float[] transforms, int w, int h) {
        mGLES20Canvas.beginRenderTarget(mRawTexture);
        GLES20Canvas.rotateTextureMatrix(transforms, mDegree);
        mGLES20Canvas.drawTexture(mExtTexture, transforms, 0, 0, w, h);
        mByteBuffer.clear();
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
        GLES20.glFinish();
        mByteBuffer.rewind();
        Bitmap originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        originalBitmap.copyPixelsFromBuffer(mByteBuffer);
        mGLES20Canvas.endRenderTarget();
        mGLES20Canvas.deleteRecycledResources();
        mByteBuffer.clear();
        return originalBitmap;
    }


    /**
     * 获取bitmap
     */
    private Bitmap getBitmap(ExtraDrawFrame frame, int w, int h) {
        mGLES20Canvas.beginRenderTarget(mRawTexture);
        GLES20Canvas.rotateTextureMatrix(frame.transforms, frame.degress + 180);
        mGLES20Canvas.drawTexture(mExtTexture, frame.transforms, 0, 0, w, h);
        mByteBuffer.clear();
        GLES20.glFinish();
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
        mByteBuffer.rewind();
        Bitmap originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        originalBitmap.copyPixelsFromBuffer(mByteBuffer);
        mGLES20Canvas.endRenderTarget();
        mGLES20Canvas.deleteRecycledResources();
        mByteBuffer.clear();
        return originalBitmap;
    }


    /**
     * 释放
     */
    public void release() {
        if (mExtTexture != null) {
            mExtTexture.recycle();
            mExtTexture = null;
        }
        if (mByteBuffer != null) {
            mByteBuffer.clear();
            mByteBuffer = null;
        }
        if (mRawTexture != null) {
            mRawTexture.recycle();
            mRawTexture = null;
        }
        mGLES20Canvas = null;

        mIsSegmentIng = false;//分割
        mIsFaceIng = true;//脸部
        if (null != mMaskBitmap) {
            mMaskBitmap.recycle();
            mMaskBitmap = null;
        }
        isSegmented = false;
        recycleLastMask();
    }

    private void recycleLastMask() {
        if (null != mLastMask && !mLastMask.isRecycled()) {
            mLastMask.recycle();
        }
        mLastMask = null;
    }


    /**
     * 设置宽高
     */
    private void setSize(int a, int b) {
        mWidth = a;
        mHeight = b;
    }

    /**
     * 设置时间
     */
    public void setPts(long pts) {
        mPts = pts;
    }

    /**
     * id
     */
    public int getId() {
        return mId;
    }

    /**
     * 导出
     */
    public void setExport(boolean export) {
        this.mIsExport = export;
    }

    private ResultListener mResultListener;

    public void setResultListener(ResultListener resultListener) {
        mResultListener = resultListener;
    }
}
