package com.pesdk.uisdk.edit;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.MixInfo;
import com.pesdk.uisdk.bean.ProportionInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.bean.IEditData;
import com.pesdk.uisdk.edit.bean.IParam;
import com.pesdk.uisdk.edit.bean.IUndo;
import com.pesdk.uisdk.edit.listener.OnChangeDataListener;
import com.pesdk.uisdk.export.DataManager;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.sticker.StickerExportHandler;
import com.pesdk.uisdk.listener.OnStepListener;
import com.pesdk.uisdk.util.helper.FilterUtil;
import com.pesdk.uisdk.util.helper.PlayerAspHelper;
import com.pesdk.uisdk.util.helper.ProportionUtil;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.models.EffectInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 撤销|恢复 数据管理
 */
public class EditDataHandler implements OnStepListener, EditUndoHandler.OnUndoListener, IEditData {
    private static final String TAG = "EditDataHandler";

    /**
     * fresh  0不刷新  1全部刷新   2只刷新音乐
     */
    public static final int UNDO_NONE = 0;
    public static final int UNDO_BUILD_ALL = 1;
    public static final int UNDO_BUILD_AUDIO = 2;

    /**
     * 删除、添加、调整
     */
    private static String PROMPT_DELETE;
    private static String PROMPT_ADD;
    private static String PROMPT_ADJUST;
    /**
     * Context
     */
    private Context mContext;
    /**
     * 撤销、还原
     */
    private EditUndoHandler mEditUndoHandler;
    private EditDataParam mEditDataParam = new EditDataParam();

    /**
     * 撤销、还原
     */
    public EditDataHandler(Context context, View btnUndo, View btnReduction) {
        mContext = context;

        //删除...
        PROMPT_DELETE = context.getString(R.string.pesdk_prompt_delete);
        PROMPT_ADD = context.getString(R.string.pesdk_prompt_add);
        PROMPT_ADJUST = context.getString(R.string.pesdk_prompt_adjust);
        //撤销
        if (btnUndo != null && btnReduction != null) {
            mEditUndoHandler = new EditUndoHandler(mContext, btnUndo, btnReduction, this);
        }

    }

    public void init(Context context) {
        mContext = context;
    }


    /**
     * 操作模式
     */
    private int mEditMode = IMenu.MODE_PREVIEW;

    public int getEditMode() {
        return mEditMode;
    }

    public void setEditMode(int editMode) {
        mEditMode = editMode;
    }


    /**
     * 草稿恢复时
     */
    public void setShortVideoInfo(VirtualIImageInfo info) {
        mEditDataParam.setShortVideoInfo(info);
        resetoreProportion();
    }


