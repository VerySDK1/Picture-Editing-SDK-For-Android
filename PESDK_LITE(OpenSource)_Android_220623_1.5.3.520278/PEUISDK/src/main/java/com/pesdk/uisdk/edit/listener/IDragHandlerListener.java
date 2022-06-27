package com.pesdk.uisdk.edit.listener;

/**
 */
public interface IDragHandlerListener {
    /**
     * 保存
     */
    boolean onSave();
    /**
     * 预览
     */
    void onPreview(boolean preview);

    /**
     * 编辑
     */
    boolean edit(int index, int mode);


    /**
     *
     * @param index
     * @param mode
     * @param enableCopy
     * @return
     */
    boolean edit(int index, int mode,boolean enableCopy,boolean enableEdit);

    /**
     * 添加字幕关键帧
     * @param update true表示修改  false表示 当前时间存在删除 反之
     */
    void addKeyframe(boolean update);
}
