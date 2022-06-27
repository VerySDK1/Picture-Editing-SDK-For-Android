package com.pesdk.uisdk.bean.model;

public interface ITimeLine {

    /**
     * item的Id 与 ThumbNailLine （SubInfo中的Id）一致
     */
    int getId();

    /**
     * 是否隐藏，通过控制不透明度来实现
     *
     * @return
     */
    boolean isHide();

    void setHide(boolean hide);

    /**
     * 不透明度
     */
    float getAlpha();


    /**
     * 设置不透明度
     *
     * @param alpha
     */
    void setAlpha(float alpha);

}
