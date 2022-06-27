package com.pesdk.uisdk.bean.model.subtitle;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 字幕模板
 */
public class SubTemplateInfo implements Parcelable {

    //本地路径
    private String localPath;

    private int ver;
    private String type;
    private String name;
    private int width;
    private int height;
    private float centerX;
    private float centerY;
    private float duration;
    private SubText[] text;
    private SubBackground background;


    /**
     * 复制
     */
    public SubTemplateInfo copy() {
        SubTemplateInfo info = new SubTemplateInfo();
        info.setLocalPath(localPath);
        info.setVer(ver);
        info.setType(type);
        info.setName(name);
        info.setWidth(width);
        info.setHeight(height);
        info.setCenterX(centerX);
        info.setCenterY(centerY);
        info.setDuration(duration);
        info.setText(text);
        if (background != null) {
            info.setBackground(background.copy());
        }
        return info;
    }




    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public SubText[] getText() {
        return text;
    }

    public void setText(SubText[] text) {
        if (text == null || text.length <= 0) {
            this.text = null;
        } else {
            this.text = new SubText[text.length];
            for (int i = 0; i < text.length; i++) {
                this.text[i] = text[i].copy();
            }
        }

    }

    public SubBackground getBackground() {
        return background;
    }

    public void setBackground(SubBackground background) {
        this.background = background;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.localPath);
        dest.writeInt(this.ver);
        dest.writeString(this.type);
        dest.writeString(this.name);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeFloat(this.centerX);
        dest.writeFloat(this.centerY);
        dest.writeFloat(this.duration);
        dest.writeTypedArray(this.text, flags);
        dest.writeParcelable(this.background, flags);
    }

    public SubTemplateInfo() {
    }

    protected SubTemplateInfo(Parcel in) {
        this.localPath = in.readString();
        this.ver = in.readInt();
        this.type = in.readString();
        this.name = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.centerX = in.readFloat();
        this.centerY = in.readFloat();
        this.duration = in.readFloat();
        this.text = in.createTypedArray(SubText.CREATOR);
        this.background = in.readParcelable(SubBackground.class.getClassLoader());
    }

    public static final Creator<SubTemplateInfo> CREATOR = new Creator<SubTemplateInfo>() {
        @Override
        public SubTemplateInfo createFromParcel(Parcel source) {
            return new SubTemplateInfo(source);
        }

        @Override
        public SubTemplateInfo[] newArray(int size) {
            return new SubTemplateInfo[size];
        }
    };
}
