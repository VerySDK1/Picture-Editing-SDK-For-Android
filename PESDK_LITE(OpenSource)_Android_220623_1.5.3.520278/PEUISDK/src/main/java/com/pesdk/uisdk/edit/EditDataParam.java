package com.pesdk.uisdk.edit;

import android.util.Log;

import com.pesdk.uisdk.bean.EffectsTag;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.bean.IEditData;
import com.pesdk.uisdk.edit.bean.IParam;
import com.pesdk.uisdk.edit.bean.IUndo;
import com.vecore.models.EffectInfo;

import java.util.ArrayList;

/**
 * 拆分EditDataHandler中的数据
 */
class EditDataParam implements IParam, IUndo, IEditData {
    private static final String TAG = "EditDataParam";
    /**
     * 新版文字
     */
    private final ArrayList<WordInfoExt> mWordNewList = new ArrayList<>();
    /**
     * 贴纸
     */
    private final ArrayList<StickerInfo> mStickerList = new ArrayList<>();

    /**
     * 画中画
     */
    private final ArrayList<CollageInfo> mCollageInfoList = new ArrayList<>();
    /**
     * 涂鸦
     */
    private final ArrayList<GraffitiInfo> mGraffitiInfoList = new ArrayList<>();

    /**
     * 特效
     */
    private final ArrayList<EffectInfo> mEffectInfoList = new ArrayList<>();


    /**
     * 滤镜 、调色
     */
    private final ArrayList<FilterInfo> mFilterList = new ArrayList<>();


    /**
     * 消除笔、裁剪、景深、背景 、镜像 ，最大size==1
     */
    private ExtImageInfo mImageInfo = new ExtImageInfo();

    /**
     * 边框（最大size==1， 一个媒体仅一个边框且预览比例与边框一致）
     */
    private final ArrayList<FrameInfo> mFrameInfoList = new ArrayList<>();

    /**
     * 叠加 绑定在虚拟视频，可能有很多个(与画中画类似)
     */
    private final ArrayList<CollageInfo> mOverLayList = new ArrayList<>();


    //记录临时变量: 手动设置比例
    @Crop.CropMode
    private int mProportionMode = Crop.CROP_FREE;

    private float mProportionValue = -1;


    /**
     *
     */
    void setShortVideoInfo(VirtualIImageInfo imageInfo) {
        restore(imageInfo);
        mProportionMode = imageInfo.getProportionMode();
        mProportionValue = imageInfo.getProportionValue();
    }


    /**
     * 撤销图层合并时，恢复虚拟图片的数据
     */
    public void restore(VirtualIImageInfo imageInfo) {

        setWordNewList((ArrayList<WordInfoExt>) imageInfo.getWordInfoList()); //字幕
        setStickerList((ArrayList<StickerInfo>) imageInfo.getStickerList());//贴纸
        setGraffitiList((ArrayList<GraffitiInfo>) imageInfo.getGraffitiList()); //涂鸦(画笔)


        setCollageList((ArrayList<CollageInfo>) imageInfo.getCollageInfos()); //图层
        setOverLayList((ArrayList<CollageInfo>) imageInfo.getOverlayList());//叠加
        setFrameList((ArrayList<FrameInfo>) imageInfo.getBorderList());//边框


        ExtImageInfo extImageInfo = imageInfo.getExtImageInfo();
        setPESceneList(extImageInfo != null ? extImageInfo.copy() : null);

        ArrayList<FilterInfo> tmp = new ArrayList<>();
        if (null != extImageInfo.getFilter()) {
            tmp.add(extImageInfo.getFilter());
        }
        if (null != extImageInfo.getAdjust()) {
            tmp.add(extImageInfo.getAdjust());
        }
        setFilterList(tmp); //虚拟图片没有美颜等参数


    }


    void setPESceneList(ExtImageInfo extImageInfo) {
        mImageInfo = extImageInfo;
    }


    void setWordNewList(ArrayList<WordInfoExt> wordList) {
        mWordNewList.clear();
        if (wordList != null && wordList.size() > 0) {
            mWordNewList.addAll(wordList);
        }
    }


