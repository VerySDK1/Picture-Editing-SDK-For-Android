package com.pesdk.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.vecore.base.http.MD5;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PathUtils {
    private static String m_sRootPath;
    private static String m_sTempPath;
    private static String m_sDownLoad;
    private static String m_sStickerPath;
    private static String m_sBorderPath;
    private static String m_sSubPath;
    private static String m_sTtfPath;
    private static String m_sHairPath;
    private static String m_sClothesPath;
    private static String m_sFilter;
    private static String m_sOverlay;//叠加
    private static String m_sFrame;//边框
    private static String mAssetPath;
    private static String mMaskPath;
    private static String m_sTemplate;//模板目录
    private static String m_sFlower;//花字目录
    private static String m_sBGPath;//背景
    private static String m_sSkyPath;//天空
    private static String m_sDraft;//草稿箱
    private static String m_sDoodle;//涂鸦

    /**
     * 获取系统临时目录
     *
     * @return
     */
    public static final String getTempPath() {
        return m_sTempPath;
    }


    /**
     * 字体icon
     *
     * @return
     */
    public static final String getTtfPath() {
        return m_sTtfPath;
    }

    public static final String getDoodlePath() {
        return m_sDoodle;
    }

    /**
     *
     */
    public static final String getHairPath() {
        return m_sHairPath;
    }
    /**
     *
     */
    public static final String getClothesPath() {
        return m_sClothesPath;
    }


    private static String mCacheDir; //临时大图
    private static String mImportPath; //临时大图


    public static String getBGPath() {
        return m_sBGPath;
    }

    public static String getSkyPath() {
        return m_sSkyPath;
    }

    public static String getFlower() {
        return m_sFlower;
    }

    public static String getMask() {
        return mMaskPath;
    }

    /**
     * 解析文件存储路径
     *
     * @param context
     * @param path    根文件夹目录
     * @throws IllegalAccessException
     */
    @SuppressLint("NewApi")
    public static void initialize(Context context, File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != path) {
            do {
                File checkRootPath = context.getExternalFilesDir(null);
                if (checkRootPath != null && path.getAbsolutePath().startsWith(checkRootPath.getParent())) { //外置
                    break;
                }
                checkRootPath = context.getFilesDir();
                if (checkRootPath != null && path.getAbsolutePath().startsWith(checkRootPath.getParent())) { //内置
                    break;
                }
                int hasReadPermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                    throw new IllegalAccessError("Can`t get WRITE_EXTERNAL_STORAGE permission. " + path.getAbsolutePath());
                }
            } while (false);
        }

        if (null == path) {
            path = context.getExternalFilesDir("pe");
        }


        checkPath(path);
        m_sRootPath = path.toString();


        path = new File(m_sRootPath, "temp/");
        checkPath(path);
        m_sTempPath = path.toString();


        path = new File(m_sRootPath, "download/"); // internet file
        checkPath(path);
        m_sDownLoad = path.toString();


        path = new File(m_sRootPath, "sticker/");
        checkPath(path);
        m_sStickerPath = path.toString();

        path = new File(m_sRootPath, "subs/");
        checkPath(path);
        m_sSubPath = path.toString();

        path = new File(m_sRootPath, "ttf/");
        checkPath(path);
        m_sTtfPath = path.toString();

        path = new File(m_sRootPath, "hair/");
        checkPath(path);
        m_sHairPath = path.toString();

        path = new File(m_sRootPath, "clothes/");
        checkPath(path);
        m_sClothesPath = path.toString();


        path = new File(m_sRootPath, "filter/");
        checkPath(path);
        m_sFilter = path.toString();

        path = new File(m_sRootPath, "border/");
        checkPath(path);
        m_sBorderPath = path.toString();

        path = new File(m_sRootPath, "asset/");
        checkPath(path);
        mAssetPath = path.toString();


        path = new File(m_sRootPath, "template/");
        checkPath(path);
        m_sTemplate = path.toString();


        path = new File(context.getExternalCacheDir(), "lsTemp/"); //临时大图
        checkPath(path);
        mCacheDir = path.toString();

        {//清理网络化之前的蒙版
            path = new File(m_sRootPath, "mask/");
            if (path.exists()) {
                path.delete();
            }
        }
        path = new File(m_sRootPath, "pemask/");
        checkPath(path);
        mMaskPath = path.toString();


        path = new File(m_sRootPath, "compress/");
        checkPath(path);
        mImportPath = path.toString();

        path = new File(m_sRootPath, "flower/");
        checkPath(path);
        m_sFlower = path.toString();

        path = new File(m_sRootPath, "bg/");
        checkPath(path);
        m_sBGPath = path.toString();


        path = new File(m_sRootPath, "draft/");
        checkPath(path);
        m_sDraft = path.toString();

        path = new File(m_sRootPath, "overlay/"); //叠加
        checkPath(path);
        m_sOverlay = path.toString();

        path = new File(m_sRootPath, "frame/"); //边框
        checkPath(path);
        m_sFrame = path.toString();

        path = new File(m_sRootPath, "sky/");
        checkPath(path);
        m_sSkyPath = path.toString();

        path = new File(m_sRootPath, "doodle/");
        checkPath(path);
        m_sDoodle = path.toString();
    }

    private static final String NO_MEDIA = ".nomedia";

    /**
     * 检查path，如不存在创建之<br>
     * 并检查此路径是否存在文件.nomedia,如没有创建之
     */
    public static void checkPath(File path) {
        checkPath(path, false);
    }

    /**
     * 检查path，如不存在创建之<br>
     * 并检查此路径是否存在文件.nomedia,如没有创建之
     *
     * @param excludeNoMediaFile 是否需要检查排除.nomedia文件
     */
    public static void checkPath(File path, boolean excludeNoMediaFile) {
        if (!path.exists())
            path.mkdirs();
        File fNoMedia = new File(path, NO_MEDIA);
        if (excludeNoMediaFile) {
            if (fNoMedia.exists()) {
                fNoMedia.delete();
            }
        } else {
            if (!fNoMedia.exists()) {
                try {
                    fNoMedia.createNewFile();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 删除指定路径.nomedia文件
     */
    public static void deleteNoMedia(File path) {
        File fNoMedia = new File(path, NO_MEDIA);
        if (fNoMedia.exists()) {
            fNoMedia.delete();
        }
    }

    /**
     * 导入文件目录
     *
     * @return
     */
    public static final String getImportPath() {
        return mImportPath;
    }


    /**
     * 获取一个指定格式的临时文件
     */
    public static String getTempFileNameForSdcard(String strPrefix, String strExtension) {
        return getTempFileNameForSdcard(m_sTempPath, strPrefix, strExtension);
    }


    /**
     * 获取临时文件路径
     */
    public static String getTempFileNameForSdcard(String strRootPath, String strPrefix, String strExtension) {
        return getTempFileNameForSdcard(strRootPath, strPrefix, strExtension, false);
    }

    /**
     * 获取临时文件路径
     */
    public static String getTempFileNameForSdcard(String strRootPath, String strPrefix, String strExtension, boolean excludeNoMediaFile) {
        File rootPath = new File(strRootPath);
        checkPath(rootPath, excludeNoMediaFile);
        File localPath = new File(rootPath, String.format("%s_%s.%s", strPrefix, DateFormat.format("yyyyMMdd_kkmmss", new Date()), strExtension));
        return localPath.toString();
    }


    public static String getDownLoadDirName() {
        return m_sDownLoad;
    }

    /**
     * internet file on sdcard
     *
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getDownLoadFileNameForSdcard(String strPrefix, String strExtension) {
        File rootPath = new File(getDownLoadDirName());
        checkPath(rootPath);
        File localPath = new File(rootPath, String.format("%s_.%s", strPrefix, strExtension));
        return localPath.toString();
    }


    /**
     * 获取蒙版路径
     */
//    public static String getMaskPath(String name) {
//        if (!TextUtils.isEmpty(name)) {
//            return getFilePath(mMaskPath, String.valueOf(name.hashCode()));
//        } else {
//            return mMaskPath;
//        }
//    }

    /**
     * 清除临时文件
     */
    public static void clearTemp() {
        if (!TextUtils.isEmpty(m_sTempPath)) {
            clearTemp(m_sTempPath);
            clearTemp(mCacheDir);

        }
    }

    /**
     * 清理单个文件夹下所有文件
     *
     * @param path
     */
    private static void clearTemp(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles((dir, filename) ->
                    !TextUtils.equals(filename, NO_MEDIA));
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }
        }
    }


    public static final String TEMP_WORD = "word_", TEMP = "temp_", CACHE_MEDIA = "cache_media_";

    public static String getSubPath() {
        return m_sSubPath;
    }

    public static String getFilterPath() {
        return m_sFilter;
    }

    public static String getOverlayPath() {
        return m_sOverlay;
    }

    public static String getFramePath() {
        return m_sFrame;
    }

    /**
     * 获取贴纸目录
     */
    public static String getStickerPath() {
        return m_sStickerPath;
    }

    /**
     * 获取边框目录
     */
    public static String getBorderPath() {
        return m_sBorderPath;
    }


    public static String getFilePath(File file) {
        if (file == null) {
            return null;
        }
        String absPath;
        try {
            absPath = file.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
        return absPath;
    }


    public static String getCacheDir() {
        return mCacheDir;
    }


    /**
     * 获取目录路径   必须使用getCanonicalPath()，禁止使用getAbsolutePath()
     */
    public static String getFilePath(String dir, String name) {
        if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(name)) {
            return null;
        }
        return getFilePath(new File(dir, name));
    }

    public static String getFilePath(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        return getFilePath(new File(path));
    }

    /***
     * 获取一个大图片缩放文件路径
     */
    public static String getCacheMediaPath(int id, boolean isPNG) {
        return getTempFileNameForSdcard(CACHE_MEDIA + id, isPNG ? "png" : "jpg");
    }


    /**
     * 获取asset目录
     *
     * @return
     */
    public static String getAssetPath() {
        return mAssetPath;
    }


    public static String getDisplayName(String strPrefix, String strExtension) {
        return String.format("%s_%s.%s", strPrefix, DateFormat.format("yyyyMMdd_kkmmss", new Date()), strExtension);
    }


    /**
     * 模型
     */
    public static String getModelPath(String name) {
        return getFilePath(mAssetPath, name);
    }





    /*----------------------------------模板----------------------------------*/

    private static final String DIR_TEMPLATE_DOWN = "down";
    private static final String DIR_TEMPLATE_MIME = "mime";


    /**
     * 返回模板下载目录
     */
    public static String getDownTemplate() {
        return getFilePath(m_sTemplate, DIR_TEMPLATE_DOWN);
    }

    /**
     * 返回模板自己目录
     */
    public static String getMimeTemplate() {
        return getFilePath(m_sTemplate, DIR_TEMPLATE_MIME);
    }

    /**
     * 返回模板目录
     */
    public static String getDownTemplate(String name) {
        if (TextUtils.isEmpty(name)) {
            name = String.valueOf(System.currentTimeMillis());
        }
        //命名加上随机 template/...
        File file = new File(m_sTemplate, DIR_TEMPLATE_DOWN + "/" + name.hashCode());
        checkPath(file);
        return getFilePath(file);
    }

    /**
     * 返回模板目录
     */
    public static String getSaveTemplate(String name) {
        if (TextUtils.isEmpty(name)) {
            name = String.valueOf(System.currentTimeMillis());
        }
        //命名加上随机 template/...
        File file = new File(m_sTemplate, DIR_TEMPLATE_MIME + "/" + MD5.getMD5(name + System.currentTimeMillis()));
        checkPath(file);
        return getFilePath(file);
    }


    /**
     * 判断指定文件路径是否有效并存在
     */
    public static boolean fileExists(String strFilePath) {
        if (!TextUtils.isEmpty(strFilePath)) {
            return new File(strFilePath).exists();
        } else {
            return false;
        }
    }



    /*----------------------------------其他----------------------------------*/

    /**
     * 读取指定配置文件
     */
    public static String readFile(String path, String config) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        //是文件
        File file = new File(path);
        if (file.isFile()) {
            return FileUtils.readTxtFile(file);
        }

        //文件夹  扫描文件夹下 config
        String configPath = PathUtils.getConfigPath(path, config);
        if (configPath == null) {
            return null;
        }
        return FileUtils.readTxtFile(configPath);
    }

    /**
     * 返回指定配置的目录
     */
    public static String getConfigParentPath(String path, String config) {
        String configPath = getConfigPath(path, config);
        if (configPath != null) {
            return new File(configPath).getParent();
        }
        return null;
    }

    /**
     * 返回指定配置的目录
     */
    public static String getConfigPath(String path, String config) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (TextUtils.isEmpty(config)) {
            return path;
        }

        //文件
        File file = new File(path);
        if (file.isFile()) {
            return path;
        }

        //文件夹 扫描
        return scanConfigPath(file, config, 0);
    }

    /**
     * 当前目录是否存在
     */
    private static String scanConfigPath(File file, String config, int level) {

        if (file == null || TextUtils.isEmpty(config) || level >= 3) {
            return null;
        }

        //文件
        if (file.isFile()) {
            if (isContains(file.getName(), config)) {
                return getFilePath(file);
            }
            return null;
        }

        //文件夹
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            //优先扫描文件
            for (File f : files) {
                if (f.isFile()) {
                    if (isContains(f.getName(), config)) {
                        return getFilePath(f);
                    }
                }
            }
            //目录
            for (File f : files) {
                if (f.isDirectory()) {
                    String p = scanConfigPath(f, config, ++level);
                    if (!TextUtils.isEmpty(p)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 是否包含
     */
    private static boolean isContains(String src, String name) {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(name)) {
            return false;
        }
        return src.contains(name);
    }


    /**
     * 获取草稿箱的一个短视频目录
     */
    public static String getDraftPath(String type) {
        if (TextUtils.isEmpty(type)) {
            return m_sDraft;
        } else {
            File fileDraft = new File(m_sDraft, type);
            checkPath(fileDraft);
            return getFilePath(fileDraft);
        }
    }

    /**
     * 清理生成模板时构造的临时文件
     */
    public static void cleanUpMineTemplate() {
        ThreadPoolUtils.executeEx(() -> {
            String tmp = PathUtils.getMimeTemplate();
            File file = new File(tmp);
            if (null != file) {
                File[] fs = file.listFiles();
                if (null != fs && fs.length > 0) {
                    int len = fs.length;
                    for (int i = 0; i < len; i++) {
                        FileUtils.deleteAll(fs[i]);
                    }
                }
            }
        });
    }
}
