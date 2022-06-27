package com.pesdk.uisdk.bean.model;

/**
 * 字幕特效每一帧的显示时刻、背景图
 */
public class FrameInfo {

    @Override
    public String toString() {
        return "FrameInfo [time=" + time + ", pic=" + pic + "]";
    }

    public FrameInfo() {
    }


    //当前帧的显示时刻  单位：ms
    public int time;
    //当前帧的背景图路径
    public String pic;

}
