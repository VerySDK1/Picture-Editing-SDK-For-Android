package com.pesdk.uisdk.bean.model;

/**
 * 特效的循环时刻
 */
public class TimeArray {

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    private int begin, end;

    public TimeArray(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public int getDuration() {   //持续时间
        return end - begin;
    }
}
