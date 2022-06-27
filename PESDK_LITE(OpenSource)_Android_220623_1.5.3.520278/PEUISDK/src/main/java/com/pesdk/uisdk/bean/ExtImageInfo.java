package com.pesdk.uisdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

/**
 * 用来存放虚拟图片的扩展信息   背景、滤镜、调色
 */
public class ExtImageInfo implements Parcelable {

    public ExtImageInfo() {
    }

    private int backgroundColor = PEScene.UNKNOWN_COLOR;  //纯色背景时,build时自动生成bitmap
    private PEImageObject bgMedia = null;

    protected ExtImageInfo(Parcel in) {
        backgroundColor = in.readInt();
        bgMedia = in.readParcelable(PEImageObject.class.getClassLoader());
        mFilterInfo = in.readParcelable(FilterInfo.class.getClassLoader());
        mAdjust = in.readParcelable(FilterInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(backgroundColor);
        dest.writeParcelable(bgMedia, flags);
        dest.writeParcelable(mFilterInfo, flags);
        dest.writeParcelable(mAdjust, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExtImageInfo> CREATOR = new Creator<ExtImageInfo>() {
        @Override
        public ExtImageInfo createFromParcel(Parcel in) {
            return new ExtImageInfo(in);
        }

        @Override
        public ExtImageInfo[] newArray(int size) {
            return new ExtImageInfo[size];
        }
    };

    public FilterInfo getFilter() {
        return mFilterInfo;
    }

    public void setFilter(FilterInfo filter) {
        mFilterInfo = filter;
    }

    public FilterInfo getAdjust() {
        return mAdjust;
    }

    public void setAdjust(FilterInfo adjust) {
        mAdjust = adjust;
    }

    private FilterInfo mFilterInfo; //滤镜
    private FilterInfo mAdjust; //调色


    public PEImageObject getBackground() {
        return bgMedia;
    }

    public void setBackground(PEImageObject bgMedia) {
        this.bgMedia = bgMedia;
        backgroundColor = PEScene.UNKNOWN_COLOR;
    }


    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackground(int color) {
        this.backgroundColor = color;
        bgMedia = null;
    }


    public ExtImageInfo copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ExtImageInfo info = new ExtImageInfo(parcel);
        parcel.recycle();
        return info;
    }

    @Override
    public String toString() {
        return "ExtImageInfo{" +
                "backgroundColor=" + backgroundColor +
                ", bgMedia=" + bgMedia +
                ", mFilterInfo=" + mFilterInfo +
                ", mAdjust=" + mAdjust +
                '}';
    }

    public void reset() {
        backgroundColor = PEScene.UNKNOWN_COLOR;
        bgMedia = null;
        mFilterInfo = null;
        mAdjust = null;
    }
}
