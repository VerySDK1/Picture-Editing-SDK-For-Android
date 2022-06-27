package com.pesdk.api.callback;

import android.content.Context;

import com.pesdk.api.SdkEntry;
import com.pesdk.bean.template.RReplaceMedia;
import com.vecore.models.PEImageObject;

import java.util.ArrayList;

import androidx.activity.result.contract.ActivityResultContract;

public interface IExportCallBack {


    /**
     * 相册
     */
    ActivityResultContract<Void, ArrayList<String>> onActionAlbum();


    /**
     * 制作模板
     */
    ActivityResultContract<Void, Boolean> onActionMakeTemplate();

    /**
     * 模板
     *
     * @return Contract
     */
    ActivityResultContract<ArrayList<RReplaceMedia>, ArrayList<PEImageObject>> onActionTemplateAlbum();

    /**
     * 响应点击导出按钮
     *
     * @param context
     * @param type    类型 ：实现者，可根据不同类型，定制不同的引导UI
     */
    void onExport(Context context, @SdkEntry.ExportType int type);
}