    /**
     * 撤销|重做
     *
     * @param revoke
     * @param info
     * @param fresh
     * @return
     */
    @Override
    public UndoInfo onUndoReduction(boolean revoke, UndoInfo info, boolean fresh, boolean bToast) {
        if (info == null || mListener == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (revoke) {
            builder.append(mContext.getString(R.string.pesdk_edit_undo));
        } else {
            builder.append(mContext.getString(R.string.pesdk_edit_reduction));
        }
        mListener.onUndoReduction(UNDO_NONE, true);

        Log.e(TAG, "onUndoReduction: " + fresh + " " + revoke + " " + info);
        //0不刷新  1刷新   2音频
        //转场等意外 其他都不用重新build
        int build = UNDO_BUILD_ALL;
        VirtualImage virtualVideo = mListener.getEditorVideo();
        VirtualImageView virtualVideoView = mListener.getEditor();
        //设置数据
        ArrayList list = new ArrayList();
        boolean isExitBuild = false;//主动避免二次build
        int mode = info.getMode();
        if (mode == IMenu.text) {
            //字幕
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_text));
            list.addAll(mEditDataParam.getCloneWordNewInfos());
            //实时删除字幕
            for (WordInfoExt wordInfo : mEditDataParam.getWordList()) {
                wordInfo.getCaption().removeListLiteObject();
            }
            //实时添加
            setWordNewList(info.getList());
            for (WordInfoExt wordInfo : mEditDataParam.getWordList()) {
                wordInfo.refresh(false);
            }
            refresh();
            //不build
            build = UNDO_NONE;
        } else if (mode == IMenu.sticker) {
            //贴纸
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_sticker));
            list.addAll(mEditDataParam.getCloneStickerInfos());

            //实时删除贴纸
            for (StickerInfo stickerInfo : mEditDataParam.getStickerList()) {
                stickerInfo.removeListLiteObject(virtualVideo);
            }
            //实时插入贴纸
            setStickerList(info.getList());
            new StickerExportHandler(mContext, mEditDataParam.getStickerList(),
                    mListener.getContainer().getWidth(),
                    mListener.getContainer().getHeight())
                    .export(virtualVideo);

            refresh();
            //不build
            build = UNDO_NONE;
        } else if (mode == IMenu.pip) { //画中画
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_menu_layer));
            list.addAll(mEditDataParam.getCloneCollageInfos());
            setCollageList(info.getList());
            fresh = true;
            build = UNDO_BUILD_ALL; //必须build: 防止与叠加、边框 层级异常
        } else if (mode == IMenu.overlay) {//叠加
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_overlay));
            list.addAll(mEditDataParam.getCloneOverLayList());
            setOverLayList(info.getList());
            fresh = true;
            build = UNDO_BUILD_ALL; //必须build: 防止与叠加、边框 层级异常
        } else if (mode == IMenu.graffiti) {
            //涂鸦
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_doodling));

            list.addAll(mEditDataParam.getCloneGraffitiInfos());

            //实时删除
            for (GraffitiInfo graffitiInfo : mEditDataParam.getGraffitList()) {
                virtualVideo.deleteSubtitleObject(graffitiInfo.getLiteObject());
            }
            //实时插入
            setGraffitiList(info.getList());
            for (GraffitiInfo graffitiInfo : mEditDataParam.getGraffitList()) {
                virtualVideo.updateSubtitleObject(graffitiInfo.getLiteObject());
            }
            refresh();
            //不build
            build = UNDO_NONE;
        } else if (mode == IMenu.koutu) {
            //抠图
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_koutu));
            list.addAll(mEditDataParam.getCloneSceneList());
            setPEList(info.getList());
            fresh = true;
            //必须build
            build = UNDO_BUILD_ALL;
        } else if (mode == IMenu.mainTrack || mode == IMenu.erase || mode == IMenu.crop || mode == IMenu.depth || mode == IMenu.canvas || mode == IMenu.sky || mode == IMenu.mirror) {
            //消除笔
            builder.append("\t");
            if (mode == IMenu.erase) {
                builder.append(mContext.getString(R.string.pesdk_erase_pen));
            } else if (mode == IMenu.crop) {
                builder.append(mContext.getString(R.string.pesdk_crop));
            } else if (mode == IMenu.depth) {
                builder.append(mContext.getString(R.string.pesdk_depth));
            } else if (mode == IMenu.canvas) {
                builder.append(mContext.getString(R.string.pesdk_background));
            } else if (mode == IMenu.mirror) {
                builder.append(mContext.getString(R.string.pesdk_mirror));
            } else if (mode == IMenu.sky) {
                builder.append(mContext.getString(R.string.pesdk_sky));
            } else if (mode == IMenu.mainTrack) {
                builder.append(mContext.getString(R.string.pesdk_track_main));
            }
            list.addAll(mEditDataParam.getCloneSceneList());
            setPEList(info.getList());
            if (mode == IMenu.crop) {
                //需注意比例是否有变化，用于撤销时，再通知贴纸、字幕还原显示位置
                float asp = mListener.getPlayerAsp();
                float asp2 = getNextAsp(); //新的
                Log.e(TAG, "onUndoReduction: " + asp + "<>" + asp2);
                if (proportionChanged(asp, asp2)) {
                    mListener.setAsp(asp2);
                    isExitBuild = true; //更改比例EditActivity中已经主动build了一次
                }
            }
            fresh = true;
            //必须build
            build = UNDO_BUILD_ALL;

        }
