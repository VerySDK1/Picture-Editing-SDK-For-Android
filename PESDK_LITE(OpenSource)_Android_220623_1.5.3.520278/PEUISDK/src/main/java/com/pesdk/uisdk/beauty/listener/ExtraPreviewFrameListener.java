package com.pesdk.uisdk.beauty.listener;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.alibaba.android.mnnkit.entity.FaceDetectionReport;
import com.pesdk.uisdk.beauty.analyzer.MNNKitFaceManager;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.gles.ExtTexture;
import com.vecore.gles.GLES20Canvas;
import com.vecore.gles.RawTexture;
import com.vecore.listener.ExtraDrawFrameListener;
import com.vecore.models.ExtraDrawFrame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.vecore.recorder.api.IRecorderTextureCallBack.FLAG_OES_TEXTURE;


public class ExtraPreviewFrameListener implements ExtraDrawFrameListener {

    /**
     * Canvas
     */
    private GLES20Canvas mGLES20Canvas;
    private RawTexture mRawTexture;
    private ExtTexture mExtTexture;
    private ByteBuffer mByteBuffer;
    private Bitmap mFrameBitmap;//缩小的bitmap

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
    private long mPts;
    /**
     * 处理中
     */
    private boolean mIsFaceIng = false;//脸部

    /**
     * 人脸接口
     */
    private ExtraPreviewFaceListener mFaceListener;
    /**
     * 人脸列表
     */
    private final ArrayList<BeautyFaceInfo> mFaceList = new ArrayList<>();

    public static final int MIN_BITMAP = 640;

    @Override
    public long onDrawFrame(ExtraDrawFrame frame, long pts, Object extra) {
        if (frame == null) {
            return 0;
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
        if (mPts != pts) {
            //获取bitmap
            mFrameBitmap = getBitmap(frame, mZoomWidth, mZoomHeight);
            realTimeFace();
        }
        mPts = pts;
        return frame.textureId;
    }

    /**
     * 设置宽高
     */
    private void setSize(int a, int b) {
        mWidth = a;
        mHeight = b;
    }

    /**
     * 实时处理瘦脸
     */
    private void realTimeFace() {
        //处理中
        if (mIsFaceIng || mFaceListener == null) {
            return;
        }
        Bitmap bitmap = mFrameBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mIsFaceIng = true;
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {

            @Override
            public void onBackground() {
                processFace(bitmap, mFaceList);
            }

            @Override
            public void onEnd() {
                super.onEnd();
                bitmap.recycle();
                mFaceListener.onFaceSuccess();
                mIsFaceIng = false;
            }
        });
    }


    /**
     * 读取bitmap中的人脸信息
     *
     * @param faceBmp
     * @param faceInfos 人脸信息
     */
    public static void processFace(Bitmap faceBmp, List<BeautyFaceInfo> faceInfos) {
        faceInfos.clear();
        FaceDetectionReport[] inference = MNNKitFaceManager.getInstance().inference(faceBmp);
        if (inference != null && inference.length > 0) {
            for (FaceDetectionReport face : inference) {
                boolean exist = false;
                for (int i = 0; i < faceInfos.size(); i++) {
                    BeautyFaceInfo faceInfo = faceInfos.get(i);
                    if (faceInfo.getFaceId() == face.faceID) {
                        exist = true;
                        i = faceInfos.size();
                    }
                }
                if (!exist) {
                    PointF[] pointFList = MNNKitFaceManager.getInstance().getPointFList(face, faceBmp.getWidth(), faceBmp.getHeight());
                    if (pointFList != null) {
                        RectF faceRectF = new RectF(face.rect.left * 1.0f / faceBmp.getWidth(),
                                face.rect.top * 1.0f / faceBmp.getHeight(),
                                face.rect.right * 1.0f / faceBmp.getWidth(),
                                face.rect.bottom * 1.0f / faceBmp.getHeight());
                        float asp = faceBmp.getWidth() * 1.0f / faceBmp.getHeight();
                        faceInfos.add(new BeautyFaceInfo(face.faceID, asp, faceRectF, pointFList));
                    }
                }
            }
        }
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

        mIsFaceIng = true;//脸部
        if (null != mFrameBitmap) {
            mFrameBitmap.recycle();
            mFrameBitmap = null;
        }
    }

    /**
     * 设置时间
     */
    public void setTime(long pts) {
        mPts = pts;
    }

    /**
     * 设置人脸
     */
    public void setFaceListener(ExtraPreviewFaceListener faceListener) {
        mFaceListener = faceListener;
    }

    /**
     * 获取人脸数据
     */
    public ArrayList<BeautyFaceInfo> getFaceList() {
        return mFaceList;
    }
}
