package com.pesdk.uisdk.analyzer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.analyzer.internal.MLKitEngine;
import com.pesdk.uisdk.analyzer.internal.MattingEngine;
import com.pesdk.uisdk.analyzer.internal.person.PersonSegmentRunnable;
import com.pesdk.uisdk.analyzer.internal.person.SegmentResultListener;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.matting.MattingerFactory;
import com.vecore.models.PEImageObject;

/**
 * 人像分割引擎处理
 */
public class AnalyzerManager extends BaseManager {

    private static final String TAG = "AnalyzerManager";

    /**
     * 引擎分类
     */
    public static enum EngineType {
        MATTING, ML_KIT
    }


    /**
     * 单例模式
     **/
    @SuppressLint("StaticFieldLeak")
    private static volatile AnalyzerManager sMAnalyzerManager = null;


    /**
     * 构造函数私有化
     **/
    private AnalyzerManager() {

    }

    /**
     * 公有的静态函数，对外暴露获取单例对象的接口
     **/
    public static AnalyzerManager getInstance() {
        if (sMAnalyzerManager == null) {
            synchronized (AnalyzerManager.class) {
                if (sMAnalyzerManager == null) {
                    sMAnalyzerManager = new AnalyzerManager();
                }
            }
        }
        return sMAnalyzerManager;
    }


    private Context mContext;


    /**
     * 初始化
     */
    public void init(Context context, EngineType type, String mBinPath, String mProtoPath) {
        if (context == null) {
            return;
        }
        if (!inited) {
            mContext = context.getApplicationContext();
            if (type == EngineType.ML_KIT) {
                //创建引擎
                mEngine = new MLKitEngine();
                mEngine.createAnalyzer(0, null, null);
            } else if (type == EngineType.MATTING) {
                mEngine = new MattingEngine();
                if (mBinPath != null && mProtoPath != null) {
                    mEngine.createAnalyzer(MattingerFactory.MattingerOption.PORTRAIT_MATTING, mBinPath, mProtoPath);
                }
            }
            inited = true;
        }
    }


    /**
     * 媒体 注册扩展
     */
    public void extraMedia(PEImageObject peImageObject, boolean export) {
        extraMedia(peImageObject, export, success -> {
            if (!success) {
                mHandler.post(() -> {
                    Utils.autoToastNomal(mContext, R.string.pesdk_toast_person_segment);
                });
            }
        });
    }


    /**
     * 抠取bitmap人像
     *
     * @param bitmap   带人像的bitmap
     * @param callback
     */
    public void extraBitmap(Bitmap bitmap, SegmentResultListener callback) {
        //同步抓取mask
        PersonSegmentRunnable runnable = new PersonSegmentRunnable(bitmap, callback, mEngine);
        ThreadPoolUtils.execute(runnable);
    }
}
