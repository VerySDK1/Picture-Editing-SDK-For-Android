package com.pesdk.uisdk.export;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;

import com.pesdk.api.SdkEntry;
import com.pesdk.api.manager.ExportConfiguration;
import com.pesdk.api.manager.TextWatermarkBuilder;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.PlayerAspHelper;
import com.vecore.VirtualImage;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.listener.ExportListener;
import com.vecore.models.ImageConfig;
import com.vecore.models.MediaObject;
import com.vecore.models.Watermark;

/**
 * 导出
 */
public class ExportHelper {
    private static final String TAG = "ExportHelper";

    /**
     * 直接导出草稿时
     *
     * @param context
     * @param virtualImageInfo
     * @param withWatermark
     * @param listener
     * @return
     */
    public String export(Context context, VirtualIImageInfo virtualImageInfo, boolean withWatermark, ExportListener listener) {
        return export(context, virtualImageInfo, null, withWatermark, listener);
    }

    /**
     * 图片编辑时导出
     *
     * @param context
     * @param virtualImageInfo
     * @param editDataHandler
     * @param withWatermark
     * @param listener
     * @return
     */
    public String export(Context context, VirtualIImageInfo virtualImageInfo, EditDataHandler editDataHandler, boolean withWatermark, ExportListener listener) {
        VirtualImage virtualImage = new VirtualImage();
        float asp;
        ExportConfiguration configuration = SdkEntry.getSdkService().getExportConfig(); //导出前,自定义了输出size
        if (null == editDataHandler) { //草稿导出时
            asp = DataManager.loadData(virtualImage, virtualImageInfo);
            if (asp == 0) { //裁剪
                asp = PlayerAspHelper.getAsp(virtualImageInfo.getProportionValue());
            }
        } else { //编辑界面导出时
            asp = loadExport(virtualImage, editDataHandler);
        }

        int minWH = configuration.getMinSide();
        int[] wh = initWH(asp, minWH);
        int w = wh[0];
        int h = wh[1];

        //水印
        final String tmpPath = addWatermark(context, withWatermark, configuration, configuration.getMinSide(), virtualImage, w / (h + 0.0f));
        return export(context, virtualImage, wh, tmpPath, listener);
    }

    /**
     * 导出图片 (证件照指定size)
     *
     * @param context
     * @param virtualImage
     * @param outSize
     * @param watermarkPath
     * @param listener
     * @return
     */
    public String export(Context context, VirtualImage virtualImage, int[] outSize, String watermarkPath, ExportListener listener) {
        ExportConfiguration configuration;
        configuration = SdkEntry.getSdkService().getExportConfig(); //导出前,自定义了输出size
        int w = outSize[0];
        int h = outSize[1];

        String path;
        final boolean enableUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && configuration.saveToAlbum;
        ContentValues values = null;
        if (enableUri) { //29+ 必须按照分区存储处理
            values = ExportUtils.createContentValue(w, h, configuration.getArtist(), PathUtils.createDisplayName());
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, configuration.getRelative_path());// 保存路径
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            path = uri.toString();
        } else {
            path = PathUtils.getDstFilePath(configuration.saveDir);
        }
        final ContentValues finalValues = values;
        final String finalPath = path;
        virtualImage.export(context, path, new ImageConfig(w, h, Color.TRANSPARENT), new ExportListener() {
            @Override
            public void onExportStart() {
                listener.onExportStart();
            }

            @Override
            public boolean onExporting(int progress, int max) {
                LogUtil.i(TAG, "onExporting: " + progress + "/" + max);
                return listener.onExporting(progress, max);
            }

            @Override
            public void onExportEnd(int result, int extra, String info) {
                virtualImage.reset();
                if (result >= VirtualImage.RESULT_SUCCESS) {
                    if (enableUri) {
                        finalValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                        context.getContentResolver().update(Uri.parse(finalPath), finalValues, null, null);
                    } else if (configuration.saveToAlbum) {
                        try {
                            MediaObject tmp = new MediaObject(context, finalPath);
                            ExportUtils.insertToAlbumP(context, finalPath, tmp.getWidth(), tmp.getHeight(), configuration.getArtist());
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "onExportEnd: " + result + " " + extra + " " + info);
                    if (enableUri) {
                        context.getContentResolver().delete(Uri.parse(finalPath), null, null);
                    } else {
                        FileUtils.deleteAll(finalPath);
                    }
                }
                listener.onExportEnd(result, extra, info);
                // 删除自定义水印临时文件
                FileUtils.deleteAll(watermarkPath);
            }
        });
        return finalPath;
    }


