package com.pesdk.uisdk.bean.model;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.vecore.base.lib.utils.FileUtils;
import com.vecore.models.caption.CaptionLiteObject;

import java.io.File;

import androidx.annotation.Keep;

/**
 * 涂鸦对象
 */
@Keep
public class GraffitiInfo implements IMoveToDraft, Parcelable {

    public GraffitiInfo() {
    }

    protected GraffitiInfo(Parcel in) {
        mPath = in.readString();
        mLiteObject = in.readParcelable(CaptionLiteObject.class.getClassLoader());
        id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPath);
        dest.writeParcelable(mLiteObject, flags);
        dest.writeInt(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GraffitiInfo> CREATOR = new Creator() {
        @Override
        public GraffitiInfo createFromParcel(Parcel in) {
            return new GraffitiInfo(in);
        }

        @Override
        public GraffitiInfo[] newArray(int size) {
            return new GraffitiInfo[size];
        }
    };

    private String mPath;
    private CaptionLiteObject mLiteObject;


    private int id = 0;







    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public CaptionLiteObject getLiteObject() {
        return mLiteObject;
    }

    private void setLiteObject(CaptionLiteObject liteObject) {
        mLiteObject = liteObject;
    }

    public void createObject() {
        try {
            CaptionLiteObject captionLiteObject = new CaptionLiteObject(null, mPath);
            captionLiteObject.setShowRectF(new RectF(0, 0, 1, 1));
            setLiteObject(captionLiteObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void updateObject() {
        if (null != mLiteObject) {
//            mLiteObject.setTimelineRange(Utils.ms2s(mTimelineFrom), Utils.ms2s(mTimelineTo));
        }
    }

    @Override
    public GraffitiInfo moveToDraft(String basePath) {
        if (FileUtils.isExist(basePath)) {
            if (!mPath.contains(basePath)) {
                //文件已经在草稿箱中，不需要再剪切文件
                File fileOld = new File(mPath);
                File fileNew = new File(basePath, fileOld.getName());
                FileUtils.syncCopyFile(fileOld, fileNew, null);
                mPath = fileNew.getAbsolutePath();
                createObject();
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "GraffitiInfo{" +
                " hash=" + hashCode() +
                ", id=" + id +
//                ", mPath='" + mPath + '\'' +
                ", mLiteObject=" + mLiteObject +
                '}';
    }

    public boolean equals(GraffitiInfo graffitiInfo) {
        if (null == graffitiInfo) {
            return false;
        }
return getPath().equals(graffitiInfo.mPath);
    }


    public void setId() {
        this.id = hashCode();
    }

    public int getId() {
        return id;
    }


    public GraffitiInfo copy() {
        GraffitiInfo info = new GraffitiInfo();
        info.setPath(mPath);
        info.setLiteObject(mLiteObject.copy());
        info.setId();
        return info;
    }
}
