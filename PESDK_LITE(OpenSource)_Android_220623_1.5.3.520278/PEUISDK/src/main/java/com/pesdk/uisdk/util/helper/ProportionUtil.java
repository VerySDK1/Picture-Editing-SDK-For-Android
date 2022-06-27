package com.pesdk.uisdk.util.helper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.fragment.sticker.StickerExportHandler;
import com.pesdk.uisdk.listener.IFixPreviewListener;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.models.PEImageObject;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.utils.MiscUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 裁剪后比例变化，需修正元素：字幕、贴纸、图层 ..的显示位置
 */
public class ProportionUtil {
    /**
     * 根据新的宽高，重新修正config
     */
    private static void fixConfig(ArrayList<StyleInfo> tmp) {
        if (null != tmp && tmp.size() > 0) {
            for (StyleInfo item : tmp) {
                String file = item.mlocalpath;
                if (!TextUtils.isEmpty(file) && FileUtils.isExist(file) && item.isdownloaded) {
                    CommonStyleUtils.checkStyle(new File(item.mlocalpath), item);
                }
            }
        }
    }

    private static final String TAG = "ProportionUtil";

    /**
     * 修正要调整位置的资源 （字幕、贴纸、水印、 画中画）
     *
     * @param oldPreviewAsp      旧的比例
     * @param dst                新的字幕容器size
     * @param newSize            新的预览size （相对值并非真实像素）
     * @param dataHandler
     * @param virtualVideo
     * @param player
     * @param fixPreviewListener
     */
    public static void onFixResources(float oldPreviewAsp, int[] dst, VirtualImage.Size newSize,
                                      EditDataHandler dataHandler, VirtualImage virtualVideo,
                                      VirtualImageView player, final IFixPreviewListener fixPreviewListener) {

        Log.e(TAG, "onFixResources: " + Arrays.toString(dst) + " " + newSize);

        int newWidth = dst[0];
        int newHeight = dst[1];
        float asp = newWidth / (newHeight + 0.0f);

        //修正图层(画中画)比例
        ArrayList<CollageInfo> collageInfos = dataHandler.getParam().getCollageList();
        if (oldPreviewAsp != asp && null != collageInfos) {
            //预览比例变化了，需要修正画中画位置
            int len = collageInfos.size();
            for (int i = 0; i < len; i++) {
                CollageInfo collageInfo = collageInfos.get(i);
                //媒体比例
                PEImageObject mediaObject = collageInfo.getImageObject();
                float mediaAsp;
                RectF clipRect = mediaObject.getClipRectF();
                if (clipRect == null || clipRect.isEmpty()) {
                    mediaAsp = mediaObject.getWidth() * 1.0f / mediaObject.getHeight();
                } else {
                    mediaAsp = clipRect.width() / clipRect.height();
                }
                RectF showRectF = correctionRatio(collageInfo.getImageObject().getShowRectF(), mediaAsp, oldPreviewAsp, asp);
                collageInfo.getImageObject().setShowRectF(showRectF);
            }

//            //水印
//            CollageInfo collageInfo = dataHandler.getWatermark();
//            if (collageInfo != null) {
//                //媒体比例
//                MediaObject mediaObject = collageInfo.getMediaObject();
//                float mediaAsp;
//                RectF clipRect = mediaObject.getClipRectF();
//                if (clipRect == null || clipRect.isEmpty()) {
//                    mediaAsp = mediaObject.getWidth() * 1.0f / mediaObject.getHeight();
//                } else {
//                    mediaAsp = clipRect.width() / clipRect.height();
//                }
//                RectF showRectF = correctionRatio(collageInfo.getMediaObject().getShowRectF(), mediaAsp, oldPreviewAsp, asp);
//                collageInfo.getMediaObject().setShowRectF(showRectF);
//            }
        }

        //修正叠加比例
        collageInfos = dataHandler.getParam().getOverLayList();
        fixOverlayList(oldPreviewAsp, asp, collageInfos);

        //涂鸦
        ArrayList<GraffitiInfo> graffitiInfos = dataHandler.getParam().getGraffitList();
        if (oldPreviewAsp != asp && null != graffitiInfos) {
            //预览比例变化了，需要修正涂鸦位置
            for (GraffitiInfo info : graffitiInfos) {
                CaptionLiteObject object = info.getLiteObject();
                //显示到中心
                int w = object.getWidth();
                int h = object.getHeight();
                float aspTmp = w * 1.0f / h;

                RectF showRectF = correctionRatio(object.getShowRectF(), aspTmp, oldPreviewAsp, asp);
                object.setShowRectF(showRectF);
            }
        }
//
//        //马赛克|去水印
//        ArrayList<MOInfo> maskList = dataHandler.getMaskList();
//        if (Math.abs(oldPreviewAsp - asp) > 0.001f && maskList != null && maskList.size() > 0) {
//            for (MOInfo moInfo : maskList) {
//                DewatermarkObject object = moInfo.getObject();
//                RectF show = moInfo.getShowRectF();
//                float mediaAsp = show.width() / show.height() * oldPreviewAsp;
//                RectF showRectF = correctionRatio(show, mediaAsp, oldPreviewAsp, asp);
//                moInfo.setShowRectF(showRectF);
//                if (object.getType() == DewatermarkObject.Type.mosaic
//                        || object.getType() == DewatermarkObject.Type.blur) {
//                    //主动修正容器大小
//                    object.setParentSize(newWidth, newHeight);
//                    try {
//                        //重新应用生成jni对象
//                        object.apply(false);
//                    } catch (InvalidArgumentException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }

//        //字幕模板
//        ArrayList<WordTemplateInfo> wordTemplateList = dataHandler.getWordTemplateList();
//        if (wordTemplateList != null && wordTemplateList.size() > 0) {
//            for (WordTemplateInfo info : wordTemplateList) {
//                info.setParentSize(newSize.width, newSize.height);
//                info.setPreviewAsp(asp);
//                //修复比例
//                info.fixAsp(oldPreviewAsp, asp);
//            }
//        }

        //新版字幕
        ArrayList<WordInfoExt> wordNewList = dataHandler.getParam().getWordList();
        if (wordNewList != null && wordNewList.size() > 0) {
            for (WordInfoExt info : wordNewList) {
                info.setParentSize(newSize.width, newSize.height);
                info.setPreviewAsp(asp);
                //修复比例
                info.fixAsp(oldPreviewAsp, asp);
            }
        }

        int width = (int) CommonStyleUtils.previewWidth;
        int height = (int) CommonStyleUtils.previewHeight;
        //容器的size
        CommonStyleUtils.init(newWidth, newHeight);
        //字幕
        //根据新的宽高，重新修正字幕config
//        fixConfig(SubUtils.getInstance().getDBStyleInfos());
        fixConfig(SubUtils.getInstance().getStyleInfos());

        //贴纸
        //根据新的宽高，重新修正贴纸config
        fixConfig(StickerUtils.getInstance().getStyleInfos());
        fixConfig(StickerUtils.getInstance().getDBStyleInfos());
        //改变播放器大小，修正贴纸
        ArrayList<StickerInfo> list = dataHandler.getParam().getStickerList();
        if (list != null && list.size() > 0) {
            for (StickerInfo stickerInfo : list) {
                RectF rectOriginal = stickerInfo.getRectOriginal();
                float mediaAsp = rectOriginal.width() / rectOriginal.height();
                rectOriginal.set(rectOriginal.left / width, rectOriginal.top / height,
                        rectOriginal.right / width, rectOriginal.bottom / height);
                RectF showRectF = correctionRatio(rectOriginal, mediaAsp, oldPreviewAsp, asp);
                showRectF.set(showRectF.left * newWidth, showRectF.top * newHeight,
                        showRectF.right * newWidth, showRectF.bottom * newHeight);
                stickerInfo.setRectOriginal(showRectF);
                stickerInfo.setParent(newWidth, newHeight);
                stickerInfo.setPreviewAsp(asp);
            }
        }
        new StickerExportHandler(player.getContext(), list, newWidth, newHeight).export(null); //传null:当前贴纸不刷新，build时刷新

        //完成
        if (null != fixPreviewListener) {
            fixPreviewListener.onComplete();
        }
    }

