package com.pesdk.uisdk.ui.card.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.widget.Toast;

import com.pesdk.api.SdkEntry;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.ui.card.listener.ExportCallback;
import com.pesdk.uisdk.ui.card.vm.ExportVM;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

/**
 * 导出
 */
public class ExportHandler {
    private Activity mActivity;
    private ExportVM mExportVM;

    public ExportHandler(FragmentActivity activity) {
        mActivity = activity;
        mExportVM = new ViewModelProvider(activity, new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(ExportVM.class);
        mExportVM.getLiveData().observe(activity, this::handleResult);
    }

    private void handleResult(String path) {
        dialog.dismiss();
        if (null != path) {
            onSuccess(path);
        } else {
            Toast.makeText(mActivity, R.string.pesdk_save_error, Toast.LENGTH_SHORT).show();
        }
    }


    public void export(ExportCallback callback, Rect size) {
        dialog = getLoadingDialog(mActivity, getMsg());
        dialog.setCancelable(false);
        dialog.show();
        mExportVM.export(callback, size);
    }

    private String getMsg() {
        return mActivity.getString(R.string.pesdk_saving_image);
    }

    private ProgressDialog dialog;

    private ProgressDialog getLoadingDialog(Context context, String title) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(true);
        dialog.setMessage(title);
        return dialog;
    }

    private void onSuccess(String path) {
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, path);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

}
