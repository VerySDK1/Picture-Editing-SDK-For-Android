package com.pesdk.uisdk.bean.image;

import com.pesdk.api.IVirtualImageInfo;

/**
 *
 */
public class IImage implements IVirtualImageInfo {

    /**
     * 信息类型：普通编辑
     */
    @Deprecated
    public static final byte INFO_TYPE_NORMAL_1 = 0;// 旧版太多忽略
    public static final byte INFO_TYPE_NORMAL = 2;
    /**
     * 信息类型： 草稿箱视频
     */
    public static final byte INFO_TYPE_DRAFT = 1;


    public static final int VERSION = 2; //第一版：支持json存储数据


    public static final int ERROR_DB_ID = -1;


    int ver = VERSION;


    //数据库中的唯一Id
    int nId = ERROR_DB_ID;
    String basePath = null;
    long nCreateTime; //创建时间
    long mUpdateTime;//更新时间
    String mCover;
    int nDaftType = INFO_TYPE_NORMAL;
    boolean mIsUpload = false;//上传更新

    public String getBasePath() {
        return basePath;
    }

    @Override
    public int getId() {
        return nId;
    }

    @Override
    public long getCreateTime() {
        return nCreateTime;
    }

    @Override
    public long getUpdateTime() {
        return mUpdateTime;
    }


    public void setUpdateTime(long updateTime) {
        this.mUpdateTime = updateTime;
        mIsUpload = true;
    }


    public int getVer() {
        return ver;
    }


    public void setId(int nId) {
        this.nId = nId;
    }


    @Override
    public String getCover() {
        return mCover;
    }

    public int getDraftType() {
        return nDaftType;
    }

    @Override
    public String toString() {
        return "IImage{" +
                "nId=" + nId +
                ", basePath='" + basePath + '\'' +
                ", nCreateTime=" + nCreateTime +
                ", mUpdateTime=" + mUpdateTime +
                ", mCover='" + mCover + '\'' +
                ", nDaftType=" + nDaftType +
                ", mIsUpload=" + mIsUpload +
                '}';
    }
}
