package com.pesdk.uisdk.bean.model.subtitle;

import android.os.Parcel;
import android.os.Parcelable;

public class SubBackgroundAnim implements Parcelable {

    public SubBackgroundAnim copy() {
        return new SubBackgroundAnim();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public SubBackgroundAnim() {
    }

    protected SubBackgroundAnim(Parcel in) {
    }

    public static final Creator<SubBackgroundAnim> CREATOR = new Creator<SubBackgroundAnim>() {
        @Override
        public SubBackgroundAnim createFromParcel(Parcel source) {
            return new SubBackgroundAnim(source);
        }

        @Override
        public SubBackgroundAnim[] newArray(int size) {
            return new SubBackgroundAnim[size];
        }
    };
}
