package com.pesdk.uisdk.bean.model;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JIAN
 * @create 2019/12/2
 * @Describe
 */
@Deprecated
public class AnimInfo implements Parcelable {

    public String getName() {
        return name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<IRect> getList() {
        return mList;
    }

    private String name;
    private String cover;
    private List<IRect> mList = new ArrayList<>();

    public AnimInfo(String name, String text, String cover) {
        this.name = name;
        this.cover = cover;
        mList = new ArrayList<>();
        if (!TextUtils.isEmpty(text)) {
            init(text);
        }
    }

    private void init(String src) {
        try {
            JSONArray jarr = new JSONArray(src);
            int len = jarr.length();
            mList.clear();
            float frameDuration = 0.04f;
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = jarr.getJSONObject(i);
                JSONArray jsonArray = jsonObject.getJSONArray("c");
                IRect iRect = new IRect(frameDuration * i, new PointF((float) jsonArray.getDouble(0), (float) jsonArray.getDouble(1)), new PointF((float) jsonArray.getDouble(2),
                        (float) jsonArray.getDouble(3)), new PointF((float) jsonArray.getDouble(4), (float) jsonArray.getDouble(5)),
                        new PointF((float) jsonArray.getDouble(6), (float) jsonArray.getDouble(7)));
                mList.add(iRect);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class IRect implements Parcelable {

        private final PointF plt;
        private final PointF prt;
        private final PointF plb;
        private final PointF prb;

        private final float nTime; //当前时刻 单位：S

        public IRect(float nTime, PointF plt, PointF prt, PointF plb, PointF prb) {
            this.nTime = nTime;
            this.plt = plt;
            this.prt = prt;
            this.plb = plb;
            this.prb = prb;
        }

        public float getTime() {
            return nTime;
        }

        public PointF getPlt() {
            return plt;
        }

        public PointF getPrt() {
            return prt;
        }

        public PointF getPlb() {
            return plb;
        }

        public PointF getPrb() {
            return prb;
        }


        @Override
        public String toString() {
            return "IRect{" +
                    "nTime=" + nTime +
                    ", plt=" + plt +
                    ", prt=" + prt +
                    ", plb=" + plb +
                    ", prb=" + prb +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(this.nTime);
            dest.writeParcelable(this.plt, flags);
            dest.writeParcelable(this.prt, flags);
            dest.writeParcelable(this.plb, flags);
            dest.writeParcelable(this.prb, flags);
        }

        protected IRect(Parcel in) {
            this.nTime = in.readFloat();
            this.plt = in.readParcelable(PointF.class.getClassLoader());
            this.prt = in.readParcelable(PointF.class.getClassLoader());
            this.plb = in.readParcelable(PointF.class.getClassLoader());
            this.prb = in.readParcelable(PointF.class.getClassLoader());
        }

        public static final Creator<IRect> CREATOR = new Creator<IRect>() {
            @Override
            public IRect createFromParcel(Parcel source) {
                return new IRect(source);
            }

            @Override
            public IRect[] newArray(int size) {
                return new IRect[size];
            }
        };
    }


    /**
     * 未设置id
     */
    public static final int Unknown = -1;

    /**
     * 网络地址hash
     */
    private int mCode;
    /**
     * id
     */
    private int mAnimId = Unknown;
    private int mAnimType = 0;
    /**
     * 网络路径
     */
    private String mInternetPath;
    /**
     * 本地路径
     */
    private String mFilePath;
    /**
     * 记录当前下载的版本
     */
    public boolean mIsDownloaded = false;
    /**
     * 动画时间
     */
    public float mAnimDuration = 1;
    /**
     * 快慢
     */
    public float mCycleDuration = 0;
    /**
     * 贴纸分类代码
     */
    public String mCategory;
    /**
     * 资源id
     */
    public String mResourceId;
    /**
     * 是否是kok
     */
    public boolean mIsKok;

    public AnimInfo(String name, String cover, int animType, String internetPath) {
        this.name = name;
        this.cover = cover;
        mAnimType = animType;
        mInternetPath = internetPath;
        if (mInternetPath != null) {
            this.mCode = mInternetPath.hashCode();
        }
    }

    public AnimInfo(int animType, float animDuration) {
        mAnimType = animType;
        mAnimDuration = animDuration;
    }

    public AnimInfo copy() {
        AnimInfo info = new AnimInfo(name, cover, mAnimType, mInternetPath);
        info.setAnimId(mAnimId, mIsKok);
        info.setNetworkId(mCategory, mResourceId);
        info.setDownloaded(mIsDownloaded);
        info.setLocalFilePath(mFilePath);
        info.setAnimDuration(mAnimDuration);
        info.setCycleDuration(mCycleDuration);
        return info;
    }


    public String getCategory() {
        return mCategory;
    }

    public String getResourceId() {
        return mResourceId;
    }

    public void setNetworkId(String category, String resourceId) {
        mCategory = category;
        mResourceId = resourceId;
    }

    public void setAnimId(int animId, boolean isKok) {
        mAnimId = animId;
        mIsKok = isKok;
    }

    public void setLocalFilePath(String filePath) {
        mFilePath = filePath;
    }

    public void setDownloaded(boolean downloaded) {
        mIsDownloaded = downloaded;
    }

    public int getAnimId() {
        return mAnimId;
    }

    public String getInternetFile() {
        return mInternetPath;
    }

    public int getAnimType() {
        return mAnimType;
    }

    public void setAnimType(int animType) {
        mAnimType = animType;
    }

    public String getLocalFilePath() {
        return mFilePath;
    }

    public int getCode() {
        return mCode;
    }

    public boolean isDownloaded() {
        return mIsDownloaded && new File(mFilePath).exists();
    }

    public float getAnimDuration() {
        return mAnimDuration;
    }

    public void setAnimDuration(float animDuration) {
        mAnimDuration = animDuration;
    }

    public float getCycleDuration() {
        return mCycleDuration;
    }

    public void setCycleDuration(float cycleDuration) {
        this.mCycleDuration = cycleDuration;
    }

    public boolean isKok() {
        return mIsKok;
    }

    public void setKok(boolean kok) {
        mIsKok = kok;
    }


    /**
     * 唯一指定标识，以后不能再更改
     */
    private static final String VER_TAG = "20201020AnimInfo";
    private static final int VER = 4;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //特别标识
        {
            dest.writeString(VER_TAG);
            dest.writeInt(VER);
        }

        //ver = 4
        dest.writeByte((byte) (mIsKok ? 1 : 0));

        //ver = 3
        dest.writeFloat(mCycleDuration);

        //ver = 2
        dest.writeString(mCategory);
        dest.writeString(mResourceId);

        //ver = 1
        dest.writeFloat(this.mAnimDuration);
        dest.writeInt(this.mCode);
        dest.writeInt(this.mAnimId);
        dest.writeInt(this.mAnimType);
        dest.writeString(this.mInternetPath);
        dest.writeString(this.mFilePath);
        dest.writeByte(this.mIsDownloaded ? (byte) 1 : (byte) 0);

        dest.writeString(this.name);
        dest.writeString(this.cover);
        dest.writeTypedList(this.mList);
    }

    protected AnimInfo(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int ver = in.readInt();
            if (ver >= 4) {
                mIsKok = in.readByte() != 0;
            }
            if (ver >= 3) {
                mCycleDuration = in.readFloat();
            }
            if (ver >= 2) {
                this.mCategory = in.readString();
                this.mResourceId = in.readString();
            }
            if (ver >= 1) {
                this.mAnimDuration = in.readFloat();
                this.mCode = in.readInt();
                this.mAnimId = in.readInt();
                this.mAnimType = in.readInt();
                this.mInternetPath = in.readString();
                this.mFilePath = in.readString();
                this.mIsDownloaded = in.readByte() != 0;
            }
        } else {
            //恢复到读取之前的index
            in.setDataPosition(oldPosition);
        }

        this.name = in.readString();
        this.cover = in.readString();
        this.mList = in.createTypedArrayList(IRect.CREATOR);
    }

    public static final Creator<AnimInfo> CREATOR = new Creator<AnimInfo>() {
        @Override
        public AnimInfo createFromParcel(Parcel source) {
            return new AnimInfo(source);
        }

        @Override
        public AnimInfo[] newArray(int size) {
            return new AnimInfo[size];
        }
    };


    @Override
    public String toString() {
        return "AnimInfo{" +
                "name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", mList=" + mList +
                ", mCode=" + mCode +
                ", mAnimId=" + mAnimId +
                ", mAnimType=" + mAnimType +
                ", mInternetPath='" + mInternetPath + '\'' +
                ", mFilePath='" + mFilePath + '\'' +
                ", mIsDownloaded=" + mIsDownloaded +
                ", mAnimDuration=" + mAnimDuration +
                '}';
    }
}

