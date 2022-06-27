package com.pesdk.uisdk.listener;

import com.pesdk.uisdk.bean.model.UndoInfo;

/**
 * 步骤
 */
public interface OnStepListener {

    /**
     * 删除步骤不是撤销 只是删除
     */
    UndoInfo onDeleteStep();

    /**
     * 保存步骤   操作名字和当前操作的模式
     */
    void onSaveStep(String name, int mode);

//    /**
//     * 编辑媒体(场景)
//     */
//    void onSaveMediaStep(String name);

    /**
     * 调整
     */
    void onSaveAdjustStep(int mode);

    /**
     * 保存草稿
     */
    void onSaveDraft(int mode);

}