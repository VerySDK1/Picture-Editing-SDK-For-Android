package com.pesdk.uisdk.bean.model.subtitle;

import android.os.Parcel;
import android.os.Parcelable;

public class SubFrameArray implements Parcelable {

    private float time;
    private int pic;

    public SubFrameArray copy() {
        SubFrameArray array = new SubFrameArray();
        array.setPic(pic);
        array.setTime(time);
        return array;
    }

    public float getTime() { return time; }

    public void setTime(float value) { this.time = value; }

    public int getPic() { return pic; }

    public void setPic(int value) { this.pic = value; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.time);
        dest.writeInt(this.pic);
    }

    public void readFromParcel(Parcel source) {
        this.time = source.readFloat();
        this.pic = source.readInt();
    }

    public SubFrameArray() {
    }

    protected SubFrameArray(Parcel in) {
        this.time = in.readFloat();
        this.pic = in.readInt();
    }

    public static final Creator<SubFrameArray> CREATOR = new Creator<SubFrameArray>() {
        @Override
        public SubFrameArray createFromParcel(Parcel source) {
            return new SubFrameArray(source);
        }

        @Override
        public SubFrameArray[] newArray(int size) {
            return new SubFrameArray[size];
        }
    };
}
