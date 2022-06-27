package com.pesdk.uisdk.bean.model.subtitle;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 字幕模板解析
 */
public class SubTemplateManager {

    /**
     * 配置文件名称
     */
    private final static String CONFIG = "config.json";

    private static volatile SubTemplateManager sInstance;

    private SubTemplateManager() {
    }

    public static SubTemplateManager getInstance() {
        if (sInstance == null) {
            sInstance = new SubTemplateManager();
        }
        return sInstance;
    }


    /**
     * 存放id
     */
    private final HashMap<String, SubTemplateInfo> mParsingData = new HashMap<>();

    /**
     * config.json 目录
     */
    public SubTemplateInfo parsingConfig(String path) {

        //null
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        //已经解析过
        Set<Map.Entry<String, SubTemplateInfo>> entrySet = mParsingData.entrySet();
        for (Map.Entry<String, SubTemplateInfo> entry : entrySet) {
            if (entry.getKey().equals(path)) {
                return entry.getValue().copy();
            }
        }

        //读取 config.json
        String configPath = PathUtils.getConfigPath(path, CONFIG);
        if (configPath == null) {
            return null;
        }
        File config = new File(configPath);
        String content = FileUtils.readTxtFile(config);
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        //解析
        Gson gson = com.pesdk.uisdk.gson.Gson.getInstance().getGson();
        SubTemplateInfo info = gson.fromJson(content, new TypeToken<SubTemplateInfo>() {
        }.getType());

        //本地路径
        String localPath = config.getParent();

        //判断是否有动画
        SubText[] text = info.getText();
        if (text != null && text.length > 0) {
            for (SubText subText : text) {
                subText.setLocalPath(localPath);
                SubTextAnim[] animList = subText.getAnim();
                if (animList != null && animList.length > 0) {
                    for (SubTextAnim anim : animList) {
                        String zipName = anim.getAnimResource();
                        if (TextUtils.isEmpty(zipName)) {
                            continue;
                        }

                        //解压后
                        int endIndex = zipName.lastIndexOf(".");
                        String name;
                        if (endIndex > 0) {
                            name = zipName.substring(0, endIndex);
                        } else {
                            name = zipName;
                        }

                        //解压
                        File animFile = new File(localPath, name);
                        if (!FileUtils.isExist(animFile)) {
                            String animZipPath = PathUtils.getFilePath(localPath, zipName);
                            try {
                                FileUtils.unzip(animZipPath, localPath);
                            } catch (IOException e) {
                                continue;
                            }
                            FileUtils.deleteAll(animZipPath);
                        }

//                        //注册动画
//                        AnimInfo animInfo = new AnimInfo(name, null, 0, null);
//                        animInfo.setLocalFilePath(PathUtils.getFilePath(animFile));
//                        if (TemplateUtils.registeredAnim(animInfo)) {
//                            anim.setAnimInfo(animInfo);
//                        }
                    }
                }
            }
        }

        info.setLocalPath(localPath);
        mParsingData.put(path, info.copy());
        return info;
    }

}
