package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;

import com.vecore.gles.RawTexture;

/**
 * 分割成功
 */
public interface SegmentResultListener {
    /**
     * 成功|失败
     */
    void onResult(RawTexture original, Bitmap mask);
}
