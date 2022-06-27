package com.pesdk.uisdk.analyzer.internal.person;

import android.graphics.Bitmap;
import android.util.Log;

import com.pesdk.uisdk.analyzer.internal.OnSegmentationListener;
import com.pesdk.uisdk.analyzer.internal.SegmentationEngine;


/**
 *手动
 */
public class PersonSegmentRunnable implements Runnable {
    private Bitmap mask;
    private SegmentResultListener listener;
    private SegmentationEngine mSegmentationEngine;

    public PersonSegmentRunnable(Bitmap mask, SegmentResultListener listener, SegmentationEngine segmentationEngine) {
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
                listener.onResult(null);
            }
            return;
        }
        mSegmentationEngine.syncProcess(mask, new OnSegmentationListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (listener != null) {
                    listener.onResult(bitmap);
                }
            }

            @Override
            public void onFail(String msg) {
                if (listener != null) {
                    listener.onResult(null);
                }
            }
        });
    }

}
