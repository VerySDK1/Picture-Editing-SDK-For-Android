package com.pesdk.uisdk.export;

import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.edit.bean.IParam;
import com.pesdk.uisdk.edit.bean.IUndo;
import com.vecore.VirtualImage;
import com.vecore.models.EffectInfo;
import com.vecore.models.caption.CaptionLiteObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 预览时： VirtualIImageInfo 不一定是真实的数据，数据有延迟（原因:保存草稿使用了线程 ，所以预览时直接取 EditDataHandler 中的数据）
 */
public class BuildHelper {


    private VirtualImage mVirtualImage;

    public BuildHelper(VirtualImage virtualImage) {
        mVirtualImage = virtualImage;
    }

    /**
     * 草稿导出时
     */
    public float load(VirtualIImageInfo imageInfo) {
        return loadImp(false, mVirtualImage, imageInfo.getGraffitiList(), imageInfo.getStickerList(),
                imageInfo.getWordInfoList(),
                imageInfo.getCollageInfos(), imageInfo.getWatermark(), imageInfo.getEffectInfoList(), imageInfo.getBorderList(),
                imageInfo.getOverlayList());
    }

    /**
     * 加载数据
     *
     * @param export
     * @param dataHandler
     * @return
     */
    public float load(boolean export, EditDataHandler dataHandler) {
        if (export) { //导出时，完全隔离预览数据
            IUndo param = dataHandler.getUndo();
            return loadImp(true, mVirtualImage, param.getCloneGraffitiInfos(), param.getCloneStickerInfos(), param.getCloneWordNewInfos(),
                    param.getCloneCollageInfos(), null, param.getCloneEffects(), param.getCloneFrameInfos(), param.getCloneOverLayList());
        } else {
            IParam param = dataHandler.getParam();
            return loadImp(false, mVirtualImage, param.getGraffitList(), param.getStickerList(), param.getWordList(),
                    param.getCollageList(), null, dataHandler.getEffectAndFilter(), param.getFrameList(), param.getOverLayList());
        }
    }

    /**
     * @param export
     * @param virtualImage
     * @param graffitiInfos
     * @param stickerInfos
     * @param wordInfoExts
     * @param collageInfos
     * @param mWaterMark
     * @param effectInfos
     * @param frameInfoList
     * @param overlayList   叠加
     * @return 当前边框的比例
     */
    private float loadImp(boolean export, VirtualImage virtualImage, List<GraffitiInfo> graffitiInfos, List<StickerInfo> stickerInfos,
                          List<WordInfoExt> wordInfoExts, List<CollageInfo> collageInfos,
                          CollageInfo mWaterMark, List<EffectInfo> effectInfos,
                          List<FrameInfo> frameInfoList,
                          List<CollageInfo> overlayList) {
        float asp = 0;
        if (null != frameInfoList && frameInfoList.size() > 0) {
            asp = frameInfoList.get(0).getAsp();
        }

        //比例变化，需要强制修改素材
        if (null != graffitiInfos) {
            for (GraffitiInfo liteObject : graffitiInfos) {
                virtualImage.addCaptionLiteObject(liteObject.getLiteObject());
            }
        }
        if (null != stickerInfos) {
            for (StickerInfo info : stickerInfos) {
                if (null != info.getList()) {
                    for (CaptionLiteObject tmp : info.getList()) {
                        virtualImage.addCaptionLiteObject(tmp);
                    }
                }
            }
        }

        if (null != wordInfoExts) {
            for (WordInfoExt info : wordInfoExts) {
                virtualImage.addSubTemplate(info.getCaption());
            }
        }


        List<CollageInfo> tmp = new ArrayList<>();
        tmp.addAll(collageInfos); //图层
        AnalyzerManager.getInstance().extraCollage(collageInfos, export);


        if (null != mWaterMark) { //水印
            tmp.add(mWaterMark);
        }

        LayerManager.loadMix(virtualImage, frameInfoList, tmp, overlayList, export);
        DataManager.loadEffects(virtualImage, effectInfos);
        return asp;
    }


}
