package com.pesdk.uisdk.analyzer.internal.person;

import android.graphics.Bitmap;

/**
 * 分割成功
 */
public interface SegmentResultListener {
    /**
     * 成功|失败
     */
    void onResult(Bitmap mask);
}
