package com.pesdk.api.ActivityResultContract;

import android.content.Context;
import android.util.Log;

import com.pesdk.api.IVirtualImageInfo;
import com.pesdk.api.PEAPI;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.ui.card.CardActivity;
import com.pesdk.uisdk.ui.home.EditActivity;
import com.pesdk.uisdk.util.IntentConstants;

/**
 *
 */
public class Intent {
    private static final String TAG = "Intent";

    /**
     * 编辑草稿Intent
     */
    public static android.content.Intent createDarftIntent(Context context, IVirtualImageInfo input) {
        VirtualIImageInfo imageInfo = (VirtualIImageInfo) input;
        if (!imageInfo.isExist()) { //有场景媒体丢失
            Log.e(TAG, "createDarftIntent: image is deleted... ");
            return null;
        }
        imageInfo.restoreData(context); //恢复转场|滤镜|特效... 需要注册的信息
        PEAPI.getInstance().setShortVideo(imageInfo);
        return createIntent(context, PEAPI.getInstance().getShortImageInfo()); //编辑草稿
    }


    /**
     * 编辑
     */
    public static android.content.Intent createEditIntent(Context context, String input) {
        return createIntent(context, input); //编辑草稿
    }


    /***
     * 进入界面前，构建一个新的草稿图片
     */
    public static void creatShortImage(Context context, String path) {
        PEAPI.getInstance().init(context);
        //每次开始新的编辑，构造全新草稿
        PEAPI.getInstance().onShortImageEdit();
        PEAPI.getInstance().getShortImageInfo().initBasePip(path);
        PEAPI.getInstance().syncToDB();
    }

    /**
     * 编辑草稿
     *
     * @param context
     * @param virtualImageInfo
     * @return
     */
    private static android.content.Intent createIntent(Context context, IVirtualImageInfo virtualImageInfo) {
        android.content.Intent intent = new android.content.Intent(context, EditActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_DARFT_ID, virtualImageInfo.getId());
        return intent;
    }

    /**
     * 普通图片编辑
     */
    private static android.content.Intent createIntent(Context context, String path) {
        android.content.Intent intent = new android.content.Intent(context, EditActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE_PATH, path);
        return intent;
    }


    /**
     * 从TempUse跳转到完整编辑界面
     */
    public static android.content.Intent createTemplateUser2EditIntent(Context context, VirtualIImageInfo virtualIImageInfo) {
        PEAPI.getInstance().init(context);
        //每次开始新的编辑，构造全新草稿
        PEAPI.getInstance().onShortImageEdit(virtualIImageInfo);
        PEAPI.getInstance().syncToDB();

        restore(context, PEAPI.getInstance().getShortImageInfo());

        return createIntent(context, PEAPI.getInstance().getShortImageInfo()); //编辑草稿
    }


    /**
     * 恢复
     *
     * @param context
     * @param virtualIImageInfo
     */
    public static void restore(Context context, VirtualIImageInfo virtualIImageInfo) {
        virtualIImageInfo.restoreData(context); //恢复 画中画、 tag...
    }

    /**
     * 证件照
     *
     * @param context
     * @param input
     * @return
     */
    public static android.content.Intent createCard(Context context, CardInput input) {
        return CardActivity.createCard(context, input.getPath(), input.getSize());
    }

}