    void setStickerList(ArrayList<StickerInfo> stickerList) {
        mStickerList.clear();
        if (stickerList != null && stickerList.size() > 0) {
            mStickerList.addAll(stickerList);
        }
    }

    void setFrameList(ArrayList<FrameInfo> frameInfoList) {
        mFrameInfoList.clear();
        if (frameInfoList != null && frameInfoList.size() > 0) {
            mFrameInfoList.addAll(frameInfoList);
        }
    }


    @Override
    public ArrayList<WordInfoExt> getWordList() {
        return mWordNewList;
    }

    @Override
    public ArrayList<StickerInfo> getStickerList() {
        return mStickerList;
    }

    @Override
    public ArrayList<GraffitiInfo> getGraffitList() {
        return mGraffitiInfoList;
    }

    @Override
    public ArrayList<CollageInfo> getCollageList() {
        return mCollageInfoList;
    }

    @Override
    public ArrayList<EffectInfo> getEffectList() {
        return mEffectInfoList;
    }

    @Override
    public ArrayList<FilterInfo> getFilterList() {
        return mFilterList;
    }

    @Override
    public ExtImageInfo getExtImage() {
        return mImageInfo;
    }


    @Override
    public ArrayList<FrameInfo> getFrameList() {
        return mFrameInfoList;
    }

    @Override
    public ArrayList<CollageInfo> getOverLayList() {
        return mOverLayList;
    }


    @Override
    @Crop.CropMode
    public int getProportionMode() {
        return mProportionMode;
    }

    @Override
    public float getProportionValue() {
        return mProportionValue;
    }


