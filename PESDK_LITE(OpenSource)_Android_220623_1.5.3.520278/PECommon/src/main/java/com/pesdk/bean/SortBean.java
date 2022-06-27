package com.pesdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.Keep;

@Keep
public class SortBean implements Parcelable, Serializable {

    /**
     * 分类
     * "id": "1001336",
     * "name": "人像",
     * "name_en": "Portrait",
     * "appkey": "init",
     * "icon": null,
     * "icon_checked": null,
     * "icon_unchecked": null,
     * "type": "filter2",
     * "updatetime": "1573096399"
     */
    private String id;
    private String name;
    private String nameEn;

    private String appkey;
    private String icon;
    private String iconChecked;
    private String iconUnchecked;
    private String type;
    private String updatetime;


    public List getDataList() {
        return mDataList;
    }

    public SortBean setDataList(List dataList) {
        mDataList = dataList;
        return this;
    }

    private List mDataList;

    public SortBean() {

    }

    //显示名字
    public SortBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    //显示图标
    public SortBean(String id, String iconChecked, String iconUnchecked) {
        this.id = id;
        this.iconChecked = iconChecked;
        this.iconUnchecked = iconUnchecked;
    }

    protected SortBean(Parcel in) {
        id = in.readString();
        name = in.readString();
        nameEn = in.readString();
        appkey = in.readString();
        icon = in.readString();
        iconChecked = in.readString();
        iconUnchecked = in.readString();
        type = in.readString();
        updatetime = in.readString();
    }

    public static final Creator<SortBean> CREATOR = new Creator<SortBean>() {
        @Override
        public SortBean createFromParcel(Parcel in) {
            return new SortBean(in);
        }

        @Override
        public SortBean[] newArray(int size) {
            return new SortBean[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if (!TextUtils.isEmpty(name)) {
            return name.trim();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconChecked() {
        return iconChecked;
    }

    public void setIconChecked(String iconChecked) {
        this.iconChecked = iconChecked;
    }

    public String getIconUnchecked() {
        return iconUnchecked;
    }

    public void setIconUnchecked(String iconUnchecked) {
        this.iconUnchecked = iconUnchecked;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(nameEn);
        dest.writeString(appkey);
        dest.writeString(icon);
        dest.writeString(iconChecked);
        dest.writeString(iconUnchecked);
        dest.writeString(type);
        dest.writeString(updatetime);
    }

    @Override
    public String toString() {
        return "ISortApi{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", appkey='" + appkey + '\'' +
                ", icon='" + icon + '\'' +
                ", iconChecked='" + iconChecked + '\'' +
                ", iconUnchecked='" + iconUnchecked + '\'' +
                ", type='" + type + '\'' +
                ", updatetime='" + updatetime + '\'' +
                ", mDataList=" + mDataList +
                '}';
    }
}
