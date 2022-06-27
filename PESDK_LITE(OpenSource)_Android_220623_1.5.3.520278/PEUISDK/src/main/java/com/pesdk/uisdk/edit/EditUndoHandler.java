package com.pesdk.uisdk.edit;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageUndoHandlerListener;

import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 管理可撤销、可还原数据列表
 */
public class EditUndoHandler implements ImageUndoHandlerListener {

    private static final String TAG = "EditUndoHandler";

    /**
     * 最大保存步骤
     */
    private final int MAX_STEP = 30;

    /**
     * 返回接口
     */
    private final OnUndoListener mListener;
    private final Context mContext;

    /**
     * 控件
     */
    private final View mBtnUndo;
    private final View mBtnReduction;


    /**
     * 撤销和还原
     */
    private final Deque<UndoInfo> mRevokeList = new LinkedBlockingDeque<>(MAX_STEP);//可撤销的列表 . 记录当前状态的上一状态的数据,并非当前状态的数据(对应list.size ==0)[不记录当前状态的数据:因为撤销后要扫描上一次的同一类型的数据]
    private final Deque<UndoInfo> mRedoList = new LinkedBlockingDeque<>(MAX_STEP);//可还原的列表(重做) . 当mRevokeList 新增数据时，清空列表 mRedoList

    private final ArrayList<String> mUndoInfos = new ArrayList<>();
    private final ArrayList<String> mReductionInfos = new ArrayList<>();

    public EditUndoHandler(Context context, View btnUndo, View btnReduction, OnUndoListener listener) {
        mBtnUndo = btnUndo;
        mBtnReduction = btnReduction;
        mListener = listener;
        mContext = context;


        mBtnUndo.setOnClickListener(v -> onClickUndo(false, true, true));
        mBtnReduction.setOnClickListener(v -> onClickReduction(false, true));

        judgeEnabled();
    }


    /**
     * 判断能否撤销和还原
     */
    private void judgeEnabled() {
        Log.e(TAG, "judgeEnabled: " + mRevokeList.size() + " " + mRedoList.size());
        mBtnUndo.setEnabled(!mRevokeList.isEmpty());
        mBtnReduction.setEnabled(!mRedoList.isEmpty());
    }


    /**
     * 获取步骤名字
     */
    private String getName(UndoInfo undoInfo) {
        int mode = undoInfo.getMode();
        StringBuilder builder = new StringBuilder();
//        if (mode == MODE_SPINDLE || mode == MODE_MEDIA
//                || mode == MODE_FREEZE || mode == MODE_SWITCH_PIP) {
//            //编辑主轴 改变了场景
//            builder.append(mContext.getString(R.string.media));
//        } else

        if (mode == IMenu.text) {
            //字幕
            builder.append(mContext.getString(R.string.pesdk_text));
        } else if (mode == IMenu.sticker) {
            //贴纸
            builder.append(mContext.getString(R.string.pesdk_sticker));
        } else if (mode == IMenu.pip) {
            //画中画
            builder.append(mContext.getString(R.string.pesdk_menu_layer));
        } else if (mode == IMenu.graffiti) {
            //涂鸦
            builder.append(mContext.getString(R.string.pesdk_doodling));
        }
//        else if (mode == MODE_MASK) {
//            //马赛克
//            builder.append(mContext.getString(R.string.edit_menu_mosaic));
//        }
        else if (mode == IMenu.effect) {
            //特效
            builder.append(mContext.getString(R.string.pesdk_effect));
        } else if (mode == IMenu.adjust) {
            //调节
            builder.append(mContext.getString(R.string.pesdk_adjust));
        } else if (mode == IMenu.filter) {
            //滤镜
            builder.append(mContext.getString(R.string.pesdk_filter));
        } else if (mode == IMenu.beauty) {
            builder.append(mContext.getString(R.string.pesdk_beauty));
        } else if (mode == IMenu.canvas) {
            builder.append(mContext.getString(R.string.pesdk_background));
        } else if (mode == IMenu.depth) {
            builder.append(mContext.getString(R.string.pesdk_depth));
        } else if (mode == IMenu.erase) {
            builder.append(mContext.getString(R.string.pesdk_erase_pen));
        } else if (mode == IMenu.crop) {
            builder.append(mContext.getString(R.string.pesdk_crop));
        } else if (mode == IMenu.mirror) {
            builder.append(mContext.getString(R.string.pesdk_mirror));
        } else if (mode == IMenu.proportion) {
            //比例
            builder.append(mContext.getString(R.string.pesdk_proportion));
        } else if (mode == IMenu.mix) {
            //比例
            builder.append(mContext.getString(R.string.pesdk_layer_merge));
        }

//        else if (mode == MODE_WATERMARK) {
//            //水印
//            builder.append(mContext.getString(R.string.edit_menu_watermark_add));
//        }

        builder.append("\t");
        builder.append(undoInfo.getName());
        return builder.toString();
    }


