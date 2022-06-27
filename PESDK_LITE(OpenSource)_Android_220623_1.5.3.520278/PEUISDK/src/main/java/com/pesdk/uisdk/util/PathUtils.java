package com.pesdk.uisdk.util;


import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.vecore.base.http.MD5;

import java.io.File;

public class PathUtils extends com.pesdk.utils.PathUtils {

    private static final String EXTENSION = "png"; //固定输出为png,支持透明

    public static String createDisplayName() {
        return getDisplayName("IMG", EXTENSION);
    }

    private static String getDCIM() {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "pe").getAbsolutePath();
    }

    /**
     * 图片
     */
    private static String getIMGFileNameForDCIM() {
        String rootPath = getDCIM();
        String path = getTempFileNameForSdcard(rootPath, "IMG", EXTENSION);
        deleteNoMedia(new File(rootPath));
        return path;
    }

    /**
     * 获取文件的输出路径
     */
    public static String getDstFilePath(String dir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //android 10+ 输出到/Android/data/package/files/** 下，
            return com.pesdk.utils.PathUtils.getTempFileNameForSdcard("IMG", EXTENSION);
        } else { // 10 以下，任意目录
            if (!TextUtils.isEmpty(dir)) {
                return getTempFileNameForSdcard(dir, "IMG", EXTENSION, true);
            } else {
                return getIMGFileNameForDCIM();
            }
        }
    }

    /**
     * 滤镜
     */
    public static String getFilterFile(String url) {
        return PathUtils.getFilterPath() + "/" + MD5.getMD5(url) + ".png";
    }

    public static String getFilterDir(String url, String name) {
        return PathUtils.getFilterPath() + "/" + MD5.getMD5(url + name);
    }

    public static String getFilterZip(String url) {
        return PathUtils.getFilterPath() + "/" + MD5.getMD5(url) + ".zip";
    }

    /**
     * 叠加
     */
    public static String getOverlayFile(String url) {
        return PathUtils.getOverlayPath() + "/" + MD5.getMD5(url) + ".png";
    }

    /**
     * 边框
     */
    public static String getFrameFile(String url) {
        return PathUtils.getFramePath() + "/" + MD5.getMD5(url) + ".png";
    }


    /**
     * 字体
     */
    public static String getTTFFile(String url) {
        return PathUtils.getTtfPath() + "/" + MD5.getMD5(url) + ".ttf";
    }


    /**
     * 背景
     */
    public static String getBGFile(String url) {
        return PathUtils.getBGPath() + "/" + MD5.getMD5(url) + ".jpg";
    }

    /**
     * 天空
     */
    public static String getSkyFile(String url) {
        return PathUtils.getSkyPath() + "/" + MD5.getMD5(url) + ".jpg";
    }

    /**
     * 单个花字的解压目录
     */
    public static String getFlowerChildDir(String url) {
        return new File(PathUtils.getFlower(), Integer.toString(url.hashCode())).getAbsolutePath();
    }

    /**
     * 单个蒙版
     */
    public static String getMaskChildDir(String url) {
        return new File(PathUtils.getMask(), Integer.toString(url.hashCode())).getAbsolutePath();
    }


    public static String getStickerChildDir(String url) {
        return new File(PathUtils.getStickerPath(), Integer.toString(url.hashCode())).getAbsolutePath();
    }

    /**
     * 头发
     */
    public static String getHairChildDir(String url) {
        return new File(PathUtils.getHairPath(), Integer.toString(url.hashCode())).getAbsolutePath();
    }

    /**
     * 换装-单文件
     */
    @Deprecated
    public static String getClothesItem(String url) {
        return new File(PathUtils.getClothesPath(), Integer.toString(url.hashCode()) + ".png").getAbsolutePath();
    }

    /**
     * 换装zip文件
     *
     * @param url
     * @return
     */
    public static String getClothesChildDir(String url) {
        return new File(PathUtils.getClothesPath(), Integer.toString(url.hashCode())).getAbsolutePath();
    }


    public static String getSubChildDir(String url) {
        return new File(PathUtils.getSubPath(), Integer.toString(url.hashCode())).getAbsolutePath();
    }

    public static boolean isDownload(String path) {
        return !TextUtils.isEmpty(path) && new File(path).exists();
    }


    /**
     * 保留一张截图
     */
    public static String getMediaNail(String path) {
        File rootPath = new File(PathUtils.getTempPath(), MD5.getMD5(path) + ".png");
        return getFilePath(rootPath);
    }

    /**
     * 下载压缩包
     */
    public static String getDownloadZip(String name) {
        if (name == null) {
            name = Utils.getUUID();
        }
        File file = new File(PathUtils.getDownLoadDirName(), name.hashCode() + ".zip");
        return PathUtils.getFilePath(file);
    }
}
