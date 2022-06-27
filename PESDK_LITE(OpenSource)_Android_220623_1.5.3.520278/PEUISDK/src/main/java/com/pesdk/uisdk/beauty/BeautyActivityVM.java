package com.pesdk.uisdk.beauty;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.BeautyInfo;
import com.pesdk.uisdk.beauty.listener.ExtraPreviewFrameListener;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.BaseVirtual;
import com.vecore.VirtualImage;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.listener.ExportListener;
import com.vecore.models.ImageConfig;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.models.VisualFilterConfig;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 *
 */
public class BeautyActivityVM extends AndroidViewModel {

    public MutableLiveData<String> getMergeHairMedia() {
        return mMergeHairMedia;
    }

    private MutableLiveData<String> mMergeHairMedia;


    public BeautyActivityVM(@NonNull Application application) {
        super(application);
        mMergeHairMedia = new MutableLiveData<>();
    }

    private static final String TAG = "BeautyActivityVM";


    /**
     * 合并基础图片+头发(多人脸)
     *
     * @param srcMedia
     * @param list
     */
    public void applyHairs(String srcMedia, List<BeautyFaceInfo> list) {
        VirtualImage virtualImage = new VirtualImage();
        PEImageObject base = null;
        try {
            base = new PEImageObject(srcMedia);
            base.setBlendEnabled(true);
            virtualImage.setPEScene(new PEScene(base));
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        if (null != list) { //所有的发型
            for (BeautyFaceInfo info : list) {
                CaptionLiteObject liteObject = info.getHairInfo().getHair();
                Log.e(TAG, "applyHairs: " + info.getFaceId() + "<>" + list.size() + " > " + info.getHairInfo());
                if (null != liteObject) {
                    virtualImage.addCaptionLiteObject(liteObject);
                }
            }
        }
        String path = PathUtils.getTempFileNameForSdcard("Hair_merge", "png");
        ImageConfig imageConfig = new ImageConfig(base.getWidth(), base.getHeight());
        virtualImage.export(getApplication(), path, imageConfig, new ExportListener() {
            @Override
            public void onExportStart() {

            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int result, int extra, String info) {
                if (result >= BaseVirtual.RESULT_SUCCESS) {
                    mMergeHairMedia.setValue(path);
                } else {
                    mMergeHairMedia.setValue(null);
                }
                virtualImage.release();
            }
        });

    }


    /**
     * 计算头发在原始图片中的位置
     *
     * @param srcMedia   原始人像
     * @param hairObject 头发对象
     * @param previewW   播放器预览size
     * @param previewH
     */
    public CaptionLiteObject hairInMedia(PEImageObject srcMedia, CaptionLiteObject hairObject, int previewW, int previewH) {
        //显示位置转换：预览时,上下左右有空白,生成图时需转换成相对于原始媒体，生成图片时需保证输出size为原图，比例可能不一致
        float asp = previewW * 1f / previewH;
        float mediaAsp = srcMedia.getWidth() * 1f / srcMedia.getHeight();
        if (asp == mediaAsp) {
            return hairObject.copy();
        } else {
            RectF mediaRectF = srcMedia.getShowRectF(); //媒体在预览区域中的显示位置
            float w = mediaRectF.width();
            float h = mediaRectF.height();

            RectF srcRectF = hairObject.getShowRectF();

            RectF dst = new RectF();

            dst.left = (srcRectF.left - mediaRectF.left) / w;
            dst.right = (srcRectF.right - mediaRectF.left) / w;
            dst.top = (srcRectF.top - mediaRectF.top) / h;
            dst.bottom = (srcRectF.bottom - mediaRectF.top) / h;
//            Log.e(TAG, "createHairInMedia: " + srcRectF + " " + dst + " mediaRectF:" + mediaRectF);
            CaptionLiteObject tmp = hairObject.copy();
            tmp.setShowRectF(dst);
            return tmp;//需要记录此头发对象
        }
    }


    /**
     * 恢复头发在虚拟图片中的位置
     *
     * @param hair        头发在原始媒体中的位置
     * @param mediaObject 原始媒体的显示位置
     * @return
     */
    public CaptionLiteObject restoreHairInVirtual(CaptionLiteObject hair, PEImageObject mediaObject) {
        //头发对象转的预览位置转成相对于预览区域
        RectF mediaRectF = mediaObject.getShowRectF();
        RectF rect = hair.getShowRectF();
        RectF dst = new RectF();

        float w = mediaRectF.width(), h = mediaRectF.height();
        dst.left = mediaRectF.left + rect.left * w;
        dst.right = mediaRectF.left + rect.right * w;
        dst.top = mediaRectF.top + rect.top * h;
        dst.bottom = mediaRectF.top + rect.bottom * h;

        CaptionLiteObject dstHair = hair.copy();
        dstHair.setShowRectF(dst);

//        Log.e(TAG, "restoreHairInVirtual: " + dst + " rect:" + rect + " " + mediaRectF);
        return dstHair;
    }


    /**
     * 识别人脸信息
     */
    public void processFace(String baseMedia, BeautyActivity.Callback callback) {
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            List<BeautyFaceInfo> data;

            @Override
            public void onBackground() { //2.读取人脸
                try {
                    PEImageObject tmp = new PEImageObject(baseMedia);
                    Bitmap bitmap = MiscUtils.getBitmapByMedia(tmp.getInternal(), Math.min(Math.max(tmp.getWidth(), tmp.getHeight()), 1280));
                    data = new ArrayList<>();
                    ExtraPreviewFrameListener.processFace(bitmap, data);
                    bitmap.recycle();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                Log.e(TAG, "onEnd: " + data);
                if (null != callback)
                    callback.prepared(data);
            }
        });
    }

    /**
     * 修改美颜
     */
    public void applyFilter(PEImageObject mImageObject, BeautyInfo mBeautyInfo) {
        ArrayList<VisualFilterConfig> configs = new ArrayList<>();
        //美颜
        VisualFilterConfig.SkinBeauty skinBeauty = new VisualFilterConfig.SkinBeauty(mBeautyInfo.getValueBeautify());
        skinBeauty.setWhitening(mBeautyInfo.getValueWhitening());
        skinBeauty.setRuddy(mBeautyInfo.getValueRuddy());
        configs.add(skinBeauty);

        //人脸
        List<BeautyFaceInfo> faceList = mBeautyInfo.getFaceList();
        if (faceList != null && faceList.size() > 0) {
            for (BeautyFaceInfo faceInfo : faceList) {
                //瘦脸、大眼
                VisualFilterConfig.FaceAdjustment faceConfig = faceInfo.getFaceConfig();
                if (faceConfig != null) {
                    configs.add(faceConfig);
                }

                //五官
                VisualFilterConfig.FaceAdjustmentExtra fiveSensesConfig = faceInfo.getFiveSensesConfig();
                if (fiveSensesConfig != null) {
                    configs.add(fiveSensesConfig);
                }
            }
        }
        try {
            mImageObject.changeFilterList(configs);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
}
