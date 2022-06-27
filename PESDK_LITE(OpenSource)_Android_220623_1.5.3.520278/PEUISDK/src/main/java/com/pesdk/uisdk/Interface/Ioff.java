package com.pesdk.uisdk.Interface;

import com.pesdk.uisdk.bean.model.CollageInfo;

/**
 * 调整位置
 */
public interface Ioff {
    /**
     * 居中
     */
    void offCenter();

    /**
     * 往左微调
     */
    void offLeft();

    void offUp();

    void offDown();

    void offRight();

    void offLarge();

    /**
     * 缩小
     */
    void offNarrow();

    /**
     * 放大到全屏
     */
    void offFull();


    /**
     * 旋转
     */
    void setAngle(int angle);

    /**
     * 当前正在编辑的item
     *
     * @return
     */
    CollageInfo getCurrentCollageInfo();

}
