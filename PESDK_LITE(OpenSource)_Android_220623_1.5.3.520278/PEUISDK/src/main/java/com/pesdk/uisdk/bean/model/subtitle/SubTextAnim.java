package com.pesdk.uisdk.bean.model.subtitle;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.pesdk.uisdk.bean.model.AnimInfo;

/**
 * 文字动画
 */
public class SubTextAnim implements Parcelable {

    //类型
    @SerializedName("anim_type")
    private String animType;
    //资源
    @SerializedName("anim_resource")
    private String animResource;
    //时间
    private float duration;


    //注册后的动画
    private AnimInfo mAnimInfo;



    public SubTextAnim(){}

    public AnimInfo getAnimInfo() {
        return mAnimInfo;
    }

    public void setAnimInfo(AnimInfo animInfo) {
        mAnimInfo = animInfo;
        if (mAnimInfo != null) {
            mAnimInfo.setAnimDuration(duration);
        }
    }

    public SubTextAnim copy() {
        SubTextAnim anim = new SubTextAnim();
        anim.setAnimType(animType);
        anim.setAnimResource(animResource);
        anim.setDuration(duration);
        if (mAnimInfo != null) {
            anim.setAnimInfo(mAnimInfo.copy());
        }
        return anim;
    }




    public String getAnimType() {
        return animType;
    }

    public void setAnimType(String value) { this.animType = value; }

    public String getAnimResource() { return animResource; }

    public void setAnimResource(String value) { this.animResource = value; }

    public float getDuration() { return duration; }

    public void setDuration(float value) { this.duration = value; }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.animType);
        dest.writeString(this.animResource);
        dest.writeFloat(this.duration);
        dest.writeParcelable(this.mAnimInfo, flags);
    }

    protected SubTextAnim(Parcel in) {
        this.animType = in.readString();
        this.animResource = in.readString();
        this.duration = in.readFloat();
        this.mAnimInfo = in.readParcelable(AnimInfo.class.getClassLoader());
    }

    public static final Creator<SubTextAnim> CREATOR = new Creator<SubTextAnim>() {
        @Override
        public SubTextAnim createFromParcel(Parcel source) {
            return new SubTextAnim(source);
        }

        @Override
        public SubTextAnim[] newArray(int size) {
            return new SubTextAnim[size];
        }
    };
}
