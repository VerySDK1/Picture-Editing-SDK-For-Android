package com.pesdk.uisdk.util.helper;

import android.content.Context;
import android.graphics.Color;

import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.export.ExportHelper;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.VirtualImage;
import com.vecore.listener.ExportListener;
import com.vecore.models.ImageConfig;

/**
 * 图片合成
 */
public class MixHelper {
    /**
     * 把当前虚拟图片导出，并把生成后的图片设置为当前虚拟图片的底图
     */
    public void onMix(Context context, EditDataHandler dataHandler, Callback callback) {
        onMixFrameImp(context, dataHandler, callback);
    }

    /**
     * 导出当前虚拟图片
     */
    private void onMixFrameImp(Context context, EditDataHandler editDataHandler, Callback listener) {
        VirtualImage virtualImage = new VirtualImage();
        float asp = ExportHelper.loadExport(virtualImage, editDataHandler);
        int minWH = 1080;
        int[] wh = ExportHelper.initWH(asp, minWH);
        String path = PathUtils.getTempFileNameForSdcard("MIX", "png"); //必须是透明图片
        virtualImage.export(context, path, new ImageConfig(wh[0], wh[1], Color.TRANSPARENT), new ExportListener() {
            @Override
            public void onExportStart() {
            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int result, int extra, String info) {
                virtualImage.reset();
                if (result >= VirtualImage.RESULT_SUCCESS) {
                    listener.onResult(path);
                } else {
                    listener.onResult(null);
                }
            }
        });
    }

    public static interface Callback {
        void onResult(String path);
    }
}