    void setCollageList(ArrayList<CollageInfo> list) {
        mCollageInfoList.clear();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                CollageInfo info = list.get(i);
                if (info == null) {
                    continue;
                }
                mCollageInfoList.add(info);
            }
        }
    }


    void setGraffitiList(ArrayList<GraffitiInfo> graffiti) {
        mGraffitiInfoList.clear();
        if (graffiti != null && graffiti.size() > 0) {
            mGraffitiInfoList.addAll(graffiti);
        }
    }


    /**
     * 恢复特效
     */
    void setEffectList(ArrayList<EffectInfo> effects) {
        mEffectInfoList.clear();
        if (effects != null && effects.size() > 0) {
            for (EffectInfo info : effects) {
                mEffectInfoList.add(info);
            }
        }
    }

    /**
     * 恢复滤镜
     */
    void setFilterList(ArrayList<FilterInfo> infos) {
        mFilterList.clear();
        if (infos != null && infos.size() > 0) {
            mFilterList.addAll(infos);
        }
    }

    /**
     * 恢复叠加
     */
    void setOverLayList(ArrayList<CollageInfo> list) {
        mOverLayList.clear();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                CollageInfo info = list.get(i);
                if (info == null) {
                    continue;
                }
                mOverLayList.add(info);
            }
        }
    }


    /**
     * 设置播放器比例
     */
    void setProportionMode(@Crop.CropMode int proportionMode, float value) {
        mProportionMode = proportionMode;
        mProportionValue = value;
    }

    @Override
    public ArrayList getCloneWordNewInfos() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < mWordNewList.size(); i++) {
            arrayList.add(mWordNewList.get(i).copy());
        }
        return arrayList;
    }

    //贴纸
    @Override
    public ArrayList getCloneStickerInfos() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < mStickerList.size(); i++) {
            arrayList.add(mStickerList.get(i).copy());
        }
        return arrayList;
    }

    //图层
    @Override
    public ArrayList<CollageInfo> getCloneCollageInfos() {
        ArrayList arrayList = new ArrayList();
        for (CollageInfo info : mCollageInfoList) {
            arrayList.add(info.copy());
        }
        return arrayList;
    }


    //涂鸦
    @Override
    public ArrayList<GraffitiInfo> getCloneGraffitiInfos() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < mGraffitiInfoList.size(); i++) {
            arrayList.add(mGraffitiInfoList.get(i).copy());
        }
        return arrayList;
    }

    //特效
    @Override
    public ArrayList<EffectInfo> getCloneEffects() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < mEffectInfoList.size(); i++) {
            EffectInfo clone = mEffectInfoList.get(i).copy();
            EffectsTag tag = (EffectsTag) clone.getTag();
            if (tag != null) {
                clone.setTag(tag.copy());
            }
            arrayList.add(clone);
        }
        return arrayList;
    }

    //主图片
    @Override
    public ExtImageInfo getCloneScene() {
        return mImageInfo.copy();
    }

    //主图片
    @Override
    public ArrayList<ExtImageInfo> getCloneSceneList() {
        ArrayList<ExtImageInfo> tmp = new ArrayList<>();
        tmp.add(mImageInfo.copy());
        return tmp;
    }


    //滤镜|调色
    @Override
    public ArrayList<FilterInfo> getCloneFilterInfos() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < mFilterList.size(); i++) {
            arrayList.add(mFilterList.get(i).copy());
        }
        return arrayList;
    }

    //边框
    public ArrayList<FrameInfo> getCloneFrameInfos() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < mFrameInfoList.size(); i++) {
            arrayList.add(mFrameInfoList.get(i).copy());
        }
        return arrayList;
    }

    @Override
    public ArrayList<CollageInfo> getCloneOverLayList() {
        ArrayList arrayList = new ArrayList();
        for (CollageInfo collageInfo : mOverLayList) {
            arrayList.add(collageInfo.copy());
        }
        return arrayList;
    }


    @Override
    public WordInfoExt getWordNewInfo(int index) {
        if (index < 0 || index >= mWordNewList.size()) {
            return null;
        }
        return mWordNewList.get(index);
    }

    @Override
    public StickerInfo getStickerInfo(int index) {
        if (index < 0 || index >= mStickerList.size()) {
            return null;
        }
        return mStickerList.get(index);
    }

    @Override
    public CollageInfo getOverLay(int index) {
        if (index < 0 || index >= mOverLayList.size()) {
            return null;
        }
        return mOverLayList.get(index);

    }

    @Override
    public CollageInfo getPip(int index) {
        if (index < 0 || index >= mCollageInfoList.size()) {
            return null;
        }
        return mCollageInfoList.get(index);
    }


    /**
     * 滤镜
     */
    @Override
    public FilterInfo getFilter() {
        int len = mFilterList.size();
        for (int i = 0; i < len; i++) {
            FilterInfo tmp = mFilterList.get(i);
            if (tmp.getLookupConfig() != null) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * 调色
     */
    @Override
    public FilterInfo getAdjust() {
        int len = mFilterList.size();
        for (int i = 0; i < len; i++) {
            FilterInfo tmp = mFilterList.get(i);
            if (tmp.getMediaParamImp() != null) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * 美颜
     */
    @Override
    public FilterInfo getBeauty() {
        int len = mFilterList.size();
        for (int i = 0; i < len; i++) {
            FilterInfo tmp = mFilterList.get(i);
            if (tmp.getBeauty() != null) {
                return tmp;
            }
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        reset();
    }

    public void reset() {
        mImageInfo.reset();
        mWordNewList.clear();
        mStickerList.clear();
        mGraffitiInfoList.clear();

        mCollageInfoList.clear();
        mOverLayList.clear();
        mFrameInfoList.clear();

        mEffectInfoList.clear();
        mFilterList.clear();
//
//
//        mProportionMode = Crop.CROP_ORIGINAL;
//        mProportionValue = -1;
    }


    /**
     * 图片合成时，记录当前的参数
     */
    public void copy2VirtualImageInfo(VirtualIImageInfo virtualIImageInfo) {

        virtualIImageInfo.setExtImageInfo(getCloneScene());
        virtualIImageInfo.setWordInfoList(getCloneWordNewInfos());
        virtualIImageInfo.setStickerInfos(getCloneStickerInfos());
        virtualIImageInfo.setGraffitiList(getCloneGraffitiInfos());

        virtualIImageInfo.setCollageInfos(getCloneCollageInfos());
        virtualIImageInfo.setOverlayList(getCloneOverLayList());
        virtualIImageInfo.setFrameInfoList(getCloneFrameInfos());
        virtualIImageInfo.setEffectInfoList(getCloneEffects());

        Log.e(TAG, "copy2VirtualImageInfo: " + mProportionMode + " >" + mProportionValue);
        virtualIImageInfo.setProportionMode(mProportionMode, mProportionValue);

    }

}