    /**
     * 修正叠加比例
     *
     * @param oldPreviewAsp
     * @param asp
     * @param overLayList
     */
    public static void fixOverlayList(float oldPreviewAsp, float asp, List<CollageInfo> overLayList) {
        if (oldPreviewAsp != asp && null != overLayList) {
            //预览比例变化了，需要修正画中画位置
            int len = overLayList.size();
            for (int i = 0; i < len; i++) {
                CollageInfo collageInfo = overLayList.get(i);
                //媒体比例
                PEImageObject mediaObject = collageInfo.getImageObject();
                float mediaAsp;
                RectF clipRect = mediaObject.getClipRectF();
                if (clipRect == null || clipRect.isEmpty()) {
                    mediaAsp = mediaObject.getWidth() * 1.0f / mediaObject.getHeight();
                } else {
                    mediaAsp = clipRect.width() / clipRect.height();
                }
                RectF showRectF = correctionRatio(collageInfo.getImageObject().getShowRectF(), mediaAsp, oldPreviewAsp, asp);
                collageInfo.getImageObject().setShowRectF(showRectF);
            }
        }
    }


    /**
     * 修正比例
     *
     * @param oldRectF 原始显示区域0~1
     * @param mediaAsp 原始的比例 媒体的
     * @param oldAsp   旧的比例
     * @param newAsp   新比例
     * @return 新的显示区域
     */
    public static RectF correctionRatio(RectF oldRectF, float mediaAsp, float oldAsp, float newAsp) {
        if (oldRectF == null || oldRectF.isEmpty()) {
            return oldRectF;
        }
        //原始的比例
        float scale;
        if (oldAsp > mediaAsp) {
            scale = oldRectF.height();
        } else {
            scale = oldRectF.width();
        }

        //计算现在的区域
        RectF newRectF = new RectF();
        if (newAsp > mediaAsp) {
            newRectF.set(0, 0, mediaAsp / newAsp, 1);
        } else {
            newRectF.set(0, 0, 1, newAsp / mediaAsp);
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, newRectF.centerX(), newRectF.centerY());
        //中心偏移
        matrix.postTranslate(oldRectF.centerX() - newRectF.centerX(), oldRectF.centerY() - newRectF.centerY());
        matrix.mapRect(newRectF, newRectF);
        return newRectF;
    }


