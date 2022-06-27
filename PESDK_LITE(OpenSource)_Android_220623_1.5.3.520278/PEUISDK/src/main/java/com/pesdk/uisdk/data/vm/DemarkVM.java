package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.graphics.Bitmap;

import com.pesdk.uisdk.data.model.WatermarkModel;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.utils.MiscUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * 去水印
 */
public class DemarkVM extends AndroidViewModel {

    private static final String TAG = "DemarkModel";
    private MutableLiveData<PEImageObject> mLiveData;
    private MutableLiveData<Bitmap> mLiveBaseBmp;
    private MutableLiveData<String> mBuildPath;


    public DemarkVM(@NonNull Application application) {
        super(application);
        mLiveData = new MutableLiveData<>();
        mLiveBaseBmp = new MutableLiveData<>();
        mBuildPath = new MutableLiveData<>();
    }

    public MutableLiveData<String> getBuildPath() {
        return mBuildPath;
    }

    public MutableLiveData<PEImageObject> getMaskLiveData() {
        return mLiveData;
    }


    public MutableLiveData<Bitmap> getLiveBaseBmp() {
        return mLiveBaseBmp;
    }

    public void initBase(String path) {
        ThreadPoolUtils.executeEx(() -> { //初始化bitmap
            try {
                long st = System.currentTimeMillis();
                PEImageObject imageObject = new PEImageObject(getApplication(), path);
                Bitmap bmp;
                if (imageObject.getWidth() > 3000 || imageObject.getHeight() > 3000) { //需要压缩
                    bmp = MiscUtils.getBitmapByMedia(imageObject.getInternal(), 2048); //当前播放器内容 压缩原图,防止太大OOM
                    String tmp = PathUtils.getTempFileNameForSdcard("compress", path.endsWith("png") ? "png" : "jpg");
                    BitmapUtils.saveBitmapToFile(bmp, 100, tmp);
                    mBuildPath.postValue(tmp); //压缩后的图
                } else {//无需压缩
                    bmp = MiscUtils.getBitmapByMedia(imageObject.getInternal(), 8192);
                    mBuildPath.postValue(path);
                }
                mLiveBaseBmp.postValue(bmp);
                LogUtil.i(TAG, "initBitmap: src: " + imageObject.getWidth() + "*" + imageObject.getHeight() + " bmp: " + bmp.getWidth() + "*" + bmp.getHeight() + " ms:" + (System.currentTimeMillis() - st));
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param imageObject 原始的媒体信息
     * @param mask        mask(涂抹的内容)
     */
    public void processMask(PEImageObject imageObject, Bitmap mask) {
        Bitmap src = mLiveBaseBmp.getValue();
        if (src == null || mask == null) {
            mLiveData.postValue(imageObject);
            return;
        }
        ThreadPoolUtils.executeEx(() -> {
            long st = System.currentTimeMillis();
            WatermarkModel model = new WatermarkModel();
            String dst = model.onDewatermark(src, mask);
            LogUtil.i(TAG, "processMask: " + src.getWidth() + "*" + src.getHeight() + " ms:" + (System.currentTimeMillis() - st));
            if (null != mask) {
                mask.recycle();
            }
            PEImageObject tmp = null;
            try {
                tmp = new PEImageObject(dst);
                tmp.setShowRectF(imageObject.getShowRectF());
                tmp.setAngle(imageObject.getAngle());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            mLiveData.postValue(tmp);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (null != mLiveBaseBmp && null != mLiveBaseBmp.getValue()) {
            mLiveBaseBmp.getValue().recycle();
        }
    }
}
