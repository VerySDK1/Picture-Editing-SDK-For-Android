package com.pesdk.uisdk.listener;

import com.pesdk.uisdk.bean.model.UndoInfo;

import java.util.ArrayList;

public interface ImageUndoHandlerListener {


    /**
     * 改变数据存入撤销中
     * @param mode 当前操作模式
     * @param name 步骤名字
     * @param list 数据
     */
    void addUndo(int mode, String name, ArrayList list);

    /**
     * 删除步骤
     */
    UndoInfo deleteUndo();

}
