package com.pesdk.api.ActivityResultContract;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.pesdk.api.SdkEntry;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 证件照
 */
@Keep
public class CardContract extends ActivityResultContract<CardInput, String> {


    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, CardInput input) {
        if (SdkEntry.appKeyIsInvalid(context) || input == null) {
            return null;
        }
        return com.pesdk.api.ActivityResultContract.Intent.createCard(context, input);
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
