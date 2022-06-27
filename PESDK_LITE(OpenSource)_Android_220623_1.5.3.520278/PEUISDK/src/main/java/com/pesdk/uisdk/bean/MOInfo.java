package com.pesdk.uisdk.bean;

import android.graphics.RectF;

import com.pesdk.uisdk.bean.model.ICommon;
import com.pesdk.uisdk.util.Utils;
import com.vecore.models.DewatermarkObject;

import androidx.annotation.Keep;

/**
 * 马赛克|去水印
 */
@Keep
public class MOInfo extends ICommon implements Comparable {
    private DewatermarkObject mObject;

    public void setShowRectF(RectF showRectF) {
        if (null != showRectF) {
            mShowRectF = showRectF;
            mObject.setShowRectF(showRectF);
        }
    }

    public RectF getShowRectF() {
        return mShowRectF;
    }

    //相对于预览尺寸 0~1.0f
    private RectF mShowRectF = new RectF();

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (this.value != value) {
            this.value = value;
            mObject.setValue(this.value);
            setChanged();
            setShowRectF(getShowRectF());
            getObject().quitEditCaptionMode(true);
        }
    }

    private float value = 0.5f;


    public DewatermarkObject getObject() {
        return mObject;
    }

    @Override
    public String toString() {
        return "MOInfo{" +
                " id=" + id +
                ", mObject=" + mObject +
                ", styleId=" + styleId +
                ", changed=" + changed +
                ", mShowRectF=" + mShowRectF +
                '}';
    }


    public MOInfo copy() {
        return new MOInfo(this);
    }

    public void set(MOInfo info) {
        this.id = info.id;
        this.styleId = info.styleId;
        this.mObject = new DewatermarkObject(info.getObject());
        this.mShowRectF = new RectF(info.mShowRectF);
        value = info.value;

    }


    /**
     * @param end    更新结束点
     * @param update
     */
    public void setEnd(long end, boolean update) {
        mObject.setTimelineRange(mObject.getTimelineStart(), Utils.ms2s(end), update);
        setChanged();
    }


    /**
     * @param start
     * @param end
     * @param update 是否需要更新core中的UI
     */
    public void setTimelineRange(long start, long end, boolean update) {
        mObject.setTimelineRange(Utils.ms2s(start), Utils.ms2s(end), update);
        setChanged();
    }


    public MOInfo() {
        super();
        mObject = new DewatermarkObject();
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof MOInfo) {
            MOInfo info = (MOInfo) o;
            return getId() == info.getId()
                    && getStyleId() == info.getStyleId()
                    && getValue() == info.getValue();

        } else {
            return false;
        }
    }

    public MOInfo(MOInfo info) {
        id = info.id;
        styleId = info.styleId;
        mObject = new DewatermarkObject(info.getObject());
        mShowRectF = new RectF(info.mShowRectF);
        changed = info.IsChanged();
        value = info.getValue();
    }

    public void recycle() {
        if (null != mObject) {
            mObject.recycle();
        }
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
