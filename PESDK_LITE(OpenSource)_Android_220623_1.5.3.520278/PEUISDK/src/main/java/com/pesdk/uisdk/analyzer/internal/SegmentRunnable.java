package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;
import android.util.Log;

import com.vecore.gles.RawTexture;

/**
 *
 */
public class SegmentRunnable implements Runnable {
    public final Object lock = new Object();
    private RawTexture mOriginalTexture;
    private Bitmap mask;
    private SegmentResultListener listener;
    private SegmentationEngine mSegmentationEngine;

    public SegmentRunnable(RawTexture original, Bitmap mask, SegmentResultListener listener, SegmentationEngine segmentationEngine) {
        this.mOriginalTexture = original;
        this.mask = mask;
        this.listener = listener;
        mSegmentationEngine = segmentationEngine;
    }

    private static final String TAG = "SegmentRunnable";

    @Override
    public void run() {//分割
        if (null == mSegmentationEngine) {
            Log.e(TAG, "run: mSegmentationEngine is null ");
            if (listener != null) {
                listener.onResult(mOriginalTexture, null);
            }
            synchronized (lock) {
                lock.notifyAll();
            }
            return;
        }
        mSegmentationEngine.syncProcess(mask, new OnSegmentationListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (listener != null) {
                    listener.onResult(mOriginalTexture, bitmap);
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            @Override
            public void onFail(String msg) {
                if (listener != null) {
                    listener.onResult(mOriginalTexture, null);
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
    }

}
