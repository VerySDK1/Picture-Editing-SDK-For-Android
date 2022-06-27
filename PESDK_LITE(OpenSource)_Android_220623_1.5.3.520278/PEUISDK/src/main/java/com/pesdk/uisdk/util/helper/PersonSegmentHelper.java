package com.pesdk.uisdk.util.helper;

import android.graphics.Bitmap;

import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.analyzer.ExtraPreviewFrameListener;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.utils.MiscUtils;

import java.util.List;

/**
 * 同步抠图
 */
public class PersonSegmentHelper {


    /**
     * 单个人像
     */
    public void process(PEImageObject peImageObject, Callback callback) {
        int maxWH = 0;
        float asp = peImageObject.getWidth() * 1.0f / peImageObject.getHeight();
        if (asp > 1) { //与ExtraPreviewFrameListener 缩放一致
            maxWH = (int) (ExtraPreviewFrameListener.MIN_BITMAP * asp);
        } else {
            maxWH = (int) (ExtraPreviewFrameListener.MIN_BITMAP / asp);
        }
        Bitmap bitmap = MiscUtils.getBitmapByMedia(peImageObject.getInternal(), maxWH);
        //同步抓取mask
        AnalyzerManager.getInstance().extraBitmap(bitmap, (mask) -> {
            callback.onResult(mask);
        });
    }

    private static final String TAG = "PersonSegmentHelper";


    /**
     * 提前准备人像图片(主图|画中画)
     */
    public void process(VirtualIImageInfo virtualImageInfo) {
        final Object lock = new Object();
//        PEImageObject peImageObject = virtualImageInfo.getScene().getPEImageObject();
//        ImageOb ob = PEHelper.initImageOb(peImageObject);
//        if (ob.getSegmentType() == Segment.SEGMENT_PERSON) {
//            ImageOb finalOb = ob;
//            try {
//                process(new PEImageObject(peImageObject.getMediaPath()), bitmap -> {
//                    if (null != bitmap) {
//                        bindMask(bitmap, finalOb);
//                    }
//                    synchronized (lock) {
//                        lock.notify();
//                    }
//                });
//                synchronized (lock) {
//                    try {
//                        lock.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (InvalidArgumentException e) {
//                e.printStackTrace();
//            }
//
//        }
        List<CollageInfo> list = virtualImageInfo.getCollageInfos();
        if (null != list) {
            for (CollageInfo info : list) {
                PEImageObject tmp = info.getImageObject();
                ImageOb ob = PEHelper.initImageOb(tmp);
                if (ob.getSegmentType() == Segment.SEGMENT_PERSON) {
                    ImageOb finalOb1 = ob;
                    try {
                        process(new PEImageObject(tmp.getMediaPath()), bitmap -> {
                            if (null != bitmap) {
                                bindMask(bitmap, finalOb1);
                            }
                            synchronized (lock) {
                                lock.notify();
                            }
                        });
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void bindMask(Bitmap bitmap, ImageOb ob) {
        if (null != bitmap) {
            try {
                String path = PathUtils.getTempFileNameForSdcard("mask_" + bitmap.hashCode(), "png");
                BitmapUtils.saveBitmapToFile(bitmap, true, 100, path);
                ob.setMaskPath(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bitmap.recycle();
        }
    }

    public static interface Callback {

        void onResult(Bitmap bitmap);
    }

}
