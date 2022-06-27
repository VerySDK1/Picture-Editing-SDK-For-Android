package com.pesdk.uisdk.ActivityResultCallback.callback;

import android.app.Activity;
import android.content.Intent;

import com.pesdk.uisdk.util.IntentConstants;
import com.vecore.models.PEImageObject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

/**
 * 消除笔
 */
public abstract class EraseResultCallback implements ActivityResultCallback<ActivityResult> {

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (null != data) {
                PEImageObject tmp = data.getParcelableExtra(IntentConstants.PARAM_EDIT_IMAGE);
                if (null != tmp) {
                    onResult(tmp);
                }
            }
        }
    }

    protected abstract void onResult(PEImageObject imageObject);
}