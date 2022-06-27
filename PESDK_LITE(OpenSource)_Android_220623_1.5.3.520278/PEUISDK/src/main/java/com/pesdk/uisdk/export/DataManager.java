package com.pesdk.uisdk.export;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.analyzer.SkyAnalyzerManager;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.util.helper.FilterUtil;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.vecore.VirtualImage;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.EffectInfo;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DataManager {
    private static final String TAG = "DataManager";


    private static PEImageObject createBg(int color) {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bitmap);
        cv.drawColor(color);

        try {
            PEImageObject peImageObject = new PEImageObject(bitmap);
            peImageObject.setShowRectF(new RectF(-0.05f, -0.05f, 1.05f, 1.05f)); //图片完全铺满view
            return peImageObject;
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        } finally {
            bitmap.recycle();
        }
    }

    /**
     * 加载全部数据
     * 预览（预览时从EditDataHandler中获取全部对象，避免草稿造成数据延迟）
     *
     * @param virtualImage
     * @param export
     * @param editDataHandler
     * @return 当前边框比例
     */
    public static float loadData(VirtualImage virtualImage, boolean export, EditDataHandler editDataHandler) {
        virtualImage.reset();
        //注意背景色
        ExtImageInfo tmp = editDataHandler.getExtImage();
//        Log.e(TAG, "loadData: " + tmp);
        if (tmp.getBackgroundColor() != PEScene.UNKNOWN_COLOR) { //纯色背景
            PEImageObject peImageObject = createBg(tmp.getBackgroundColor());
            if (null != peImageObject) {
                PEScene dst = new PEScene();
                dst.setPEImage(peImageObject);
                virtualImage.setPEScene(dst);
            }
        } else if (null != tmp.getBackground()) { //背景图
            PEScene dst = new PEScene();
            if (export) {//导出和预览对象完全分开
                dst.setPEImage(tmp.getBackground().copy());
            } else {
                dst.setPEImage(tmp.getBackground());
            }
            virtualImage.setPEScene(dst);
        }

        addEffect(virtualImage, editDataHandler, null);

        BuildHelper buildHelper = new BuildHelper(virtualImage);
        return buildHelper.load(export, editDataHandler);
    }


    /**
     * 导出草稿
     */
    public static float loadData(VirtualImage virtualImage, VirtualIImageInfo virtualIImageInfo) {
        virtualImage.reset();
        ExtImageInfo tmp = virtualIImageInfo.getExtImageInfo();
        if (tmp.getBackgroundColor() != PEScene.UNKNOWN_COLOR) { //纯色背景
            PEImageObject peImageObject = createBg(tmp.getBackgroundColor());
            if (null != peImageObject) {
                PEScene dst = new PEScene();
                dst.setPEImage(peImageObject);
                virtualImage.setPEScene(dst);
            }
        } else if (null != tmp.getBackground()) { //背景图
            PEScene dst = new PEScene();
            dst.setPEImage(tmp.getBackground().copy());
            virtualImage.setPEScene(dst);
        }


        addEffect(virtualImage, null, tmp);
        BuildHelper buildHelper = new BuildHelper(virtualImage);
        return buildHelper.load(virtualIImageInfo);
    }

    /**
     * 滤镜、调色
     *
     * @param virtualImage
     * @param editDataHandler
     * @param imageInfo
     */
    private static void addEffect(VirtualImage virtualImage, EditDataHandler editDataHandler, ExtImageInfo imageInfo) {
        List<EffectInfo> effectInfos = null;
        if (null != editDataHandler) {
            effectInfos = editDataHandler.getEffectList();
        } else if (null != imageInfo) {
            effectInfos = FilterUtil.getFilterList(imageInfo.getFilter(), imageInfo.getAdjust());
        }
        if (null == effectInfos || effectInfos.size() == 0) {
            return;
        }
        for (EffectInfo info : effectInfos) {
            try {
                virtualImage.addEffect(info);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 绑定画中画抠图对象
     */
    public static void extraCollageInsert(CollageInfo collageInfo, boolean export) {
        PEImageObject object = collageInfo.getImageObject();
        if (object.getTag() instanceof ImageOb) {
            ImageOb imageOb = (ImageOb) object.getTag();
            if (imageOb.isPersonSegment()) {
                AnalyzerManager.getInstance().extraCollageInsert(collageInfo);
            } else if (imageOb.isSkySegment()) {
                SkyAnalyzerManager.getInstance().extraCollageInsert(collageInfo);
            }
        }
    }

    /**
     * 移除画中画抠图
     */
    public static void extraCollageRemove(CollageInfo collageInfo, boolean export) {
        PEImageObject object = collageInfo.getImageObject();
        if (object.getTag() instanceof ImageOb) {
            ImageOb imageOb = PEHelper.initImageOb(object);
            if (imageOb.isPersonSegment()) {
                AnalyzerManager.getInstance().extraCollageRemove(collageInfo);
            } else if (imageOb.isSkySegment()) {
                SkyAnalyzerManager.getInstance().extraCollageRemove(collageInfo);
            }
        }
    }

    /**
     * 添加特效到虚拟视频
     */
    public static void loadEffects(VirtualImage virtualImage, List<EffectInfo> list) {
        if (virtualImage != null && null != list && list.size() > 0) {
            try {
                for (EffectInfo effectInfo : list) {
                    virtualImage.addEffect(effectInfo);
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 实时插入、更新 画中画(必须暂停状态下)
     */
    public static void upInsertCollage(VirtualImage virtualImage, CollageInfo info) {
        if (null != info && virtualImage != null) {
            extraCollageInsert(info, false);

            if (null != info.getBG()) { //先背景,后前景
                virtualImage.updatePIPMediaObject(info.getBG());
            }
            virtualImage.updatePIPMediaObject(info.getImageObject());
        }
    }

    /**
     * 移除单个画中画
     */
    public static void removeCollage(VirtualImage virtualImage, CollageInfo info) {
        if (null != info && virtualImage != null) {
            extraCollageRemove(info, false);
            if (null != info.getBG()) {
                virtualImage.deletePIPMediaObject(info.getBG());
            }
            virtualImage.deletePIPMediaObject(info.getImageObject());
        }
    }

    /**
     * 移出画中画
     */
    public static void removeCollage(VirtualImage virtualImage, ArrayList<CollageInfo> infoList) {
        if (virtualImage != null) {
            //移出
            if (infoList != null && infoList.size() > 0) {
                for (CollageInfo info : infoList) {
                    extraCollageRemove(info, false);
                    if (null != info.getBG()) {
                        virtualImage.deletePIPMediaObject(info.getBG());
                    }
                    virtualImage.deletePIPMediaObject(info.getImageObject());
                }
            }
        }
    }

    /**
     * 实时更新画中画
     */
    public static void updateCollage(VirtualImage virtualImage, ArrayList<CollageInfo> infoList) {
        if (null != infoList && infoList.size() > 0 && virtualImage != null) {
            int size = infoList.size() - 1;
            for (int i = 0; i <= size; i++) {
                CollageInfo info = infoList.get(i);
                extraCollageInsert(info, false);

                if (null != info.getBG()) {//先背景,后前景
                    virtualImage.updatePIPMediaObject(info.getBG());
                }
                virtualImage.updatePIPMediaObject(info.getImageObject(), i >= size);
            }
        }
    }
}
