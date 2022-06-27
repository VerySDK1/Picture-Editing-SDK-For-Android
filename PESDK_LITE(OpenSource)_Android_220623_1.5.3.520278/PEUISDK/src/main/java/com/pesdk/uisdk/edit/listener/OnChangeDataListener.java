package com.pesdk.uisdk.edit.listener;

import android.widget.FrameLayout;

import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;

/**
 *
 */
public interface OnChangeDataListener {
    /**
     * 特效等增加或者删除了 刷新下方
     */
    void onChange(boolean b);

    /**
     * true 可以撤销； false 不允许撤销
     */
    boolean isCanUndo();

    /**
     * 有撤销步骤
     */
    void onStepChanged(boolean changed);
    /**
     * 撤销、返回
     * fresh  0不刷新  1全部刷新   2只刷新音乐
     * start开始
     */
    void onUndoReduction(int build, boolean start);


    /**
     * 设置比例
     */
    void setAsp(float asp);

    /**
     * 获取 虚拟视频播放器
     */
    VirtualImageView getEditor();

    /**
     * 获取播放
     */
    VirtualImage getEditorVideo();

    /**
     * 获取容器
     */
    FrameLayout getContainer();

    /**
     * 全部刷新  true全部刷新  false代表仅刷新音乐
     */
    void onRefresh(boolean all);


    /**
     * 操作数据 设置选中
     */
    void onSelectData(int id);

    /**
     * 播放器当前预览比例
     */
    float getPlayerAsp();


}
