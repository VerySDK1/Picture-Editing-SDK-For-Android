package com.pesdk.uisdk.analyzer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.text.TextUtils;

import com.pesdk.uisdk.analyzer.internal.ResultListener;
import com.pesdk.uisdk.analyzer.internal.SegmentRunnable;
import com.pesdk.uisdk.analyzer.internal.SegmentationEngine;
import com.pesdk.uisdk.analyzer.internal.Util;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.gles.BitmapTexture;
import com.vecore.gles.ExtTexture;
import com.vecore.gles.GLES20Canvas;
import com.vecore.gles.RawTexture;
import com.vecore.listener.ExtraDrawFrameListener;
import com.vecore.models.ExtraDrawFrame;
import com.vecore.models.PEImageObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.vecore.recorder.api.IRecorderTextureCallBack.FLAG_OES_TEXTURE;

/**
 * 预览回调
 */
public class ExtraPreviewFrameListener implements ExtraDrawFrameListener {
    private static final String TAG = "ExtraPreviewFrameListen";
    /**
     * 建议的最大、最小的尺寸
     */
    public static final int MIN_BITMAP = 640;


    /**
     * 媒体
     */
    private PEImageObject mPEImageObject; //主元素
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

    /**
     * 导出 可能需要同步
     */
    private boolean mIsExport = false;

    private SegmentationEngine mSegmentationEngine;

    /**
     * 媒体
     */
    public ExtraPreviewFrameListener(PEImageObject mediaObject, SegmentationEngine segmentationEngine) {
        mPEImageObject = mediaObject;
        mSegmentationEngine = segmentationEngine;
    }

    /**
     * 图册
     */
    public ExtraPreviewFrameListener(PEImageObject peImageObject, int id, SegmentationEngine segmentationEngine) {
        this.mPEImageObject = peImageObject;
        this.mId = id;
        mSegmentationEngine = segmentationEngine;
    }

    public void update(PEImageObject imageObject) {
        mPEImageObject = imageObject;
        isSegmented=false;
    }

    @Override
    public long onDrawFrame(ExtraDrawFrame frame, long pts, Object extra) {
//        Log.e(TAG, "onDrawFrame: " + pts + " >" + frame + " mPts:" + mPts + " >" + mPEImageObject);
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
//        Log.e(TAG, "realTimeSegmentation: same: " + same + " mPts:" + mPts + " mImageOb:" + mImageOb + " mMaskBitmap:" + mMaskBitmap + " mOriginalTexture:" + mOriginalTexture);
        if (mImageOb.isSegment()) {
            //同一时间 没有初始化 bitmap为null
            if (!same || !isSegmented || mOriginalTexture == null) {
                //导出或者未分割中
                if (mIsExport || !mIsSegmentIng) {
                    mIsSegmentIng = true;
                    //缩放bitmap
                    Bitmap mFrameBitmap = getBitmap(frame, mZoomWidth, mZoomHeight);

                    //原始
                    RawTexture texture = new RawTexture(mWidth, mHeight, false);
                    mGLES20Canvas.beginRenderTarget(texture);
                    GLES20Canvas.rotateTextureMatrix(frame.transforms, mDegree);
                    mGLES20Canvas.drawTexture(mExtTexture, frame.transforms, 0, 0, mWidth, mHeight);
                    mGLES20Canvas.endRenderTarget();
                    mGLES20Canvas.deleteRecycledResources();
                    mLastMask = mMaskBitmap;
                    if (!TextUtils.isEmpty(mImageOb.getMaskPath())) { //已经存在抠图文件
                        mMaskBitmap = BitmapFactory.decodeFile(mImageOb.getMaskPath());
                        mOriginalTexture = texture;
                    } else {
                        //同步抓取mask
                        SegmentRunnable runnable = new SegmentRunnable(texture, mFrameBitmap, (original, mask) -> {
                            mOriginalTexture = original;
                            if (null != mask && Util.hasSegment(mask)) { //有人像|天空
                                mMaskBitmap = mask;
                                ThreadPoolUtils.executeEx(() -> bindMask(mask, mImageOb));
                                mFrameBitmap.recycle();
                                if (null != mResultListener) {
                                    mResultListener.onResult(true);
                                }
                            } else {
                                LogUtil.i(TAG, "realTimeSegmentation: mask: " + mask);
                                //抠图失败：未找到人脸|天空
                                if (null != mResultListener) {
                                    mResultListener.onResult(false);
                                }
                            }
                        }, mSegmentationEngine);
                        ThreadPoolUtils.executeEx(runnable);
                        synchronized (runnable.lock) {
                            try {
                                runnable.lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
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
    private Bitmap getBitmap(ExtraDrawFrame frame, int w, int h) {
        Bitmap originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        frame.asBitmap(originalBitmap);
        return originalBitmap;
    }


    /**
     * 绑定mask，避免同一个素材频繁抠图
     */
    public static void bindMask(Bitmap bitmap, ImageOb ob) {
        if (null == bitmap) {
            return;
        }
        try {
            String path = PathUtils.getTempFileNameForSdcard("mask_" + bitmap.hashCode(), "png");
            BitmapUtils.saveBitmapToFile(bitmap, true, 100, path);
            ob.setMaskPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
