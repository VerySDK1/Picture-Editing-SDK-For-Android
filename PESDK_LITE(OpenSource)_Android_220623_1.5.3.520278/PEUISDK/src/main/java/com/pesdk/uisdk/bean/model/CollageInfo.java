package com.pesdk.uisdk.bean.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.models.VisualFilterConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Keep;

/**
 * 片段中的画中画小块信息 (包含时间线、旋转角度、显示位置)
 */
@Keep
public class CollageInfo implements IMoveToDraft, ITimeLine, Parcelable {
    private static final String TAG = "CollageInfo";
    //缩放比
    private PEImageObject mImageObject;
    private int mId = 0;
    private PEImageObject mBG;//天空、背景
    //标识符号
    public String identifier;
    public int groupId;

    public CollageInfo(CollageInfo src) {
        this(src, false);
    }

    /**
     * @param src
     * @param regenerateId true 重新生成Id，false 完全复制
     */
    public CollageInfo(CollageInfo src, boolean regenerateId) {
        this.mImageObject = src.mImageObject.copy();
        mImageObject.setTag(PEHelper.initImageOb(src.mImageObject).copy());
        if (null != src.mBG) {
            mBG = src.mBG.copy();
            mBG.setTag(((PipBgParam) src.mBG.getTag()).copy());
        }
        this.mId = regenerateId ? hashCode() : src.mId;
        this.groupId = src.groupId;
        this.identifier = src.identifier;
    }

    private CollageInfo() {

    }

    protected CollageInfo(Parcel in) {
        mImageObject = in.readParcelable(PEImageObject.class.getClassLoader());
        mId = in.readInt();
        mBG = in.readParcelable(PEImageObject.class.getClassLoader());
        identifier = in.readString();
        groupId = in.readInt();
        mHide = in.readByte() != 0;
        mAlpha = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mImageObject, flags);
        dest.writeInt(mId);
        dest.writeParcelable(mBG, flags);
        dest.writeString(identifier);
        dest.writeInt(groupId);
        dest.writeByte((byte) (mHide ? 1 : 0));
        dest.writeFloat(mAlpha);
    }


    public CollageInfo copy() {

        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        CollageInfo dst = new CollageInfo(parcel);
        parcel.recycle();
        ImageOb ob = PEHelper.initImageOb(getImageObject());
        dst.getImageObject().setTag(null != ob ? ob.copy() : null);

        if (null != mBG && null != dst.mBG) {
            dst.mBG.setTag(((PipBgParam) mBG.getTag()).copy());
        }
        return dst;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollageInfo> CREATOR = new Creator<CollageInfo>() {
        @Override
        public CollageInfo createFromParcel(Parcel in) {
            return new CollageInfo(in);
        }

        @Override
        public CollageInfo[] newArray(int size) {
            return new CollageInfo[size];
        }
    };


    /**
     * @param imageObject
     */
    public CollageInfo(PEImageObject imageObject) {
        mImageObject = imageObject;
        mId = this.hashCode();
    }

    public PEImageObject getImageObject() {
        return mImageObject;
    }


    /**
     * 替换媒体
     *
     * @param mediaObject
     */
    public void setMedia(PEImageObject mediaObject) {
        mImageObject = mediaObject;
    }

    /**
     * 添加滤镜
     */
    public void changeFilterList(List<VisualFilterConfig> list) {
        if (mImageObject != null) {
            try {
                mImageObject.changeFilterList(list);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 移出滤镜抠图
     */
    public void removeCutout() {
        if (null != mImageObject) {
            List<VisualFilterConfig> configList = mImageObject.getFilterList();
            if (configList != null) {
                List<VisualFilterConfig> filterList = new ArrayList<>();
                for (int i = 0; i < configList.size(); i++) {
                    if (!(configList.get(i) instanceof VisualFilterConfig.ChromaKey)) {
                        filterList.add(configList.get(i).copy());
                    }
                }
                changeFilterList(filterList);
            }
        }
    }

    /***
     * 判断画中画是否一致
     * @param info
     * @return
     */
    public boolean equals(CollageInfo info) {
        if (null != info) {
            return
                    TextUtils.equals(getImageObject().getMediaPath(), info.getImageObject().getMediaPath())
//                            && getId() == info.getId()
                            && getImageObject().getShowRectF().equals(info.getImageObject().getShowRectF())
                            && getImageObject().getAngle() == info.getImageObject().getAngle()
                            && mHide == info.mHide
                            && mAlpha == info.mAlpha;

        } else {
            return false;
        }
    }


    /***
     * 移动到草稿箱
     * @param basePath
     */
    @Override
    public CollageInfo moveToDraft(String basePath) {
        if (FileUtils.isExist(basePath)) {
            //1.mix图片合成
            ImageOb tmp = PEHelper.initImageOb(getImageObject());
            if (getImageObject().getMediaPath().startsWith(PathUtils.getTempPath())) {
                mImageObject = getImageObject().moveToDraft(basePath);
                mImageObject.setTag(tmp);
            }

            //2.自定义抠图
            ImageOb ob = PEHelper.initImageOb(getImageObject());
            if (!TextUtils.isEmpty(ob.getMaskPath()) && new File(ob.getMaskPath()).exists()) {
                if (!ob.getMaskPath().startsWith(basePath)) {
                    File file = new File(ob.getMaskPath());
                    File dst = new File(basePath, file.getName());
                    FileUtils.syncCopyFile(file, dst, null);
                    ob.setMaskPath(dst.getAbsolutePath());
                }
            } else {
                ob.setMaskPath(null);
            }

            //3.人像美发
            ob = PEHelper.initImageOb(getImageObject());
            FilterInfo filterInfo = ob.getBeauty();
            if (null != filterInfo) {
                filterInfo.moveToDraft(basePath);
            }
        }
        return this;
    }

    @Override
    public int getId() {
        return mId;
    }


    private boolean mHide = false;

    @Override
    public boolean isHide() {
        return mHide;
    }

    @Override
    public void setHide(boolean hide) {
        mHide = hide;
        getImageObject().setAlpha(mHide ? 0f : mAlpha); //完全透明
    }

    private float mAlpha = 1.0f;

    @Override
    public float getAlpha() {
        return mAlpha;
    }

    @Override
    public void setAlpha(float alpha) {
        mAlpha = alpha;
        getImageObject().setAlpha(mAlpha);
    }


    public PEImageObject getBG() {
        return mBG;
    }


    public void setBG(PEImageObject bgBlur) {
        mBG = bgBlur;
    }

    @Override
    public String toString() {
        return "CollageInfo{" +
                " hash =" + hashCode() +
                ", mMediaObject=" + mImageObject +
//                ", mAlpha='" + mAlpha + '\'' +
//                ", bg='" + mBG + '\'' +
                ", mId=" + mId +
                '}';
    }

}
