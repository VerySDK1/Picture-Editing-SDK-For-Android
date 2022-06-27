package com.pesdk.uisdk.analyzer;

import android.annotation.SuppressLint;
import android.content.Context;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.analyzer.internal.MattingEngine;
import com.pesdk.uisdk.util.Utils;
import com.vecore.matting.MattingerFactory;
import com.vecore.models.PEImageObject;

/**
 * 天空抠图引擎处理
 */
public class SkyAnalyzerManager extends BaseManager {

    private static final String TAG = "SkyAnalyzerManager";
    /**
     * 单例模式
     **/
    @SuppressLint("StaticFieldLeak")
    private static volatile SkyAnalyzerManager sMAnalyzerManager = null;


    /**
     * 构造函数私有化
     **/
    private SkyAnalyzerManager() {

    }

    /**
     * 公有的静态函数，对外暴露获取单例对象的接口
     **/
    public static SkyAnalyzerManager getInstance() {
        if (sMAnalyzerManager == null) {
            synchronized (SkyAnalyzerManager.class) {
                if (sMAnalyzerManager == null) {
                    sMAnalyzerManager = new SkyAnalyzerManager();
                }
            }
        }
        return sMAnalyzerManager;
    }


    private Context mContext;


    /**
     * 初始化
     */
    public void init(Context context, String mBinPath, String mProtoPath) {
        if (context == null) {
            return;
        }
        if (!inited) {
            mContext = context;
            mEngine = new MattingEngine();
            if (mBinPath != null && mProtoPath != null) {
                mEngine.createAnalyzer(MattingerFactory.MattingerOption.SKY_MATTING, mBinPath, mProtoPath);
                inited = true;
            }

        }

    }

    /**
     * 媒体 注册扩展
     */
    public void extraMedia(PEImageObject peImageObject, boolean export) {
        extraMedia(peImageObject, export, success -> mHandler.post(() -> {
            if (!success) {
                Utils.autoToastNomal(mContext, R.string.pesdk_toast_sky_segment);
            }
        }));
    }


    /**
     * 设置当前时间
     * 主要是瘦脸、大眼调整时刷新
     */
//    public void adjustFace() {
//        if (mMediaFrameList.size() > 0) {
//            for (ExtraPreviewFrameListener listener : mMediaFrameList) {
//                listener.setPts(-1);
//            }
//        }
//        if (mCollageFrameList.size() > 0) {
//            for (ExtraPreviewFrameListener listener : mCollageFrameList) {
//                listener.setPts(-1);
//            }
//        }
//    }


}
