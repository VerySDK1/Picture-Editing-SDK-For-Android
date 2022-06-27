package com.pesdk.uisdk.bean.model;

public class CoverInfo {

    private String mPath;//地址
    private String mName;//名字

    public CoverInfo(String path, String name) {
        mPath = path;
        mName = name;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getName() {
        return mName;
    }

    public int getDrawableIcon() {
        return drawableIcon;
    }

    private int drawableIcon;

    public CoverInfo(String name, int drawableIcon) {
        mName = name;
        this.drawableIcon = drawableIcon;
    }

    public void setName(String name) {
        mName = name;
    }

}
