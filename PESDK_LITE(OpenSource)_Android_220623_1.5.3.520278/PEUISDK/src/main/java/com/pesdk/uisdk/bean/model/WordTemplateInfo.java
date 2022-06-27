package com.pesdk.uisdk.bean.model;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.model.subtitle.SubBackground;
import com.pesdk.uisdk.bean.model.subtitle.SubTemplateInfo;
import com.pesdk.uisdk.bean.model.subtitle.SubText;
import com.pesdk.uisdk.manager.CaptionBroadcastReceiver;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.vecore.annotation.AnimationType;
import com.vecore.models.FlipType;
import com.vecore.models.caption.CaptionExtObject;
import com.vecore.models.caption.CaptionItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 文字模板
 */
public class WordTemplateInfo implements Parcelable {

    //字幕特效默认配置文件是按照640*360 大小设计的，
    // 所以为了适配任意的分辨率，需要转化为0~1.0f , 再根据目标宽高
    public static final float PWIDTH = 640f;
    public static final float PHEIGHT = 360f;

    /**
     * id
     */
    private int mId;
    /**
     * 网络地址、本地地址
     */
    private String mUrl;
    /**
     * 图标
     */
    private String mIcon;
    /**
     * 分类代码
     */
    public String mCategory;
    /**
     * 资源id
     */
    public String mResourceId;
    /**
     * 本地
     */
    private String mLocalPath;
    /**
     * 层级
     */
    private int mLevel = 0;
    /**
     * 动画
     */
    private final CaptionExtObject mCaptionExtObject;
    /**
     * 动画
     */
    private final HashMap<Integer, AnimInfo> mAnimInList = new HashMap<>();//入场动画
    private final HashMap<Integer, AnimInfo> mAnimOutList = new HashMap<>();//出场动画
    private final HashMap<Integer, AnimInfo> mAnimGroupList = new HashMap<>();//循环动画
    /**
     * 镜像
     */
    private FlipType mFlipType = FlipType.FLIP_TYPE_NONE;
    /**
     * 编辑时的视频比例
     */
    private float mPreviewAsp = 1f;

    public WordTemplateInfo(String url, String icon) {
        super();
        mCaptionExtObject = new CaptionExtObject();
        setUrl(url, icon);
    }

    public WordTemplateInfo(String category, String resourceId, String localPath) {
        mCategory = category;
        mResourceId = resourceId;
        mLocalPath = localPath;
        mCaptionExtObject = new CaptionExtObject();
    }

    public WordTemplateInfo(WordTemplateInfo wordTemplateInfo) {

        if (wordTemplateInfo.mAnimInList != null && wordTemplateInfo.mAnimInList.size() > 0) {
            for (Map.Entry<Integer, AnimInfo> entry : wordTemplateInfo.mAnimInList.entrySet()) {
                AnimInfo value = entry.getValue();
                if (value != null) {
                    mAnimInList.put(entry.getKey(), value.copy());
                }
            }
        }
        if (wordTemplateInfo.mAnimOutList != null && wordTemplateInfo.mAnimOutList.size() > 0) {
            for (Map.Entry<Integer, AnimInfo> entry : wordTemplateInfo.mAnimOutList.entrySet()) {
                AnimInfo value = entry.getValue();
                if (value != null) {
                    mAnimOutList.put(entry.getKey(), value.copy());
                }
            }
        }
        if (wordTemplateInfo.mAnimGroupList != null && wordTemplateInfo.mAnimGroupList.size() > 0) {
            for (Map.Entry<Integer, AnimInfo> entry : wordTemplateInfo.mAnimGroupList.entrySet()) {
                AnimInfo value = entry.getValue();
                if (value != null) {
                    mAnimGroupList.put(entry.getKey(), value.copy());
                }
            }
        }
        this.mFlipType = wordTemplateInfo.mFlipType;

        mCaptionExtObject = new CaptionExtObject(wordTemplateInfo.getCaption());
        mId = wordTemplateInfo.getId();
        mUrl = wordTemplateInfo.getUrl();
        mIcon = wordTemplateInfo.getIcon();
        mCategory = wordTemplateInfo.getCategory();
        mResourceId = wordTemplateInfo.getResourceId();
        mLocalPath = wordTemplateInfo.getLocalPath();
        mLevel = wordTemplateInfo.getLevel();
        mPreviewAsp = wordTemplateInfo.getPreviewAsp();

        //设置模板
        setTemplate(wordTemplateInfo.mTemplateShowRectF, wordTemplateInfo.mContentList);
        setRecoverScale(wordTemplateInfo.recoverScale);
        mFontSize = wordTemplateInfo.mFontSize;
    }


