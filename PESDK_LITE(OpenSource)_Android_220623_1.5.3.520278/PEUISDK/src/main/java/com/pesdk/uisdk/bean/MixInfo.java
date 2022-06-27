package com.pesdk.uisdk.bean;

import com.pesdk.uisdk.bean.image.VirtualIImageInfo;

/**
 * 记录步骤: 图片合成
 */
public class MixInfo {


    private VirtualIImageInfo mInfo;
    private String path;//合成后的新文件

    public MixInfo(VirtualIImageInfo info, String path) {
        mInfo = info;
        this.path = path;
    }

    public VirtualIImageInfo getInfo() {
        return mInfo;
    }

    public String getPath() {
        return path;
    }

    public MixInfo copy() {
        return new MixInfo(mInfo.copy(), path);
    }

    @Override
    public String toString() {
        return "MixInfo{" +
                "hash=" + hashCode() +
                ",path='" + path +
                ",mInfo=" + mInfo +
                '}';
    }
}
