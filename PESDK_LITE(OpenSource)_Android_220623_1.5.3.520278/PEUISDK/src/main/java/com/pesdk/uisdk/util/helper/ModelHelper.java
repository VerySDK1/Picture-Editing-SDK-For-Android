package com.pesdk.uisdk.util.helper;

import android.content.Context;
import android.widget.Toast;

import com.pesdk.analyzer.ModelAssetHelper;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.widget.ExtProgressDialog;
import com.pesdk.uisdk.widget.SysAlertDialog;

/**
 * 准备模型
 */
class ModelHelper {

    /**
     *
     */
    public void checkModel(Context context, boolean isPerson, Callback callback) {
        checkModel(context, isPerson, callback, true);
    }

    /**
     * 检查模板
     *
     * @param context
     * @param isPerson     true 人像; false 天空
     * @param callback
     * @param autoDownload 是否自动下载
     */
    public void checkModel(Context context, boolean isPerson, Callback callback, boolean autoDownload) {
        ModelAssetHelper modelAssetHelper = new ModelAssetHelper();
        ModelAssetHelper.IModel model;
        model = modelAssetHelper.checkModel(isPerson);
        if (null != model) { //ai模型已下载
            callback.onSuccess(model.getLocalBin(), model.getLocalParam());
        } else if (autoDownload) {//模型尚未准备就绪,需要下载
            modelAssetHelper.startDownload(context, new ModelAssetHelper.Callback() {

                        ExtProgressDialog mDialog;
                        final int MAX = 100;

                        @Override
                        public void begin() {
                            mDialog = SysAlertDialog.showProgressDialog(context, R.string.pesdk_dialog_loading_model, false, true,
                                    dialog -> modelAssetHelper.cancel());
                            mDialog.setMax(MAX);
                            mDialog.setProgress(0);
                        }

                        @Override
                        public void failed() {
                            mDialog.dismiss();
                            SysAlertDialog.showAutoHideDialog(context, 0, R.string.common_pe_loading_error, Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void progress(float progress) {
                            mDialog.setProgress((int) (progress * MAX));
                        }

                        @Override
                        public void complete(String bin, String param) {
                            mDialog.dismiss();
                            callback.onSuccess(bin, param);
                        }
                    }
            );
        }
    }

    public static interface Callback {
        void onSuccess(String bin, String param);
    }
}
