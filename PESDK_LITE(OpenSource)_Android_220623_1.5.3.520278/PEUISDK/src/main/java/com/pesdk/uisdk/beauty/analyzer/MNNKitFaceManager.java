package com.pesdk.uisdk.beauty.analyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.alibaba.android.mnnkit.actor.FaceDetector;
import com.alibaba.android.mnnkit.entity.FaceDetectionReport;
import com.alibaba.android.mnnkit.entity.MNNFlipType;
import com.alibaba.android.mnnkit.intf.InstanceCreatedListener;
import com.alibaba.android.mnnkit.monitor.MNNMonitor;

/**
 * 人脸识别
 */
public class MNNKitFaceManager {

    private static volatile MNNKitFaceManager sInstance;

    private MNNKitFaceManager() {
    }

    public static MNNKitFaceManager getInstance() {
        if (sInstance == null) {
            sInstance = new MNNKitFaceManager();
        }
        return sInstance;
    }

    private FaceDetector mFaceDetector;


    /**
     * 初始化
     */
    public MNNKitFaceManager createFaceAnalyzer(Context context) {
        return createFaceAnalyzer(context, null);
    }

    public MNNKitFaceManager createFaceAnalyzer(Context context, Callback callback) {
        FaceDetector.FaceDetectorCreateConfig createConfig = new FaceDetector.FaceDetectorCreateConfig();
        createConfig.mode = FaceDetector.FaceDetectMode.MOBILE_DETECT_MODE_IMAGE;
        FaceDetector.createInstanceAsync(context, createConfig, new InstanceCreatedListener<FaceDetector>() {
            @Override
            public void onSucceeded(FaceDetector faceDetector) {
                mFaceDetector = faceDetector;
                if (null != callback) {
                    callback.prepared();
                }
            }

            @Override
            public void onFailed(int i, Error error) {
            }
        });
        MNNMonitor.setMonitorEnable(false);
        return this;
    }

    public static interface Callback {
        void prepared();
    }

    /**
     * 释放
     */
    public void release() {
        if (mFaceDetector != null) {
            mFaceDetector.release();
            mFaceDetector = null;
        }
    }

    /**
     * 异步
     */
    public FaceDetectionReport[] inference(Bitmap bitmap) {
        if (mFaceDetector != null) {
            return mFaceDetector.inference(bitmap, 0, 0, 0, MNNFlipType.FLIP_NONE);
        } else {
            return null;
        }
    }

    /**
     * 根据MLFace返回点
     */
    public PointF[] getPointFList(FaceDetectionReport face, float w, float h) {
        PointF[] pointFS = new PointF[106];
        if (face != null) {
            float[] keyPoints = face.keyPoints;
            if (keyPoints != null && keyPoints.length >= 212) {
                for (int i = 0; i < pointFS.length; i++) {
                    pointFS[i] = new PointF(keyPoints[i * 2] / w, keyPoints[i * 2 + 1] / h);
                }
            }
        }
        return pointFS;
    }

}