    /**
     * 不再使用
     */
    @Deprecated
    public static float getPlayerAsp(PEImageObject base) {
        RectF clip = base.getClipRectF();
        if (clip.isEmpty()) {
            return (float) base.getWidth() / base.getHeight();
        } else {
            return clip.width() / clip.height();
        }
    }


    /**
     * 修正显示位置： 依据媒体的裁剪比例，计算显示位置
     *
     * @param previewAsp 播放器预览区域的比例
     */
    public static void fixMediaShowRectFByClip(PEImageObject mediaObject, float previewAsp) {
        RectF rectF = mediaObject.getClipRectF();
        float asp;
        if (null == rectF || rectF.isEmpty()) {
            asp = mediaObject.getWidth() / (mediaObject.getHeight() + .0f);
        } else {
            asp = rectF.width() / rectF.height();
        }
        RectF showRectF = new RectF();
        MiscUtils.fixShowRectF(asp, 1000, (int) (1000 / (previewAsp)), showRectF);
        mediaObject.setShowRectF(null);
    }


    /**
     * 比例发生了变化
     *
     * @param newAsp
     * @param oldAsp
     * @return
     */
    public static boolean proportionChanged(float newAsp, float oldAsp) {
        return Math.abs(newAsp - oldAsp) > 0.01f;
    }
}
