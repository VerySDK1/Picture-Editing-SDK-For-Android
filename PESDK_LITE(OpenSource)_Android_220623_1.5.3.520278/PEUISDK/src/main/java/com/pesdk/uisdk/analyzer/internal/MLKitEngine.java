package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

/**
 * 谷歌mlKit (仅人像抠图)
 */
public class MLKitEngine implements SegmentationEngine {

    /**
     * 引擎
     */
    private Segmenter mSegmenter;

    /**
     * 创建分割引擎
     */
    @Override
    public void createAnalyzer(int mode, String binPath, String paramPath) {
        SelfieSegmenterOptions options =
                new SelfieSegmenterOptions.Builder()
                        //SINGLE_IMAGE_MODE 不会分析先前帧  STREAM_MODE会分析先前帧
                        .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
                        //.enableRawSizeMask()
                        .build();
        mSegmenter = Segmentation.getClient(options);
    }

    /**
     * 释放
     */
    public void release() {
        if (mSegmenter != null) {
            mSegmenter.close();
            mSegmenter = null;
        }
    }

    @Override
    public void asyncProcess(Bitmap bitmap, OnSegmentationListener listener) {
        Task<SegmentationMask> task = mSegmenter.process(InputImage.fromBitmap(bitmap, 0));
        task.addOnSuccessListener(new OnSuccessListener<SegmentationMask>() {

            @Override
            public void onSuccess(@NonNull SegmentationMask segmentationMask) {
                listener.onSuccess(maskColorsFromByteBuffer(segmentationMask));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFail(e.getMessage());
            }
        });
    }

    @Override
    public void syncProcess(Bitmap bitmap, OnSegmentationListener listener) {
        Task<SegmentationMask> task = mSegmenter.process(InputImage.fromBitmap(bitmap, 0));
        task.addOnSuccessListener(new OnSuccessListener<SegmentationMask>() {

            @Override
            public void onSuccess(@NonNull SegmentationMask segmentationMask) {
                listener.onSuccess(maskColorsFromByteBuffer(segmentationMask));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFail(e.getMessage());
            }
        });


    }

    /**
     * mask Bitmap
     */
    private Bitmap maskColorsFromByteBuffer(SegmentationMask segmentationMask) {
        ByteBuffer buffer = segmentationMask.getBuffer();
        int maskWidth = segmentationMask.getWidth();
        int maskHeight = segmentationMask.getHeight();
        int[] colors = new int[maskWidth * maskHeight];
        for (int i = 0; i < maskWidth * maskHeight; i++) {
            colors[i] = Color.argb((int) (255 * buffer.getFloat()), 255, 255, 255);

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
        return Bitmap.createBitmap(colors, maskWidth, maskHeight, Bitmap.Config.ARGB_8888);
    }


    /**
     * 异步
     */
    public Task<SegmentationMask> asyncAnalyseBitmap(Bitmap bitmap) {
        return asyncAnalyseFrame(InputImage.fromBitmap(bitmap, 0));
    }

    /**
     * 异步
     */
    public Task<SegmentationMask> asyncAnalyseFrame(InputImage image) {
        if (mSegmenter != null) {
            return mSegmenter.process(image);//异步
        }
        return null;
    }

}
