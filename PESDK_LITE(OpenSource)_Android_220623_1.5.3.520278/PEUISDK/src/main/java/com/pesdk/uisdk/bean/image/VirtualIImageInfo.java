package com.pesdk.uisdk.bean.image;

import android.content.Context;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.util.DraftFileUtils;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.util.helper.PlayerAspHelper;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.EffectInfo;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 记录虚拟图片所有元素
 */
public class VirtualIImageInfo extends IImage {

    private static final String TAG = "VirtualImageInfo";
    @Deprecated
    PEScene peScene;


    /**
     *
     */
    public VirtualIImageInfo() {
        nCreateTime = System.currentTimeMillis();
        mExtImageInfo = new ExtImageInfo();
    }


    private List<CollageInfo> mCollageInfos = new ArrayList<>(); //画中画

    public List<CollageInfo> getCollageInfos() {
        return mCollageInfos;
    }

    public void setCollageInfos(List<CollageInfo> collageInfos) {
        mCollageInfos = collageInfos;
    }


    public List<CollageInfo> getOverlayList() {
        return mOverlayList;
    }

    public void setOverlayList(List<CollageInfo> overlayList) {
        mOverlayList = overlayList;
    }

    private List<CollageInfo> mOverlayList = new ArrayList<>(); //叠加
    @Deprecated
    private List<EffectInfo> mEffectInfoList = new ArrayList<>();

    private ExtImageInfo mExtImageInfo;

    public ExtImageInfo getExtImageInfo() {
        return mExtImageInfo;
    }

    public void setExtImageInfo(ExtImageInfo info) {
        mExtImageInfo = info;
    }

    public void initBasePip(String path) { //导入第一张图时
        CollageInfo collageInfo = initBaseCollage(path);
        if (null == collageInfo) {
            Log.e(TAG, "initBasePip: " + path);
            return;
        }
        mCollageInfos.add(collageInfo);
        mOriginalProportion = collageInfo.getImageObject().getWidth() * 1.0f / collageInfo.getImageObject().getHeight();
        setProportionMode(Crop.CROP_FREE, mOriginalProportion);
    }

