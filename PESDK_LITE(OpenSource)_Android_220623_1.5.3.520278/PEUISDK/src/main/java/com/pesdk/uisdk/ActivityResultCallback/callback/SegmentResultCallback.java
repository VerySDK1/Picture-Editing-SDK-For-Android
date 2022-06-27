package com.pesdk.uisdk.ActivityResultCallback.callback;

import android.app.Activity;
import android.content.Intent;

import com.pesdk.uisdk.util.IntentConstants;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

/**
 * 抠图
 */
public abstract class SegmentResultCallback implements ActivityResultCallback<ActivityResult> {
    private static final String TAG = "SegmentResultCallback";

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (null != data) {
                String maskPath = data.getStringExtra(IntentConstants.PARAM_SEGMENT_RESULT);
                if (null != maskPath) {
                    onResult(maskPath);
                }
            }
        }
    }

    protected abstract void onResult(String maskPath);
}