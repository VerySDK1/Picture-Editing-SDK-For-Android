package com.pesdk.uisdk.bean.model;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

/**
 * 记录每一块字幕 、特效的区域
 */
@Keep
public class SubInfo implements Parcelable {
    private int id;
    private String str = "";

    public void setTimelinefrom(int timelinefrom) {
        this.timelinefrom = timelinefrom;
    }

    public void setTimelineTo(int timelineTo) {
        this.timelineTo = timelineTo;
    }

    private int timelinefrom = 0, timelineTo = 0;
    private Rect rect = new Rect();

    public SubInfo copy() {
        SubInfo subInfo = new SubInfo(timelinefrom, timelineTo, id);
        if (null != rect) {
            subInfo.rect.set(rect);
        }
        return subInfo;
    }

    protected SubInfo(Parcel in) {
        id = in.readInt();
        str = in.readString();
        timelinefrom = in.readInt();
        timelineTo = in.readInt();
        rect = in.readParcelable(Rect.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(str);
        dest.writeInt(timelinefrom);
        dest.writeInt(timelineTo);
        dest.writeParcelable(rect, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubInfo> CREATOR = new Creator<SubInfo>() {
        @Override
        public SubInfo createFromParcel(Parcel in) {
            return new SubInfo(in);
        }

        @Override
        public SubInfo[] newArray(int size) {
            return new SubInfo[size];
        }
    };

    private void SubInfoData(int startP, int endP, int height, String str,
                             int id) {

        this.str = str;
        this.id = id;
        rect.set(startP, 0, endP, height);
    }

    public SubInfo(int timelinefrom, int timelineto, int id) {
        setTimeLine(timelinefrom, timelineto);
        this.id = id;
    }

    public SubInfo(ICommon info) {
        this.id = info.getId();
    }

    public SubInfo(int startP, int endP, int height, String str, int id) {

        SubInfoData(startP, endP, height, str, id);
    }

    public SubInfo(SubInfo info, int height) {
        SubInfoData(info.getStart(), info.getEnd(), height, info.str, info.id);
    }

    public int getEnd() {
        return rect.right;
    }

    public int getStart() {
        return rect.left;
    }

    public void setStart(int startPx) {
        rect.left = startPx;
    }

    public void setEnd(int endPx) {
        rect.right = endPx;
    }


    /**
     * 整体平移时，增量偏移
     *
     * @param dx
     */
    public void offset(float dx) {
        rect.offset((int) dx, 0);
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "SubInfo{" +
//                "hash=" + hashCode() +
                ", id=" + id +
//                ", str='" + str + '\'' +
                ", timelinefrom=" + timelinefrom +
                ", timelineTo=" + timelineTo +
                ", rect=" + rect +
                '}';
    }

    public String getStr() {
        return null == str ? "" : str;
    }


    public int getId() {
        return id;
    }


    public Rect getRect() {
        return rect;
    }

    /**
     * 单位：ms
     *
     * @param nstart
     * @param nend
     */
    public void setTimeLine(int nstart, int nend) {
        this.timelinefrom = nstart;
        this.timelineTo = nend;
    }

    public int getTimelinefrom() {
        return timelinefrom;
    }

    public int getTimelineTo() {
        return timelineTo;
    }

}
