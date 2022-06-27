package com.pesdk.api.ActivityResultContract;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.pesdk.api.SdkEntry;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 使用完整的编辑功能(剪同款)
 */
@Keep
public class TemplateUse2EditContract extends ActivityResultContract<VirtualIImageInfo, String> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, VirtualIImageInfo input) {
        if (SdkEntry.appKeyIsInvalid(context)) {
            return null;
        }
        return com.pesdk.api.ActivityResultContract.Intent.createTemplateUser2EditIntent(context, input);
    }


    @Override
    public String parseResult(int resultCode, @Nullable Intent intent) { //导出成功
        if (resultCode == Activity.RESULT_OK) {
            return intent.getStringExtra(SdkEntry.EDIT_RESULT);
        } else {
            return null;
        }
    }
}
