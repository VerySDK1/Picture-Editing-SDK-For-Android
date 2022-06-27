package com.pesdk.uisdk.fragment.callback;

import com.pesdk.uisdk.fragment.main.IMenu;

/**
 * 字幕、贴纸 UI控制
 */
public interface IDrag {

    /**
     * 已经删除字幕、贴纸
     * @param type
     * @param id
     */
    void deleted(@IMenu int type, int id);

}
