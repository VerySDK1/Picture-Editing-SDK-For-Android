package com.pesdk.uisdk.bean.model;

import com.pesdk.uisdk.fragment.main.IMenu;

import java.util.ArrayList;

public class UndoInfo {

    //操作的步骤 字幕、贴纸......
    @IMenu
    private int mMode; //类型
    //名字 删除、添加、调整
    private String mName;
    //现在的数据
    private ArrayList mList;
    //缩略图 （排除文字）
    private String mIcon;

    public UndoInfo(int mode, String name, ArrayList list) {
        this(mode, name, list, null);
    }

    public UndoInfo(int mode, String name, ArrayList list, String icon) {
        mMode = mode;
        mName = name;
        mList = list;
        mIcon = icon;
    }

    @IMenu
    public int getMode() {
        return mMode;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ArrayList getList() {
        return mList;
    }

    public String getIcon() {
        return mIcon;
    }

    @Override
    public String toString() {
        return "UndoInfo{" +
                "mMenu=" + mMode +
                ", mName=" + mName +
                ", mList=" + mList +
//                ", mIcon='" + mIcon + '\'' +
                '}';
    }
}