    /**
     * 复制
     */
    public WordTemplateInfo copy() {
        return new WordTemplateInfo(this);
    }

    /**
     * 是否下载
     */
    public boolean isDown() {
        return !TextUtils.isEmpty(mLocalPath) && new File(mLocalPath).exists();
    }


    /**
     * 重置
     */
    public void onReset() {
        //缩放
        RectF showRectF = new RectF(mCaptionExtObject.getOriginShow());
        mCaptionExtObject.refreshShowRectF(showRectF, true);
        //中心位置
        mCaptionExtObject.setCenter(new PointF(0.5f, 0.5f));
        mCaptionExtObject.setRotateCaption(0);
    }

    /**
     * 路径
     */
    public void setUrl(String url, String icon) {
        mUrl = url;
        mIcon = icon;
        if (!TextUtils.isEmpty(mUrl)) {
            mLocalPath = PathUtils.getFilePath(PathUtils.getSubPath(), String.valueOf(mUrl.hashCode()));
        }
    }

    /**
     * 设置ID
     */
    public void setId(int id) {
        mId = id;
    }

    /**
     * 设置文字
     */
    public void setText(String text) {
        mCaptionExtObject.setText(text);
    }

    /**
     * 设置 模板
     */
    public void setTemplateInfo(SubTemplateInfo info) {
        if (info != null) {
            //原始区域
            RectF oldShowRectF = new RectF(mCaptionExtObject.getShowRectF());
            ArrayList<CaptionItem> captionAll = mCaptionExtObject.getCaptionAll();

            mCaptionExtObject.reset();
            mAnimInList.clear();
            mAnimOutList.clear();
            mAnimGroupList.clear();

            SubText[] text = info.getText();
            //文字
            if (text != null && text.length > 0) {

                boolean auto = info.getWidth() <= 0 || info.getHeight() <= 0;

                if (auto) {
                    mCaptionExtObject.setAutoSize(false);
                    //文字
                    for (int i = 0; i < text.length; i++) {
                        SubText subText = text[i];
                        CaptionItem label = subText.getLabel();

                        //恢复文字
                        if (captionAll != null && i < captionAll.size()) {
                            String textContent = captionAll.get(i).getTextContent();
                            if (!TextUtils.isEmpty(textContent)) {
                                label.setTextContent(textContent);
                            }
                        }

//                        //动画
//                        SubTextAnim[] animList = subText.getAnim();
//                        if (animList != null && animList.length > 0) {
//                            for (SubTextAnim anim : animList) {
//                                AnimInfo animInfo = anim.getAnimInfo();
//                                if ("in".equals(anim.getAnimType())) {
//                                    if (animInfo != null) {
//                                        mAnimInList.put(i, animInfo);
//                                    }
//                                } else if ("out".equals(anim.getAnimType())) {
//                                    if (animInfo != null) {
//                                        mAnimOutList.put(i, animInfo);
//                                    }
//                                } else if ("group".equals(anim.getAnimType())) {
//                                    if (animInfo != null) {
//                                        mAnimGroupList.put(i, animInfo);
//                                    }
//                                }
//                            }
//                        }

                        //添加文字
                        mCaptionExtObject.addLabel(label);
                    }
                } else {
                    mCaptionExtObject.setAutoSize(true);
                    //显示区域
                    float w = info.getWidth() * 1.0f / PWIDTH / 2;
                    float h = info.getHeight() * 1.0f / PHEIGHT / 2;
                    RectF showRectF = new RectF(info.getCenterX() - w, info.getCenterY() - h,
                            info.getCenterX() + w, info.getCenterY() + h);
                    float asp = PWIDTH * 1.0f / PHEIGHT;
                    float scaleW = 1;
                    float scaleH = 1;
                    if (Math.abs(asp - mPreviewAsp) > 0.0001f) {
                        //修正比例
                        float mediaAsp = showRectF.width() / showRectF.height() * asp;
                        RectF rectF = Utils.correctionRatio(showRectF, mediaAsp, asp, mPreviewAsp);
                        scaleW = rectF.width() / showRectF.width();
                        scaleH = rectF.height() / showRectF.height();
                        //设置宽高
                        Matrix matrix = new Matrix();
                        float scale = mCaptionExtObject.getScale();
                        matrix.postScale(scale, scale, showRectF.centerX(), showRectF.centerY());
                        matrix.postTranslate(oldShowRectF.centerX() - showRectF.centerX(),
                                oldShowRectF.centerY() - showRectF.centerY());
                        RectF show = new RectF();
                        matrix.mapRect(show, rectF);
                        mCaptionExtObject.setOriginalRectF(rectF, show);
                    } else {
                        //设置宽高
                        Matrix matrix = new Matrix();
                        float scale = mCaptionExtObject.getScale();
                        matrix.postScale(scale, scale, showRectF.centerX(), showRectF.centerY());
                        matrix.postTranslate(oldShowRectF.centerX() - showRectF.centerX(),
                                oldShowRectF.centerY() - showRectF.centerY());
                        RectF show = new RectF();
                        matrix.mapRect(show, showRectF);
                        mCaptionExtObject.setOriginalRectF(showRectF, show);
                    }

                    //文字
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleW, scaleH, showRectF.centerX(), showRectF.centerY());
                    for (int i = 0; i < text.length; i++) {
                        SubText subText = text[i];
                        CaptionItem label = subText.getLabel();

                        //恢复文字
                        if (captionAll != null && i < captionAll.size()) {
                            String textContent = captionAll.get(i).getTextContent();
                            if (!TextUtils.isEmpty(textContent)) {
                                label.setTextContent(textContent);
                            }
                        }

                        //显示位置 相对全局
                        RectF show = subText.getShowRect();
                        if (show.isEmpty()) {
                            label.setShowRect(new RectF(0, 0, 1, 1));
                        } else {
                            RectF rectF = new RectF(showRectF.left + show.left / PWIDTH, showRectF.top + show.top / PHEIGHT,
                                    showRectF.left + show.right / PWIDTH, showRectF.top + show.bottom / PHEIGHT);
                            //修正比例
                            if (Math.abs(asp - mPreviewAsp) > 0.0001f) {
                                matrix.mapRect(rectF, rectF);
                            }
                            //计算相对显示区域的显示区域
                            RectF extShow = mCaptionExtObject.getOriginShow();
                            label.setShowRect(new RectF(
                                    (rectF.left - extShow.left) / extShow.width(),
                                    (rectF.top - extShow.top) / extShow.height(),
                                    (rectF.right - extShow.left) / extShow.width(),
                                    (rectF.bottom - extShow.top) / extShow.height()
                            ));
                        }

//                        //动画
//                        SubTextAnim[] animList = subText.getAnim();
//                        if (animList != null && animList.length > 0) {
//                            for (SubTextAnim anim : animList) {
//                                AnimInfo animInfo = anim.getAnimInfo();
//                                if ("in".equals(anim.getAnimType())) {
//                                    if (animInfo != null) {
//                                        mAnimInList.put(i, animInfo);
//                                    }
//                                } else if ("out".equals(anim.getAnimType())) {
//                                    if (animInfo != null) {
//                                        mAnimOutList.put(i, animInfo);
//                                    }
//                                } else if ("group".equals(anim.getAnimType())) {
//                                    if (animInfo != null) {
//                                        mAnimGroupList.put(i, animInfo);
//                                    }
//                                }
//                            }
//                        }

                        //添加文字
                        mCaptionExtObject.addLabel(label);
                    }

                }
                //刷新动画
                refreshAnim();
            }

            //背景
            SubBackground background = info.getBackground();
            if (background != null) {
                mCaptionExtObject.setFrameArray(background.getFrameInfo(info.getLocalPath(), info.getName()), null);
            }

            //还原大小位置
            mCaptionExtObject.setCenter(new PointF(oldShowRectF.centerX(), oldShowRectF.centerY()));
            mCaptionExtObject.setIndex(0);
        }
    }

    /**
     * 时间偏移
     */
    public void offset(float time) {
        float start = mCaptionExtObject.getStartTimeline() + time;
        float end = mCaptionExtObject.getEndTimeline() + time;
        mCaptionExtObject.setTimeline(start, end);
    }

    /**
     * 设置时间
     */
    public void setTimeline(int start, int end) {
        mCaptionExtObject.setTimeline(Utils.ms2s(start), Utils.ms2s(end));
    }

    /**
     * 设置资源id
     */
    public void setSource(String category, String resourceId) {
        this.mCategory = category;
        this.mResourceId = resourceId;
    }

    /**
     * 层级
     */
    public void setLevel(int level) {
        mLevel = level;
    }

    /**
     * 刷新宽高
     */
    public void refreshShow(RectF rectF) {
        if (rectF == null || mCaptionExtObject == null) {
            return;
        }
        mCaptionExtObject.refreshShowRectF(rectF, true);
    }

    /**
     * 广播返回 刷新大小
     */
    public void refreshSize() {
        mCaptionExtObject.refreshSize();
    }

    /**
     * 角度
     */
    public void setAngle(float rotateCaption) {
        mCaptionExtObject.setRotateCaption(rotateCaption);
    }

    /**
     * 设置入场动画
     */
    public void setInAnimInfo(AnimInfo inAnimInfo) {
        mAnimInList.put(mCaptionExtObject.getIndex(), inAnimInfo);
    }

    /**
     * 设置出场动画
     */
    public void setOutAnimInfo(AnimInfo outAnimInfo) {
        mAnimOutList.put(mCaptionExtObject.getIndex(), outAnimInfo);
    }

    /**
     * 设置组合场动画
     */
    public void setGroupAnimInfo(AnimInfo groupAnimInfo) {
        mAnimGroupList.put(mCaptionExtObject.getIndex(), groupAnimInfo);
    }

    /**
     * 刷新动画
     */
    public void refreshAnim() {
        ArrayList<CaptionItem> captionAll = mCaptionExtObject.getCaptionAll();
        for (int i = 0; i < captionAll.size(); i++) {
            CaptionItem item = captionAll.get(i);
            //出场动画
            animInfo(item, mAnimOutList.get(i), false);
            //设置时间
            AnimInfo animGroup = mAnimGroupList.get(i);
            AnimInfo animIn = mAnimInList.get(i);
            if (animGroup != null) {
                animGroup.setAnimDuration(0);
            }
            if (animIn != null) {
                animInfo(item, animGroup, true);
                animInfo(item, animIn, true);
            } else {
                animInfo(item, null, true);
                animInfo(item, animGroup, true);
            }
        }
    }

    /**
     * 动画信息
     */
    private void animInfo(CaptionItem captionItem, AnimInfo animInfo, boolean in) {
        if (captionItem != null) {
            if (animInfo == null) {
                captionItem.setAnimation(in ? AnimationType.ANIMATION_TYPE_IN : AnimationType.ANIMATION_TYPE_OUT,
                        0, 0, 0, false);
            } else {
                captionItem.setAnimation(in ? AnimationType.ANIMATION_TYPE_IN : AnimationType.ANIMATION_TYPE_OUT,
                        animInfo.getAnimId(), animInfo.getAnimDuration(), 0, animInfo.mIsKok);
            }
        }
    }

    /**
     * 刷新
     */
    public void refresh() {
        refresh(false);
    }

    /**
     * 测量宽高
     */
    public void refreshMeasuring() {
        mCaptionExtObject.refreshMeasuring();
    }

    /**
     * 刷新
     */
    public void refresh(boolean apply) {
        if (mCaptionExtObject != null) {
            mCaptionExtObject.refresh(apply, false);
        }
    }


    /**
     * 設置大小
     */
    public void setParentSize(int w, int h) {
        if (mCaptionExtObject != null) {
            mCaptionExtObject.setParentSize(w, h, false);
        }
    }

    /**
     * 修复比例
     */
    public void fixAsp(float oldPreviewAsp, float newPreviewAsp) {
        if (mCaptionExtObject != null) {
            RectF oldShow = new RectF(getShowRectF());

            //计算原始的宽高
            RectF oldOrigin = new RectF(mCaptionExtObject.getOriginShow());
            float mediaAsp = oldOrigin.width() / oldOrigin.height() * oldPreviewAsp;
            RectF newOrigin = Utils.correctionRatio(oldOrigin, mediaAsp, oldPreviewAsp, newPreviewAsp);

            mediaAsp = oldShow.width() / oldShow.height() * oldPreviewAsp;
            RectF newShow = Utils.correctionRatio(oldShow, mediaAsp, oldPreviewAsp, newPreviewAsp);

            mCaptionExtObject.setOriginalRectF(newOrigin, newShow);
            refreshShow(newShow);
        }
    }

    /**
     * 显示区域 主要是关键帧
     */
    public void setShowRectF(RectF rectF) {
        mCaptionExtObject.refreshShowRectF(rectF, false);
    }

    /**
     * 返回本地地址
     */
    public String getLocalPath() {
        return mLocalPath;
    }

    /**
     * 网络地址
     */
    public String getUrl() {
        return mUrl;
    }

    public String getIcon() {
        return mIcon;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getResourceId() {
        return mResourceId;
    }

    public CaptionExtObject getCaption() {
        return mCaptionExtObject;
    }

    public float getAngle() {
        return mCaptionExtObject.getRotateCaption();
    }

    public RectF getShowRectF() {
        return mCaptionExtObject.getShowRectF();
    }

    public int getStart() {
        return Utils.s2ms(mCaptionExtObject.getStartTimeline());
    }

    public int getEnd() {
        return Utils.s2ms(mCaptionExtObject.getEndTimeline());
    }

    public int getDuration() {
        return Utils.s2ms(mCaptionExtObject.getEndTimeline() - mCaptionExtObject.getStartTimeline());
    }

    public int getId() {
        return mId;
    }

    public int getLevel() {
        return mLevel;
    }

    public float getPreviewAsp() {
        return mPreviewAsp;
    }

    public void setPreviewAsp(float asp) {
        this.mPreviewAsp = asp;
    }

    public String getText() {
        CaptionItem captionItem = mCaptionExtObject.getCaptionItem();
        return captionItem == null ? null : captionItem.getTextContent();
    }

    public String getHintText() {
        CaptionItem captionItem = mCaptionExtObject.getCaptionItem();
        return captionItem == null ? null : captionItem.getHintContent();
    }

    public HashMap<Integer, AnimInfo> getAnimInList() {
        return mAnimInList;
    }

    public HashMap<Integer, AnimInfo> getAnimOutList() {
        return mAnimOutList;
    }

    public HashMap<Integer, AnimInfo> getAnimGroupList() {
        return mAnimGroupList;
    }


    /**
     * 注册广播
     */
    private transient CaptionBroadcastReceiver mReceiver;

    /**
     * 注册字幕
     */
    public void registeredCaption() {
        if (!mCaptionExtObject.isAutoSize() || mFontSize) {
            mReceiver = new CaptionBroadcastReceiver();
            String registered = mCaptionExtObject.registered(mReceiver);
            if (registered != null) {
                mReceiver.setWordTemplateInfo(this);
            } else {
                mReceiver = null;
            }
        }
    }

    /**
     * 注册字幕
     */
    public void unRegisteredCaption() {
        if (mReceiver != null) {
            mCaptionExtObject.unRegistered(mReceiver);
        }
        mReceiver = null;
    }

    /**
     * 刷新字体大小
     */
    public void refreshFontSize() {
        if (mFontSize) {
            //获取失败时不转成固定大小
            boolean success = mCaptionExtObject.refreshFontSize();
            //取消重新注册
            unRegisteredCaption();
            mFontSize = false;
            if (success) {
                mCaptionExtObject.setAutoSize(false);
                mReceiver = new CaptionBroadcastReceiver();
                mReceiver.setWordTemplateInfo(this);
                mCaptionExtObject.cutoverCaption(mReceiver);
            } else {
                refresh(true);
            }
        } else {
            unRegisteredCaption();
            mCaptionExtObject.refreshSize();
        }
    }


    private static final String VER_TAG = "210603WordTemplateInfo";
    private static final int VER = 3;

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

        //恢复模板
        dest.writeByte((byte) (mFontSize ? 1 : 0));
        //恢复模板
        dest.writeFloat(recoverScale);
        dest.writeParcelable(this.mTemplateShowRectF, flags);
        dest.writeStringList(mContentList);


        //镜像
        dest.writeInt(this.mFlipType == null ? -1 : this.mFlipType.ordinal());
        //动画
        dest.writeMap(this.mAnimInList);
        dest.writeMap(this.mAnimOutList);
        dest.writeMap(this.mAnimGroupList);

        dest.writeInt(this.mId);
        dest.writeString(this.mUrl);
        dest.writeString(this.mIcon);
        dest.writeString(this.mCategory);
        dest.writeString(this.mResourceId);
        dest.writeString(this.mLocalPath);
        dest.writeInt(this.mLevel);
        dest.writeParcelable(this.mCaptionExtObject, flags);
    }

    protected WordTemplateInfo(Parcel in) {
        //当前读取的position
        int oldPosition = in.dataPosition();
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();

            if (tVer >= 3) {
                mFontSize = in.readByte() != 0;
            }

            if (tVer >= 2) {
                recoverScale = in.readFloat();
                mTemplateShowRectF = in.readParcelable(RectF.class.getClassLoader());
                mContentList = new ArrayList<>();
                in.readStringList(mContentList);
            }

            //镜像
            int tmpFlipType = in.readInt();
            this.mFlipType = tmpFlipType == -1 ? null : FlipType.values()[tmpFlipType];
            //动画
            in.readMap(mAnimInList, getClass().getClassLoader());
            in.readMap(mAnimOutList, getClass().getClassLoader());
            in.readMap(mAnimGroupList, getClass().getClassLoader());
        } else {
            in.setDataPosition(oldPosition);
        }

        this.mId = in.readInt();
        this.mUrl = in.readString();
        this.mIcon = in.readString();
        this.mCategory = in.readString();
        this.mResourceId = in.readString();
        this.mLocalPath = in.readString();
        this.mLevel = in.readInt();
        this.mCaptionExtObject = in.readParcelable(CaptionExtObject.class.getClassLoader());
    }

    public static final Creator<WordTemplateInfo> CREATOR = new Creator<WordTemplateInfo>() {
        @Override
        public WordTemplateInfo createFromParcel(Parcel source) {
            return new WordTemplateInfo(source);
        }

        @Override
        public WordTemplateInfo[] newArray(int size) {
            return new WordTemplateInfo[size];
        }
    };

    /**
     * API模板恢复时使用
     */
    private float recoverScale;
    private RectF mTemplateShowRectF;
    private ArrayList<String> mContentList;
    /**
     * 标记 需要计算显示区域
     */
    private boolean mFontSize = false;

    /**
     * 模板
     */
    public void setTemplate(RectF showRectF, ArrayList<String> contentList) {
        if (showRectF != null) {
            mTemplateShowRectF = new RectF(showRectF);
        }
        if (contentList != null) {
            mContentList = new ArrayList<>(contentList);
        }
    }

    /**
     * 恢复大小
     */
    public void setRecoverScale(float scale) {
        this.recoverScale = scale;
    }

    /**
     * 恢复模板
     */
    public void recoverTemplate(SubTemplateInfo info) {
        if (info != null && mTemplateShowRectF != null) {

            //模板
            setTemplateInfo(info);

            //恢复文字
            ArrayList<CaptionItem> captionAll = mCaptionExtObject.getCaptionAll();
            for (int i = 0; i < mContentList.size(); i++) {
                String textContent = mContentList.get(i);
                if (i >= captionAll.size()) {
                    break;
                }
                if (!TextUtils.isEmpty(textContent)) {
                    captionAll.get(i).setTextContent(textContent);
                }
            }

            //还原大小位置
            RectF newOrigin = new RectF();
            mCaptionExtObject.setCenter(new PointF(mTemplateShowRectF.centerX(), mTemplateShowRectF.centerY()));
            Matrix matrix = new Matrix();
            matrix.postScale(1 / recoverScale, 1 / recoverScale, mTemplateShowRectF.centerX(), mTemplateShowRectF.centerY());
            matrix.mapRect(newOrigin, mTemplateShowRectF);
            mCaptionExtObject.setOriginalRectF(newOrigin, mTemplateShowRectF);


            //全部当作自动大小计算
            mFontSize = !mCaptionExtObject.isAutoSize();
            mCaptionExtObject.setAutoSize(true);

            //刷新最终大小
            refreshShow(mTemplateShowRectF);
        }
    }

    /**
     * 是否需要计算文字大小    模板和升级字幕会使用
     */
    public boolean isFontSize() {
        return mFontSize;
    }

}
