package com.pesdk.uisdk.util.helper;

import android.graphics.Rect;
import android.graphics.RectF;

import com.pesdk.uisdk.bean.MOInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.fragment.sticker.StickerExportHandler;
import com.vecore.BaseVirtual;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.DewatermarkObject;
import com.vecore.utils.MiscUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图层:更改比例，调整元素位置
 */
public class Fix {


    /**
     * @param width  播放器view的宽高
     * @param height
     * @param dstAsp 目标显示比例
     * @return 新的字幕容器size
     */
    public static VirtualImage.Size getVGroupSize(int width, int height, float dstAsp) {
        if (dstAsp > 0) {
            Rect rect = new Rect();
            MiscUtils.fixClipRect(dstAsp, width, height, rect);
            return new VirtualImage.Size(rect.width(), rect.height());
        } else {
            return new VirtualImage.Size(width, height);
        }
    }


    /**
     * 获取预览size
     *
     * @param maxWH  最大边
     * @param dstAsp 预览比例
     * @return
     */
    public static VirtualImage.Size getPreviewSize(int maxWH, float dstAsp) {
        if (dstAsp > 1) {
            return new VirtualImage.Size(maxWH, (int) (maxWH / dstAsp));
        } else {
            return new VirtualImage.Size((int) (maxWH * dstAsp), maxWH);
        }
    }


    /**
     * 修复单个元素位置
     *
     * @param oldPreviewAsp
     * @param previewSize      预览size ，虚拟的(非真实像素 ，只关注比例即可)
     * @param collageInfos
     * @param wordInfos
     * @param stickerInfos
     * @param vGroupSize       字幕容器的viewgroup宽高 真实像素
     * @param virtualImage
     * @param virtualImageView
     * @param watermark
     */
    public static void onFixPreviewDataSource(float oldPreviewAsp, BaseVirtual.Size previewSize, List<CollageInfo> collageInfos,
                                              List<WordInfoExt> wordInfos, List<StickerInfo> stickerInfos,
                                              BaseVirtual.Size vGroupSize, VirtualImage virtualImage,
                                              VirtualImageView virtualImageView, CollageInfo watermark) {

        int previewWidth = previewSize.width, previewHeight = previewSize.height;
        int vGroupWidth = vGroupSize.width, vGroupHeight = vGroupSize.height;

        float asp = previewWidth / (previewHeight + 0.0f);
        if (null != watermark) { //水印
            fixCollageItem(watermark, oldPreviewAsp, asp);
        }

        //修正画中画比例
        fixCollage(oldPreviewAsp, asp, collageInfos);
        {
            //马赛克|去水印
//             fixMO(oldPreviewAsp, asp, TempVideoParams.getInstance().getMosaicDuraionChecked(), previewWidth, previewHeight);
        }
        {

            //修正已经存在的字幕
//            ArrayList<WordInfo> wordInfos = TempVideoParams.getInstance().getWordInfos();
            fixCaptionList(virtualImage, virtualImageView, vGroupWidth, vGroupHeight, wordInfos);
        }

        {
            //贴纸
            {
                ArrayList<StyleInfo> tmp = StickerUtils.getInstance().getStyleInfos();
                if (null == tmp || tmp.size() == 0) { //防止从草稿箱->编辑，没有进入贴纸
                    //查询已下载的贴纸
//                    StickerUtils.getInstance().restoreDB();
                }
                //根据新的宽高，重新修正贴纸config
                fixConfig(StickerUtils.getInstance().getStyleInfos());
            }
            //改变播放器大小，修正贴纸
            new StickerExportHandler(virtualImageView.getContext(), stickerInfos, vGroupWidth, vGroupHeight).export(null);
        }
    }

    /**
     * 根据新的宽高，重新修正config
     *
     * @param tmp
     */
    private static void fixConfig(ArrayList<StyleInfo> tmp) {
        if (null != tmp && tmp.size() > 0) {
            for (StyleInfo item : tmp) {
                if (null != item) {
                    String path = item.mlocalpath;
                    if (FileUtils.isExist(path) && item.isdownloaded) {
                        CommonStyleUtils.checkStyle(new File(path), item);
                    }
                }
            }
        }
    }

