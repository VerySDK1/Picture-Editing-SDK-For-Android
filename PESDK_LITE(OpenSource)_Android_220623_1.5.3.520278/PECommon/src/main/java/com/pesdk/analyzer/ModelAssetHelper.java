package com.pesdk.analyzer;

import android.content.Context;

import com.pesdk.utils.PathUtils;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownListener;
import com.vecore.base.http.MD5;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载抠图需要的模型
 */
public class ModelAssetHelper {

    private Callback mCallback;
    private Context mContext;
    private static final String baseUrl = "https://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/init/pe_asset/model/";
    private List<IParam> downList = new ArrayList<>();//需要下载的文件列表
    private boolean bCanceled = false;
//        https://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/init/pe_asset/model/matting-480.bin

    /**
     * 第一步：验证本地是否存在文件
     *
     * @param person true 人像; false 天空
     * @return
     */
    public IModel checkModel(boolean person) {
        downList.clear();

        String url = getUrl(person ? "matting-480.bin" : "sky_seg.bin");
        File file = getFile(url, "bin");
        boolean noExitsBin = null == file || !file.exists();
        downList.add(new IParam(url, file.getPath()));


        url = getUrl(person ? "matting-480.proto" : "sky_seg.param");
        file = getFile(url, person ? "proto" : "param");
        boolean noExitsParam = null == file || !file.exists();
        downList.add(new IParam(url, file.getPath()));

        if (noExitsBin || noExitsParam) {
            return null;
        }
        return new IModel(downList.get(0).localPath, downList.get(1).localPath);
    }


    private File getFile(String url, String strExtension) {
        return new File(PathUtils.getAssetPath(), MD5.getMD5(url) + "." + strExtension);
    }


    /**
     * 第二步: 下载人像识别所需参数
     */
    public void startDownload(Context context, Callback callback) {
        bCanceled = false;
        mContext = context;
        mCallback = callback;
        mCallback.begin();
        mIndex = 0; //下载全部
        downFile();
    }

    private int mIndex = 0;
    private final float BIN_SIZE = 0.95f;

    private void downFile() {
        IParam param = downList.get(mIndex);
        float begin;
        float itemMax;
        if (mIndex == 0) { //bin文件较大，适当调整权重
            begin = 0;
            itemMax = BIN_SIZE;
        } else {
            begin = BIN_SIZE;
            itemMax = 1 - BIN_SIZE;
        }

//        float itemMax = 1.0f / downList.size();
//        float begin = itemMax * mIndex;
        File file = new File(param.localPath);
        if (file.exists()) {
            file.delete();
        }
        DownLoadUtils downLoadUtils = new DownLoadUtils(mContext, mIndex, param.url, file.getAbsolutePath());
        downLoadUtils.DownFile(new IDownListener() {
            @Override
            public void onFailed(long l, int i) {
                mCallback.failed();
            }

            @Override
            public void Canceled(long l) {
            }

            @Override
            public void onProgress(long l, int i) {
                if (!bCanceled) {
                    mCallback.progress(begin + (itemMax * i / 100.0f));
                }
            }

            @Override
            public void Finished(long l, String s) {
                mIndex++;
                if (!bCanceled) {
                    if (mIndex >= downList.size()) { //全部下载完毕
                        mCallback.complete(downList.get(0).localPath, downList.get(1).localPath);
                    } else {
                        downFile();
                    }
                }
            }
        });
    }

    private String getUrl(String fileName) {
        return baseUrl + fileName;
    }


    /**
     * AI模型文件
     */
    public static class IModel {

        private String localBin;
        private String localParam;

        public IModel(String localBin, String localParam) {
            this.localBin = localBin;
            this.localParam = localParam;
        }

        public String getLocalBin() {
            return localBin;
        }

        public String getLocalParam() {
            return localParam;
        }

    }


    /**
     * 要下载的文件
     */
    private class IParam {

        private String url;
        private String localPath;

        public IParam(String url, String localPath) {
            this.url = url;
            this.localPath = localPath;
        }

    }



    public void cancel() {
        downList.clear();
        bCanceled = true;
        DownLoadUtils.forceCancelAll();
    }

    public static interface Callback {


        void begin();

        /**
         * 失败
         */
        void failed();

        /**
         * 进度0~1.0f
         */
        void progress(float progress);


        void complete(String bin, String param);
    }
}
