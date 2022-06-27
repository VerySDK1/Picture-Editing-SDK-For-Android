package com.pesdk.uisdk.bean.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.code.SegmentResult;

import androidx.annotation.Keep;

import static com.pesdk.uisdk.bean.code.Crop.CROP_ORIGINAL;

/**
 * PEImageObeject绑定扩展信息
 */
@Keep
public class ImageOb implements Parcelable {
    public ImageOb() {
        mCropMode = DEFAULT_CROP;
    }

    public ImageOb(@Crop.CropMode int cropMode) {
        mCropMode = cropMode;
    }


    protected ImageOb(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        int parcelVersion = 0;
        if (VER_TAG.equals(tmp)) {
            parcelVersion = in.readInt();
            if (parcelVersion >= 1) {
                mMaskPath = in.readString();
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        mFilterInfo = in.readParcelable(FilterInfo.class.getClassLoader());
        mAdjust = in.readParcelable(FilterInfo.class.getClassLoader());
        mBeauty = in.readParcelable(FilterInfo.class.getClassLoader());


        personResult = in.readInt();
        skyResult = in.readInt();
        mSegmentType = in.readInt();

        mCropMode = in.readInt();


    }

    //唯一指定标识，以后不能再更改
    private static final String VER_TAG = "220217imageOb";
    private static final int VER = 1; //序列化版本

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }
        dest.writeString(mMaskPath);
        dest.writeParcelable(mFilterInfo, flags);
        dest.writeParcelable(mAdjust, flags);
        dest.writeParcelable(mBeauty, flags);

        dest.writeInt(personResult);
        dest.writeInt(skyResult);
        dest.writeInt(mSegmentType);

        dest.writeInt(mCropMode);

    }

    public static final Creator<ImageOb> CREATOR = new Creator<ImageOb>() {
        @Override
        public ImageOb createFromParcel(Parcel in) {
            return new ImageOb(in);
        }

        @Override
        public ImageOb[] newArray(int size) {
            return new ImageOb[size];
        }
    };


    @Crop.CropMode
    private int mCropMode = CROP_ORIGINAL; // 0自由裁切，1 正方形裁切，2 原始裁切,

    private String mMaskPath;//抠图用的透明纯白图（提前准备mask图片，避免频繁扣取）

    public String getMaskPath() {
        return mMaskPath;
    }

    public void setMaskPath(String maskPath) {
        mMaskPath = maskPath;
    }

    public ImageOb copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ImageOb imageOb = new ImageOb(parcel);
        parcel.recycle();
        return imageOb;
    }

    public boolean equals(ImageOb dst) {
        if (null == dst)
            return false;

        if (mCropMode == dst.getCropMode() && mSegmentType == dst.mSegmentType && personResult == dst.personResult && skyResult == dst.skyResult) {
            if (mFilterInfo != null && !mFilterInfo.equals(dst.mFilterInfo)) {
                return false;
            }
            if (mAdjust != null && !mAdjust.equals(dst.mAdjust)) {
                return false;
            }
            if (mBeauty != null && !mBeauty.equals(dst.mBeauty)) {
                return false;
            }
            return true;
        }
        return false;
    }


    @Crop.CropMode
    public static final int DEFAULT_CROP = CROP_ORIGINAL;

    private FilterInfo mFilterInfo; //普通滤镜
    private FilterInfo mAdjust;//调色
    private FilterInfo mBeauty;//美颜


//    public void setFilter(FilterInfo filter, FilterInfo adjust, FilterInfo beauty) {
//        mFilterInfo = filter;
//        mAdjust = adjust;
//        mBeauty = beauty;
//    }

    public void setFilterInfo(FilterInfo filterInfo) {
        mFilterInfo = filterInfo;
    }

    public void setAdjust(FilterInfo adjust) {
        mAdjust = adjust;
    }

    public void setBeauty(FilterInfo beauty) {
        mBeauty = beauty;
    }

    /**
     * 滤镜
     */
    public FilterInfo getFilter() {
        return mFilterInfo;
    }

    /**
     * 调色
     */
    public FilterInfo getAdjust() {
        return mAdjust;
    }

    /**
     * 美颜
     */
    public FilterInfo getBeauty() {
        return mBeauty;
    }


    @Crop.CropMode
    public int getCropMode() {
        return mCropMode;
    }

    public void setCropMode(@Crop.CropMode int cropMode) {
        this.mCropMode = cropMode;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    //0表示不抠图、1人像抠图、2天空抠图
    @Segment.Type
    private int mSegmentType = Segment.NONE;

    public boolean isSegment() {
        return isPersonSegment() || isSkySegment();
    }

    public boolean isPersonSegment() {
        return mSegmentType == Segment.SEGMENT_PERSON;
    }

    public boolean isSkySegment() {
        return mSegmentType == Segment.SEGMENT_SKY;
    }

    @Segment.Type
    public int getSegmentType() {
        return mSegmentType;
    }

    public void setSegment(@Segment.Type int segment) {
        mSegmentType = segment;
    }

    @SegmentResult.reuslt
    private int personResult = SegmentResult.None; //人像的抠图结果

    public int getSkyResult() {
        return skyResult;
    }

    public void setSkyResult(@SegmentResult.reuslt int result) {
        this.skyResult = result;
    }

    @SegmentResult.reuslt
    private int skyResult = SegmentResult.None; //天空的抠图结果


    public void setPersonResult(@SegmentResult.reuslt int code) {
        personResult = code;
    }

    @SegmentResult.reuslt
    public int getPersonResult() {
        return personResult;
    }

    @Override
    public String toString() {
        return "ImageOb{" +
                "hash=" + hashCode() +
                ", mCropMode=" + mCropMode +
                ", mMaskPath='" + mMaskPath + '\'' +
                ", mFilterInfo=" + mFilterInfo +
                ", mAdjust=" + mAdjust +
                ", mBeauty=" + mBeauty +
                ", mSegmentType=" + mSegmentType +
                ", personResult=" + personResult +
                ", skyResult=" + skyResult +
                '}';
    }
}
