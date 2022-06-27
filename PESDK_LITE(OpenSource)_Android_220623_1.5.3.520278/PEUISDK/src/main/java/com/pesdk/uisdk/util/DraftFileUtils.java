package com.pesdk.uisdk.util;

import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.vecore.base.lib.utils.FileUtils;

import java.io.File;

/**
 * 虚拟图片存储到本地config.json中
 */
public class DraftFileUtils {
    private static final String VE_DRAFT_CONF = "config.json";

    private static final String TAG = "DraftFileUtils";


    /***
     * 写文件
     * @param info
     */
    public static void write2File(VirtualIImageInfo info) {
        File file = new File(info.getBasePath());
        File config = new File(file, VE_DRAFT_CONF);
        String json = info.toGSONString();
        FileUtils.writeText2File(json, config);
    }

    public static VirtualIImageInfo toShortInfo(String basePath) {
        File file = new File(basePath);
        File config = new File(file, VE_DRAFT_CONF);
        String content = FileUtils.readTxtFile(config);
        return VirtualIImageInfo.toShortInfo(content);
    }

}
