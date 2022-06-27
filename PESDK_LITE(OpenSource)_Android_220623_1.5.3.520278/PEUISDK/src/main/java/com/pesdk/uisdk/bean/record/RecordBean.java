package com.pesdk.uisdk.bean.record;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 记录mask下载路径
 */
public class RecordBean implements Parcelable {

    public RecordBean(String name, String path) {
        this.name = name;
        this.path = path;
    }

    protected RecordBean(Parcel in) {
        name = in.readString();
        path = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RecordBean> CREATOR = new Creator<RecordBean>() {
        @Override
        public RecordBean createFromParcel(Parcel in) {
            return new RecordBean(in);
        }

        @Override
        public RecordBean[] newArray(int size) {
            return new RecordBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    private String name;
    private String path;

    @Override
    public String toString() {
        return "RecordBean{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
