package com.pesdk.uisdk.ActivityResultCallback.callback;

import android.app.Activity;
import android.content.Intent;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.util.IntentConstants;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

/**
 * 美颜
 */
public abstract class BeautyResultCallback implements ActivityResultCallback<ActivityResult> {
    private static final String TAG = "BeautyResultCallback";

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (null != data) {
                FilterInfo tmp = data.getParcelableExtra(IntentConstants.PARAM_EDIT_BEAUTY_FILTER_RESULT);
                String  hairMedia = data.getStringExtra(IntentConstants.PARAM_EDIT_BEAUTY_FILTER_HAIR_MEDIA); //带美发效果的媒体
                if (null != tmp) {
                    onResult(data.getBooleanExtra(IntentConstants.PARAM_EDIT_BEAUTY_ADD, true), tmp,hairMedia);
                }
            }
        }
    }

    protected abstract void onResult(boolean isAdd, FilterInfo filterInfo,String hairMedia);
}