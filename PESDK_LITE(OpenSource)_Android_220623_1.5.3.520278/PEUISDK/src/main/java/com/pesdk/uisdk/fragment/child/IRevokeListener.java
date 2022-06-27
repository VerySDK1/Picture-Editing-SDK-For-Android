package com.pesdk.uisdk.fragment.child;

/**
 * 画笔|消除笔界面   4个菜单
 */
public interface IRevokeListener {
    void onRevoke();

    void onUndo();

    void onReset();

    void onDiffBegin();

    void onDiffEnd();


}
