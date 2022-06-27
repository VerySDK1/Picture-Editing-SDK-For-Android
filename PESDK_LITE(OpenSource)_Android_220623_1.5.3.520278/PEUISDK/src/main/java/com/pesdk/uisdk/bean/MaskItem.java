package com.pesdk.uisdk.bean;

import com.pesdk.uisdk.fragment.mask.MaskRender;

import androidx.annotation.DrawableRes;

public class MaskItem {

    private String mName;
    private String url;
    private String icon;
    @DrawableRes
    private int mDrawbleId;//本地资源
    private String localpath;
    private int mMaskId;
    private MaskRender mMaskRender;


    public MaskItem(String name, int drawbleId) {
        mName = name;
        mDrawbleId = drawbleId;
    }

    public MaskItem(String name, String url, String icon) {
        mName = name;
        this.url = url;
        this.icon = icon;
    }


    public String getName() {
        return mName;
    }

    public String getUrl() {
        return url;
    }

    public String getIcon() {
        return icon;
    }

    public int getMaskId() {
        return mMaskId;
    }

    public void setMaskId(int maskId) {
        mMaskId = maskId;
    }


    public void setMaskRender(MaskRender maskRender) {
        mMaskRender = maskRender;
    }

    public MaskRender getMaskRender() {
        return mMaskRender;
    }


    public String getLocalpath() {
        return localpath;
    }

    public int getDrawbleId() {
        return mDrawbleId;
    }


    public void setLocalpath(String localpath) {
        this.localpath = localpath;
    }

}
