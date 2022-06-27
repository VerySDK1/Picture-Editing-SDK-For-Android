package com.pesdk.uisdk.bean;

import com.vecore.models.PEImageObject;

/**
 * 边框
 */
public class FrameInfo {

    private PEImageObject mPEImageObject;
    private float asp;
    private String mSortId;

    public FrameInfo(PEImageObject peImageObject,   String sortId) {
        mPEImageObject = peImageObject;
        asp = peImageObject.getWidth() / (peImageObject.getHeight() + 0.0f);
        mSortId = sortId;
    }

    public String getSortId() {
        return mSortId;
    }

    public float getAsp() {
        return asp;
    }

    public PEImageObject getPEImageObject() {
        return mPEImageObject;
    }

    public FrameInfo copy() {
        return new FrameInfo(mPEImageObject.copy(), mSortId);
    }

    @Override
    public String toString() {
        return "BorderInfo{" +
                "mPEImageObject=" + mPEImageObject +
                ", asp=" + asp +
                ", mSortId='" + mSortId + '\'' +
                '}';
    }
}
