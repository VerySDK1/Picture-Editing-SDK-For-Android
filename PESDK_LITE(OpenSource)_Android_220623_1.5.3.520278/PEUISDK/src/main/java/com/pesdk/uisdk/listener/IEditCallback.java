package com.pesdk.uisdk.listener;

import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.edit.EditDragHandler;
import com.pesdk.uisdk.fragment.helper.OverLayHandler;
import com.pesdk.uisdk.util.helper.sky.SkyHandler;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.main.fg.PipLayerHandler;

/**
 *
 */
public interface IEditCallback {

    EditDragHandler getEditDragHandler();

    EditDataHandler getEditDataHandler();

    /**
     * 前景
     */
    PipLayerHandler getPipLayerHandler();

    /**
     * 叠加(与前景类似)
     */
    OverLayHandler getOverLayHandler();


    /**
     * 天空 （调整位置）
     */
    SkyHandler getSkyHandler();

    @IMenu
    int getMenu();
}
