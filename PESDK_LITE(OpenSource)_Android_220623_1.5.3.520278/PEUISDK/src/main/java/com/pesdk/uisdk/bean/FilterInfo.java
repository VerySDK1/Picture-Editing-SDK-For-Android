package com.pesdk.uisdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.model.IMediaParamImp;
import com.pesdk.uisdk.beauty.bean.BeautyInfo;
import com.pesdk.uisdk.util.Utils;
import com.vecore.models.VisualFilterConfig;

import androidx.annotation.Keep;


/**
 * 滤镜、调色、美颜。 如果同时使用滤镜+调色+美颜。应绑定3个FilterInfo
 */
@Keep
public class FilterInfo implements Parcelable {

    //lookup滤镜
    private VisualFilterConfig mLookupConfig; //基础lookup滤镜
    private int mId;//id

    //调色
    private IMediaParamImp mMediaParamImp;

    //美颜
    private BeautyInfo mBeautyInfo;


    //资源分组Id
    private String mSortId;
    //素材Id
    private String mMaterialId;


    /**
     * 普通lookup
     */
    public FilterInfo(VisualFilterConfig lookupConfig) {
        this();
        mLookupConfig = lookupConfig;
    }

    /**
     * 调色
     */
    public FilterInfo(IMediaParamImp mediaParamImp) {
        this();
        mMediaParamImp = mediaParamImp;
    }

    /**
     * 美颜
     */
    public FilterInfo(BeautyInfo beautyInfo) {
        this();
        mBeautyInfo = beautyInfo;
    }

    private FilterInfo() {
        mId = Utils.getRandomId();
    }


    protected FilterInfo(Parcel in) {
        mLookupConfig = in.readParcelable(VisualFilterConfig.class.getClassLoader());
        mId = in.readInt();
        mMediaParamImp = in.readParcelable(IMediaParamImp.class.getClassLoader());
        mBeautyInfo = in.readParcelable(BeautyInfo.class.getClassLoader());
        mSortId = in.readString();
        mMaterialId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLookupConfig, flags);
        dest.writeInt(mId);
        dest.writeParcelable(mMediaParamImp, flags);
        dest.writeParcelable(mBeautyInfo, flags);
        dest.writeString(mSortId);
        dest.writeString(mMaterialId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FilterInfo> CREATOR = new Creator<FilterInfo>() {
        @Override
        public FilterInfo createFromParcel(Parcel in) {
            return new FilterInfo(in);
        }

        @Override
        public FilterInfo[] newArray(int size) {
            return new FilterInfo[size];
        }
    };

    public FilterInfo copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        FilterInfo tmp = new FilterInfo(parcel);
        parcel.recycle();
        return tmp;
    }

    /**
     * 调色参数
     */
    public IMediaParamImp getMediaParamImp() {
        return mMediaParamImp;
    }


    /**
     * 美颜参数
     */
    public BeautyInfo getBeauty() {
        return mBeautyInfo;
    }



    public VisualFilterConfig getLookupConfig() {
        return mLookupConfig;
    }


    public void setLookupConfig(VisualFilterConfig lookupConfig) {
        mLookupConfig = lookupConfig;
    }


    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }


    public void setNetworkId(String sortId, String materialId) {
        mSortId = sortId;
        mMaterialId = materialId;
    }

    public String getSortId() {
        return mSortId;
    }

    public String getMaterialId() {
        return mMaterialId;
    }


    @Override
    public String toString() {
        return "FilterInfo{" +
                "mLookupConfig=" + mLookupConfig +
                ", mId=" + mId +
                ", mMediaParamImp=" + mMediaParamImp +
                ", mBeautyInfo=" + mBeautyInfo +
                ", mSortId='" + mSortId + '\'' +
                ", mMaterialId='" + mMaterialId + '\'' +
                '}';
    }

    public boolean equals(FilterInfo dst) {
        if (null == dst) {
            return false;
        }
        if (dst.getLookupConfig() != null && getLookupConfig() != null) { //普通滤镜
            if (dst.getLookupConfig().getDefaultValue() == getLookupConfig().getDefaultValue() && TextUtils.equals(dst.getLookupConfig().getFilterFilePath(), getLookupConfig().getFilterFilePath())) {
                return true;
            }
        }
        if (dst.getMediaParamImp() != null && getMediaParamImp() != null) { //调色参数
            return dst.getMediaParamImp().equals(getMediaParamImp());
        }
        if (dst.getBeauty() != null && getBeauty() != null) { //美颜参数
            return dst.getBeauty().equals(getBeauty());
        }
        return false;
    }

    public void moveToDraft(String basePath) {
        if(null!=getBeauty()){
            mBeautyInfo.moveToDraft(basePath);
        }

    }
}
