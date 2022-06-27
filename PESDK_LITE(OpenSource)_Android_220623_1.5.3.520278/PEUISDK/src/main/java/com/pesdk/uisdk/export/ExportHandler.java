package com.pesdk.uisdk.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.data.vm.EditExportVM;
import com.pesdk.uisdk.edit.EditDataHandler;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

/**
 * 导出
 */
public class ExportHandler {
    private Activity mActivity;
    private VirtualIImageInfo mImageInfo;
    private EditExportVM mEditExportVM;
    private Callback mCallback;

    public ExportHandler(FragmentActivity activity, VirtualIImageInfo info, Callback callback) {
        mActivity = activity;
        mCallback = callback;
        mImageInfo = info;
        mEditExportVM = new ViewModelProvider(activity, new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(EditExportVM.class);
        mEditExportVM.getLiveData().observe(activity, this::handleResult);
    }

    private void handleResult(String path) {
        dialog.dismiss();
        if (null != path) {
            mCallback.onSuccess(path);
        } else {
            Toast.makeText(mActivity, R.string.pesdk_save_error, Toast.LENGTH_SHORT).show();
        }
    }


    public void export(EditDataHandler editDataHandler, boolean withWatermark) {
        dialog = getLoadingDialog(mActivity, getMsg());
        dialog.setCancelable(false);
        dialog.show();
        mEditExportVM.export(mImageInfo, editDataHandler, withWatermark);
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


    public static interface Callback {
        void onSuccess(String path);

    }
}
