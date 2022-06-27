package com.pesdk.uisdk.util.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.PipBgParam;
import com.pesdk.uisdk.util.Utils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.models.VisualFilterConfig;
import com.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PipHelper {
    private static final String TAG = "PipHelper";

    /**
     * 复制参数(显示位置、角度)
     *
     * @param src 前景画中画
     * @param bg  画中画背景
     */
    public static void copyParam(PEImageObject src, PEImageObject bg) {
        bg.setShowRectF(src.getShowRectF());
        bg.setShowAngle(src.getShowAngle());
    }


    /**
     * 修正画中画背景的裁剪区域 （按照 前景的裁剪区域）
     *
     * @param imageObject
     * @param bg
     */
    public static void applyBgClip(PEImageObject imageObject, PEImageObject bg) {
        RectF clipRectF = imageObject.getClipRectF();
        float asp;
        if (clipRectF.isEmpty()) {
            asp = imageObject.getWidth() / (imageObject.getHeight() + 0.0f);
        } else {
            asp = clipRectF.width() / (clipRectF.height() + 0.0f);
        }
        Rect rect = new Rect();
        MiscUtils.fixClipRect(asp, bg.getWidth(), bg.getHeight(), rect);
        bg.setClipRect(rect);
    }


    /**
     * 画中画应用纯色背景
     */
    public static void onPipBgColor(CollageInfo collageInfo, int color) {
        PEImageObject peImageObject = collageInfo.getImageObject();
        Bitmap bitmap = Bitmap.createBitmap(peImageObject.getWidth(), peImageObject.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bitmap);
        cv.drawColor(color);
        try {
            PEImageObject tmp = new PEImageObject(bitmap);
            copyParam(peImageObject, tmp);
            tmp.setTag(new PipBgParam(color));
            collageInfo.setBG(tmp);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        bitmap.recycle();
    }

    /**
     * 应用画中画   背景、天空
     *
     * @param collageInfo
     * @param tmp
     * @param path
     */
    public static void onPipBGStyle(CollageInfo collageInfo, float tmp, String path) {
        PEImageObject imageObject = collageInfo.getImageObject();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            PEImageObject bg = new PEImageObject(path);
            PipHelper.copyParam(imageObject, bg);
            PipHelper.applyBgClip(imageObject, bg);
            PipBgParam pipBgParam = new PipBgParam(path);
            bg.setTag(pipBgParam);
            collageInfo.setBG(bg);      //画中画
            pipBgParam.setBlurIntensity(tmp);
            onBlurPipBg(bg, tmp); //仅景深
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    /**
     * 画中画抠图
     *
     * @param collageInfo
     * @param type        人像、天空
     */
    public static void onPipSegment(CollageInfo collageInfo, @Segment.Type int type) {
        PEImageObject imageObject = collageInfo.getImageObject();
        Utils.setSegment(imageObject, type);

        ImageOb ob = PEHelper.initImageOb(imageObject);
        ob.setMaskPath(null); //切换时，清理掉mask图(可能Mask图的类型与新的抠图类型不一致)

    }

    /**
     * 画中画-背景景深
     */
    public static void onBlurPipBg(PEImageObject bg, float factor) {
        onBlurBG(bg, factor);
        if (null != bg && factor > 0) {
            if (null != bg.getTag()) { //PipBgParam: 仅画中画才有此参数
                PipBgParam pipBgParam = (PipBgParam) bg.getTag();
                if (null != pipBgParam) {
                    pipBgParam.setBlurIntensity(factor);
                }
            }
        }
    }

    /**
     * 虚拟图片 背景-景深
     */
    public static void onBlurBG(PEImageObject bg, float factor) {
        if (null != bg && factor > 0) {
            List<VisualFilterConfig> list = new ArrayList<>();
            list.add(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR, factor));
            try {
                bg.changeFilterList(list);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "onBlur: " + factor + " " + bg);
        }
    }
}
