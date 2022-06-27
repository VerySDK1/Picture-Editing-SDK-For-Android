package com.pesdk.uisdk.bean.model;

import android.graphics.RectF;

import com.vecore.VirtualImage;
import com.vecore.models.FlipType;
import com.vecore.models.caption.CaptionLiteObject;

import java.util.ArrayList;

import androidx.annotation.Keep;

/**
 * 仅贴纸
 */
@Keep
public class StickerInfo extends ISubStickerInfo {


    public RectF getRectOriginal() {
        return mRectOriginal;
    }

    private static final String TAG = "StickerInfo";

    /**
     * @param rectOriginal 单位：像素
     */
    public void setRectOriginal(RectF rectOriginal) {
        mRectOriginal = new RectF(rectOriginal);
    }

    //没有角度时，图片的显示位置（插入liteObject时，显示区域必须是旋转角度为0时的位置）
    private RectF mRectOriginal = new RectF();


    public float getPreviewAsp() {
        return nPreviewAsp;
    }

    public void setPreviewAsp(float nPreviewAsp) {
        this.nPreviewAsp = nPreviewAsp;
    }


    private float nPreviewAsp = 1f; //编辑时的视频比例


    public StickerInfo copy() {
        return new StickerInfo(this);
    }

    public void set(StickerInfo info) {
        this.mRectOriginal = new RectF(mRectOriginal);
        this.nPreviewAsp = info.nPreviewAsp;
        this.left = info.left;
        this.top = info.top;
        this.id = info.id;
        this.styleId = info.styleId;
        this.mCategory = info.mCategory;
        this.mIcon = info.mIcon;
        this.mFlipType = info.mFlipType;
        setDisf(info.getDisf());
        setCenterxy(info.getCenterxy());
        this.mResourceId = info.getResourceId();
    }


    @Override
    public int getId() {
        return id;
    }

    private float mAngle;

    @Override
    public void setRotateAngle(float rotateAngle) {
        mAngle = rotateAngle;
        setChanged();
    }


    public StickerInfo() {
        super();
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof StickerInfo) {
            StickerInfo info = (StickerInfo) o;
            return getId() == info.getId()
                    && getRotateAngle() == info.getRotateAngle()
                    && getTextColor() == info.getTextColor()
                    && getDisf() == info.getDisf()
                    && getCenterxy() == info.getCenterxy()
                    && getFlipType() == info.getFlipType()
                    && getStyleId() == info.getStyleId();
        } else {
            return false;
        }
    }


    public StickerInfo(StickerInfo info) {
        this.mRectOriginal = new RectF(info.mRectOriginal);
        this.mAngle = info.mAngle;
        this.nPreviewAsp = info.nPreviewAsp;
        this.left = info.left;
        this.top = info.top;
        this.id = info.id;
        this.mCategory = info.mCategory;
        this.mIcon = info.mIcon;
        this.mFlipType = info.mFlipType;
        setStyleId(info.getStyleId());
        ArrayList<CaptionLiteObject> temps = info.getList();
        int len = temps.size();
        for (int i = 0; i < len; i++) {
            this.mArrayList.add(new CaptionLiteObject(temps.get(i)));
        }
        setDisf(info.getDisf());
    }

    public float getRotateAngle() {
        return mAngle;
    }


    public float getDisf() {
        return mDisf;
    }

    private float mDisf = 1f;

    @Override
    public void setDisf(float disf) {
        mDisf = disf;
        setChanged();
    }

    //贴纸分类代码、贴纸图标
    private String mCategory;
    private String mIcon;

    public String getIcon() {
        return mIcon;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String mCategory, String icon) {
        this.mCategory = mCategory;
        this.mIcon = icon;
    }

    /**
     * 每个特效的jni对象列表
     */
    private ArrayList<CaptionLiteObject> mArrayList = new ArrayList<CaptionLiteObject>();

    public ArrayList<CaptionLiteObject> getList() {
        return mArrayList;
    }

    /**
     *
     */
    public void recycle() {
        mArrayList.clear();
    }


    public void addSubObject(CaptionLiteObject subobj) {
        mArrayList.add(subobj);
    }

    /**
     * 移动到草稿箱
     *
     * @param basePath
     */
    public void moveToDraft(String basePath) {
        int len = mArrayList.size();
        for (int i = 0; i < len; i++) {
            CaptionLiteObject tmp = mArrayList.get(i).moveToDraft(basePath);
            if (null != tmp) {
                mArrayList.set(i, tmp);
            }
        }
    }

    /**
     * 实时清理播放器中的对象
     */
    public void removeListLiteObject(VirtualImage virtualImage) {
        if (null != mArrayList) {
            int count = mArrayList.size();
            for (int j = 0; j < count; j++) { //删除旧的对象
                virtualImage.deleteSubtitleObject(mArrayList.get(j));
            }
        }

    }


    @Override
    public String toString() {
        return "StickerInfo{" +
                " hash:" + hashCode() +
                ", mRectOriginal=" + mRectOriginal +
                ", nPreviewAsp=" + nPreviewAsp +
                ", mAngle=" + mAngle +
                ", mDisf=" + mDisf +
                ", mCategory='" + mCategory + '\'' +
//                ", mIcon='" + mIcon + '\'' +
//                ", mArrayList=" + mArrayList +
                '}';
    }

    /**
     * 镜像
     */
    private FlipType mFlipType = FlipType.FLIP_TYPE_NONE;

    /**
     * 获取镜像类型
     */
    public FlipType getFlipType() {
        return mFlipType;
    }

    /**
     * 设置镜像类型
     */
    public void setFlipType(FlipType flipType) {
        this.mFlipType = flipType;
        if (mArrayList != null) {
            for (CaptionLiteObject object : mArrayList) {
                object.setFlipType(mFlipType);
            }
        }
    }

    /**
     * 资源id
     */
    private String mResourceId;

    public long getStart() {
        return 0;
    }

    public long getEnd() {
        return 3000;
    }


    public String getResourceId() {
        return mResourceId;
    }

    public void setResourceId(String resourceId) {
        mResourceId = resourceId;
    }


    public void update(VirtualImage virtualImage) {
        ArrayList<CaptionLiteObject> tmp = getList();
        int count = tmp.size();
        for (int j = 0; j < count; j++) {
            virtualImage.updateSubtitleObject(tmp.get(j));  //实时插入新的lite对象到虚拟视频
        }
    }
}
