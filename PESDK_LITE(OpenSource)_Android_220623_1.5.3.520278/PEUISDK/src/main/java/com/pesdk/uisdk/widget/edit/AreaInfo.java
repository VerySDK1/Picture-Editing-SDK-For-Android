package com.pesdk.uisdk.widget.edit;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 区域
 */
public class AreaInfo implements Parcelable {

    private RectF mRectF;
    private float mAngle;

    public AreaInfo(RectF rectF, float angle) {
        mRectF = rectF;
        mAngle = angle;
    }

    public RectF getRectF() {
        return mRectF;
    }

    public void setRectF(RectF rectF) {
        mRectF = rectF;
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mRectF, flags);
        dest.writeFloat(this.mAngle);
    }

    protected AreaInfo(Parcel in) {
        this.mRectF = in.readParcelable(RectF.class.getClassLoader());
        this.mAngle = in.readFloat();
    }

    public static final Creator<AreaInfo> CREATOR = new Creator<AreaInfo>() {
        @Override
        public AreaInfo createFromParcel(Parcel source) {
            return new AreaInfo(source);
        }

        @Override
        public AreaInfo[] newArray(int size) {
            return new AreaInfo[size];
        }
    };

    @Override
    public String toString() {
        return "AreaInfo{" +
                "mRectF=" + mRectF +
                ", mAngle=" + mAngle +
                '}';
    }
}
