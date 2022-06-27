package com.pesdk.uisdk.beauty.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.vecore.models.caption.CaptionLiteObject;

/**
 * 单个人脸对应的头发
 */
public class FaceHairInfo implements Parcelable {

    public FaceHairInfo() {
    }

    public FaceHairInfo(String hairSortId, String hairMaterialId) {
        mHairSortId = hairSortId;
        mHairMaterialId = hairMaterialId;
    }

    protected FaceHairInfo(Parcel in) {
        mHairSortId = in.readString();
        mHairMaterialId = in.readString();
        mHair = in.readParcelable(CaptionLiteObject.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mHairSortId);
        dest.writeString(mHairMaterialId);
        dest.writeParcelable(mHair, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FaceHairInfo> CREATOR = new Creator<FaceHairInfo>() {
        @Override
        public FaceHairInfo createFromParcel(Parcel in) {
            return new FaceHairInfo(in);
        }

        @Override
        public FaceHairInfo[] newArray(int size) {
            return new FaceHairInfo[size];
        }
    };

    public String getHairSortId() {
        return mHairSortId;
    }

    public void setHair(String hairSortId, String hairMaterialId) {
        mHairSortId = hairSortId;
        mHairMaterialId = hairMaterialId;
    }

    public String getHairMaterialId() {
        return mHairMaterialId;
    }


    //美发资源分组Id
    private String mHairSortId;
    //美发素材Id
    private String mHairMaterialId;
    //头发对象
    private CaptionLiteObject mHair;//相对于基础媒体(未裁剪)自身


    public CaptionLiteObject getHair() {
        return mHair;
    }

    public void setHair(CaptionLiteObject hair) {
        mHair = hair;
    }

    @Override
    public String toString() {
        return "FaceHairInfo{" +
                "mHairSortId='" + mHairSortId + '\'' +
                ", mHairMaterialId='" + mHairMaterialId + '\'' +
                ", mHair=" + mHair +
                '}';
    }

    public void moveToDraft(String basePath) {
        if (null != mHair) { //头发相对于当前人像自身
            mHair = mHair.moveToDraft(basePath);
        }
    }


    public FaceHairInfo copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        FaceHairInfo tmp = new FaceHairInfo(parcel);
        parcel.recycle();
        return tmp;
    }
}
