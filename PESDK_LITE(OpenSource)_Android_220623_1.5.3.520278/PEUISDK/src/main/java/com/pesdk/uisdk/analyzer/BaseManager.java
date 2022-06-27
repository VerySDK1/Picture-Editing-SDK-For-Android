package com.pesdk.uisdk.analyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.pesdk.uisdk.analyzer.internal.OnSegmentationListener;
import com.pesdk.uisdk.analyzer.internal.ResultListener;
import com.pesdk.uisdk.analyzer.internal.SegmentationEngine;
import com.pesdk.uisdk.analyzer.internal.Util;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.ui.home.segment.listener.PreviewFrameListener;
import com.pesdk.uisdk.util.helper.BitmapUtil;
import com.vecore.models.PEImageObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class BaseManager {
    boolean inited = false;

    public void setEnableShowSegmentToast(boolean enableShowSegmentToast) {
        this.enableShowSegmentToast = enableShowSegmentToast;
    }

    boolean enableShowSegmentToast = false;

    SegmentationEngine mEngine;

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: {

                }
                break;
                default: {
                }
                break;
            }
        }
    };


    /**
     * 注册媒体
     */
    final ArrayList<ExtraPreviewFrameListener> mMediaFrameList = new ArrayList<>();
    /**
     * 画中画
     */
    final ArrayList<ExtraPreviewFrameListener> mCollageFrameList = new ArrayList<>();

    /**
     * 画中画 注册扩展
     */
    public void extraCollage(List<CollageInfo> collageList, boolean export) {
        releaseExtraCollage();
        if (collageList == null || collageList.size() <= 0) {
            return;
        }
        for (CollageInfo info : collageList) {
            PEImageObject object = info.getImageObject();
            ExtraPreviewFrameListener listener = new ExtraPreviewFrameListener(object, info.getId(), mEngine);
            listener.setExport(export);
            object.setExtraDrawListener(listener);
            mCollageFrameList.add(listener);
        }
    }


    public ExtraPreviewFrameListener getFrameListener(CollageInfo info) {
        for (ExtraPreviewFrameListener listener : mCollageFrameList) {
            if (listener.getId() == info.getId()) {
                //释放、移出
                listener.release();
                return listener;
            }
        }
        return null;
    }


    public void extraCollage(CollageInfo info, boolean export) {
        PEImageObject object = info.getImageObject();
        ExtraPreviewFrameListener listener = new ExtraPreviewFrameListener(object, info.getId(), mEngine);
        listener.setExport(export);
        object.setExtraDrawListener(listener);
        mCollageFrameList.add(listener);
    }

    /**
     * 插入、更新画中画
     */
    public void extraCollageInsert(CollageInfo info) {
        if (info == null) {
            return;
        }
        //先移出
        extraCollageRemove(info);
        //再添加
        PEImageObject object = info.getImageObject();
        ExtraPreviewFrameListener listener = new ExtraPreviewFrameListener(object, info.getId(), mEngine);
        listener.setExport(false);
        object.setExtraDrawListener(listener);
        mCollageFrameList.add(listener);
    }

    /**
     * 移出画中画
     */
    public void extraCollageRemove(CollageInfo info) {
        if (info == null) {
            return;
        }
        for (ExtraPreviewFrameListener listener : mCollageFrameList) {
            if (listener.getId() == info.getId()) {
                //释放、移出
                listener.release();
                mCollageFrameList.remove(listener);
                break;
            }
        }
    }

    void extraMedia(PEImageObject peImageObject, boolean export, ResultListener resultListener) {
        releaseExtraMedia();
        ExtraPreviewFrameListener listener = new ExtraPreviewFrameListener(peImageObject, mEngine);
        if (enableShowSegmentToast && !export) {
            listener.setResultListener(resultListener);
        }
        listener.setExport(export);
        peImageObject.setExtraDrawListener(listener);
        mMediaFrameList.add(listener);
    }


    public void extraMaskMedia(PEImageObject peImageObject, boolean export) {
        releaseExtraMedia();
        PreviewFrameListener listener = new PreviewFrameListener(peImageObject, mEngine);
        listener.setExport(export);
        peImageObject.setExtraDrawListener(listener);
    }


    private static final String TAG = "BaseManager";

    /**
     * 当前图片是否有人脸|天空
     */
    public void hasSegment(Context context, PEImageObject peImageObject, ISegmentationListener listener) {
        Bitmap srcBmp = BitmapUtil.getBitmap(context, peImageObject, 640);
        mEngine.asyncProcess(srcBmp, new OnSegmentationListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (null != listener) {
                    listener.existAlpha(null != bitmap ? Util.hasSegment(bitmap) : false);
                }
                srcBmp.recycle();
                if (null != bitmap) {
                    bitmap.recycle();
                }
            }

            @Override
            public void onFail(String msg) {
                Log.e(TAG, "onFail: " + msg);
                if (null != listener) {
                    listener.existAlpha(false);
                }
                srcBmp.recycle();
            }
        });
    }

    public boolean isInited() {
        return inited;
    }

    /**
     * 释放
     */
    public void release() {
        inited = false;
        if (null != mEngine) {
            mEngine.release();
            mEngine = null;
        }
        //释放
        releaseExtraMedia();
        releaseExtraCollage();
    }

    /**
     * 释放 媒体
     */
    private void releaseExtraMedia() {
        if (mMediaFrameList.size() > 0) {
            for (ExtraPreviewFrameListener listener : mMediaFrameList) {
                listener.release();
            }
            mMediaFrameList.clear();
        }
    }

    /**
     * 强制刷新
     */
    public void force() {
        if (mMediaFrameList.size() > 0) {
            for (ExtraPreviewFrameListener listener : mMediaFrameList) {
                listener.force();
            }
        }
        if (mCollageFrameList.size() > 0) {
            for (ExtraPreviewFrameListener listener : mCollageFrameList) {
                listener.force();
            }
        }
    }

    /**
     * 是否设置了抠图回调
     *
     * @param info
     * @return
     */
    public boolean isRegistered(CollageInfo info) {
        if (info == null) {
            return false;
        }
        for (ExtraPreviewFrameListener listener : mCollageFrameList) {
            if (listener.getId() == info.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 释放 画中画
     */
    private void releaseExtraCollage() {
        if (mCollageFrameList.size() > 0) {
            for (ExtraPreviewFrameListener listener : mCollageFrameList) {
                listener.release();
            }
            mCollageFrameList.clear();
        }
    }
}
