package com.pesdk.uisdk.fragment.canvas.callback;

/**
 *
 */
public interface Callback extends SkyCallback {
    /**
     * 选择背景颜色
     *
     * @param color
     */
    void onColor(int color);

    int getBgColor();

}
