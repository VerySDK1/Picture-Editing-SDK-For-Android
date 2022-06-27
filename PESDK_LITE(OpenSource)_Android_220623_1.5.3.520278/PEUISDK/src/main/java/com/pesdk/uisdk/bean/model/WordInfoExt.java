package com.pesdk.uisdk.bean.model;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.pesdk.uisdk.bean.model.flower.Flower;
import com.pesdk.uisdk.bean.model.flower.FlowerManager;
import com.pesdk.uisdk.bean.model.flower.WordFlower;
import com.pesdk.uisdk.manager.CaptionBroadcastReceiver;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.helper.SubUtils;
import com.vecore.BaseVirtual;
import com.vecore.BaseVirtualView;
import com.vecore.annotation.AnimationType;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.FlipType;
import com.vecore.models.caption.CaptionExtObject;
import com.vecore.models.caption.CaptionItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * 新版文字
 */
public class WordInfoExt implements Parcelable, ITimeLine {

    /**
     * 默认字体大小
     */
    public static final int DEFAULT_SIZE = 20;

    /**
     * id
     */
    private int mId;
    /**
     * 气泡 分类代码、资源id
     */
    public String mBubbleCategory;
    public String mBubbleResourceId;
    public String mBubblePath;
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
    private AnimInfo mAnimIn;//入场动画
    private AnimInfo mAnimOut;//出场动画
    private AnimInfo mAnimGroup;//循环动画
    /**
     * 整体高级动画
     */
    private final HashMap<Integer, AnimInfo> mAnimList = new HashMap<>();
    /**
     * 镜像
     */
    private FlipType mFlipType = FlipType.FLIP_TYPE_NONE;
    /**
     * 编辑时的视频比例
     */
    private float mPreviewAsp = 1f;
    /**
     * 花字
     */
    private Flower mFlower;
    /**
     * 默认去掉虚线
     */
    private boolean mHideDashed = false;
    /**
     * 标识符号
     */
    private String mIdentifier;
    /**
     * 场景标识符
     */
    private String mIdentifierScene;
    private int mGroupId;


    public WordInfoExt() {
        mCaptionExtObject = new CaptionExtObject();
        mId = Utils.getId();
    }

    public WordInfoExt(WordInfoExt wordInfoExt) {
        mAnimIn = wordInfoExt.getAnimIn();
        mAnimOut = wordInfoExt.getAnimOut();
        mAnimGroup = wordInfoExt.getAnimGroup();
        if (wordInfoExt.mAnimList != null && wordInfoExt.mAnimList.size() > 0) {
            for (Map.Entry<Integer, AnimInfo> entry : wordInfoExt.mAnimList.entrySet()) {
                AnimInfo value = entry.getValue();
                if (value != null) {
                    mAnimList.put(entry.getKey(), value.copy());
                }
            }
        }
        this.mFlipType = wordInfoExt.mFlipType;

        mCaptionExtObject = new CaptionExtObject(wordInfoExt.getCaption());
        mId = wordInfoExt.getId();
        mBubbleCategory = wordInfoExt.getCategory();
        mBubbleResourceId = wordInfoExt.getResourceId();
        mBubblePath = wordInfoExt.getBubblePath();
        mLevel = wordInfoExt.getLevel();
        mPreviewAsp = wordInfoExt.getPreviewAsp();
        mFlower = wordInfoExt.getFlower();
        mHideDashed = wordInfoExt.mHideDashed;

        //恢复
        setRecoverAnim(wordInfoExt.recoverInAnim, wordInfoExt.recoverOutAnim);
        setRecoverScale(wordInfoExt.recoverScale);
        mFontSize = wordInfoExt.mFontSize;
        this.mIdentifier = wordInfoExt.mIdentifier;
        this.mGroupId = wordInfoExt.mGroupId;
    }

    /**
     * 初始化默认
     */
    public void initDefault(String hint) {
        //字幕
        CaptionItem captionItem = new CaptionItem();
        captionItem.setFontSize(DEFAULT_SIZE);
        captionItem.setTextColor(Color.WHITE);
        captionItem.setHintContent(hint);
        mCaptionExtObject.addLabel(captionItem);

        //显示区域
        mCaptionExtObject.setOriginalRectF(null, new RectF(0.4f, 0.45f, 0.6f, 0.55f));
        mCaptionExtObject.setScale(2.5f);
        setBubble(SubUtils.getInstance().getDefault(), true);

        //测量高度
        refreshMeasuring();
    }