    public static CollageInfo initBaseCollage(String path) {
        try {
            PEImageObject imageObject = new PEImageObject(null, path);
            PEHelper.initImageOb(imageObject);
            imageObject.setShowRectF(new RectF(0, 0, 1f, 1f));
            return new CollageInfo(imageObject);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String getCover() {
        if (FileUtils.isExist(mCover)) {
            return mCover;
        } else {
            try {
                PEImageObject imageObject = mCollageInfos.get(0).getImageObject();
                mCover = Utils.getCover(imageObject, !TextUtils.isEmpty(basePath) ? basePath : PathUtils.getTempPath());
            } catch (Exception e) {
                e.printStackTrace();
                mCover = null;
            }
        }
        return mCover;
    }


    private List<GraffitiInfo> mGaffitiList = new ArrayList<>(); //涂鸦


    public List<EffectInfo> getEffectInfoList() {
        return mEffectInfoList;
    }

    public void setEffectInfoList(List<EffectInfo> effectInfoList) {
        mEffectInfoList = effectInfoList;
    }


    public void setStickerInfos(List<StickerInfo> stickerInfos) {
        mStickerInfos = stickerInfos;
    }

    private List<StickerInfo> mStickerInfos = new ArrayList<>();

    public List<StickerInfo> getStickerList() {
        return mStickerInfos;
    }


    public List<WordInfoExt> getWordInfoList() {
        return mWordInfoList;
    }


    public void setWordInfoList(List<WordInfoExt> wordInfoList) {
        mWordInfoList = wordInfoList;
    }

    private List<WordInfoExt> mWordInfoList = new ArrayList<>();


    public void setGraffitiList(ArrayList<GraffitiInfo> list) {
        mGaffitiList.clear();
        if (null != list && list.size() > 0) {
            mGaffitiList.addAll(list);
        }
    }

    public List<GraffitiInfo> getGraffitiList() {
        return mGaffitiList;
    }


    public void setWatermark(CollageInfo watermark) {
        mWaterMark = watermark;
    }

    @Deprecated
    private CollageInfo mWaterMark;

    public CollageInfo getWatermark() {
        return mWaterMark;
    }


    private static final String KEY_GRAFFITI_LIST = "graffiti_List";
    private static final String KEY_COLLAGE_LIST = "collageList";
    private static final String KEY_MO_LIST = "moInfos";
    //    private static final String KEY_EFFECT_LIST = "mEffectInfos";
    private static final String KEY_BASEPATH = "basePath";
    private static final String KEY_CREATETIME = "nCreateTime";
    private static final String KEY_UDPATE_TIME = "nUpdate";
    private static final String KEY_COVER = "mCover";
    private static final String KEY_DRAFT_TYPE = "nDraftType";
    private static final String KEY_VER = "ver"; //版本
    private static final String KEY_BG_INFO = "mBGInfo";
    private static final String KEY_WORD_LIST = "mWordInfoList";
    private static final String KEY_STICKER_LIST = "mStickerInfos";
    private static final String KEY_ID = "nId";
    private static final String KEY_WATERMARK = "mWatermark";
    private static final String KEY_OVERLAY_LIST = "overlayList";//叠加
    private static final String KEY_FRAME_LIST = "frameList";//边框
    private static final String KEY_PROPORTION_MODE = "proportionMode";//比例类型
    private static final String KEY_PROPORTION_VALUE = "proportionValue";//比例值
    private static final String KEY_PROPORTION_ORIGINAL = "proportionOriginal";//比例值


    @Deprecated
    private static final String KEY_PE_SCENE = "mPEScene";


    @Crop.CropMode
    private int mProportionMode = Crop.CROP_FREE; //free 等效于对应原始比例
    private float mProportionValue = -1;
    private float mOriginalProportion = 1f; //原始比例 (主图|剪同款中的比例）


    public void setOriginalProportion(float originalProportion) {
        mOriginalProportion = originalProportion;
    }

    public float getOriginalProportion() {
        return mOriginalProportion;
    }


    /**
     * 指定虚拟图片的比例(没有边框时有效参数)
     */
    public void setProportionMode(@Crop.CropMode int proportionMode, float value) {
        mProportionMode = proportionMode;
        mProportionValue = value;
    }

    @Crop.CropMode
    public int getProportionMode() {
        return mProportionMode;
    }

    public float getProportionValue() {
        return mProportionValue;
    }


    /**
     * 保存到本地config.json
     */
    public void saveToGSONConfig() {
        DraftFileUtils.write2File(this);
    }

    /**
     * 拆分，可控、方便维护
     *
     * @return
     */
    public String toGSONString() {
        Gson gson = com.pesdk.uisdk.gson.Gson.getInstance().getGson();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_VER, ver);
            jsonObject.put(KEY_UDPATE_TIME, mUpdateTime);
            jsonObject.put(KEY_GRAFFITI_LIST, gson.toJson(mGaffitiList));

            jsonObject.put(KEY_COLLAGE_LIST, gson.toJson(mCollageInfos)); //图层
            jsonObject.put(KEY_FRAME_LIST, gson.toJson(mFrameInfoList));//边框
            jsonObject.put(KEY_OVERLAY_LIST, gson.toJson(mOverlayList)); //叠加
//            jsonObject.put(KEY_MO_LIST, gson.toJson(mMOInfos));
//            jsonObject.put(KEY_EFFECT_LIST, gson.toJson(mEffectInfoList));
            jsonObject.put(KEY_BASEPATH, basePath);
            jsonObject.put(KEY_CREATETIME, nCreateTime);
            jsonObject.put(KEY_COVER, mCover);
            jsonObject.put(KEY_DRAFT_TYPE, nDaftType);
            jsonObject.put(KEY_BG_INFO, gson.toJson(mExtImageInfo));

            jsonObject.put(KEY_WORD_LIST, gson.toJson(mWordInfoList));
            jsonObject.put(KEY_STICKER_LIST, gson.toJson(mStickerInfos));
            jsonObject.put(KEY_PROPORTION_MODE, mProportionMode);
            jsonObject.put(KEY_PROPORTION_VALUE, mProportionValue);
            jsonObject.put(KEY_PROPORTION_ORIGINAL, mOriginalProportion);
            jsonObject.put(KEY_ID, nId);

//            jsonObject.put(KEY_WATERMARK, gson.toJson(mWatermark));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    public static VirtualIImageInfo toShortInfo(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        VirtualIImageInfo imp = new VirtualIImageInfo();
        try {
            JSONObject jsonObject = new JSONObject(str);
            Gson gson = com.pesdk.uisdk.gson.Gson.getInstance().getGson();
            imp.ver = jsonObject.optInt(KEY_VER, 0);
            imp.nId = jsonObject.getInt(KEY_ID);

            imp.mGaffitiList = gson.fromJson(jsonObject.optString(KEY_GRAFFITI_LIST), new TypeToken<List<GraffitiInfo>>() {
            }.getType());
            imp.mCollageInfos = gson.fromJson(jsonObject.optString(KEY_COLLAGE_LIST), new TypeToken<List<CollageInfo>>() {
            }.getType());


            imp.mFrameInfoList = gson.fromJson(jsonObject.optString(KEY_FRAME_LIST), new TypeToken<List<FrameInfo>>() {
            }.getType());
            imp.mOverlayList = gson.fromJson(jsonObject.optString(KEY_OVERLAY_LIST), new TypeToken<List<CollageInfo>>() {
            }.getType());

//            imp.mMOInfos = gson.fromJson(jsonObject.optString(KEY_MO_LIST), new TypeToken<List<MOInfo>>() {
//            }.getType());
//            imp.mEffectInfoList = gson.fromJson(jsonObject.optString(KEY_EFFECT_LIST), new TypeToken<List<EffectInfo>>() {
//            }.getType());
//            if (null != imp.mEffectInfoList && imp.mEffectInfoList.size() > 0) {
////                fixRestoreEffectList(imp.mEffectInfoList);
//            }
            imp.basePath = jsonObject.optString(KEY_BASEPATH);
            imp.nCreateTime = jsonObject.optLong(KEY_CREATETIME);
            imp.mUpdateTime = jsonObject.optLong(KEY_UDPATE_TIME);
            if (imp.mUpdateTime == 0) {
                imp.mUpdateTime = imp.nCreateTime;
            }
            imp.mCover = jsonObject.optString(KEY_COVER);
            imp.nDaftType = jsonObject.getInt(KEY_DRAFT_TYPE);


            String tmp = jsonObject.optString(KEY_WORD_LIST);
            imp.mWordInfoList = gson.fromJson(tmp, new TypeToken<List<WordInfoExt>>() {
            }.getType());
            imp.mStickerInfos = gson.fromJson(jsonObject.optString(KEY_STICKER_LIST), new TypeToken<List<StickerInfo>>() {
            }.getType());

            imp.mProportionMode = jsonObject.optInt(KEY_PROPORTION_MODE, Crop.CROP_FREE);

            if (imp.getVer() >= VERSION) {//版本升级
                imp.mExtImageInfo = gson.fromJson(jsonObject.optString(KEY_BG_INFO), new TypeToken<ExtImageInfo>() {
                }.getType());
            } else {
                imp.peScene = gson.fromJson(jsonObject.optString(KEY_PE_SCENE), new TypeToken<PEScene>() {
                }.getType());
            }

            if (jsonObject.has(KEY_PROPORTION_VALUE)) {
                imp.mProportionValue = (float) jsonObject.optDouble(KEY_PROPORTION_VALUE, -1);
                imp.mOriginalProportion = (float) jsonObject.optDouble(KEY_PROPORTION_ORIGINAL, -1);
            } else {//旧版升级过来的无比例参数
                FrameInfo frameInfo = null;
                if (null != imp.mFrameInfoList && imp.mFrameInfoList.size() > 0) {
                    frameInfo = imp.mFrameInfoList.get(0);
                }
                if (imp.peScene != null) {
                    PEImageObject imageObject = imp.peScene.getPEImageObject();
                    imp.mProportionValue = PlayerAspHelper.getAsp(frameInfo, 0, imageObject);
                    imp.mOriginalProportion = imageObject.getWidth() * 1.0f / imageObject.getHeight();
                } else {
                    imp.mProportionValue = PlayerAspHelper.getAsp(frameInfo, 1, null);
                    imp.mOriginalProportion = 1;
                }
            }
//            Log.e(TAG, "toShortInfo: " + imp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imp;
    }


    public void moveToDraft(Context context) {
        //每次保存到草稿，均创建新的草稿文件夹，复制封面等
        if (nDaftType != INFO_TYPE_DRAFT) {
            //防止重复加入草稿箱，不断复制文件
            nDaftType = INFO_TYPE_DRAFT;
            createBasePath();
        }
        //防止新增的字幕、特效、配音，有部分额数据没有放到此草稿箱文件夹里
        moveToDraftImp(context);
    }


    /**
     * 创建一个工作目录
     */
    public void createBasePath() {
        if (TextUtils.isEmpty(basePath)) {
            basePath = PathUtils.getDraftPath(UUID.randomUUID().toString());
        }
    }

    /**
     * 保存草稿时，需要记录当前媒体绑定的滤镜文件
     */
//    private void onSaveFilterFile(MediaObject src) {
//        ArrayList<EffectInfo> effectInfos = src.getEffectInfos();
//        //当前媒体绑定的自定义特效滤镜（记录滤镜文件）
//        if (null != effectInfos && effectInfos.size() > 0) {
//            for (int m = 0; m < effectInfos.size(); m++) {
//                EffectInfo effectInfo = effectInfos.get(m);
//                if (effectInfo.getFilterId() == EffectInfo.Unknown) {
//                    effectInfo.setTag(null);
//                }
//            }
//        }
//    }

    /**
     * 移动全部资源到指定的文件夹
     */
    private void moveToDraftImp(Context context) {
        File fileNew;
        fileNew = new File(basePath);
        if (!fileNew.exists()) {
            fileNew.mkdirs();
        }


        if (null != mExtImageInfo.getBackground()) {//背景图
            PEImageObject tmp = mExtImageInfo.getBackground();
            if (tmp.getMediaPath().startsWith(PathUtils.getTempPath())) {
                PEImageObject dst = tmp.moveToDraft(basePath);
                mExtImageInfo.setBackground(dst);
            }
        }


        //新版字幕
        int len = mWordInfoList.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                WordInfoExt wordInfo = mWordInfoList.get(i);
                wordInfo.getCaption().moveToDraft(basePath);
            }
        }

        //贴纸
        if (null != mStickerInfos && mStickerInfos.size() > 0) {
            len = mStickerInfos.size();
            StickerInfo info;
            for (int i = 0; i < len; i++) {
                info = mStickerInfos.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }
        }
//        //马赛克|水印
//        if (null != mMOInfos && mMOInfos.size() > 0) {
//            len = mMOInfos.size();
//            MOInfo info;
//            for (int i = 0; i < len; i++) {
//                info = mMOInfos.get(i);
//                if (null != info) {
//                    info.getObject().moveToDraft(basePath);
//                }
//            }
//        }
        //画中画
        if (null != mCollageInfos && mCollageInfos.size() > 0) {
            len = mCollageInfos.size();
            for (int i = 0; i < len; i++) {
                CollageInfo info = mCollageInfos.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }
        }
        //涂鸦
        if (null != mGaffitiList && mGaffitiList.size() > 0) {
            len = mGaffitiList.size();
            for (int i = 0; i < len; i++) {
                GraffitiInfo info = mGaffitiList.get(i);
                if (null != info) {
                    info.moveToDraft(basePath);
                }
            }
        }

    }