//
//        else if (mode == MODE_MASK) {
//            //马赛克
//            builder.append("\t");
//            builder.append(mContext.getString(R.string.edit_menu_mosaic));
//
//            list.addAll(getCloneMOInfs());
//
//            //实时删除
//            for (MOInfo moInfo : mMOInfoList) {
//                moInfo.getObject().remove();
//            }
//            //实时插入
//            setMaskList(info.getList());
//            for (MOInfo moInfo : mMOInfoList) {
//                try {
//                    moInfo.getObject().setVirtualVideo(virtualVideo, virtualVideoView);
//                    moInfo.getObject().quitEditCaptionMode(true);
//                } catch (InvalidArgumentException e) {
//                    e.printStackTrace();
//                }
//            }
//            //不build
//            build = UNDO_NONE;
//        }
//
        else if (mode == IMenu.effect) { //特效
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_effect));
            list.addAll(mEditDataParam.getCloneEffects());
            setEffectList(info.getList());
            //清理播放器中已加载的全部特效 （实时预览必须先删除再新增）
            virtualVideo.clearEffects(virtualVideoView);
            //再次往虚拟视频添加特效
            DataManager.loadEffects(virtualVideo, mEditDataParam.getEffectList());
            //更新播放器
            virtualVideo.updateEffects(virtualVideoView);
            refresh();
            //不build
            build = UNDO_NONE;
        } else if (mode == IMenu.frame) { //边框
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_frame));
            float asp = mListener.getPlayerAsp();
            list.addAll(mEditDataParam.getCloneFrameInfos());
            setBorderList(info.getList());

            float asp2 = getNextAsp();
            if (proportionChanged(asp, asp2)) {
                mListener.setAsp(asp2);
                isExitBuild = true; //更改比例EditActivity中已经主动build了一次
            }
            fresh = true;
            build = UNDO_BUILD_ALL;
        } else if (mode == IMenu.mix) { //图层合并
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_layer_merge));
            if (null != mMixInfo) {
                list.add(mMixInfo.copy());
            }
            List<MixInfo> old = info.getList();
            if (old.size() > 0) {
                mMixInfo = old.get(0);
                if (revoke) {
                    restore(mMixInfo.getInfo()); //恢复虚拟图片内容
                } else {
                    mEditDataParam.reset();
                    VirtualIImageInfo imageInfo = new VirtualIImageInfo();
                    CollageInfo tmp = VirtualIImageInfo.initBaseCollage(mMixInfo.getPath());
                    imageInfo.getCollageInfos().add(tmp);
                    restore(imageInfo);
                }
            }
            fresh = true;
            build = UNDO_BUILD_ALL;
        } else if (mode == IMenu.filter || mode == IMenu.adjust || mode == IMenu.beauty) {
            //调节
            builder.append("\t");
            if (mode == IMenu.adjust) {
                builder.append(mContext.getString(R.string.pesdk_adjust));
            } else if (mode == IMenu.filter) {
                builder.append(mContext.getString(R.string.pesdk_filter));
            } else if (mode == IMenu.beauty) {
                builder.append(mContext.getString(R.string.pesdk_beauty));
            }

            list.addAll(mEditDataParam.getCloneFilterInfos());
            setFilterList(info.getList());
            fresh = true;
            build = UNDO_BUILD_ALL;
        } else if (mode == IMenu.proportion) {//比例
            builder.append("\t");
            builder.append(mContext.getString(R.string.pesdk_proportion));

            ArrayList<ProportionInfo> tmp = new ArrayList<>();
            tmp.add(new ProportionInfo(mEditDataParam.getProportionMode(), mEditDataParam.getProportionValue()));
            list.addAll(tmp);

            ArrayList<ProportionInfo> src = info.getList();
            if (src.size() > 0) {
                ProportionInfo bean = src.get(0);
                mEditDataParam.setProportionMode(bean.getProportionMode(), bean.getProportionValue());
            } else {
                mEditDataParam.setProportionMode(Crop.CROP_FREE, -1);
            }
            resetoreProportion();

            float asp = mListener.getPlayerAsp();
            float asp2 = getNextAsp(); //新的
            if (proportionChanged(asp, asp2)) {
                mListener.setAsp(asp2);
                isExitBuild = true; //更改比例EditActivity中已经主动build了一次
            }

            fresh = true;
            //必须build
            build = UNDO_BUILD_ALL;
        }


        //        else if (mode == MODE_WATERMARK) {
