package com.pesdk.uisdk.util.helper;

import android.graphics.RectF;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.utils.MiscUtils;

/**
 * 处理ActivityResult 回调之后的部分参数调整
 */
public class ResultHelper {
    private static final String TAG = "ResultHelper";

    /**
     * 裁剪之后，修正图层默认的显示位置 (保证中心点不变)
     *
     * @param srcShowRectF 之前的显示位置
     * @param clipRect     新的裁剪比例
     * @param group        播放器容器size
     * @return 新的显示位置
     */
    public static RectF fixPipShowRectF(RectF srcShowRectF, RectF clipRect, ViewGroup group) {
        float clipAsp = clipRect.width() / clipRect.height();
        return fixReplacePipShowRectF(srcShowRectF, clipAsp, group);
    }


    /**
     * 替换画中画后，重新计算媒体显示位置
     *
     * @param srcShowRectF
     * @param clipAsp
     * @param group
     * @return
     */
    public static RectF fixReplacePipShowRectF(RectF srcShowRectF, float clipAsp, ViewGroup group) {
        int groupWidth = group.getWidth();
        int groupHeight = group.getHeight();
        float tw, th;
        if (clipAsp >= 1) {
            tw = srcShowRectF.width();
            th = tw * groupWidth / clipAsp / groupHeight;
        } else {
            th = srcShowRectF.height();
            tw = th * groupHeight * clipAsp / groupWidth;
        }
        float left = srcShowRectF.centerX() - (tw / 2.0f);
        float top = srcShowRectF.centerY() - (th / 2.0f);


        RectF tmp = new RectF(left, top, left + tw, top + th); //默认的显示位置

        //修正显示位置(防止越裁越小)
        float t1 = Math.min(1 / tmp.width(), 1 / tmp.height()); //防止出现expadding， 最大与预览size齐平
        float scale = Math.min(Math.max(srcShowRectF.width() / tmp.width(), srcShowRectF.height() / tmp.height()), t1); //保证短的一边与原始端的一边一致
//        Log.e(TAG, "fixPipShowRectF: " + tmp + " " + scale);
        if (scale != 1) {
            return MiscUtils.zoomRectF(tmp, scale, scale);
        } else {
            return tmp;
        }
    }


    /**
     * 裁剪之后，修正图层默认的显示位置 (保证中心点不变,要完全铺满)
     *
     * @param srcShowRectF 之前的显示位置
     * @param clipAsp      新的裁剪比例
     * @param groupWidth   播放器容器size
     * @param groupHeight
     * @return 新的显示位置
     */
    public static RectF fixMainTrackShowRectF(RectF srcShowRectF, float clipAsp, int groupWidth, int groupHeight) {
        //原始的矩形区域的显示位置
        RectF dst = new RectF(srcShowRectF.left * groupWidth, srcShowRectF.top * groupHeight, srcShowRectF.right * groupWidth, srcShowRectF.bottom * groupHeight);
        float srcAsp = dst.width() / dst.height();
        float tw, th;   // 0~1.0f
        if (clipAsp > srcAsp) {
            th = srcShowRectF.height();
            tw = th * groupHeight * clipAsp / groupWidth;
        } else {
            tw = srcShowRectF.width();
            th = tw * groupWidth / clipAsp / groupHeight;
        }

        float left = srcShowRectF.centerX() - (tw / 2.0f);
        float top = srcShowRectF.centerY() - (th / 2.0f);
        return new RectF(left, top, left + tw, top + th);
    }


    /**
     * 修正图层美颜
     */
    public static void fixPipBeauty(CollageInfo collageInfo, FilterInfo filterInfo, String hairMedia) {
        PEImageObject tmp = collageInfo.getImageObject();
        if (!TextUtils.isEmpty(hairMedia) && !tmp.getMediaPath().equals(hairMedia)) {//更改了美发效果
            try {
                PEImageObject dst = new PEImageObject(hairMedia);
                dst.setShowRectF(tmp.getShowRectF());
                dst.setClipRectF(tmp.getClipRectF());
                dst.setShowAngle(tmp.getShowAngle());
                dst.setAngle(tmp.getAngle());
                dst.setTag(tmp.getTag());

                Object obj = dst.getTag();
                if (obj instanceof ImageOb) { //头发修改之后，需要重新设置mask图(忽略手动抠图)
                    ImageOb tag = (ImageOb) obj;
                    tag.setMaskPath(null);
                }
                collageInfo.setMedia(dst);
                tmp = collageInfo.getImageObject();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        ImageOb imageOb = PEHelper.initImageOb(tmp);
        imageOb.setBeauty(filterInfo);
        try {
            tmp.changeFilterList(FilterUtil.getFilterList(imageOb));
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
}
