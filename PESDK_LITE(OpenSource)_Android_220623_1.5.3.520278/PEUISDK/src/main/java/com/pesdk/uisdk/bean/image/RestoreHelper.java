package com.pesdk.uisdk.bean.image;

import android.content.Context;
import android.graphics.RectF;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.PipBgParam;
import com.pesdk.uisdk.bean.template.TemplateUtils;
import com.pesdk.uisdk.util.helper.OverlayHelper;
import com.pesdk.uisdk.util.helper.PipHelper;
import com.pesdk.uisdk.util.manager.MaskManager;
import com.vecore.base.lib.utils.ParcelableUtils;
import com.vecore.models.MaskObject;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.utils.MiscUtils;

import java.util.List;

/**
 * 草稿恢复时，还原参数
 */
class RestoreHelper {
    private static final String TAG = "RestoreHelper";

    public void restoreData(Context context, VirtualIImageInfo virtualIImageInfo) {
        if (virtualIImageInfo.getVer() < VirtualIImageInfo.VERSION) { //兼容过期的字段
            fixParam(virtualIImageInfo);
        }

        ExtImageInfo extImageInfo = virtualIImageInfo.getExtImageInfo();
        PEImageObject imageObject = extImageInfo.getBackground(); //背景-景深
        if (null != imageObject) { //背景不需要绑定tag
            imageObject.setTag(null);
        }

        List<CollageInfo> list = virtualIImageInfo.getCollageInfos();
        if (null != list && list.size() > 0) {
            for (CollageInfo info : list) {

                //修正画中画抠图、背景色
                PEImageObject peImageObject = info.getImageObject();
                if (null != peImageObject && peImageObject.getTag() instanceof String) {
                    ImageOb ob = ParcelableUtils.toParcelObj((String) peImageObject.getTag(), ImageOb.CREATOR);
                    peImageObject.setTag(ob); //是否有抠图

                    PEImageObject bg = info.getBG();
                    if (null != bg && bg.getTag() instanceof String) {
                        PipBgParam tmp = ParcelableUtils.toParcelObj((String) bg.getTag(), PipBgParam.CREATOR);
                        if (null != tmp) {
                            if (!TextUtils.isEmpty(tmp.getPath())) {
                                PipHelper.onPipSegment(info, ob.getSegmentType());
                                PipHelper.onPipBGStyle(info, tmp.getBlurIntensity(), tmp.getPath());
                            } else if (tmp.getColor() != PEScene.UNKNOWN_COLOR) { //构造纯色背景
                                PipHelper.onPipBgColor(info, tmp.getColor());
                            }
                        } else {
                            info.setBG(null);
                        }
                    }
                } else {
                    info.setBG(null);
                }


                //蒙版
                MaskObject maskObject = peImageObject.getMaskObject();
                if (maskObject != null) {
                    int registered = MaskManager.getInstance().getRegistered(maskObject.getName());
                    if (registered > 0) {
                        maskObject.setMaskId(registered);
                    } else {
                        maskObject.setMaskId(TemplateUtils.registeredMask(maskObject.getMediaPath()));
                    }
                }
            }
        }

        List<CollageInfo> overlayList = virtualIImageInfo.getOverlayList();
        if (null != overlayList && overlayList.size() > 0) { //叠加
            for (CollageInfo overlay : overlayList) {
                OverlayHelper.setOverlay(overlay.getImageObject());
            }
        }


    }


    /**
     * 兼容过期的参数:
     * 把原场景转换成 第0个画中画
     */
    private static void fixParam(VirtualIImageInfo imp) {
        PEScene peScene = imp.peScene;
        if (peScene == null) {
            return;
        }
        float asp = imp.getProportionValue();
        {//原底图
            PEImageObject imageObject = peScene.getPEImageObject();
            if (imageObject == null) {
                return;
            }
            //仅保留主媒体
            CollageInfo collageInfo = new CollageInfo(imageObject);
            RectF rectF = imageObject.getShowRectF();
            if (rectF.isEmpty()) {
                RectF dst = new RectF();
                RectF clip = imageObject.getClipRectF();
                if (clip == null || clip.isEmpty()) {
                    clip = new RectF(0, 0, imageObject.getWidth(), imageObject.getHeight());
                }
                MiscUtils.fixShowRectF(clip.width() * 1.0f / clip.height(), 1080, (int) (1080f / asp), dst);
                imageObject.setShowRectF(dst);
            }
            imp.getCollageInfos().add(0, collageInfo);
        }

        { //原背景色|背景样式
            int color = peScene.getBackgroundColor();
            if (color != PEScene.UNKNOWN_COLOR) {
                imp.getExtImageInfo().setBackground(color);
            } else if (null != peScene.getBackground()) {
                PEImageObject bg = peScene.getBackground();
                if (bg.getShowRectF().isEmpty()) {
                    RectF dst = new RectF();
                    MiscUtils.fixShowRectF(bg.getWidth() * 1.0f / bg.getHeight(), 1080, (int) (1080f / asp), dst);
                    bg.setShowRectF(dst);
                }
                bg.setTag(null);
                imp.getExtImageInfo().setBackground(bg);
            }
        }
    }
}