//            //水印
//            builder.append("\t");
//            builder.append(mContext.getString(R.string.edit_menu_watermark_add));
//            list.addAll(getCloneWatermark());
//
//            //实时删除水印
//            DataManager.removeWatermark(mListener.getEditorVideo(), mWatermark);
//            ArrayList cover = info.getList();
//            if (cover != null && cover.size() > 0) {
//                mWatermark = (CollageInfo) cover.get(0);
//            } else {
//                mWatermark = null;
//            }
//            //实时插入水印
//            if (mWatermark != null) {
//                mWatermark.fixMediaLine(0, Utils.ms2s(mVideoDuration));
//                DataManager.insertWatermark(mListener.getEditorVideo(), mWatermark);
//            }
//            //不build
//            build = UNDO_NONE;
//        }

        builder.append("\t");
        builder.append(info.getName());
        //撤销/还原  +  类型(媒体、画中画) + 动作(调整、分割)

        Log.e(TAG, "onUndoReduction: " + isExitBuild + " " + mode + " " + fresh + " " + build + " builder: " + builder.toString());

        if (bToast) {
            SysAlertDialog.showAutoHideDialog(mContext, null, builder.toString(), Toast.LENGTH_SHORT);
        }


        if (!isExitBuild) { //拦截: 裁剪-切换比例二次build
            if (fresh) {
                build = UNDO_BUILD_ALL;
                mListener.onUndoReduction(build, false);
            }
        }
        onSaveDraft(mode);
        return new UndoInfo(mode, info.getName(), list);
    }


    /**
     * 比例发生了变化
     *
     * @param newAsp
     * @param oldAsp
     * @return
     */
    public boolean proportionChanged(float newAsp, float oldAsp) {
        return ProportionUtil.proportionChanged(newAsp, oldAsp);
    }

    public IParam getParam() {
        return mEditDataParam;
    }

    public ArrayList<CollageInfo> getCloneCollageInfos() {
        return mEditDataParam.getCloneCollageInfos();
    }

    public IUndo getUndo() {
        return mEditDataParam;
    }

    private void setEffectList(ArrayList list) {
        mEditDataParam.setEffectList(list);
    }

    private void setPEList(ArrayList<ExtImageInfo> list) {
        mEditDataParam.setPESceneList(list.get(0));
    }

    public void setFilterList(ArrayList list) {
        mEditDataParam.setFilterList(list);
    }

    private void setGraffitiList(ArrayList<GraffitiInfo> list) {
        mEditDataParam.setGraffitiList(list);
    }

    public void setStickerList(ArrayList<StickerInfo> list) {
        mEditDataParam.setStickerList(list);
    }

    public void setWordNewList(ArrayList<WordInfoExt> wordList) {
        mEditDataParam.setWordNewList(wordList);
    }

    public void setCollageList(ArrayList<CollageInfo> list) {
        mEditDataParam.setCollageList(list);
        if (list != null && list.size() > 0) {
            if (mListener != null) {
                mListener.onChange(true);
            }
        }
    }

    public void setBorderList(ArrayList<FrameInfo> list) {
        mEditDataParam.setFrameList(list);
        if (list != null && list.size() > 0) {
            if (mListener != null) {
                mListener.onChange(true);
            }
        }
    }

    public void setOverLayList(ArrayList<CollageInfo> list) {
        mEditDataParam.setOverLayList(list);
        if (list != null && list.size() > 0) {
            if (mListener != null) {
                mListener.onChange(true);
            }
        }
    }

    private void restore(VirtualIImageInfo info) {
        mEditDataParam.restore(info);
    }


    @Override
    public boolean isCanUndo() {
        return mListener != null && mListener.isCanUndo();
    }

    @Override
    public void onStepChanged(boolean changed) {
        if (null != mListener) {
            mListener.onStepChanged(changed);
        }
    }


    /**
     * 编辑时删除 出现new一个
     */
    public void deleteWordNewInfo(WordInfoExt info) {
        for (int i = 0; i < mEditDataParam.getWordList().size(); i++) {
            if (mEditDataParam.getWordList().get(i).getId() == info.getId()) {
                mEditDataParam.getWordList().remove(i);
                //保存草稿
                break;
            }
        }
    }

    public void deleteWordNewInfo2(WordInfoExt info) {
        for (int i = 0; i < mEditDataParam.getWordList().size(); i++) {
            if (mEditDataParam.getWordList().get(i).getId() == info.getId()) {
                onSaveStep(PROMPT_DELETE, IMenu.text);
                mEditDataParam.getWordList().remove(i);
                //保存草稿
                onSaveDraft(IMenu.text, true);
                //保存草稿
                break;
            }
        }
    }

    private void deleteWordNewInfo(int index) {
        if (index < 0 || index >= mEditDataParam.getWordList().size()) {
            return;
        }
        onSaveStep(PROMPT_DELETE, IMenu.text);
        mEditDataParam.getWordList().remove(index);
        //保存草稿
        onSaveDraft(IMenu.text, true);
    }

    public boolean deleteSticker(StickerInfo info) {
        if (info == null) {
            return false;
        }
        for (int i = 0; i < mEditDataParam.getStickerList().size(); i++) {
            if (mEditDataParam.getStickerList().get(i).getId() == info.getId()) {
                mEditDataParam.getStickerList().remove(i);
                //保存草稿
                onSaveDraft(IMenu.sticker, true);
                return true;
            }
        }
        return false;
    }

    /**
     * 放弃贴纸修改
     */
    public boolean restoreSticker(StickerInfo info, StickerInfo old) {
        if (info == null) {
            return false;
        }
        for (int i = 0; i < mEditDataParam.getStickerList().size(); i++) {
            if (mEditDataParam.getStickerList().get(i).getId() == info.getId()) {
                mEditDataParam.getStickerList().remove(i);
                if (null != old) {
                    mEditDataParam.getStickerList().add(i, old);
                }
                //保存草稿
                onSaveDraft(IMenu.sticker, true);
                return true;
            }
        }
        return false;
    }


    public boolean deleteSticker2(StickerInfo info) {
        if (info == null) {
            return false;
        }

        for (int i = 0; i < mEditDataParam.getStickerList().size(); i++) {
            if (mEditDataParam.getStickerList().get(i).getId() == info.getId()) {
                onSaveStep(PROMPT_DELETE, IMenu.sticker);
                mEditDataParam.getStickerList().remove(i);
                //保存草稿
                onSaveDraft(IMenu.sticker, true);
                return true;
            }
        }
        return false;
    }


    @Override
    public UndoInfo onDeleteStep() {
        Log.e(TAG, "onDeleteStep: " + this);
        if (mEditUndoHandler != null) {
            return mEditUndoHandler.deleteUndo();
        }
        return null;
    }

    /**
     * 撤销上一步
     */
    public void onUndo() {
        if (null != mEditUndoHandler) {
            mEditUndoHandler.onUndo();
        }
    }

    @Override
    public void onSaveStep(String name, int mode) {
        Log.e(TAG, "onSaveStep: " + name + " " + mode);
        if (mEditUndoHandler == null) {
            return;
        }
        if (bPauseImageRecord) {
            Log.e(TAG, "onSaveStep: bPauseImageRecord... ");
            return;
        }

        mMixInfo = null;
        if (mode == IMenu.effect) {  //特效
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneEffects());
        } else if (mode == IMenu.mainTrack || mode == IMenu.erase || mode == IMenu.crop || mode == IMenu.depth || mode == IMenu.canvas || mode == IMenu.sky || mode == IMenu.mirror) {
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneSceneList());
        } else if (mode == IMenu.koutu) {//抠图
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneSceneList());
        }
