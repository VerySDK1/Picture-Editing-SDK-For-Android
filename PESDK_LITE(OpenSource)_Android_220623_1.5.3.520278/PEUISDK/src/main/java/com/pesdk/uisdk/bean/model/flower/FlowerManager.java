package com.pesdk.uisdk.bean.model.flower;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.pesdk.uisdk.gson.Gson;
import com.pesdk.uisdk.util.PathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 花字管理
 */
public class FlowerManager {

    /**
     * 配置文件名称
     */
    private final static String CONFIG = "config.json";

    private static volatile FlowerManager sInstance;

    private FlowerManager() {
    }

    public static FlowerManager getInstance() {
        if (sInstance == null) {
            sInstance = new FlowerManager();
        }
        return sInstance;
    }

    /**
     * 存放id
     */
    private final HashMap<String, WordFlower> mParsingData = new HashMap<>();

    /**
     * config.json 目录
     */
    public WordFlower parsingConfig(String path) {

        //null
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        //已经解析过
        Set<Map.Entry<String, WordFlower>> entrySet = mParsingData.entrySet();
        for (Map.Entry<String, WordFlower> entry : entrySet) {
            if (entry.getKey().equals(path)) {
                return entry.getValue();
            }
        }

        //读取 config.json
        String content = PathUtils.readFile(path, CONFIG);
        if (TextUtils.isEmpty(content)) {
            content = PathUtils.readFile(path, ".json");
            if (TextUtils.isEmpty(content)) {
                return null;
            }
        }

        //解析
        com.google.gson.Gson gson = Gson.getInstance().getGson();
        WordFlower info = gson.fromJson(content, new TypeToken<WordFlower>() {
        }.getType());
        mParsingData.put(path, info);
        return info;
    }

}
