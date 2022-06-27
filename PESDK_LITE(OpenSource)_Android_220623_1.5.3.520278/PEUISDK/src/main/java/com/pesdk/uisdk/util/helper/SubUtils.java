package com.pesdk.uisdk.util.helper;

import android.content.Context;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.util.AppConfiguration;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * 文字
 */
public class SubUtils {
    private static final String TAG = "SubUtils";

    public String getAssetSmapleLocalPath() {
        return mAssetSmapleLocalPath;
    }

    //默认的内置资源是否存在
    private String mAssetSmapleLocalPath;
    public static final String DEFAULT_STYLE_CODE = "text_sample";


    /**
     * 默认字幕id
     */
    private static final long LAST_UPDATE_TIME = new BigDecimal("1568603370000").longValue();
    public static final int DEFAULT_ID = "text_sample".hashCode();
    public static final String DEFAULT_RESOURCE = "1000001";
    public static final String DEFAULT_CATEGORY = "16239885";

    /**
     * @param key
     * @return
     */
    public StyleInfo getStyleInfo(int key) {
        if (sArray.size() != 0) {
            return getIndex2(sArray, key);
        }
        return null;
    }

    /**
     * 资源id获取样式 没有保存数据库
     *
     * @param resourceId id
     */
    public StyleInfo getStyleInfo(String resourceId) {
        if (TextUtils.isEmpty(resourceId)) {
            return null;
        }
        try {
            if (sArray.size() != 0) {
                for (StyleInfo styleInfo : sArray) {
                    if (resourceId.equals(styleInfo.resourceId) && FileUtils.isExist(styleInfo.mlocalpath)) {
                        return styleInfo;
                    }
                }
            }
//            if (mStyleDownList.size() != 0) {
//                for (StyleInfo styleInfo : mStyleDownList) {
//                    if (resourceId.equals(styleInfo.resourceId) && FileUtils.isExist(styleInfo.mlocalpath)) {
//                        return styleInfo;
//                    }
//                }
//            }
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
        if (result == null) {
            return SubUtils.getInstance().initDefaultStyle();
        } else {
            return result;
        }
    }

    /**
     * @return
     */
    public ArrayList<StyleInfo> getStyleInfos() {
        return sArray;
    }

    public void putStyleInfo(StyleInfo info) {
        //强制替换
        sArray.add(info);
    }

    public void replaceOAdd(StyleInfo info) {
        if (null != info) {
            boolean isReplace = false;
            for (int i = 0; i < sArray.size(); i++) {
                StyleInfo tmp = sArray.get(i);
                if (info.pid == tmp.pid) {
                    sArray.set(i, info);
                    isReplace = true;
                    break;
                }
            }
            if (!isReplace) {
                sArray.add(info);
            }
        }
    }


    private static SubUtils instance;

    public static SubUtils getInstance() {
        if (null == instance) {
            instance = new SubUtils();
        }
        return instance;
    }

    private SubUtils() {

    }

    private static ArrayList<StyleInfo> sArray = new ArrayList<StyleInfo>();

    //用以获取前面两个两样的pid
    public int getArraykey(int index) {
        return sArray.get(index).pid;
    }

    /**
     * Activity onDestory() 释放内存
     */
    public void recycle() {
        sArray.clear();
        StyleInfo tmp = initDefaultStyle();
        if (null != tmp) {
            putStyleInfo(tmp);
        }
    }


    /***
     * 导出默认的内置字幕
     * @param context
     */
    public void exportDefault(Context context) {
        mAssetSmapleLocalPath = null;
        String src = DEFAULT_STYLE_CODE + ".zip";
        if (FileUtils.isExist(context, "asset:///" + src)) {
            String path = PathUtils.getSubPath();
            String fileName = DEFAULT_STYLE_CODE;
            File dirTarget = new File(path, fileName); //解压后的目录
            File fconfig = new File(dirTarget, CommonStyleUtils.CONFIG_JSON);
            if (fconfig.exists()) {
                mAssetSmapleLocalPath = dirTarget.getAbsolutePath();
            } else {
                try {
                    File dstZip = new File(path, src);
                    CoreUtils.assetRes2File(context.getAssets(), src, dstZip.getAbsolutePath());
                    dirTarget = new File(FileUtils.unzip(dstZip.getAbsolutePath(), new File(path).getAbsolutePath()));
                    mAssetSmapleLocalPath = dirTarget.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        Log.e(TAG, "exportDefault: " + mAssetSmapleLocalPath + " >" + FileUtils.isExist(mAssetSmapleLocalPath));
    }

    /**
     * 构建默认的字幕样式(防止所有字幕样式未下载)
     */
    public StyleInfo initDefaultStyle() {
        if (FileUtils.isExist(mAssetSmapleLocalPath)) {
            StyleInfo styleInfo = new StyleInfo(true);
            initStyle(styleInfo, mAssetSmapleLocalPath);
            return styleInfo;
        }
        return null;
    }

    public void initStyle(StyleInfo styleInfo, String path) {
        styleInfo.isdownloaded = true;
        styleInfo.mlocalpath = path;
        CommonStyleUtils.checkStyle(new File(path), styleInfo);
    }


    /**
     * 初始化默认字幕
     */
    public StyleInfo getDefault() {
        StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(DEFAULT_RESOURCE);
        if (styleInfo == null) {
            Context context = AppConfiguration.getContext();
            String path = PathUtils.getAssetPath();
            String fileName = "text_sample";
            File dstZip = new File(path, fileName + ".zip");
            String dstZipPath = PathUtils.getFilePath(dstZip);
            if (dstZip.exists()) {
                if (dstZip.lastModified() <= LAST_UPDATE_TIME) {
                    dstZip.delete();
                    CoreUtils.assetRes2File(context.getAssets(), fileName + ".zip", dstZipPath);
                }
            } else {
                CoreUtils.assetRes2File(context.getAssets(), fileName + ".zip", dstZipPath);
            }
            File dirTarget = new File(path, fileName);
            String unzip = null;
            if (dirTarget.exists()) {
                if (dirTarget.lastModified() <= LAST_UPDATE_TIME) {
                    dirTarget.delete();
                    try {
                        unzip = com.vecore.base.lib.utils.FileUtils.unzip(dstZipPath, PathUtils.getFilePath(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    unzip = com.vecore.base.lib.utils.FileUtils.unzip(dstZipPath, PathUtils.getFilePath(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!TextUtils.isEmpty(unzip)) {
                dirTarget = new File(unzip);
            }
            styleInfo = new StyleInfo(true);
            styleInfo.pid = DEFAULT_ID;
            File config = new File(dirTarget, CommonStyleUtils.CONFIG_JSON);
            styleInfo.mlocalpath = PathUtils.getFilePath(dirTarget);
            styleInfo.resourceId = DEFAULT_RESOURCE;
            styleInfo.category = DEFAULT_CATEGORY;
            CommonStyleUtils.getConfig(config, styleInfo);
//            SubData.getInstance().replace(styleInfo);
        }
        return styleInfo;
    }

}
