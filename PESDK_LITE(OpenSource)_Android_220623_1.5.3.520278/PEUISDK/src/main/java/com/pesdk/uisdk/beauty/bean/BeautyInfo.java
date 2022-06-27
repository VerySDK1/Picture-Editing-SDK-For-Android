package com.pesdk.uisdk.beauty.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.vecore.models.caption.CaptionLiteObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 美颜信息
 */
public class BeautyInfo implements Parcelable {

    // 磨皮
    private float mValueBeautify = 0;
    // 美白
    private float mValueWhitening = 0;
    // 红润
    private float mValueRuddy = 0;

    public String getBaseMediaPath() {
        return baseMediaPath;
    }

    public void setBaseMediaPath(String baseMediaPath) {
        this.baseMediaPath = baseMediaPath;
    }


    //美发资源分组Id
    @Deprecated
    private String mHairSortId;
    //美发素材Id
    @Deprecated
    private String mHairMaterialId;
    //头发对象
    @Deprecated
    private CaptionLiteObject mHair;//相对于基础媒体(未裁剪)自身


    //美发的原始对象
    private String baseMediaPath;

    private static final String TAG = "BeautyInfo";

    public void setFaceList(List<BeautyFaceInfo> faceList) {
        mFaceList = faceList;
    }

    //五官
    private List<BeautyFaceInfo> mFaceList = new ArrayList<>();

    public BeautyInfo() {
    }

//    private int parcelVersion = VER;

    /**
     * 早期头发绑定到BeautyInfo，无法支持多人脸，需兼容
     */
    public void fixHairParam() {
        Log.e(TAG, "fixHairParam: " + mHair);
//        if (parcelVersion < 1 || parcelVersion >= 2) {
//            return;
//        }
        if (mFaceList.size() > 0 && null != mHair) {   //兼容把头发绑定到第一个人脸中
            BeautyFaceInfo faceInfo = getBeautyFace(0);
            faceInfo.getHairInfo().setHair(mHair);
            faceInfo.getHairInfo().setHair(mHairSortId, mHairMaterialId);
            mHair = null;
            mHairSortId = null;
            mHairMaterialId = null;
        }
    }


    protected BeautyInfo(Parcel in) {

        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int parcelVersion = in.readInt();
            if (parcelVersion >= 1) {
                mHairSortId = in.readString();
                mHairMaterialId = in.readString();
                baseMediaPath = in.readString();
                mHair = in.readParcelable(CaptionLiteObject.class.getClassLoader());
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        mValueBeautify = in.readFloat();
        mValueWhitening = in.readFloat();
        mValueRuddy = in.readFloat();
        mFaceList = in.createTypedArrayList(BeautyFaceInfo.CREATOR);
    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "220311BeautyInfo";
    private static final int VER = 1; //序列化版本

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }

        dest.writeString(mHairSortId);
        dest.writeString(mHairMaterialId);
        dest.writeString(baseMediaPath);
        dest.writeParcelable(mHair, flags);

        dest.writeFloat(mValueBeautify);
        dest.writeFloat(mValueWhitening);
        dest.writeFloat(mValueRuddy);
        dest.writeTypedList(mFaceList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BeautyInfo> CREATOR = new Creator<BeautyInfo>() {
        @Override
        public BeautyInfo createFromParcel(Parcel in) {
            return new BeautyInfo(in);
        }

        @Override
        public BeautyInfo[] newArray(int size) {
            return new BeautyInfo[size];
        }
    };

    public List<BeautyFaceInfo> getFaceList() {
        return mFaceList;
    }

    public BeautyFaceInfo getBeautyFace(int faceID) {
        for (BeautyFaceInfo faceInfo : mFaceList) {
            if (faceInfo.getFaceId() == faceID) {
                return faceInfo;
            }
        }
        return null;
    }

    public float getValueBeautify() {
        return mValueBeautify;
    }

    public void setValueBeautify(float valueBeautify) {
        mValueBeautify = valueBeautify;
    }

    public float getValueWhitening() {
        return mValueWhitening;
    }

    public void setValueWhitening(float valueWhitening) {
        mValueWhitening = valueWhitening;
    }

    public float getValueRuddy() {
        return mValueRuddy;
    }

    public void setValueRuddy(float valueRuddy) {
        mValueRuddy = valueRuddy;
    }

    public BeautyInfo copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        BeautyInfo tmp = new BeautyInfo(parcel);
        parcel.recycle();
        return tmp;
    }


    public boolean equals(BeautyInfo dst) {
        if (null == dst)
            return false;
        if (dst.mValueBeautify == mValueBeautify && dst.mValueWhitening == mValueWhitening && dst.mValueRuddy == mValueRuddy) {
            if (mFaceList.size() == dst.mFaceList.size()) {
                if (mFaceList.size() > 0) {
                    return mFaceList.get(0).equals(dst.getFaceList().get(0));
                } else {
                    return true;
                }
            }
        }
        return false;
    }


    public void moveToDraft(String basePath) {
        if (null != mFaceList && mFaceList.size() > 0) {
            for (BeautyFaceInfo faceInfo : mFaceList) {
                if (faceInfo.getHairInfo() != null) {
                    faceInfo.getHairInfo().moveToDraft(basePath);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "BeautyInfo{" +
//                "mValueBeautify=" + mValueBeautify +
//                ", mValueWhitening=" + mValueWhitening +
//                ", mValueRuddy=" + mValueRuddy +
                ", mHairSortId='" + mHairSortId + '\'' +
                ", mHairMaterialId='" + mHairMaterialId + '\'' +
                ", mHair=" + mHair +
                ", baseMediaPath='" + baseMediaPath + '\'' +
                ", mFaceList=" + mFaceList +
                '}';
    }
}
