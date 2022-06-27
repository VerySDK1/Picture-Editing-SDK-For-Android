package com.pesdk.uisdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class RegisteredInfo implements Parcelable {

    private String mPath;
    private int mRegisterId;
    private boolean mIsKok;

    public RegisteredInfo(String path, int registerId, boolean isKok) {
        mPath = path;
        mRegisterId = registerId;
        mIsKok = isKok;
    }

    public RegisteredInfo(String path, int registerId) {
        mPath = path;
        mRegisterId = registerId;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public int getRegisterId() {
        return mRegisterId;
    }

    public void setRegisterId(int registerId) {
        mRegisterId = registerId;
    }

    public boolean isKok() {
        return mIsKok;
    }

    public void setKok(boolean kok) {
        mIsKok = kok;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPath);
        dest.writeInt(this.mRegisterId);
        dest.writeByte(this.mIsKok ? (byte) 1 : (byte) 0);
    }

    protected RegisteredInfo(Parcel in) {
        this.mPath = in.readString();
        this.mRegisterId = in.readInt();
        this.mIsKok = in.readByte() != 0;
    }

    public static final Creator<RegisteredInfo> CREATOR = new Creator<RegisteredInfo>() {
        @Override
        public RegisteredInfo createFromParcel(Parcel source) {
            return new RegisteredInfo(source);
        }

        @Override
        public RegisteredInfo[] newArray(int size) {
            return new RegisteredInfo[size];
        }
    };
}
