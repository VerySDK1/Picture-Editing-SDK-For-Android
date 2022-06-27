package com.pesdk.uisdk.bean.model;

import com.pesdk.uisdk.util.helper.SubUtils;

import androidx.annotation.Keep;

/**
 * 字幕、贴纸、马赛克、水印 基础类
 */
@Keep
public abstract class ICommon implements ITimeLine {
    public int id = -1;
    //绑定的样式
    protected int styleId = SubUtils.DEFAULT_ID;
    protected boolean changed = false;

    public ICommon() {

    }

    public void setStyleId(int _styleId) {
        styleId = _styleId;
        setChanged();
    }

    public int getStyleId() {
        return styleId;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setChanged() {
        changed = true;
    }

    public boolean IsChanged() {
        return changed;
    }

    public void resetChanged() {
        changed = false;
    }



    private boolean hide;

    @Override
    public boolean isHide() {
        return hide;
    }

    @Override
    public void setHide(boolean hide) {
        this.hide = hide;
    }

    private float mAlpha = 1.0f; //不透明度

    /**
     * 不透明度
     */
    @Override
    public float getAlpha() {
        return mAlpha;
    }


    /**
     * 设置不透明度
     *
     * @param alpha
     */
    @Override
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }
}
