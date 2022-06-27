package com.pesdk.uisdk.bean.model.subtitle;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.pesdk.uisdk.util.Utils;
import com.vecore.models.caption.FrameInfo;

public class SubBackground implements Parcelable {

    //类型
    private String type;
    private int count;
    private SubFrameArray[] frameArray;
    private int stretchability;
    private SubBackgroundAnim[] anims;

    public SubBackground copy() {
        SubBackground background = new SubBackground();
        background.setType(type);
        background.setCount(count);
        background.setFrameArray(frameArray);
        background.setStretchability(stretchability);
        background.setAnims(anims);
        return background;
    }

    public SparseArray<FrameInfo> getFrameInfo(String localPath, String name) {
        if (frameArray == null || frameArray.length <= 0) {
            return null;
        } else {
            if ("image".equals(type)) {
                SparseArray<FrameInfo> frameList = new SparseArray<>();
                for (SubFrameArray array : frameArray) {
                    int time = Utils.s2ms(array.getTime());
                    frameList.put(time, new com.vecore.models.caption.FrameInfo(time, localPath + "/" + name + array.getPic() + ".png"));
                }
                return frameList;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public SubFrameArray[] getFrameArray() {
        return frameArray;
    }

    public void setFrameArray(SubFrameArray[] frameArray) {
        if (frameArray == null || frameArray.length <= 0) {
            this.frameArray = null;
        } else {
            this.frameArray = new SubFrameArray[frameArray.length];
            for (int i = 0; i < frameArray.length; i++) {
                this.frameArray[i] = frameArray[i].copy();
            }
        }
    }

    public int getStretchability() {
        return stretchability;
    }

    public void setStretchability(int stretchability) {
        this.stretchability = stretchability;
    }

    public SubBackgroundAnim[] getAnims() {
        return anims;
    }

    public void setAnims(SubBackgroundAnim[] anims) {
        if (anims == null || anims.length <= 0) {
            this.anims = null;
        } else {
            this.anims = new SubBackgroundAnim[anims.length];
            for (int i = 0; i < anims.length; i++) {
                this.anims[i] = anims[i].copy();
            }
        }
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeInt(this.count);
        dest.writeTypedArray(this.frameArray, flags);
        dest.writeInt(this.stretchability);
        dest.writeTypedArray(this.anims, flags);
    }

    public SubBackground() {
    }

    protected SubBackground(Parcel in) {
        this.type = in.readString();
        this.count = in.readInt();
        this.frameArray = in.createTypedArray(SubFrameArray.CREATOR);
        this.stretchability = in.readInt();
        this.anims = in.createTypedArray(SubBackgroundAnim.CREATOR);
    }

    public static final Creator<SubBackground> CREATOR = new Creator<SubBackground>() {
        @Override
        public SubBackground createFromParcel(Parcel source) {
            return new SubBackground(source);
        }

        @Override
        public SubBackground[] newArray(int size) {
            return new SubBackground[size];
        }
    };
}
