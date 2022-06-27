package com.pesdk.uisdk.Interface;

import java.util.HashMap;

/**
 * 下载进度
 */
public interface IProgressCallBack {

    /**
     * 获取下载进度
     *
     * @return
     */
    HashMap<String, Integer> getMap();
}
