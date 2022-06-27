package com.pesdk.bean.template;

import android.os.Parcel;
import android.os.Parcelable;

import com.vecore.models.MediaObject;
import com.vecore.models.PEImageObject;
import com.vecore.models.Transition;

import androidx.annotation.Keep;

import static com.pesdk.bean.template.LockingType.LockingNone;


/**
 * 中转"剪同款ReplaceMedia"
 */
@Keep
public class RReplaceMedia implements Parcelable {
    /**
     * 标识符
     */
    private String identifier;
    /**
     * 编组 0表示未编组
     */
    private int group;
    /**
     * 编辑类型 画中画 场景 字幕
     */
    private final ReplaceType mediaType;
    /**
     * 开始时间
     */
    private int timeLineStart;
    /**
     * 时长
     */
    private int duration;
    /**
     * 替换类型 锁定 仅图片 仅视频 不限制
     */
    private LockingType lockingType = LockingNone;
    /**
     * 名字路径 或者分割
     */
    private String name;
    /**
     * 锁定
     */
    private boolean locking = false;
    /**
     * 下标
     */
    private int position = 0;
    /**
     * 媒体
     */
    private PEImageObject mediaObject;
    /**
     * 背景媒体
     */
    private PEImageObject backgroundMedia;
    private int backgroundColor;
    /**
     * 转场
     */
    private Transition transitionInfo;
    /**
     * 字幕
     */
    private String mCover;


    public RReplaceMedia(ReplaceType mediaType) {
        this.mediaType = mediaType;
    }

    public RReplaceMedia(String identifier, ReplaceType mediaType) {
        this.identifier = identifier;
        this.mediaType = mediaType;
    }

//    public RReplaceMedia copy() {
//        RReplaceMedia media = new RReplaceMedia(identifier, mediaType);
//        media.setDuration(duration);
//        media.setGroup(group);
//        media.setLockingType(lockingType);
//        media.setName(name);
//        media.setLocking(locking);
//        media.setPosition(position);
//        media.setTimeLineStart(timeLineStart);
//        if (mediaObject != null) {
//            media.setMediaObject(mediaObject.copy());
//        }
//        if (backgroundMedia != null) {
//            media.setBackgroundMedia(backgroundMedia.copy());
//        }
//        media.setBackgroundColor(backgroundColor);
//        if (transitionInfo != null) {
//            media.setTransitionInfo(transitionInfo.copy());
//        }
//        media.setCover(mCover);
//        return media;
//    }

    /**
     * 相同
     */
    public boolean isSame(RReplaceMedia media) {
        if (media == null) {
            return false;
        }
        return media.getMediaType() == mediaType && media.getPosition() == position && group == media.getGroup();
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public LockingType getLockingType() {
        return lockingType;
    }

    public void setLockingType(LockingType lockingType) {
        this.lockingType = lockingType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocking() {
        return locking;
    }

    public void setLocking(boolean locking) {
        this.locking = locking;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ReplaceType getMediaType() {
        return mediaType;
    }

    public PEImageObject getMediaObject() {
        return mediaObject;
    }

    public void setMediaObject(PEImageObject mediaObject) {
        this.mediaObject = mediaObject;
        if (mediaObject != null) {
            name = mediaObject.getMediaPath();
        }
    }

    public PEImageObject getBackgroundMedia() {
        return backgroundMedia;
    }

    public void setBackgroundMedia(PEImageObject backgroundMedia) {
        this.backgroundMedia = backgroundMedia;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Transition getTransitionInfo() {
        return transitionInfo;
    }

    public void setTransitionInfo(Transition transitionInfo) {
        this.transitionInfo = transitionInfo;
    }

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }


    public int getTimeLineStart() {
        return timeLineStart;
    }

    public void setTimeLineStart(int timeLineStart) {
        this.timeLineStart = timeLineStart;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeInt(timeLineStart);
        dest.writeString(mCover);
        dest.writeParcelable(transitionInfo, flags);
        dest.writeInt(backgroundColor);
        dest.writeParcelable(backgroundMedia, flags);
        dest.writeParcelable(mediaObject, flags);
        dest.writeInt(this.position);
        dest.writeInt(this.mediaType == null ? -1 : this.mediaType.ordinal());
        dest.writeInt(this.duration);
        dest.writeInt(this.group);
        dest.writeInt(this.lockingType == null ? -1 : this.lockingType.ordinal());
        dest.writeString(this.name);
        dest.writeByte(this.locking ? (byte) 1 : (byte) 0);
    }

    protected RReplaceMedia(Parcel in) {
        identifier = in.readString();
        this.timeLineStart = in.readInt();
        this.mCover = in.readString();
        this.transitionInfo = in.readParcelable(Transition.class.getClassLoader());
        this.backgroundColor = in.readInt();
        this.backgroundMedia = in.readParcelable(MediaObject.class.getClassLoader());
        this.mediaObject = in.readParcelable(MediaObject.class.getClassLoader());
        this.position = in.readInt();
        int tmpMediaType = in.readInt();
        this.mediaType = tmpMediaType == -1 ? null : ReplaceType.values()[tmpMediaType];
        this.duration = in.readInt();
        this.group = in.readInt();
        int tmpLockingType = in.readInt();
        this.lockingType = tmpMediaType == -1 ? null : LockingType.values()[tmpLockingType];
        this.name = in.readString();
        this.locking = in.readByte() != 0;
    }

    public static final Creator<RReplaceMedia> CREATOR = new Creator<RReplaceMedia>() {
        @Override
        public RReplaceMedia createFromParcel(Parcel source) {
            return new RReplaceMedia(source);
        }

        @Override
        public RReplaceMedia[] newArray(int size) {
            return new RReplaceMedia[size];
        }
    };
}
