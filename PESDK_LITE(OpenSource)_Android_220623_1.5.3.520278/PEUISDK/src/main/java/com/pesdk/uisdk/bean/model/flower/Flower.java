package com.pesdk.uisdk.bean.model.flower;

import android.os.Parcel;
import android.os.Parcelable;

import com.pesdk.uisdk.util.Utils;


public class Flower implements Parcelable {

    private String mId;
    /**
     * 网络地址
     */
    private String mUrl;
    /**
     * 图标
     */
    private transient String mIcon;
    private transient int mIconId;

    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }

    /**
     * 本地
     */
    private String mLocalPath;

    public Flower(int icon) {
        mId = String.valueOf(Utils.getId());
        mIconId = icon;
    }

    public Flower(String url, String icon) {
        mUrl = url;
        mIcon = icon;
        if (mUrl != null) {
            mId = String.valueOf(url.hashCode());
        } else {
            mId = String.valueOf(Utils.getId());
        }
    }


    public int getIconId() {
        return mIconId;
    }

    public String getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getIcon() {
        return mIcon;
    }


    public String getLocalPath() {
        return mLocalPath;
    }

    public void setId(String id) {
        mId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mIconId);
        dest.writeString(this.mId);
        dest.writeString(this.mUrl);
        dest.writeString(this.mIcon);
        dest.writeString(this.mLocalPath);
    }

    protected Flower(Parcel in) {
        this.mIconId = in.readInt();
        this.mId = in.readString();
        this.mUrl = in.readString();
        this.mIcon = in.readString();
        this.mLocalPath = in.readString();
    }

    public static final Creator<Flower> CREATOR = new Creator<Flower>() {
        @Override
        public Flower createFromParcel(Parcel source) {
            return new Flower(source);
        }

        @Override
        public Flower[] newArray(int size) {
            return new Flower[size];
        }
    };
}