//        else if (mode == MODE_MASK) {
//            //马赛克
//            mEditUndoHandler.addUndo(mode, name, getCloneMOInfs());
//        }
        else if (mode == IMenu.graffiti) {//涂鸦
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneGraffitiInfos());
        } else if (mode == IMenu.text) {//新版字幕
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneWordNewInfos());
        } else if (mode == IMenu.sticker) {//贴纸
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneStickerInfos());
        } else if (mode == IMenu.pip) {  //画中画
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneCollageInfos());
        } else if (mode == IMenu.frame) { //边框
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneFrameInfos());
        } else if (mode == IMenu.overlay) { //叠加
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneOverLayList());
        } else if (mode == IMenu.filter || mode == IMenu.adjust || mode == IMenu.beauty) {  //滤镜|调色|美颜
            mEditUndoHandler.addUndo(mode, name, mEditDataParam.getCloneFilterInfos());
        } else if (mode == IMenu.proportion) {     //比例
            ArrayList<ProportionInfo> factorList = new ArrayList<>();
            factorList.add(new ProportionInfo(mEditDataParam.getProportionMode(), mEditDataParam.getProportionValue()));
            mEditUndoHandler.addUndo(mode, name, factorList);
        } else if (mode == IMenu.mix) {//合并图层(保存当前画面的)，区分其他数据保存
            ArrayList<MixInfo> list = new ArrayList<>();
            VirtualIImageInfo tmp = copyParam();
            mMixInfo = new MixInfo(tmp, mMixPath);
            list.add(mMixInfo);
            mEditUndoHandler.addUndo(mode, name, list);
        }
