package com.pesdk.uisdk.data.model;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.widget.ExtProgressDialog;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.BaseVirtual;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.MediaObject;
import com.vecore.models.MediaType;
import com.vecore.models.VideoConfig;
import com.vecore.utils.ExportUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 处理导入视频时，压缩逻辑
 * 最大支持4K视频 ，8k视频需要压缩
 */
@Deprecated
public class ImportModel {
    private Context mContext;
    private static final String TAG = "ImportModel";
    private CallBack mCallBack;
    /**
     * 最大支持的视频尺寸，否则先压缩
     */
    private final int MAX_SUPPORT_SIZE = 3840 * 2160;
    private final float MAX_WH = 1920.0f;
    private String sp_name = "pro_compress";
    private SharedPreferences mPreferences;
    private String KEY_COMPRESS = "kvCompress";
    private HashMap<String, String> mMap = new HashMap<>();
    private JSONArray jsonArray = null;
    private final String SRC = "src", DST = "dst", MODIFY = "lastModify";

    public ImportModel(Context context) {
        mContext = context;
    }

    private void restore() {
        mPreferences = mContext.getSharedPreferences(sp_name, Context.MODE_PRIVATE);
        String data = mPreferences.getString(KEY_COMPRESS, "");
        jsonArray = null;
        mMap.clear();
        if (!TextUtils.isEmpty(data)) {
            try {
                jsonArray = new JSONArray(data);
                if (null != jsonArray) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String src = jsonObject.getString(SRC);
                        String dst = jsonObject.getString(DST);
                        if (FileUtils.isExist(src) && FileUtils.isExist(dst)) {
                            long time = jsonObject.getLong(MODIFY);
                            File file = new File(src);
                            if (file.lastModified() == time) {
                                mMap.put(src, dst);
                            } else {
                                jsonArray.remove(i);
                            }
                        } else {
                            jsonArray.remove(i);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private List<MediaObject> mList = new ArrayList<>();

    /**
     * 导入判断压缩
     *
     * @param list
     * @param callBack
     */
    public void onImport(final List<MediaObject> list, @NotNull CallBack callBack) {
        mCallBack = callBack;
        mList = list;
        //校验压缩
        final List<MediaObject> compressList = new ArrayList<>();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            MediaObject tmp = list.get(i);
            if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE && tmp.getWidth() * tmp.getHeight() > MAX_SUPPORT_SIZE) {     //视频大尺寸需压缩
                compressList.add(tmp);
            }
        }
        //执行压缩
        if (compressList.size() > 0) {
            ThreadPoolUtils.executeEx(new Runnable() {
                @Override
                public void run() {
                    restore();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onCompress(compressList);
                        }
                    });
                }
            });
        } else {
            callBack.onResult(list);
        }
    }

    private ExtProgressDialog mDialog;
    private List<MediaObject> mCompressList;

    /**
     * 压缩列表
     *
     * @param compressList 需要压缩的列表
     */
    private void onCompress(List<MediaObject> compressList) {
        mCompressList = compressList;
        compressItem(0, compressList.size());
    }

    /**
     * @param index
     * @param len
     * @param progress 0~100
     * @return
     */
    private String getMessage(int index, int len, int progress) {
        if (len > 1) {
            return mContext.getString(R.string.pesdk_import_compress_multiple, "0%", index + 1, len);
        }
        return mContext.getString(R.string.pesdk_import_compress, progress + "%");
    }

    /**
     * 压缩当个
     *
     * @param index
     * @param len
     */
    private void compressItem(final int index, final int len) {
        final MediaObject mediaObject = mCompressList.get(index);
        final String src = mediaObject.getMediaPath();
        String dst = mMap.get(src);
        if (TextUtils.isEmpty(dst)) { //未压缩
            String msg = getMessage(index, len, 0);
            if (mDialog == null) {
                mDialog = SysAlertDialog.showProgressDialog(mContext, msg, true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        LogUtil.i(TAG, "onCancel: " + this);
                        ExportUtils.cancelCompress();
                        mDialog = null;
                        if (null != mCallBack) {
                            mCallBack.onFailed();
                        }
                    }
                });
            } else {
                mDialog.setMessage(msg);
            }
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMax(1000);


            VideoConfig vc = new VideoConfig();
            BaseVirtual.getMediaInfo(mediaObject.getMediaPath(), vc, false);
            int width = vc.getVideoWidth();
            int height = vc.getVideoHeight();
            float scale = Math.max(width, height) / MAX_WH;
            ExportUtils.CompressConfig compressConfig = new ExportUtils.CompressConfig(Math.min(8, vc.getVideoEncodingBitRate() / 1024 / 1024.0),
                    false, false, 0, (int) (width / scale), (int) (height / scale), null);
            final String path = PathUtils.getTempFileNameForSdcard(PathUtils.getImportPath(), "Video", "mp4", false);
            ExportUtils.compressVideo(mContext, src, path, compressConfig, new ExportUtils.CompressVideoListener() {
                @Override
                public void onCompressStart() {
                }

                @Override
                public void onProgress(int progress, int max) {
                    if (null != mDialog) {
                        mDialog.setMax(max);
                        mDialog.setProgress(progress);
                        mDialog.setMessage(getMessage(index, len, progress / 10));
                    }
                }

                @Override
                public void onCompressComplete(String path) {
                    LogUtil.i(TAG, "onCompressComplete: " + path);
                    mMap.put(src, path);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(SRC, src);
                        jsonObject.put(DST, path);
                        jsonObject.put(MODIFY, new File(src).lastModified());
                        if (null == jsonArray) {
                            jsonArray = new JSONArray();
                        }
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    copyMediaInfo(index, path, mediaObject);
                    onNext(index, len);
                }

                @Override
                public void onCompressError(int result) {
                    Log.e(TAG, "onCompressError: " + result);
                    dismissDialog();
                    if (null != mCallBack) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (null != mCallBack) {
                                    mCallBack.onFailed();
                                }
                            }
                        });
                    }
                }
            });
        } else {
            copyMediaInfo(index, dst, mediaObject);
            onNext(index, len);
        }
    }

    /**
     * 拷贝信息
     *
     * @param index
     * @param dst
     * @param src
     */
    private void copyMediaInfo(int index, String dst, MediaObject src) {
        try {
            MediaObject tmp = new MediaObject(dst); //只拷贝旋转角度、截取时间  （裁剪区域不一致，忽略）
            tmp.setShowAngle(src.getShowAngle());
            tmp.setTimeRange(src.getTrimStart(), src.getTrimEnd());
            mList.set(index, tmp);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private void onNext(int index, int len) {
        if (index < len - 1) {  //继续
            mHandler.obtainMessage(MSG_NEXT, index + 1, len).sendToTarget();
        } else {
            mHandler.sendEmptyMessage(MSG_SUCCESS);
        }
    }

    private void onCompressSuccess() {
        if (null != jsonArray && null != mPreferences) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(KEY_COMPRESS, jsonArray.toString());
            editor.apply();
        }
        dismissDialog();
        if (null != mCallBack) {
            mCallBack.onResult(mList);
        }
    }

    private void dismissDialog() {
        if (null != mDialog) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private final int MSG_NEXT = 100;
    private final int MSG_SUCCESS = MSG_NEXT + 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_NEXT: {
                    compressItem(msg.arg1, msg.arg2);
                }
                break;
                case MSG_SUCCESS: {
                    onCompressSuccess();
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    public static interface CallBack {
        /**
         * 导入成功
         *
         * @param list
         */
        void onResult(List<MediaObject> list);

        void onFailed();
    }


}