    /**
     * 复制
     */
    public WordInfoExt copy() {
        return new WordInfoExt(this);
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
     * 设置时间
     */
    public void setTimeline(int start, int end) {
        mCaptionExtObject.setTimeline(Utils.ms2s(start), Utils.ms2s(end));
    }

    /**
     * 层级
     */
    public void setLevel(int level) {
        mLevel = level;
    }

    /**
     * 角度
     */
    public void setAngle(float rotateCaption) {
        mCaptionExtObject.setRotateCaption(rotateCaption);
    }

    /**
     * 设置动画
     */
    public void setAnim(AnimInfo in, AnimInfo out, AnimInfo group) {
        mAnimIn = in;
        mAnimOut = out;
        mAnimGroup = group;

        animInfo(mAnimOut, false);
        //设置时间
        if (mAnimGroup != null) {
            mAnimGroup.setAnimDuration(0);
        }
        if (mAnimIn != null) {
            animInfo(mAnimGroup, true);
            animInfo(mAnimIn, true);
        } else {
            animInfo(null, true);
            animInfo(mAnimGroup, true);
        }
    }

    /**
     * 设置高级动画
     */
    public void setAdvanceAnim(int type, AnimInfo info) {
        mAnimList.put(type, info);
        if (info == null) {
            mCaptionExtObject.setAdvanceAnimate(type, 0, 0, 0);
        } else {
            mCaptionExtObject.setAdvanceAnimate(type, info.getAnimId(), info.getAnimDuration(),
                    info.getCycleDuration());
        }
    }

    /**
     * 清空高级动画
     */
    public void resetAdvanceAnim() {
        mAnimList.clear();
        mCaptionExtObject.setAdvanceAnimate(AnimationType.ANIMATION_TYPE_IN, 0, 0, 0);
        mCaptionExtObject.setAdvanceAnimate(AnimationType.ANIMATION_TYPE_OUT, 0, 0, 0);
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
     * 花字
     */
    public void setFlower(Flower flower) {
        mFlower = flower;
        WordFlower wordFlower = null;
        if (mFlower != null) {
            wordFlower = FlowerManager.getInstance().parsingConfig(mFlower.getLocalPath());
        }
        ArrayList<CaptionItem> captionAll = mCaptionExtObject.getCaptionAll();
        if (captionAll != null) {
            for (CaptionItem item : captionAll) {
                if (item != null) {
                    item.setEffectConfig(wordFlower == null ? null : wordFlower.getEffect());
                }
            }
        }
    }

    /**
     * 设置kok颜色
     */
    public void setKokColor(int color) {
        ArrayList<CaptionItem> captionAll = mCaptionExtObject.getCaptionAll();
        if (captionAll != null) {
            for (CaptionItem item : captionAll) {
                if (item != null) {
                    item.setKtvColor(color);
                }
            }
        }
    }


    /**
     * 重置
     */
    public void onReset() {
        //缩放
        RectF showRectF = new RectF();
        RectF originShow = mCaptionExtObject.getOriginShow();
        Matrix matrix = new Matrix();
        matrix.postScale(2.5f, 2.5f, originShow.centerX(), originShow.centerY());
        matrix.mapRect(showRectF, originShow);
        mCaptionExtObject.refreshShowRectF(showRectF, true);
        //中心位置
        mCaptionExtObject.setCenter(new PointF(0.5f, 0.5f));
        mCaptionExtObject.setRotateCaption(0);
    }

    /**
     * 显示区域 主要是关键帧
     */
    public void setShowRectF(RectF rectF) {
        mCaptionExtObject.refreshShowRectF(rectF, false);
    }

    /**
     * 刷新
     */
    public void refresh(boolean apply) {
        mCaptionExtObject.refresh(apply, false);
    }

    /**
     * 广播返回 刷新大小
     */
    public void refreshSize() {
        mCaptionExtObject.refreshSize();
    }

    /**
     * 测量宽高
     */
    public void refreshMeasuring() {
        mCaptionExtObject.refreshMeasuring();
    }

    /**
     * 刷新位置
     */
    public void refreshShow(RectF rectF) {
        if (rectF == null || mCaptionExtObject == null) {
            return;
        }
        mCaptionExtObject.refreshShowRectF(rectF, true);
    }

    private static final String TAG = "WordInfoExt";

    /**
     * 设置气泡
     */
    public void setBubble(StyleInfo styleInfo, boolean cutover) {
        if (styleInfo != null) {
            //气泡id
            this.mBubbleCategory = styleInfo.category;
            this.mBubbleResourceId = styleInfo.resourceId;
            this.mBubblePath = styleInfo.mlocalpath;

            //原始区域
            RectF showRectFOld = new RectF(mCaptionExtObject.getShowRectF());

            CaptionItem captionItem = mCaptionExtObject.getCaptionItem();

            //移除
            mCaptionExtObject.removeListLiteObject();

            //气泡
            SparseArray<com.vecore.models.caption.FrameInfo> frameArrays = new SparseArray<>();
            int len = styleInfo.frameArray.size();
            for (int i = 0; i < len; i++) {
                FrameInfo info = styleInfo.frameArray.valueAt(i);
                frameArrays.put(info.time, new com.vecore.models.caption.FrameInfo(info.time, info.pic));
            }

            //时间
            ArrayList<com.vecore.models.caption.TimeArray> timeArrays = new ArrayList<>();
            for (int i = 0; i < styleInfo.timeArrays.size(); i++) {
                TimeArray timeArray = styleInfo.timeArrays.get(i);
                timeArrays.add(new com.vecore.models.caption.TimeArray(timeArray.getBegin(), timeArray.getEnd()));
            }

            //帧
            mCaptionExtObject.setFrameArray(frameArrays, timeArrays);
            //自动大小
            mCaptionExtObject.setAutoSize(frameArrays.size() > 0);


            RectF showRectF = new RectF(styleInfo.mShowRectF);
            //切换和恢复
            if (cutover) {
                //设置显示区域 字体20 就默认的1倍
                RectF originRectF = new RectF();
                Matrix matrix = new Matrix();
                matrix.postScale(1, 1, originRectF.centerX(), originRectF.centerY());
                matrix.mapRect(originRectF, showRectF);

                //计算缩放
                float scale = mCaptionExtObject.getScale();
                showRectF.set(originRectF);
                //文字显示区域
                matrix.reset();
                matrix.postScale(scale, scale, showRectF.centerX(), showRectF.centerY());
                matrix.postTranslate(showRectFOld.centerX() - showRectF.centerX(),
                        showRectFOld.centerY() - showRectF.centerY());
                matrix.mapRect(showRectF, showRectF);

                mCaptionExtObject.setOriginalRectF(originRectF, showRectF);

                //显示虚线
                mHideDashed = "text_sample".equals(styleInfo.code) || "text_vertical".equals(styleInfo.code)
                        || !mCaptionExtObject.isAutoSize();

                //文字显示区域
                captionItem.setShowRect(styleInfo.getTextRectF());

                //可拉伸
                if (styleInfo.lashen) {
                    //直接设置默认大小为20
                    captionItem.setFontSize(DEFAULT_SIZE);

                    //自动大小
                    mCaptionExtObject.setAutoSize(false);
                    //设置缩放范围 暂时未实现
                    RectF ninePitch = styleInfo.getNinePitch();
                    RectF nineRectF = new RectF((float) (ninePitch.left * styleInfo.srcWidth),
                            (float) (ninePitch.top * styleInfo.srcHeight),
                            (float) (ninePitch.right * styleInfo.srcWidth),
                            (float) (ninePitch.bottom * styleInfo.srcHeight));
                    mCaptionExtObject.setNinePatch(nineRectF, styleInfo.getTextRectF(), (float) styleInfo.srcWidth, (float) styleInfo.srcHeight);
                } else {
                    captionItem.setFontSize(0);
                    mCaptionExtObject.setNinePatch(null, null, 0, 0);
                }

                //对齐
                captionItem.setAlignment(1, 1);
                //方向
                captionItem.setVertical(styleInfo.vertical ? 1 : 0);
                //文字颜色
                captionItem.setTextColor(styleInfo.getTextDefaultColor());
                //字体
                captionItem.setFontFile(styleInfo.tFont);
                //描边
                captionItem.setOutline(true);
                captionItem.setOutlineColor(styleInfo.strokeColor);
                captionItem.setOutlineWidth(styleInfo.strokeWidth);

                //关键帧动画设置为null
                if (mCaptionExtObject.getKeyFrameAnimateList() != null) {
                    mCaptionExtObject.setKeyAnimate(null);
                }
            } else {
                //方向
                captionItem.setVertical(styleInfo.vertical ? 1 : 0);
                //设置显示区域
                mCaptionExtObject.setOriginalRectF(styleInfo.mShowRectF, showRectF);
                if (styleInfo.lashen) {
                    //自动大小
                    mCaptionExtObject.setAutoSize(false);
                    //设置缩放范围 暂时未实现
                    RectF ninePitch = styleInfo.getNinePitch();
                    RectF nineRectF = new RectF((float) (ninePitch.left * styleInfo.srcWidth),
                            (float) (ninePitch.top * styleInfo.srcHeight),
                            (float) (ninePitch.right * styleInfo.srcWidth),
                            (float) (ninePitch.bottom * styleInfo.srcHeight));
                    mCaptionExtObject.setNinePatch(nineRectF, styleInfo.getTextRectF(),
                            (float) styleInfo.srcWidth, (float) styleInfo.srcHeight);
                } else {
                    mCaptionExtObject.setNinePatch(null, null, 0, 0);
                }
            }
        }
    }

    /**
     * 修复比例
     */
    public void fixAsp(float oldPreviewAsp, float newPreviewAsp) {
        if (mCaptionExtObject.isAutoSize()) {
            RectF oldShow = new RectF(getShowRectF());

            //计算原始的宽高
            RectF oldOrigin = new RectF(mCaptionExtObject.getOriginShow());
            float mediaAsp = oldOrigin.width() / oldOrigin.height() * oldPreviewAsp;
            RectF newOrigin = Utils.correctionRatio(oldOrigin, mediaAsp, oldPreviewAsp, newPreviewAsp);

            mediaAsp = oldShow.width() / oldShow.height() * oldPreviewAsp;
            RectF newShow = Utils.correctionRatio(oldShow, mediaAsp, oldPreviewAsp, newPreviewAsp);

            mCaptionExtObject.setOriginalRectF(newOrigin, newShow);
            refreshShow(newShow);
        } else {
            mCaptionExtObject.fixAsp();
        }
    }

    /**
     * 设置要绑定的虚拟视频和播放器
     */
    public void setVirtualVideo(BaseVirtual virtualVideo, BaseVirtualView virtualVideoView) {
        try {
            mCaptionExtObject.setVirtualVideo(virtualVideo, virtualVideoView);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 設置大小
     */
    public void setParentSize(int w, int h) {
        mCaptionExtObject.setParentSize(w, h, false);
    }

    /**
     * 动画信息
     */
    private void animInfo(AnimInfo animInfo, boolean in) {
        ArrayList<CaptionItem> captionAll = mCaptionExtObject.getCaptionAll();
        if (captionAll != null) {
            for (CaptionItem item : captionAll) {
                if (item != null) {
                    if (animInfo == null) {
                        item.setAnimation(in ? AnimationType.ANIMATION_TYPE_IN : AnimationType.ANIMATION_TYPE_OUT,
                                0, 0, 0, false);
                    } else {
                        item.setAnimation(in ? AnimationType.ANIMATION_TYPE_IN : AnimationType.ANIMATION_TYPE_OUT,
                                animInfo.getAnimId(), animInfo.getAnimDuration(), animInfo.getCycleDuration(), animInfo.mIsKok);
                    }
                }
            }
        }
    }

    public void setPreviewAsp(float asp) {
        this.mPreviewAsp = asp;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public String getCategory() {
        return mBubbleCategory;
    }

    public String getResourceId() {
        return mBubbleResourceId;
    }

    public String getBubblePath() {
        return mBubblePath;
    }

    public CaptionExtObject getCaption() {
        return mCaptionExtObject;
    }

    public CaptionItem getCaptionItem() {
        return mCaptionExtObject.getCaptionItem();
    }

    public int getAngle() {
        return (int) mCaptionExtObject.getRotateCaption();
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

    @Override
    public boolean isHide() {
        return false;
    }

    @Override
    public void setHide(boolean hide) {

    }

    @Override
    public float getAlpha() {
        return 0;
    }

    @Override
    public void setAlpha(float alpha) {

    }

    public void setIdentifierScene(String id) {
        mIdentifierScene = id;
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

    public String getText() {
        CaptionItem captionItem = mCaptionExtObject.getCaptionItem();
        return captionItem == null ? null : captionItem.getTextContent();
    }

    public String getHintText() {
        CaptionItem captionItem = mCaptionExtObject.getCaptionItem();
        return captionItem == null ? null : captionItem.getHintContent();
    }

    public AnimInfo getAnimIn() {
        return mAnimIn;
    }

    public AnimInfo getAnimOut() {
        return mAnimOut;
    }

    public AnimInfo getAnimGroup() {
        return mAnimGroup;
    }

    public HashMap<Integer, AnimInfo> getAnimList() {
        return mAnimList;
    }

    public Flower getFlower() {
        return mFlower;
    }

    public String getIdentifierScene() {
        return mIdentifierScene;
    }

    public boolean isHideDashed() {
        return mHideDashed;
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
                mReceiver.setWordInfoExt(this);
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
                mReceiver.setWordInfoExt(this);
                mCaptionExtObject.cutoverCaption(mReceiver);
            } else {
                refresh(true);
            }
        } else {
            unRegisteredCaption();
            mCaptionExtObject.refreshSize();
        }
    }


    private static final String VER_TAG = "210715WordInfoExt";
    private static final int VER = 6;

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

        //标识cv
        dest.writeByte((byte) 0);

        //标识
        dest.writeString(mIdentifier);
        dest.writeInt(mGroupId);

        //隐藏虚线
        dest.writeByte((byte) (mHideDashed ? 1 : 0));

        //恢复气泡
        dest.writeFloat(recoverScale);
        dest.writeByte((byte) (mFontSize ? 1 : 0));
        dest.writeParcelable(recoverInAnim, flags);
        dest.writeParcelable(recoverOutAnim, flags);

        //气泡路径
        dest.writeString(this.mBubblePath);

        //花字
        dest.writeParcelable(mFlower, flags);
        //镜像
        dest.writeInt(this.mFlipType == null ? -1 : this.mFlipType.ordinal());
        //动画
        dest.writeParcelable(this.mAnimIn, flags);
        dest.writeParcelable(this.mAnimOut, flags);
        dest.writeParcelable(this.mAnimGroup, flags);
        dest.writeMap(this.mAnimList);

        dest.writeInt(this.mId);
        dest.writeString(this.mBubbleCategory);
        dest.writeString(this.mBubbleResourceId);
        dest.writeInt(this.mLevel);
        dest.writeParcelable(this.mCaptionExtObject, flags);
    }

    protected WordInfoExt(Parcel in) {
        //当前读取的position
        String tmp = in.readString();
        if (VER_TAG.equals(tmp)) {
            int tVer = in.readInt();

            if (tVer >= 6) {
                in.readByte();
            }
            if (tVer >= 5) {
                mIdentifier = in.readString();
                mGroupId = in.readInt();
            }
            if (tVer >= 4) {
                mHideDashed = in.readByte() != 0;
            }
            if (tVer >= 3) {
                recoverScale = in.readFloat();
                mFontSize = in.readByte() != 0;
                recoverInAnim = in.readParcelable(AnimInfo.class.getClassLoader());
                recoverOutAnim = in.readParcelable(AnimInfo.class.getClassLoader());
            }
            if (tVer >= 2) {
                mBubblePath = in.readString();
            }

            //花字
            this.mFlower = in.readParcelable(Flower.class.getClassLoader());
            //镜像
            int tmpMFlipType = in.readInt();
            this.mFlipType = (tmpMFlipType < 0 || tmpMFlipType >= FlipType.values().length) ? null : FlipType.values()[tmpMFlipType];
            //动画
            mAnimIn = in.readParcelable(AnimInfo.class.getClassLoader());
            mAnimOut = in.readParcelable(AnimInfo.class.getClassLoader());
            mAnimGroup = in.readParcelable(AnimInfo.class.getClassLoader());
            in.readMap(mAnimList, getClass().getClassLoader());
            //id等
            this.mId = in.readInt();
            this.mBubbleCategory = in.readString();
            this.mBubbleResourceId = in.readString();
            this.mLevel = in.readInt();
            this.mCaptionExtObject = in.readParcelable(CaptionExtObject.class.getClassLoader());
        } else {
            this.mCaptionExtObject = new CaptionExtObject();
        }
    }

    public static final Creator<WordInfoExt> CREATOR = new Creator<WordInfoExt>() {
        @Override
        public WordInfoExt createFromParcel(Parcel source) {
            return new WordInfoExt(source);
        }

        @Override
        public WordInfoExt[] newArray(int size) {
            return new WordInfoExt[size];
        }
    };

    //API模板恢复时使用
    private float recoverScale;
    //标记 需要计算字体大小
    private boolean mFontSize = false;
    //旧版动画===》新版动画
    private AnimInfo recoverInAnim;
    private AnimInfo recoverOutAnim;

    /**
     * 需要恢复动画
     */
    public void setRecoverAnim(AnimInfo recoverInAnim, AnimInfo recoverOutAnim) {
        this.recoverInAnim = recoverInAnim;
        this.recoverOutAnim = recoverOutAnim;
    }

    /**
     * 恢复大小
     */
    public void setRecoverScale(float scale) {
        this.recoverScale = scale;
    }

    /**
     * 气泡地址
     */
    public void setBubble(String category, String resourceId, String localPath) {
        //气泡id
        this.mBubbleCategory = category;
        this.mBubbleResourceId = resourceId;
        this.mBubblePath = localPath;
    }

    /**
     * 恢复气泡
     */
    public void recoverBubble() {
        //原始显示区域
        RectF showRectF = new RectF(getShowRectF());

        //气泡
        StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(mBubbleResourceId);
        if (styleInfo == null) {
            styleInfo = SubUtils.getInstance().initDefaultStyle();
        }
        if (styleInfo != null) {
            File config = new File(styleInfo.mlocalpath, CommonStyleUtils.CONFIG_JSON);
            CommonStyleUtils.getConfig(config, styleInfo);
            setBubble(styleInfo, false);
        }

        //显示区域
        if (recoverScale > 0) {
            //缩放 现在显示区域 计算缩放值
            RectF originRectF = new RectF();
            Matrix matrix = new Matrix();
            matrix.postScale(1 / recoverScale, 1 / recoverScale, showRectF.centerX(), showRectF.centerY());
            matrix.mapRect(originRectF, showRectF);
            mCaptionExtObject.setOriginalRectF(originRectF, showRectF);

            //文字固定大小修复文字区域显示
            if (styleInfo != null) {
                //去掉虚线
                if (!mCaptionExtObject.isAutoSize()) {
                    mHideDashed = "text_sample".equals(styleInfo.code) || "text_vertical".equals(styleInfo.code);
                    CaptionItem captionItem = mCaptionExtObject.getCaptionItem();
                    //具体大小只是一个比例作用
                    float srcWidth = (float) CommonStyleUtils.previewWidth;
                    float srcHeight = (float) CommonStyleUtils.previewHeight;
                    //原始显示区域
                    RectF originalShow = styleInfo.mShowRectF;
                    RectF originalText = styleInfo.getTextRectF();
                    //计算现在的文字显示区域
                    float left = originalText.left * originalShow.width() * srcWidth;
                    float top = originalText.top * originalShow.height() * srcHeight;
                    float right = (1 - originalText.right) * originalShow.width() * srcWidth;
                    float bottom = (1 - originalText.bottom) * originalShow.height() * srcHeight;
                    //所占比例
                    float totalW = originRectF.width() * srcWidth;
                    float totalH = originRectF.height() * srcHeight;
                    RectF newText = new RectF(left / totalW, top / totalH,
                            (totalW - right) / totalW, (totalH - bottom) / totalH);
                    captionItem.setShowRect(newText);
                }
            }
        }

        //全部当作自动大小计算
        mFontSize = !mCaptionExtObject.isAutoSize();
        mCaptionExtObject.setAutoSize(true);

        //恢复原始大小
        refreshShow(showRectF);

    }

    /**
     * 是否需要计算文字大小    模板和升级字幕会使用
     */
    public boolean isFontSize() {
        return mFontSize;
    }

    public AnimInfo getRecoverInAnim() {
        return recoverInAnim;
    }

    public AnimInfo getRecoverOutAnim() {
        return recoverOutAnim;
    }
}
