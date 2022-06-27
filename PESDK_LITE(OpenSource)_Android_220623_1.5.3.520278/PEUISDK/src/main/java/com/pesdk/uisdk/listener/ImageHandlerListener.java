package com.pesdk.uisdk.listener;

import android.widget.FrameLayout;

import com.pesdk.uisdk.Interface.PreivewListener;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.fragment.helper.OverLayHandler;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.main.fg.PipLayerHandler;
import com.pesdk.uisdk.widget.edit.DragBorderLineView;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.models.EffectInfo;

public interface ImageHandlerListener extends ImageListener {

    /**
     * 点击其他按钮时，优先保存当前选中的内容(退出编辑)
     */
    void preMenu();

    /**
     * 确定
     */
    void onSure(boolean fresh);

    /**
     * 全部刷新  true全部刷新  false代表仅刷新音乐
     */
    void onRefresh(boolean all);


    /**
     * 获取 虚拟视频播放器
     */
    VirtualImageView getEditor();

    /**
     * 获取播放
     */
    VirtualImage getEditorImage();


    /**
     * 获取容器
     */
    FrameLayout getContainer();

    /**
     * 播放器同级的容器(用来装载画中画拖拽组件，可挪动到真实预览区域外)
     *
     * @return
     */
    FrameLayout getPlayerContainer();


    DragBorderLineView getLineView();


    /**
     * 操作数据 设置选中
     */
    void onSelectData(int id);


    /**
     * 选中字幕|贴纸
     */
    void onSelectedItem(@IMenu int mode, int index);


    int getIndex(@IMenu int mode,int id);



    /**
     * 滤镜
     */
    void onChangeEffectFilter();


    VirtualIImageInfo getVirtualImageInfo();


    int getDuration();

    int getCurrentPosition();

    void onBack();

    void onSure();


    PipLayerHandler getForeground();

    OverLayHandler getOverLayerHandler();

    void reBuild();


    void onExitSelect();


    /**
     * 更改滤镜
     */
    void onFilterChange();

    /**
     * 特效 预览单个特效
     */
    void onEffect(EffectInfo info);


    void registerListener(PreivewListener listener);

    void unregisterListener(PreivewListener listener);


    /**
     * 切换比例，调整字幕、贴纸、画中画位置
     *
     * @param newAsp             新的预览比例
     * @param fixPreviewListener
     */
    void fixDataSourceAfterReload(float newAsp, IFixPreviewListener fixPreviewListener);


    /**
     * 播放器当前预览比例
     */
    float getPlayerAsp();


    boolean enablePipDeleteMenu();


}