    /**
     * 设置水印
     *
     * @param context
     * @param withWatermark
     * @param configuration
     * @param minSide
     * @param virtualImage
     * @param mCurProportion
     * @return
     */
    private String addWatermark(Context context, boolean withWatermark, ExportConfiguration configuration, int minSide, VirtualImage virtualImage, float mCurProportion) {
        String mStrCustomWatermarkTempPath = null;
        //水印
        if (withWatermark) {
            if (configuration.enableTextWatermark) {  // 自定义view水印
                mStrCustomWatermarkTempPath = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP + "watermark_", "png");
                TextWatermarkBuilder textWatermarkBuilder = new TextWatermarkBuilder(context, mStrCustomWatermarkTempPath);
                textWatermarkBuilder.setWatermarkContent(configuration.textWatermarkContent);
                textWatermarkBuilder.setTextSize(configuration.textWatermarkSize);
                textWatermarkBuilder.setTextColor(configuration.textWatermarkColor);
                if (configuration.isGravityMode) { //新的位置方式
                    textWatermarkBuilder.setGravity(configuration.mWatermarkGravity);
                    textWatermarkBuilder.setXAdj(configuration.xAdj);
                    textWatermarkBuilder.setYAdj(configuration.yAdj);
                } else {
                    textWatermarkBuilder.setShowRect(configuration.watermarkShowRectF);
                }
                textWatermarkBuilder.setTextShadowColor(configuration.textWatermarkShadowColor);
                virtualImage.setWatermark(textWatermarkBuilder);
            } else if (FileUtils.isExist(context, configuration.getWatermarkPath())) {  //图片水印
                RectF rectF = WatermarkUtil.fixExportWaterRectF(context, configuration.getWatermarkPath(), minSide, mCurProportion, configuration.xAdj, configuration.yAdj);
                Watermark watermark = new Watermark(configuration.getWatermarkPath());
                if ((configuration.isGravityMode && configuration.mWatermarkGravity == (Gravity.RIGHT | Gravity.BOTTOM)) && null != rectF && !rectF.isEmpty()) {
                    watermark.setShowRect(rectF); ///强制更改水印位置为rect，与录屏一致  (右下方时，与录屏算法一致,保证两个水印重叠)
                    watermark.setUseLayoutRect(true);
                } else {
                    if (configuration.isGravityMode) { //新的位置方式
                        watermark.setGravity(configuration.mWatermarkGravity);
                        watermark.setXAdj(configuration.xAdj);
                        watermark.setYAdj(configuration.yAdj);
                    } else {
                        if (configuration.watermarkShowRectF != null) {
                            watermark.setShowRect(configuration.watermarkShowRectF);
                            watermark.setUseLayoutRect(false);
                        }
                        if (mCurProportion > 1) {  //横屏使用横屏水印
                            if (configuration.watermarkLandLayoutRectF != null) {
                                watermark.setShowRect(configuration.watermarkLandLayoutRectF);
                                watermark.setUseLayoutRect(true);
                            } else {
                                if (configuration.watermarkPortLayoutRectF != null) {
                                    watermark.setShowRect(configuration.watermarkPortLayoutRectF);
                                    watermark.setUseLayoutRect(true);
                                }
                            }
                        } else {
                            if (configuration.watermarkPortLayoutRectF != null) {
                                watermark.setShowRect(configuration.watermarkPortLayoutRectF);
                                watermark.setUseLayoutRect(true);
                            } else {
                                if (configuration.watermarkLandLayoutRectF != null) {
                                    watermark.setShowRect(configuration.watermarkLandLayoutRectF);
                                    watermark.setUseLayoutRect(true);
                                }
                            }
                        }
                    }
                }
                watermark.setShowMode(configuration.watermarkShowMode);
                virtualImage.setWatermark(watermark);
            }
        }
        return mStrCustomWatermarkTempPath;
    }

    /**
     * 加载导出参数
     */
    public static float loadExport(VirtualImage virtualImage, EditDataHandler editDataHandler) {
        float asp = DataManager.loadData(virtualImage, true, editDataHandler);
        if (asp == 0) {
            asp = PlayerAspHelper.getAsp(editDataHandler.getProportionValue());
        }
        return asp;
    }

    /**
     * 指定图片宽高
     */
    public static int[] initWH(float asp, int minWH) {
        int w = asp > 1 ? (int) (minWH * asp) : minWH;
        int h = asp > 1 ? minWH : (int) (minWH / asp);
        return new int[]{w, h};
    }
}
