package com.pesdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

@Keep
public class DataBean implements Parcelable {
    public DataBean() {
    }

    public DataBean(String file, long updatetime) {
        this.file = file;
        this.updatetime = updatetime;
    }

    public DataBean(DataBean src) {
        this.id = src.id;
        this.cover = src.cover;
        this.width = src.width;
        this.height = src.height;
        this.file = src.file;
        this.name = src.name;
        this.video = src.video;
        this.text_need = src.text_need;
        this.video_need = src.video_need;
        this.picture_need = src.picture_need;
        this.updatetime = src.updatetime;
        this.desc = src.desc;
    }

    /**
     * id	String	1013393
     * ufid	String	36321649
     * name	String
     * file	String	http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/frame/16399879489224/data.png
     * cover	String	http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/frame/16399879489224/cover.png
     * updatetime	String
     * height	String	0
     * width	String	0
     * video	String
     * appkey	String	39c98bc39ca150af
     */

    private String id;
    private String cover;
    private int width;
    private int height;
    private String file;
    private String name;
    private String desc;
    private String video;
    private int text_need;
    private int video_need;
    private int picture_need;
    private long updatetime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public int getText_need() {
        return text_need;
    }

    public void setText_need(int text_need) {
        this.text_need = text_need;
    }

    public int getVideo_need() {
        return video_need;
    }

    public void setVideo_need(int video_need) {
        this.video_need = video_need;
    }

    public int getPicture_need() {
        return picture_need;
    }

    public void setPicture_need(int picture_need) {
        this.picture_need = picture_need;
    }

    public long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(long updatetime) {
        this.updatetime = updatetime;
    }


    public int getMId() {
        return getFile().hashCode();
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "id='" + id + '\'' +
                ", cover='" + cover + '\'' +
                ", file='" + file + '\'' +
                ", name='" + name + '\'' +
                ", updatetime=" + updatetime +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.cover);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.file);
        dest.writeString(this.name);
        dest.writeString(this.desc);
        dest.writeString(this.video);
        dest.writeInt(this.text_need);
        dest.writeInt(this.video_need);
        dest.writeInt(this.picture_need);
        dest.writeLong(this.updatetime);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.cover = source.readString();
        this.width = source.readInt();
        this.height = source.readInt();
        this.file = source.readString();
        this.name = source.readString();
        this.desc = source.readString();
        this.video = source.readString();
        this.text_need = source.readInt();
        this.video_need = source.readInt();
        this.picture_need = source.readInt();
        this.updatetime = source.readLong();
    }

    protected DataBean(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<DataBean> CREATOR = new Parcelable.Creator<DataBean>() {
        @Override
        public DataBean createFromParcel(Parcel source) {
            return new DataBean(source);
        }

        @Override
        public DataBean[] newArray(int size) {
            return new DataBean[size];
        }
    };
}
