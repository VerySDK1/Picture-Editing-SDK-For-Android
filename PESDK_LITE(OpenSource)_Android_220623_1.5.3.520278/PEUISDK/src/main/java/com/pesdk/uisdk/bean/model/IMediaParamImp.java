package com.pesdk.uisdk.bean.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;


/**
 * 仅调色参数
 */
@Keep
public class IMediaParamImp implements Parcelable {
    public IMediaParamImp() {

    }

    protected IMediaParamImp(Parcel in) {
        mBrightness = in.readFloat();
        mContrast = in.readFloat();
        mSaturation = in.readFloat();
        mSharpen = in.readFloat();
        mWhite = in.readFloat();
        mVignette = in.readFloat();
        mTemperature = in.readFloat();
        mTintValue = in.readFloat();
        mHighlightsValue = in.readFloat();
        mShadowsValue = in.readFloat();
        mGraininess = in.readFloat();
        mLightSensation = in.readFloat();
        mFade = in.readFloat();
        mVignetteId = in.readInt();
        mTypeId = in.readString();
        mResourceId = in.readString();
        mGroupSortId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mBrightness);
        dest.writeFloat(mContrast);
        dest.writeFloat(mSaturation);
        dest.writeFloat(mSharpen);
        dest.writeFloat(mWhite);
        dest.writeFloat(mVignette);
        dest.writeFloat(mTemperature);
        dest.writeFloat(mTintValue);
        dest.writeFloat(mHighlightsValue);
        dest.writeFloat(mShadowsValue);
        dest.writeFloat(mGraininess);
        dest.writeFloat(mLightSensation);
        dest.writeFloat(mFade);
        dest.writeInt(mVignetteId);
        dest.writeString(mTypeId);
        dest.writeString(mResourceId);
        dest.writeString(mGroupSortId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IMediaParamImp> CREATOR = new Creator<IMediaParamImp>() {
        @Override
        public IMediaParamImp createFromParcel(Parcel in) {
            return new IMediaParamImp(in);
        }

        @Override
        public IMediaParamImp[] newArray(int size) {
            return new IMediaParamImp[size];
        }
    };

    public float getBrightness() {
        return mBrightness;
    }

    public void setBrightness(float brightness) {
        mBrightness = brightness;
    }

    public float getContrast() {
        return mContrast;
    }

    public void setContrast(float contrast) {
        mContrast = contrast;
    }

    public float getSaturation() {
        return mSaturation;
    }

    public void setSaturation(float saturation) {
        mSaturation = saturation;
    }

    public float getSharpen() {
        return mSharpen;
    }

    private static final String TAG = "IMediaParamImp";

    public void setSharpen(float sharpen) {
        mSharpen = sharpen;
    }

    public float getWhite() {
        return mWhite;
    }

    public void setWhite(float white) {
        mWhite = white;
    }

    public float getVignette() {
        return mVignette;
    }

    public void setVignette(float vignette) {
        mVignette = vignette;
    }

    public int getVignetteId() {
        return mVignetteId;
    }

    public void setVignetteId(int vignetteId) {
        mVignetteId = vignetteId;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public void setTemperature(float temperature) {
        mTemperature = temperature;
    }

    public float getTintValue() {
        return mTintValue;
    }

    public void setTintValue(float tintValue) {
        mTintValue = tintValue;
    }

    public float getHighlightsValue() {
        return mHighlightsValue;
    }

    public void setHighlightsValue(float highlightsValue) {
        mHighlightsValue = highlightsValue;
    }

    public float getShadowsValue() {
        return mShadowsValue;
    }

    public void setShadowsValue(float shadowsValue) {
        mShadowsValue = shadowsValue;
    }

    /**
     * 记录暗角滤镜 （mVignetteId ==VisualFilterConfig.FILTER_ID_VIGNETTE时，暗角滤镜启用）
     */
    public static final int NO_VIGNETTE_ID = -1;

    /**
     * 调色 记录对比度、亮度、白平衡、锐度、饱和度
     */
    private float mBrightness = Float.NaN;
    private float mContrast = Float.NaN;
    private float mSaturation = Float.NaN;
    private float mSharpen = Float.NaN;
    private float mWhite = Float.NaN;
    private float mVignette = Float.NaN;
    private float mTemperature = Float.NaN;//白平衡 (色温)
    private float mTintValue = Float.NaN;//色调
    private float mHighlightsValue = Float.NaN;//减少高亮变暗
    private float mShadowsValue = Float.NaN;//调整图像的阴影

    private float mGraininess = Float.NaN; //颗粒感
    private float mLightSensation = Float.NaN; //光感调节
    private float mFade = Float.NaN; //褪色


    //记录暗角滤镜 （mVignetteId ==VisualFilterConfig.FILTER_ID_VIGNETTE时，暗角滤镜启用）
    public static final int NO_VIGNETTEDID = -1;
    /**
     * 暗角id
     */
    private int mVignetteId = NO_VIGNETTE_ID;


    /**
     * 分类id
     */
    private String mTypeId;
    /**
     * 数据ID
     */
    @Deprecated
    private transient String mResourceId;

    public float getGraininess() {
        return mGraininess;
    }

    public void setGraininess(float graininess) {
        mGraininess = graininess;
    }

    public float getLightSensation() {
        return mLightSensation;
    }

    public void setLightSensation(float lightSensation) {
        mLightSensation = lightSensation;
    }

    public float getFade() {
        return mFade;
    }

    public void setFade(float fade) {
        mFade = fade;
    }

    public IMediaParamImp copy() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        IMediaParamImp tmp = new IMediaParamImp(parcel);
        parcel.recycle();
        return tmp;
    }

    public boolean equals(IMediaParamImp dst) {
        if (null == dst)
            return false;
        return dst.mBrightness == mBrightness &&
                dst.mContrast == mContrast &&
                dst.mSaturation == mSaturation &&
                dst.mSharpen == mSharpen &&
                dst.mWhite == mWhite &&
                dst.mVignette == mVignette &&
                dst.mVignetteId == mVignetteId &&
                dst.mTypeId == mTypeId &&
                dst.mResourceId == mResourceId &&
                dst.mTemperature == mTemperature &&
                dst.mTintValue == mTintValue &&
                dst.mHighlightsValue == mHighlightsValue &&
                dst.mShadowsValue == mShadowsValue &&
                dst.mGraininess == mGraininess &&
                dst.mLightSensation == mLightSensation &&
                dst.mFade == mFade;
    }


    /**
     * 参数值是否有意义
     *
     * @return true 有效，false 默认效果
     */
    public boolean isValid() {
        if (Float.isNaN(mBrightness)             //亮度
//                && Float.isNaN(mExposure)                  //曝光
                && Float.isNaN(mContrast)            //对比度
                && Float.isNaN(mSaturation)          //饱和度
                && Float.isNaN(mWhite)   //白平衡 (色温)
                && Float.isNaN(mSharpen)) {           //锐度
//                && Float.isNaN(mFeatherX)) {              //左右羽化
            return false;
        }
        return true;
    }

    @Deprecated
    private transient String mGroupSortId;


    @Override
    public String toString() {
        return "IMediaParamImp{" +
                "mBrightness=" + mBrightness +
                ", mContrast=" + mContrast +
                ", mSaturation=" + mSaturation +
                ", mSharpen=" + mSharpen +
                ", mWhite=" + mWhite +
                ", mVignette=" + mVignette +
                ", mTemperature=" + mTemperature +
                ", mTintValue=" + mTintValue +
                ", mHighlightsValue=" + mHighlightsValue +
                ", mShadowsValue=" + mShadowsValue +
                ", mGraininess=" + mGraininess +
                ", mLightSensation=" + mLightSensation +
                ", mFade=" + mFade +
                ", mVignetteId=" + mVignetteId +
                ", mTypeId='" + mTypeId + '\'' +
                '}';
    }
}
