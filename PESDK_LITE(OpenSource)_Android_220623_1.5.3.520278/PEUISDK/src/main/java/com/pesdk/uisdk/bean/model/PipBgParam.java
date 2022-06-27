package com.pesdk.uisdk.bean.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.vecore.models.PEScene;

import androidx.annotation.Keep;

/**
 * 画中画背景参数 (草稿: 恢复纯色)
 */
@Keep
public class PipBgParam implements Parcelable {


    private String path;
    private float blurIntensity = 0;//景深
    private int mColor = PEScene.UNKNOWN_COLOR;


    private String networkCategoryId;
    private String networkResourceId;

    public PipBgParam(int color) {
        mColor = color;
    }

    public PipBgParam(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        mColor = PEScene.UNKNOWN_COLOR;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        path = null;
    }


    public void setNetId(String networkCategoryId, String networkResourceId) {
        this.networkCategoryId = networkCategoryId;
        this.networkResourceId = networkResourceId;
    }


    public String getNetworkCategoryId() {
        return networkCategoryId;
    }

    public String getNetworkResourceId() {
        return networkResourceId;
    }

    public float getBlurIntensity() {
        return blurIntensity;
    }

    public void setBlurIntensity(float blurIntensity) {
        this.blurIntensity = blurIntensity;
    }

    protected PipBgParam(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        int parcelVersion = 0;
        if (VER_TAG.equals(tmp)) {
            parcelVersion = in.readInt();
            if (parcelVersion >= 2) {
                networkCategoryId = in.readString();
                networkResourceId = in.readString();
            }
            if (parcelVersion >= 1) {
                blurIntensity = in.readFloat();
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }
        path = in.readString();
        mColor = in.readInt();
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "220126pipbg";
    private static final int VER = 2; //序列化版本

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeString(networkCategoryId);
        dest.writeString(networkResourceId);

        dest.writeFloat(blurIntensity);
        dest.writeString(path);
        dest.writeInt(mColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PipBgParam> CREATOR = new Creator<PipBgParam>() {
        @Override
        public PipBgParam createFromParcel(Parcel in) {
            return new PipBgParam(in);
        }

        @Override
        public PipBgParam[] newArray(int size) {
            return new PipBgParam[size];
        }
    };

    @Override
    public String toString() {
        return "PipBgParam{" +
                "path='" + path + '\'' +
                ", blurIntensity=" + blurIntensity +
                ", mColor=" + mColor +
                ", networkCategoryId='" + networkCategoryId + '\'' +
                ", networkResourceId='" + networkResourceId + '\'' +
                '}';
    }

    public PipBgParam copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        PipBgParam tmp = new PipBgParam(parcel);
        parcel.recycle();
        return tmp;
    }
}
