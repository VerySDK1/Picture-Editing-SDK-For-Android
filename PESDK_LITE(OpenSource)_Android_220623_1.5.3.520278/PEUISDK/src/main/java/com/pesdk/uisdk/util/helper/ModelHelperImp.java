package com.pesdk.uisdk.util.helper;

import android.content.Context;

import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.analyzer.SkyAnalyzerManager;

/**
 * 统一管理模型: 注册下载
 */
public class ModelHelperImp {
    /**
     * 加载模型 (首次进入|直接导出草稿时)  ,不执行下载流程
     */
    public static void loadModel(Context context) {
        if (!AnalyzerManager.getInstance().isInited()) { //人像抠图
            new ModelHelper().checkModel(context, true, (bin, param) -> {
                AnalyzerManager.getInstance().init(context, AnalyzerManager.EngineType.MATTING, bin, param);
            }, false);
            //  AnalyzerManager.getInstance().init(this, AnalyzerManager.EngineType.ML_KIT);
        }

        if (!SkyAnalyzerManager.getInstance().isInited()) {//天空抠图
            new ModelHelper().checkModel(context, false, (bin, param) -> {
                SkyAnalyzerManager.getInstance().init(context, bin, param);
            }, false);
        }
    }

    /**
     * 准备人像抠图，自动完成下载并初始化
     */
    public static void checkAnalyzer(Context context, Callbck callbck) {
        if (AnalyzerManager.getInstance().isInited()) {
            callbck.inited();
        } else {
            new ModelHelper().checkModel(context, true, (bin, param) -> {
                AnalyzerManager.getInstance().init(context, AnalyzerManager.EngineType.MATTING, bin, param); //初始化
                callbck.inited();
            });
        }

    }

    /**
     * 准备天空抠图
     */
    public static void checkSkyAnalyzer(Context context, Callbck callbck) {
        if (SkyAnalyzerManager.getInstance().isInited()) {
            callbck.inited();
        } else {
            new ModelHelper().checkModel(context, false, (bin, param) -> {
                SkyAnalyzerManager.getInstance().init(context, bin, param); //初始化
                callbck.inited();
            });
        }

    }

    public static interface Callbck {
        void inited();
    }
}
