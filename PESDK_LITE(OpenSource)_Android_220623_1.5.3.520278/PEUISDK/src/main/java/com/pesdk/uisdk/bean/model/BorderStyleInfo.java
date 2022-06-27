package com.pesdk.uisdk.bean.model;

import com.pesdk.bean.SortBean;

/**
 * 边框样式
 */
public class BorderStyleInfo {
    private SortBean mSortBean;


    public String name;//名字
    public String mUrl;//路径
    public String mIcon;//图标
    public String mLocalpath;//本地路径
    public long nTime = 0;//更新时间
    private int mId;

    public BorderStyleInfo(String name, String mUrl, String icon, String localpath) {
        this.name = name;
        mId = name.hashCode();
        this.mUrl = mUrl;
        this.mIcon = icon;
        this.mLocalpath = localpath;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setLocalpath(String localpath) {
        mLocalpath = localpath;
    }

    public String getLocalpath() {
        return mLocalpath;
    }


    public SortBean getSortBean() {
        return mSortBean;
    }

    public void setSortBean(SortBean sortBean) {
        mSortBean = sortBean;
    }

    @Override
    public String toString() {
        return "BorderStyleInfo{" +
                "mISortApi=" + mSortBean +
                ", name='" + name + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mIcon='" + mIcon + '\'' +
                ", mLocalpath='" + mLocalpath + '\'' +
                ", nTime=" + nTime +
                ", mId=" + mId +
                '}';
    }
}