    /**
     * 删除全部数据
     */
    public void deleteData() {
        FileUtils.deleteAll(basePath);
    }

    /**
     * 判断草稿箱视频的主要媒体是否全部存在
     *
     * @return true 全部存在；false 文件不存在
     */
    public boolean isNoExit() {
        return true;
    }

    public void setFrameInfoList(List<FrameInfo> frameInfoList) {
        mFrameInfoList = frameInfoList;
    }

    private List<FrameInfo> mFrameInfoList;

    public List<FrameInfo> getBorderList() {
        return mFrameInfoList;
    }

    public boolean isExist() {
        return true;
    }


    private transient boolean bRestored = false; //当前进程内参数值有效

    /**
     * 恢复草稿中的隐藏信息(仅进入编辑|直接导出时调用，给DraftData.readItem() 减少耗时)
     */
    public void restoreData(Context context) {
        if (!bRestored) {
            RestoreHelper helper = new RestoreHelper();
            helper.restoreData(context, this);
            bRestored = true;
        }
    }


    public void setCover(String dst) {
        mCover = dst;
    }


    @Override
    public String toString() {
        return "VirtualIImageInfo{" +
                "hash=" + hashCode() +
                " super=" + super.toString() +
//                "peScene=" + peScene +
//                ", mCollageInfos=" + mCollageInfos +
                ", mOverlayList=" + mOverlayList +
//                ", mEffectInfoList=" + mEffectInfoList +
                ", mExtImageInfo=" + mExtImageInfo +
//                ", mGaffitiList=" + mGaffitiList +
//                ", mStickerInfos=" + mStickerInfos +
//                ", mWordInfoList=" + mWordInfoList +
//                ", mWaterMark=" + mWaterMark +
                ", mProportionMode=" + mProportionMode +
                ", mProportionValue=" + mProportionValue +
                ", mOriginalProportion=" + mOriginalProportion +
                ", mFrameInfoList=" + mFrameInfoList +
                ", bRestored=" + bRestored +
                '}';
    }

