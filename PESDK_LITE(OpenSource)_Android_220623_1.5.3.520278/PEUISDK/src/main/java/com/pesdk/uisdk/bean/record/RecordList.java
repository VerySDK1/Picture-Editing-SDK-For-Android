package com.pesdk.uisdk.bean.record;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 *
 */
public class RecordList implements Parcelable {
    public RecordList(List<RecordBean> mlist) {
        this.mlist = mlist;
    }

    public List<RecordBean> getlist() {
        return mlist;
    }

    private List<RecordBean> mlist;

    protected RecordList(Parcel in) {
        mlist = in.createTypedArrayList(RecordBean.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mlist);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RecordList> CREATOR = new Creator<RecordList>() {
        @Override
        public RecordList createFromParcel(Parcel in) {
            return new RecordList(in);
        }

        @Override
        public RecordList[] newArray(int size) {
            return new RecordList[size];
        }
    };

    @Override
    public String toString() {
        return "RecordList{" +
                "mlist=" + mlist +
                '}';
    }
}
