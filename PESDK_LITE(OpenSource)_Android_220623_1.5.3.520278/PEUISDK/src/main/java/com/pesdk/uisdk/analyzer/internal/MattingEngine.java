package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;

import com.vecore.matting.MattingResult;
import com.vecore.matting.Mattinger;
import com.vecore.matting.MattingerFactory;
import com.vecore.matting.tasks.MattingTask;

import java.io.IOException;

/**
 * 支持人像抠图|天空抠图
 */
public class MattingEngine implements SegmentationEngine {
    private Mattinger mMattinger;


    @Override
    public void createAnalyzer(int mode, String binPath, String paramPath) {
        mMattinger = MattingerFactory.getMattinger(new MattingerFactory.MattingerOption().setModel(mode, binPath, paramPath));

    }

    @Override
    public void release() {
        if (null != mMattinger) {
            try {
                mMattinger.close();
                mMattinger = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void asyncProcess(Bitmap bitmap, OnSegmentationListener listener) {
        MattingTask<MattingResult> task = mMattinger.process(com.vecore.matting.InputImage.fromBitmap(bitmap));
        if (null != task) {
            task.addOnSuccessListener(mattingResult -> {
                if (mattingResult.data != null) {
                    Bitmap tmp = Bitmap.createBitmap(mattingResult.data);
                    mattingResult.data.recycle();
                    listener.onSuccess(tmp);
                } else {
                    listener.onFail("");
                }
            }).addOnFailureListener(ex -> listener.onFail(ex.getMessage()));
        } else {
            listener.onFail("");
        }
    }

    @Override
    public void syncProcess(Bitmap bitmap, OnSegmentationListener listener) {
        MattingResult mattingResult = mMattinger.processSync(com.vecore.matting.InputImage.fromBitmap(bitmap));
        if (null != mattingResult && mattingResult.data != null) {
            Bitmap tmp = Bitmap.createBitmap(mattingResult.data);
            mattingResult.data.recycle();
            listener.onSuccess(tmp);
        } else {
            listener.onFail("");
        }
    }


}