    @Override
    public void addUndo(int mode, String name, ArrayList list) {
        if (list == null) {
            return;
        }
        Log.e(TAG, "addUndo:-----------------------------------> " + mode + " " + name + " " + list.size() + "  mReductionInfoList:" + mRedoList.size() + " mReductionInfos:" + mReductionInfos.size());
        mRedoList.clear();
        mReductionInfos.clear();
        if (mRevokeList.size() >= MAX_STEP) {
            //出队
            mRevokeList.pollLast();
            mUndoInfos.remove(0);
        }
        //入队
        UndoInfo info = new UndoInfo(mode, name, list);
        if (!mRevokeList.offerFirst(info)) {
            //出队
            mRevokeList.pollLast();
            mUndoInfos.remove(0);
            //入队
            if (mRevokeList.offerFirst(info)) {
                mUndoInfos.add(getName(info));
            }
        } else {
            mUndoInfos.add(getName(info));
        }
        if (null != mListener) {
            mListener.onStepChanged(mode != IMenu.mix);
        }
        judgeEnabled();
    }

    @Override
    public UndoInfo deleteUndo() {
//        Log.e(TAG, "deleteUndo: " + mRevokeList.size());
        UndoInfo info = null;
        if (!mRevokeList.isEmpty()) {
            info = mRevokeList.pollFirst();
            if (mUndoInfos.size() > 0) {
                mUndoInfos.remove(mUndoInfos.size() - 1);
            }
        }
        judgeEnabled();
        return info;
    }


    /**
     * 恢复上一步的状态（撤销，但要清理可还原列表 ，类似：deleteUndo+数据还原）
     */
    public void onUndo() {
        onClickUndo(false, false, false);
    }

    /**
     * 撤销
     *
     * @param b
     * @param bToast
     * @param enableRedo true 是否可还原
     */
    private void onClickUndo(boolean b, boolean bToast, boolean enableRedo) {
//        Log.e(TAG, "onClickUndo: " + mListener + " mUndoInfoList: " + mRevokeList.size());
        if (mListener != null && mListener.isCanUndo() && !mRevokeList.isEmpty()) {
            UndoInfo tmp = mRevokeList.pollFirst();
            UndoInfo info = mListener.onUndoReduction(true, tmp, b, bToast);
            if (mUndoInfos.size() > 0) {
                mUndoInfos.remove(mUndoInfos.size() - 1);
            }
            if (mUndoInfos.size() == 0) { //不可以合并图层
                mListener.onStepChanged(false);
            }
//            Log.e(TAG, "onClickUndo: " + info);
            if (info.getMode() == IMenu.mix) {
                mListener.onStepChanged(true);
            } else {
                mListener.onStepChanged(false);
            }
            if (enableRedo && info != null) {
                if (mRedoList.offerFirst(info)) {
                    mReductionInfos.add(getName(info));
                }
            }
            judgeEnabled();
        }
    }

    /**
     * 还原
     */
    private void onClickReduction(boolean b, boolean bToast) {
        if (mListener != null && mListener.isCanUndo() && !mRedoList.isEmpty()) {
            UndoInfo info = mListener.onUndoReduction(false, mRedoList.pollFirst(), b, bToast);
            if (mReductionInfos.size() > 0) {
                mReductionInfos.remove(mReductionInfos.size() - 1);
            }
            if (info != null) {
                if (mRevokeList.offerFirst(info)) {
                    mUndoInfos.add(getName(info));
                }
            }
//            Log.e(TAG, "onClickReduction: " + info);
            if (mUndoInfos.size() > 0 && info.getMode() != IMenu.mix) { //可以合并图层
                mListener.onStepChanged(true);
            } else {
                mListener.onStepChanged(false);
            }
            judgeEnabled();
        }
    }


    public interface OnUndoListener {

        /**
         * 撤销、还原
         */
        UndoInfo onUndoReduction(boolean undo, UndoInfo info, boolean build, boolean bToast);

        /**
         * 能否撤销还原
         */
        boolean isCanUndo();


        void onStepChanged(boolean changed);
    }

}
