package com.pesdk.uisdk.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.pesdk.api.SdkEntry;
import com.pesdk.api.callback.IExportCallBack;
import com.pesdk.bean.template.RReplaceMedia;
import com.vecore.models.PEImageObject;

import java.util.ArrayList;

import androidx.activity.result.contract.ActivityResultContract;

/**
 * 回调
 */
public class SdkEntryHandler {

    private IExportCallBack iExportCallBack;

    private static SdkEntryHandler instance;

    public static SdkEntryHandler getInstance() {
        if (null == instance) {
            instance = new SdkEntryHandler();
        }
        return instance;
    }

    private SdkEntryHandler() {

    }

    public void setIExportCallBack(IExportCallBack callBack) {
        iExportCallBack = callBack;
    }

    public void onExportClick(Context context, @SdkEntry.ExportType int exportType) {
        if (null != iExportCallBack) {
            Message msg = mHandler.obtainMessage(MSG_ON_EXPORT);
            msg.obj = context;
            msg.arg1 = exportType;
            msg.sendToTarget();
        }
    }

    public ActivityResultContract<Void, ArrayList<String>> getAlbumContract() {
        if (iExportCallBack != null) {
            return iExportCallBack.onActionAlbum();
        }
        return null;
    }

    public ActivityResultContract<Void, Boolean> getMakeTemplateContract() {
        if (iExportCallBack != null) {
            return iExportCallBack.onActionMakeTemplate();
        }
        return null;
    }

    public ActivityResultContract<ArrayList<RReplaceMedia>, ArrayList<PEImageObject>> getAlbumTemplateContract() {
        if (iExportCallBack != null) {
            return iExportCallBack.onActionTemplateAlbum();
        }
        return null;
    }

    /**
     * 点击导出响应
     */
    private final int MSG_ON_EXPORT = 11;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ON_EXPORT) {
                if (null != iExportCallBack) {
                    iExportCallBack.onExport((Context) msg.obj, msg.arg1);
                }
            }
            msg.obj = null;
        }
    };

}
