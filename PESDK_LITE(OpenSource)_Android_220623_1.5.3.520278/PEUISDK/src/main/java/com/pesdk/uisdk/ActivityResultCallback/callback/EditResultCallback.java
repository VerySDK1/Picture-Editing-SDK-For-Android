package com.pesdk.uisdk.ActivityResultCallback.callback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;

import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.util.IntentConstants;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

/**
 * 编辑
 */
public abstract class EditResultCallback implements ActivityResultCallback<ActivityResult> {

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (null != data) {
                RectF rectF = data.getParcelableExtra(IntentConstants.PARAM_EDIT_RESULT_CLIPRECTF);
                @Crop.CropMode
                int mode = data.getIntExtra(IntentConstants.PARAM_EDIT_RESULT_CROPMODE, Crop.CROP_ORIGINAL);
                if (null != rectF) {
                    onResult(rectF, mode);
                }
            }
        }
    }

    protected abstract void onResult(RectF rectF, @Crop.CropMode int mode);
}