    /**
     * 修复单个画中画的位置
     *
     * @param collageInfo
     * @param srcPreviewAsp
     * @param newPreviewAsp
     */
    public static void fixCollageItem(CollageInfo collageInfo, float srcPreviewAsp, float newPreviewAsp) {
        RectF rectF = collageInfo.getImageObject().getShowRectF(); //原始的显示位置
        RectF showRectF = fixPreviewRect(srcPreviewAsp, newPreviewAsp, new RectF(rectF));
        showRectF.offset(rectF.centerX() - showRectF.centerX(), rectF.centerY() - showRectF.centerY()); //保证中心点不变
        collageInfo.getImageObject().setShowRectF(showRectF);
    }

    /**
     * 预览size改变，调整水印位置，依据：（保证水印的centerF不变 ，区域的size不变，只是平移了而已）
     *
     * @param srcPreviewAsp 原始比例
     * @param newPreviewAsp 新的比例
     * @param collageInfos  画中画
     */
    private static void fixCollage(float srcPreviewAsp, float newPreviewAsp, final List<CollageInfo> collageInfos) {
        if (srcPreviewAsp != newPreviewAsp && null != collageInfos) {
            //预览比例变化了，需要修正画中画位置
            int len = collageInfos.size();
            for (int i = 0; i < len; i++) {
                CollageInfo collageInfo = collageInfos.get(i);
                fixCollageItem(collageInfo, srcPreviewAsp, newPreviewAsp);
            }
//            TempVideoParams.getInstance().setCollageList(collageInfos);
        }
    }

