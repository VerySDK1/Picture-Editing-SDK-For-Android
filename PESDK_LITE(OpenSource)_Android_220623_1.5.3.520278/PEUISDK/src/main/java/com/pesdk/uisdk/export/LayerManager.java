package com.pesdk.uisdk.export;

import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.vecore.VirtualImage;

import java.util.List;

/**
 * 图层、叠加manager
 */
public class LayerManager {
    private static final String TAG = "LayerManager";
    private static VirtualImage virtualImage;


    /***
     * @param image
     * @param frameInfos 边框
     * @param list  图层
     * @param overLayList  叠加
     */
    public static void loadMix(VirtualImage image, List<FrameInfo> frameInfos, List<CollageInfo> list, List<CollageInfo> overLayList, boolean export) {
        //画中画
        virtualImage = image;

        if (null != list) { //画中画
            for (CollageInfo info : list) {
                if (null != info.getBG()) {
                    virtualImage.addPIPMediaObject(info.getBG());
                }
                virtualImage.addPIPMediaObject(info.getImageObject());
                DataManager.extraCollageInsert(info, export);
            }
        }

        if (null != frameInfos) { //背景-边框
            for (FrameInfo tmp : frameInfos) {
                virtualImage.addPIPMediaObject(tmp.getPEImageObject());
            }
        }


        if (null != overLayList) { //叠加
            for (CollageInfo info : overLayList) {
                virtualImage.addPIPMediaObject(info.getImageObject());
            }
        }

    }

    /**
     * 实时插入媒体(必须暂停状态下)
     *
     * @param info
     */
    public static void insertCollage(CollageInfo info) {
        DataManager.upInsertCollage(virtualImage, info);
    }


    /***
     * 移除单个画中画
     * @param info
     */
    public static void remove(CollageInfo info) {
        DataManager.removeCollage(virtualImage, info);
    }

}
