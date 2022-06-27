package com.pesdk.api;

/**
 * 草稿
 */
public interface IVirtualImageInfo {

    int getId();

    /**
     * 存为草稿的时刻 (基于系统时间) 单位：毫秒
     *
     * @return
     */
    long getCreateTime();

    /**
     * 更新时间
     *
     * @return 单位：毫秒
     */
    long getUpdateTime();

    /**
     * 虚拟封面
     */
    String getCover();


}