    /**
     * 修正去水印|马赛克显示位置
     *
     * @param srcPreviewAsp 原始的预览size
     * @param newPreviewAsp 新的预览size
     * @param list          要change的列表
     * @param width         新的预览宽
     * @param height        新的预览高
     */
    private static void fixMO(float srcPreviewAsp, float newPreviewAsp, List<MOInfo> list, int width, int height) {
        if (srcPreviewAsp != newPreviewAsp && null != list) {
            //预览比例变化了，需要修正 去水印|马赛克位置
            int len = list.size();
            ArrayList<MOInfo> tmp = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                MOInfo moInfo = list.get(i);
                RectF showRectF = fixPreviewRect(srcPreviewAsp, newPreviewAsp, new RectF(moInfo.getShowRectF()));
                moInfo.setShowRectF(showRectF);
                if (moInfo.getObject().getType() == DewatermarkObject.Type.mosaic || moInfo.getObject().getType() == DewatermarkObject.Type.blur) {
                    //主动修正容器大小
                    moInfo.getObject().setParentSize(width, height);
                    try {
                        //重新应用生成jni对象
                        moInfo.getObject().apply(false);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
                tmp.add(moInfo);
            }
//            TempVideoParams.getInstance().setMosaics(tmp);
        }
    }

    /**
     * 修正字幕
     *
     * @param virtualImage
     * @param player
     * @param playerWidth  新的显示宽
     * @param playerHeight 新的显示高
     * @param wordInfos
     */
    public static void fixCaptionList(VirtualImage virtualImage, VirtualImageView player, int playerWidth, int playerHeight, List<WordInfoExt> wordInfos) {

        //容器的size
        CommonStyleUtils.init(playerWidth, playerHeight);
        {
            //根据新的宽高，重新修正字幕config
//                fixConfig(SubUtils.getInstance().getDBStyleInfos());
            fixConfig(SubUtils.getInstance().getStyleInfos());
        }


        int size = wordInfos.size();
        for (int i = 0; i < size; i++) {
            WordInfoExt info = wordInfos.get(i);
//            info.setAnimList(null); //改变比例。清理动画
//            CaptionObject captionObject = info.getCaptionObject();
//            try {
//                captionObject.setVirtualVideo(virtualImage, player);
//            } catch (InvalidArgumentException e) {
//                e.printStackTrace();
//            }
//
//            StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(info.getStyleId());
//            if (null != styleInfo) {
//                //修正默认显示比例
//                captionObject.updateStyleDisf(styleInfo.disf);
//            }
//            //主动修正字幕容器大小
//            captionObject.setParentSize(playerWidth, playerHeight);
//            try {
//                //重新应用生成jni对象
//                captionObject.apply(false);
//                info.setList(info.getCaptionObject().getListPoint());
//            } catch (InvalidArgumentException e) {
//                e.printStackTrace();
//            }
        }
    }


    public static RectF fixPreviewRect(float srcPreviewAsp, float newPreviewAsp, RectF rectF) {
        return fixPreviewRect(srcPreviewAsp, newPreviewAsp, rectF, true);
    }

    /**
     * 切换比例修正目标显示位置
     *
     * @param srcPreviewAsp 原始的预览比例
     * @param newPreviewAsp 新的预览比例
     * @param rectF         原始的显示位置 0~1.0f
     * @param bFixBorder    true ：限制在0~1.0f ;false 允许越到预览区域外
     * @return 新的显示位置 0~1.0f
     */
    public static RectF fixPreviewRect(float srcPreviewAsp, float newPreviewAsp, RectF rectF, boolean bFixBorder) {
        //预览比例变化了，需要修正位置
        RectF previewRectF = new RectF();
        getPreviewSizeByAsp(srcPreviewAsp, previewRectF);
        float width = previewRectF.width();
        float height = previewRectF.height();

        float centerX = rectF.centerX(), centerY = rectF.centerY();

        //转化为像素
        rectF.left *= width;
        rectF.top *= height;

        rectF.right *= width;
        rectF.bottom *= height;


        //新的预览size
        previewRectF = new RectF();
        getPreviewSizeByAsp(newPreviewAsp, previewRectF);
        float previewWidth = previewRectF.width();
        float previewHeight = previewRectF.height();


        //中心点（像素）
        float centerXPx = centerX * previewWidth;
        float centerYPx = centerY * previewHeight;
        //中心点
        RectF showRectF = new RectF(centerXPx, centerYPx, centerXPx, centerYPx);


        showRectF.inset(-(rectF.width() / 2.0f), -(rectF.height() / 2.0f));


//        Log.e(TAG, "fixPreviewRect:  dst:" + showRectF);


        if (bFixBorder) {
            //防止越界到预览区域（previewWidth*previewHeight）外
            if (showRectF.left < 0) {
                showRectF.offset(-showRectF.left, 0);
            }
            if (showRectF.top < 0) {
                showRectF.offset(0, -showRectF.top);
            }
            if (showRectF.right > previewWidth) {
                showRectF.offset(previewWidth - showRectF.right, 0);
            }
            if (showRectF.bottom > previewHeight) {
                showRectF.offset(previewHeight - showRectF.bottom, 0);
            }
        }

        showRectF.left /= previewWidth;
        showRectF.top /= previewHeight;
        showRectF.right /= previewWidth;
        showRectF.bottom /= previewHeight;


        return showRectF;

    }

    private static final float ASP_43 = 4 / 3.0f;
    private static final float ASP_34 = 3 / 4.0f;

    /***
     * 根据比例获取一个预览尺寸
     * @param asp
     * @param rectF  获取显示像素
     */
    private static void getPreviewSizeByAsp(float asp, RectF rectF) {
        //640 无真实的意义（640仅参与运算，可以是 >100的任意数），最终绑定的依然是rectF（0~1.0f）
        if (asp >= ASP_43) {
            rectF.set(0, 0, 640, (640 / asp));
        } else if (asp <= ASP_34) {
            rectF.set(0, 0, 640 * asp, 640);
        } else {
            rectF.set(0, 0, 640, 640);
        }

    }

}
