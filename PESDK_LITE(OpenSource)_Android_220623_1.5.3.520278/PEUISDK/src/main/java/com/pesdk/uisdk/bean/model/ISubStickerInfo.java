package com.pesdk.uisdk.bean.model;

import androidx.annotation.Keep;


/**
 * 定义字幕和特效公共参数
 */
@Keep
public abstract class ISubStickerInfo extends ICommon implements Comparable {

    public ISubStickerInfo() {
        super();
    }


    /**
     * start,end 字幕的起止时间，毫秒 x,y; 表示字幕相对于视频的位置 0<x<1,0<y<1;
     */
    //宽高占当前预览区域宽高的比列
    protected double left = 0.2, top = 0.5;// (0<=left,top<1)控件距离字幕区域范围(left ,top


    public double getLeft() {
        return left;
    }

    public void setLeft(Double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(Double top) {
        this.top = top;
    }


    public float[] getCenterxy() {
        return centerxy;
    }

    public void setCenterxy(float[] centerxy) {
        this.centerxy = centerxy;
        setChanged();
    }


    protected float[] centerxy = new float[]{0.5f, 0.5f}; // 图片旋转中心点坐标在x，y的比例


    abstract void setRotateAngle(float rotateAngle);


    protected int mTextColor;


    public int getTextColor() {
        return mTextColor;
    }


    /**
     * 缩放比
     *
     * @return
     */
    abstract float getDisf();

    abstract void setDisf(float disf);


    public float getParentWidth() {
        return parentWidth;
    }

    public float getParentHeight() {
        return parentHeight;
    }
    public void setParent(float parentWidth, float parentHeight) {
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
    }

    //导出时，记录当前的贴纸size
    protected float parentWidth = 0, parentHeight;

    @Override
    public int compareTo(Object o) {
        return 0;
    }



}
