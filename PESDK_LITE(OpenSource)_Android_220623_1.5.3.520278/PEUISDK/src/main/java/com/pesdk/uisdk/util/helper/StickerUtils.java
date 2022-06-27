package com.pesdk.uisdk.util.helper;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.model.ApngInfo;
import com.pesdk.uisdk.bean.model.FrameInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.TimeArray;
import com.pesdk.uisdk.util.ApngExtractFrames;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.utils.MiscUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 贴纸相关辅助
 */
public class StickerUtils {

    /**
     * 获取
     */
    public StyleInfo getStyleInfo(int key, String resourceId) {
        try {
            StyleInfo info = null;
            if (list.size() != 0) {
                info = getStyleInfo(list, key, resourceId);
            }
            if (null == info) {
                if (downloaded.size() != 0) {
                    info = getStyleInfo(downloaded, key, resourceId);
                }
            }
            if (null != info) {
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 列表寻找
     */
    private StyleInfo getStyleInfo(ArrayList<StyleInfo> list, int styleId, String resourceId) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            StyleInfo temp = list.get(i);
            if (temp.pid == styleId) {
                return temp;
            }
            if (!TextUtils.isEmpty(resourceId) && resourceId.equals(temp.resourceId) && FileUtils.isExist(temp.mlocalpath)) {
                return temp;
            }
        }
        return null;
    }


    public StyleInfo getStyleInfo(int key) {
        try {
            StyleInfo info = null;
            if (list.size() != 0)
                info = getIndex2(list, key);
            if (null == info) {
                if (downloaded.size() != 0) {
                    info = getIndex2(downloaded, key);
                }
            }
            if (null != info) {
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private StyleInfo getIndex2(ArrayList<StyleInfo> list, int styleId) {
        StyleInfo temp = null, result = null;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            temp = list.get(i);
            if (temp.pid == styleId) {
                result = temp;
                break;
            }

        }

        return result;
    }

    public ArrayList<StyleInfo> getStyleInfos() {
        return list;
    }

    public ArrayList<StyleInfo> getDBStyleInfos() {
        return downloaded;
    }

    public void clearArray() {
        list.clear();
    }

    private String TAG = "StickerUtils";

    public void putStyleInfo(StyleInfo info) {
        if (info == null)
            return;
        int i = 0;
        for (; i < list.size(); i++) {
            StyleInfo tmp = list.get(i);
            if (null != tmp && info.pid == tmp.pid) {
                list.set(i, info);
                break;
            }
        }
        if (i >= list.size()) {
            list.add(info);
        }
    }

    private static StickerUtils instance;

    public static StickerUtils getInstance() {
        if (null == instance) {
            instance = new StickerUtils();
        }
        return instance;
    }

    /**
     * Activity onDestory() 释放内存
     */
    public void recycle() {
        downloaded.clear();
        list.clear();
    }

    private StickerUtils() {

    }


    private static ArrayList<StyleInfo> list = new ArrayList<StyleInfo>(),
            downloaded = new ArrayList<StyleInfo>();

    /**
     * 自定义网络特效接口的数据
     *
     * @return
     */
    public ArrayList<StyleInfo> getStyleDownloaded() {
        getDownloadData();
        return downloaded;

    }

    private void getDownloadData() {
        downloaded.clear();
//        ArrayList<StyleInfo> dblist = StickerData.getInstance().getAll();
//        for (int i = 0; i < dblist.size(); i++) {
//            StyleInfo tempInfo = dblist.get(i);
//            if (!TextUtils.isEmpty(tempInfo.mlocalpath)) {
//                File f = new File(tempInfo.mlocalpath);
//                CommonStyleUtils.checkStyle(f, tempInfo);
//            }
//            tempInfo.st = CommonStyleUtils.STYPE.special;
//            downloaded.add(tempInfo);
//        }
//        dblist.clear();

    }


    /**
     * 把apng 解析成png 序列，并构造帧列表
     *
     * @param baseName
     * @param baseDir  当前贴纸的文件目录
     */
    public static void initApng(String baseName, File baseDir, StyleInfo info) {
        File srcApng = new File(baseDir, baseName + ".png");
        if (!srcApng.exists()) {
            //兼容
            srcApng = new File(baseDir, baseName + ".apng");
        }
        info.frameArray.clear();
        if (ApngExtractFrames.process(srcApng) > 0) { //优先检查是否是apng文件
            //apng 转png序列
            ApngInfo apng = ApngInfo.createApng(srcApng, baseName);
            //构造图片序列
            int len = apng.getFrameList().size();
            int itemDuration = MiscUtils.s2ms(apng.getItemDuration());
            for (int i = 0; i < len; i++) {
                int time = itemDuration * i;
                FrameInfo frameInfo = new FrameInfo();
                frameInfo.time = time;
                frameInfo.pic = apng.getFrameList().get(i);
                info.frameArray.put(time, frameInfo);
            }
            info.du = itemDuration * len;
        } else {
            //解析apng失败 ( 当前普通图片png处理)
            File dst = new File(baseDir, baseName + "0.png");
            FileUtils.syncCopyFile(srcApng, dst, null);
            FrameInfo frameInfo = new FrameInfo();
            frameInfo.time = 0;
            frameInfo.pic = dst.getAbsolutePath();
            info.frameArray.put(0, frameInfo);
            info.du = 200;
        }
        if (info.timeArrays.isEmpty()) {
            //防止没有配置时间
            info.timeArrays.add(new TimeArray(0, info.du));
        }
    }

    /**
     * 读取size
     *
     * @param info
     */
    public static void readSize(StyleInfo info) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (info.frameArray.size() > 0) {
            String path = info.frameArray.valueAt(0).pic;
            if (FileUtils.isExist(path)) {
                BitmapFactory.decodeFile(path, options);
                info.w = options.outWidth;
                info.h = options.outHeight;
            }
        }
    }


}
