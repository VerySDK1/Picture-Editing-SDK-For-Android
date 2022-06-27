package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;

/**
 *
 */
public interface OnSegmentationListener {
    /**
     * 成功 (透明人像区域图 )
     */
    void onSuccess(Bitmap bitmap);

    /**
     * 失败
     */
    void onFail(String msg);
}
