package com.pesdk.uisdk.listener;

import com.pesdk.uisdk.edit.EditDataHandler;

public interface ImageListener {


    /**
     * 返回当前时间
     */
    int getCurrentPosition();


    /**
     * 返回存储的数据
     */
    EditDataHandler getParamHandler();

    /**
     * 返回总的时间
     */
    int getDuration();

}
