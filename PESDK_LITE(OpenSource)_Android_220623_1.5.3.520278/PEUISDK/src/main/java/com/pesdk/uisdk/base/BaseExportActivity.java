package com.pesdk.uisdk.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.pesdk.api.SdkEntry;
import com.pesdk.uisdk.internal.SdkEntryHandler;
import com.pesdk.api.manager.ExportConfiguration;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * 统一处理导出
 */
public abstract class BaseExportActivity extends BaseActivity {

    protected boolean withWatermark = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    /**
     * 默认的导出配置
     */
    public abstract ExportConfiguration getExportConfig();

    /**
     * 准备导出  第一步：验证是否需要回调
     */
    public final void prepareExport() {
        prepareExport(SdkEntry.EXPORT_IMAGE);
    }

    private int mExportType = SdkEntry.EXPORT_IMAGE;

    /**
     * 指定导出类型
     *
     * @param exportType {@link SdkEntry#EXPORT_IMAGE
     * @link SdkEntry#EXPORT_GIF
     * }
     */
    public final void prepareExport(@SdkEntry.ExportType int exportType) {
        if (getExportConfig() != null && getExportConfig().useCustomExportGuide) { //回调-自定义扩展（如：导出前的引导vip流程）
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(SdkEntry.MSG_EXPORT));
            mExportType = exportType;
            SdkEntryHandler.getInstance().onExportClick(this, exportType);
        } else {
            onExport();
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SdkEntry.MSG_EXPORT)) { //接收自定义回调。是否清除水印
                LocalBroadcastManager.getInstance(BaseExportActivity.this).unregisterReceiver(mReceiver);
                withWatermark = intent.getBooleanExtra(SdkEntry.EXPORT_WITH_WATERMARK, true);
                if (mExportType == SdkEntry.EXPORT_IMAGE) {//gif|压缩|提取音频， 分辨率忽略
                    int wh = intent.getIntExtra(SdkEntry.EXPORT_IMAGE_WH, -1);
                    if (null != getExportConfig() && wh > 100) {
                        getExportConfig().setMinSide(wh); //指定分辨率
                    }
                }
                onExport();
            }
        }
    };

    /**
     * 准备构造导出
     */
    public abstract void onExport();


}