    /**
     * 复制新对象，用于缩略图
     */
    public VirtualIImageInfo copy() {
        VirtualIImageInfo virtualIImageInfo = new VirtualIImageInfo();
        virtualIImageInfo.mProportionMode = mProportionMode;
        virtualIImageInfo.mProportionValue = mProportionValue;
        virtualIImageInfo.mOriginalProportion = mOriginalProportion;

        virtualIImageInfo.mExtImageInfo = mExtImageInfo.copy();


        //文字
        List<WordInfoExt> wordInfoExts = new ArrayList<>();
        if (null != mWordInfoList) {
            for (WordInfoExt ext : mWordInfoList) {
                wordInfoExts.add(ext.copy());
            }
        }
        virtualIImageInfo.setWordInfoList(wordInfoExts);


        //贴纸
        List<StickerInfo> stickerInfos = new ArrayList<>();
        if (null != mWordInfoList) {
            for (StickerInfo info : mStickerInfos) {
                stickerInfos.add(info.copy());
            }
        }
        virtualIImageInfo.setStickerInfos(stickerInfos);


        //涂鸦
        ArrayList<GraffitiInfo> graffitiInfos = new ArrayList<>();
        if (null != mGaffitiList) {
            for (GraffitiInfo info : mGaffitiList) {
                graffitiInfos.add(info.copy());
            }
        }
        virtualIImageInfo.setGraffitiList(graffitiInfos);


        //画中画
        List<CollageInfo> list = new ArrayList<>();
        if (null != mCollageInfos) {
            for (CollageInfo info : mCollageInfos) {
                list.add(info.copy());
            }
        }
        virtualIImageInfo.setCollageInfos(list);

        //边框
        ArrayList<FrameInfo> frameInfos = new ArrayList<>();
        if (null != mFrameInfoList) {
            for (FrameInfo info : mFrameInfoList) {
                frameInfos.add(info.copy());
            }
        }
        virtualIImageInfo.setFrameInfoList(frameInfos);


        //叠加
        ArrayList<CollageInfo> overlayList = new ArrayList<>();
        if (null != mOverlayList) {
            for (CollageInfo info : mOverlayList) {
                overlayList.add(info.copy());
            }
        }
        virtualIImageInfo.setOverlayList(overlayList);

        return virtualIImageInfo;
    }


}