//  else if (mode == MODE_WATERMARK) {
//            //水印
//            mEditUndoHandler.addUndo(mode, name, getCloneWatermark());
//        }
    }


    @Override
    public void onSaveAdjustStep(int mode) {
        onSaveStep(PROMPT_ADJUST, mode);
    }

    @Override
    public void onSaveDraft(int mode) {
        if (mEditUndoHandler == null) {
            Log.e(TAG, "onSaveDraft:  mEditUndoHandler is null ");
            return;
        }

        if (bPauseImageRecord) {
            Log.e(TAG, "onSaveDraft: pause record" );
            return;
        }

        DraftManager.getInstance().onSaveDraft(mode);
    }

    /**
     * 保存草稿 刷新
     */
    private void onSaveDraft(int mode, boolean change) {
        if (change && mListener != null) {
            mListener.onChange(true);
        }
        onSaveDraft(mode);
    }


    public int addSticker(StickerInfo info, boolean saveStep) {
        if (info != null) {
            if (saveStep) {
                onSaveStep(PROMPT_ADD, IMenu.sticker);
            }
            mEditDataParam.getStickerList().add(info);
            //保存草稿
            onSaveDraft(IMenu.sticker, true);
            return mEditDataParam.getStickerList().size() - 1;
        }
        return -1;
    }


    public int addWordNewInfo(WordInfoExt info) {
        if (info != null) {
            onSaveStep(PROMPT_ADD, IMenu.text);
            mEditDataParam.getWordList().add(info);
            //保存草稿
            onSaveDraft(IMenu.text, true);
            return mEditDataParam.getWordList().size() - 1;
        }
        return -1;
    }


    public void addGraffiti(GraffitiInfo info) {
        if (info != null) {
            onSaveStep(PROMPT_ADD, IMenu.graffiti);
            mEditDataParam.getGraffitList().add(info);
            //保存草稿
            onSaveDraft(IMenu.graffiti, true);
        }
    }


    /**
     * 添加画中画
     */
    public void addCollage(CollageInfo info) {
        if (info != null) {
            onSaveStep(PROMPT_ADD, IMenu.pip);
            mEditDataParam.getCollageList().add(info);
            //保存草稿
            onSaveDraft(IMenu.pip, true);
        }
    }

    /**
     * 删除画中画
     */
    public void deleteCollage(CollageInfo info) {
        if (info != null) {
            Log.e(TAG, "deleteCollage: " + mEditDataParam.getCollageList().size());
            onSaveStep(PROMPT_DELETE, IMenu.pip);
            mEditDataParam.getCollageList().remove(info);
            //保存草稿
            onSaveDraft(IMenu.pip, true);
        }
    }


    /**
     * 添加叠加
     */
    public void addOverlay(CollageInfo info, boolean save) {
        if (info != null) {
            if (save) {
                onSaveStep(PROMPT_ADD, IMenu.overlay);
            }
            mEditDataParam.getOverLayList().add(info);
            if (save) {
                //保存草稿
                onSaveDraft(IMenu.overlay, true);
            }
        }
    }


    /**
     * 删除叠加
     *
     * @param info
     */
    public void deleteOverlay(CollageInfo info) {
        if (info != null) {
            onSaveStep(PROMPT_DELETE, IMenu.overlay);
            mEditDataParam.getOverLayList().remove(info);
            //保存草稿
            onSaveDraft(IMenu.overlay, true);
        }

    }


    /**
     * @param scene
     * @param menu  消除笔、编辑、景深、背景、天空
     */
    public void replaceImage(ExtImageInfo scene, @IMenu int menu) {
        if (scene != null) {
            if (menu == IMenu.mainTrack) {
                onSaveStep(PROMPT_ADJUST, menu);
            } else if (menu == IMenu.koutu) {
                onSaveStep(PROMPT_ADJUST, menu);
            } else if (menu == IMenu.crop) {
                onSaveStep(PROMPT_ADJUST, menu);
            } else {
                onSaveStep(PROMPT_ADD, menu);
            }
            mEditDataParam.setPESceneList(scene);//必须清除，只保证一个媒体
            //保存草稿
            onSaveDraft(menu, true);
        }
    }

    /**
     * 恢复：操作过程中修改了数据。放弃临时修改
     */
    public void restorePE(ExtImageInfo bk) {
        mEditDataParam.setPESceneList(bk);//必须清除，只保证一个媒体
    }

    /**
     * 滤镜|调色|美颜
     *
     * @param filterInfo
     * @param mode
     */
    public void addFilterInfo(FilterInfo filterInfo, int mode) {
        if (filterInfo != null) {
            if (filterInfo.getLookupConfig() != null) {
                onSaveStep(PROMPT_ADD, mode);
            } else if (filterInfo.getMediaParamImp() != null) {
                onSaveStep(PROMPT_ADD, mode);
            } else if (filterInfo.getBeauty() != null) {
                onSaveStep(PROMPT_ADD, mode);
            }
            mEditDataParam.getFilterList().add(filterInfo);
            //保存草稿
            onSaveDraft(mode, true);
        }
    }


    /**
     * 编辑滤镜|调色|美颜
     */
    public void editFilterInfo(FilterInfo filterInfo, int mode) {
        if (filterInfo != null) {
            if (filterInfo.getLookupConfig() != null) { //滤镜
                onSaveStep(PROMPT_ADJUST, mode);

                FilterInfo tmp = mEditDataParam.getFilter();
                if (null != tmp) {
                    mEditDataParam.getFilterList().remove(tmp);
                }
                mEditDataParam.getFilterList().add(filterInfo);
            } else if (null != filterInfo.getMediaParamImp()) { //调色
                onSaveStep(PROMPT_ADJUST, mode);

                FilterInfo tmp = mEditDataParam.getAdjust();
                if (null != tmp) {
                    mEditDataParam.getFilterList().remove(tmp);
                }
                mEditDataParam.getFilterList().add(filterInfo);
            } else if (null != filterInfo.getBeauty()) { //美颜
                onSaveStep(PROMPT_ADJUST, mode);
                FilterInfo tmp = mEditDataParam.getBeauty();
                if (null != tmp) {
                    mEditDataParam.getFilterList().remove(tmp);
                }
                mEditDataParam.getFilterList().add(filterInfo);
            } else {
                Log.e(TAG, "editFilterInfo: unknow " + filterInfo);
            }
            //保存草稿
            onSaveDraft(IMenu.filter, true);
        }
    }


    public void deleteFilterInfo(FilterInfo filterInfo) {
        if (null != filterInfo && mEditDataParam.getFilterList().remove(filterInfo)) {
            //保存草稿
            onSaveDraft(IMenu.filter, true);
        }
    }

    @Override
    public WordInfoExt getWordNewInfo(int index) {
        return mEditDataParam.getWordNewInfo(index);
    }

    @Override
    public StickerInfo getStickerInfo(int index) {
        return mEditDataParam.getStickerInfo(index);
    }

    @Override
    public CollageInfo getOverLay(int index) {
        return mEditDataParam.getOverLay(index);
    }

    @Override
    public CollageInfo getPip(int index) {
        return mEditDataParam.getPip(index);
    }

    /**
     * 仅有一个Layer时，不允许删除
     */
    public boolean enablePipDeleteMenu() {
        return mEditDataParam.getCollageList().size() > 1;
    }

    /**
     * 增加特效
     */
    public void addEffect(EffectInfo info, int mode, boolean step) {
        Log.e(TAG, "addEffect: " + info + " " + step);
        if (info != null) {
            if (step) {
                onSaveStep(PROMPT_ADD, mode);
            }
            mEditDataParam.getEffectList().add(info.copy());
            //保存草稿
            onSaveDraft(mode, true);
        }
    }

    /**
     * 删除特效
     */
