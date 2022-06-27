package com.pesdk.uisdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

import androidx.annotation.Keep;

/**
 * 特效序列化辅助
 */
@Keep
public class EffectsTag implements Parcelable {

    private String url; //文件网络连接
    private String mName;//名字
    private String mEffectType;//分类

    public int getIndex() {
        return nIndex;
    }

    public void setIndex(int nIndex) {
        this.nIndex = nIndex;
    }

    private int nIndex; //在已添加列表(list)中的下标

    public int getNId() {
        return nId;
    }

    private int nId;  //唯一id ,与缩略图轴一致

    public EffectsTag() {
    }

    public EffectsTag(String url, String name, String type, int nId) {
        this.url = url;
        this.nId = nId;
        this.mName = name;
        this.mEffectType = type;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setEffectType(String effectType) {
        mEffectType = effectType;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return mName;
    }

    public String getEffectType() {
        return mEffectType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "200106EffectTAG";
    private static final int VER = 2;


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeInt(nId);
        dest.writeInt(nIndex);
        dest.writeString(url);
        dest.writeString(this.mName);
        dest.writeString(this.mEffectType);
    }


    protected EffectsTag(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tparcelVer = in.readInt();
            if (tparcelVer >= 2) {
                nId = in.readInt();
                nIndex = in.readInt();
            }
            if (tparcelVer >= 1) {
                url = in.readString();
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }
        this.mName = in.readString();
        this.mEffectType = in.readString();
    }

    @Override
    public String toString() {
        return "EffectsTag{" +
                " hash='" + hashCode() + '\'' +
                "url='" + url + '\'' +
                ", mName='" + mName + '\'' +
                ", mEffectType='" + mEffectType + '\'' +
                ", nIndex=" + nIndex +
                ", nId=" + nId +
                '}';
    }

    public static final Creator<EffectsTag> CREATOR = new Creator<EffectsTag>() {
        @Override
        public EffectsTag createFromParcel(Parcel source) {
            return new EffectsTag(source);
        }

        @Override
        public EffectsTag[] newArray(int size) {
            return new EffectsTag[size];
        }
    };

    public EffectsTag(Map<String, String> map) {
        if (null != map) {
            mName = map.get("mName");
            mEffectType = map.get("mEffectType");
            url = map.get("url");
        }
    }

    public static final String getUrl(Map<String, String> map) {
        if (null != map) {
            return map.get("url");
        }
        return null;
    }

    public EffectsTag copy() {
        EffectsTag tag = new EffectsTag(url, mName, mEffectType, nId);
//        tag.setDataId(mDataId);
//        tag.setCategory(mCategory);
//        tag.setLevel(mLevel);
        tag.setIndex(nIndex);
//        tag.setResourceId(mResourceId);
//        tag.setLocalPath(mLocalPath);
//        tag.setDuration(mDuration);
        return tag;
    }

}
