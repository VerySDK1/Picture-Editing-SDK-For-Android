package com.pesdk.api.ActivityResultContract;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.pesdk.api.IVirtualImageInfo;
import com.pesdk.api.SdkEntry;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 继续编辑草稿
 */
@Keep
public class EditDraftResultContract extends ActivityResultContract<IVirtualImageInfo, String> {


    private static final String TAG = "EditDraftResultContract";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, IVirtualImageInfo input) {
        if (SdkEntry.appKeyIsInvalid(context)) {
            return null;
        }
        return com.pesdk.api.ActivityResultContract.Intent.createDarftIntent(context, input); //编辑草稿
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