//    private void deleteEffect(int index) {
//        if (index < 0 || index >= mEditDataParam.getEffectList().size()) {
//            return;
//        }
//        onSaveStep(PROMPT_DELETE, IMenu.effect);
//        mEditDataParam.getEffectList().remove(index);
//        //保存草稿
//        onSaveDraft(IMenu.effect, true);
//    }


    /**
     * 获取特效
     */
    public ArrayList<EffectInfo> getEffectAndFilter() {
        return new ArrayList<>(mEditDataParam.getEffectList());
    }


    private OnChangeDataListener mListener;

    public void setListener(OnChangeDataListener listener) {
        mListener = listener;
    }

    /**
     * 当前状态的滤镜|调色
     *
     * @return
     */
    public List<EffectInfo> getEffectList() {
        return FilterUtil.getFilterList(mEditDataParam.getFilter(), mEditDataParam.getAdjust());
    }


    /**
     * 第一个画中画
     */
    public CollageInfo getBaseCollage() {
        if (mEditDataParam.getCollageList().size() > 0) {
            return mEditDataParam.getCollageList().get(0);
        }
        return null;
    }

    /**
     * 替换主媒体后
     */
    public ExtImageInfo getExtImage() {
        return mEditDataParam.getExtImage();
    }


    /**
     * 首次-增加边框
     */
    public void addFrame(FrameInfo info, boolean step) {
        if (step) {
            onSaveStep(PROMPT_ADD, IMenu.frame);
        }
        mEditDataParam.getFrameList().clear(); //必须清除。只保证一个叠加
        if (info != null) {
            mEditDataParam.getFrameList().add(info.copy());
        }
        //保存草稿
        onSaveDraft(IMenu.frame, true);
    }

    /**
     * 二次进入时-编辑边框
     */
    public void editFrame(FrameInfo info, boolean step) {
        if (info != null) {
            if (step) {
                onSaveStep(PROMPT_ADJUST, IMenu.frame);
            }
            mEditDataParam.getFrameList().clear(); //必须清除。只保证一个叠加
            mEditDataParam.getFrameList().add(info.copy());
            //保存草稿
            onSaveDraft(IMenu.frame, true);
        }
    }

    public void clearFrame(boolean step) {
        if (step) {
            onSaveStep(PROMPT_DELETE, IMenu.frame);
        }
        mEditDataParam.getFrameList().clear(); //必须清除。只保证一个叠加
        //保存草稿
        onSaveDraft(IMenu.frame, true);
    }

    /**
     * 统一管理
     * 当前参数状态下，下次的预览比例
     */
    public float getNextAsp() {
        FrameInfo tmp = mEditDataParam.getFrameList().size() > 0 ? mEditDataParam.getFrameList().get(0) : null;
        return PlayerAspHelper.getAsp(tmp, mProportionInfo.getProportionValue(), null);
    }

    //记录临时变量: 手动设置比例
    private ProportionInfo mProportionInfo;


    @Crop.CropMode
    public int getProportionMode() {
        return mProportionInfo.getProportionMode();
    }

    public float getProportionValue() {
        return mProportionInfo.getProportionValue();
    }


    /**
     * 手动切换图片比例
     */
    public void setProportionMode(@Crop.CropMode int proportionMode, float value) {
        mProportionInfo.setProportionMode(proportionMode, value);
    }


    /**
     * 保存比例
     */
    public void onSaveProportionStep() {
        onSaveStep(PROMPT_ADJUST, IMenu.proportion);
        mEditDataParam.setProportionMode(mProportionInfo.getProportionMode(), mProportionInfo.getProportionValue());
        //保存草稿
        onSaveDraft(IMenu.proportion, true);
    }

    /**
     * 恢复之前的比例
     */
    public void resetoreProportion() {
        mProportionInfo = new ProportionInfo(mEditDataParam.getProportionMode(), mEditDataParam.getProportionValue());
    }


    private void refresh() {
        mListener.getEditor().refresh();
    }

    private MixInfo mMixInfo;
    private String mMixPath;

    /**
     * 图层合并
     */
    public void onMixStep(String path) {
        //1.拷贝当前记录当前虚拟图片的所有参数到MixInfo对象,并保存到可撤销队列
        mMixPath = path;
        onSaveStep(PROMPT_ADD, IMenu.mix);

        //2.修改当前虚拟图片的所有内容
        mEditDataParam.reset();


        ArrayList<CollageInfo> collageInfos = new ArrayList<>();
        collageInfos.add(VirtualIImageInfo.initBaseCollage(path));
        mEditDataParam.setCollageList(collageInfos);

        //保存草稿
        onSaveDraft(IMenu.mix, true);

    }


    private VirtualIImageInfo copyParam() {
        VirtualIImageInfo tmp = new VirtualIImageInfo();
        mEditDataParam.copy2VirtualImageInfo(tmp);
        return tmp;
    }

    private boolean bPauseImageRecord = false;

    /**
     * 暂停步骤记录
     */
    public void pause() {
        bPauseImageRecord = true;
    }

    public void resume() {
        bPauseImageRecord = false;
    }

    /**
     * 恢复旧的pip列表
     */
    public void restorePipList(ArrayList<CollageInfo> bkList) {
        setCollageList(bkList);
    }

    /**
     * 恢复旧的字幕列表
     */
    public void restoreTextList(ArrayList list) {
        setWordNewList(list);
    }
}
