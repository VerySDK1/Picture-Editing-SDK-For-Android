package com.pesdk.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pesdk.api.callback.IExportCallBack;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.BuildConfig;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.database.DraftData;
import com.pesdk.uisdk.edit.DraftManager;
import com.pesdk.uisdk.export.ExportHelper;
import com.pesdk.uisdk.internal.SdkEntryHandler;
import com.pesdk.uisdk.util.AppConfiguration;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.ModelHelperImp;
import com.pesdk.uisdk.util.manager.MaskManager;
import com.vecore.PECore;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.listener.ExportListener;
import com.vesdk.common.CommonInit;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * PESDK入口
 */
public class SdkEntry {
    private static final String TAG = "SdkEntry";
    private static boolean mIsInitialized;
    private static final String SDK_NOT_INITIALIZED_INFO = "PEUISdk not initialized!";
    private static final String NOT_SUPPORTED_CPU_ARCH = "Not supported CPU architecture!";

    public static final String EDIT_RESULT = "edit_result";

    public static Context mContext;


    public static final String MSG_EXPORT = "msg_export";
    public static final String EXPORT_WITH_WATERMARK = "export_with_watermark";
    public static final String EXPORT_IMAGE_WH = "export_image_wh";


    public static final int EXPORT_IMAGE = 0; //普通图片导出
    public static final int EXPORT_GIF = 2; //gif

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EXPORT_IMAGE, EXPORT_GIF})
    public @interface ExportType {

    }

    /**
     * 初始化SDK
     *
     * @param context    应用上下文
     * @param appKey     在平台申请的Appkey
     * @param appSecret  在平台申请的appScrect
     * @param licenseKey 在平台申请的licenseKey
     * @param callBack   导出前的回调通知
     * @return 返回true代表正常初始化SDK
     */
    public static boolean initialize(Context context, String appKey, String appSecret, String licenseKey, IExportCallBack callBack) {
        if (isInitialized()) {
            Log.w(TAG, "peuisdk is initialized");
            return true;
        } else {
            try {
                File cache = context.getExternalFilesDir("pe");
                //公共
                CommonInit.initialize(context,cache);
                AppConfiguration.initContext(context);
                PathUtils.initialize(context, cache);
                PECore.initialize(context, cache.getAbsolutePath(), appKey, appSecret, licenseKey, BuildConfig.DEBUG);
                PECore.setMediaPlaceHolderPath(context, "asset://placeholder.png");
                PECore.setViewFrameHolderPath(context, "asset://bg.jpg"); //播放器预览区域的底图(防止使用透明图片时全白)
                PENetworkRepository.setAppkey(appKey);
                MaskManager.getInstance().init(context);
                SdkEntryHandler.getInstance().setIExportCallBack(callBack);
                DraftManager.getInstance().init(context);//草稿
                mIsInitialized = true;
                return true;
            } catch (IllegalAccessError ex) {
                ex.printStackTrace();
                return false;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }



    /**
     * Set Library(.so) load mode
     */
    public static void setLoadLibraryMode(PECore.LoadLibraryMode mode) {
        PECore.setLoadLibraryMode(mode);
    }

    /**
     * 返回是否已正常初始
     *
     * @return 是否已正常初始
     */
    public static boolean isInitialized() {
        return mIsInitialized;
    }

    public static SdkService sdkService;


    public static SdkService getSdkService() {
        if (sdkService == null) {
            sdkService = new SdkService();
        }
        return sdkService;
    }


    /**
     * 检查appkey是否无效,返回true代表无效
     */
    public static boolean appKeyIsInvalid(Context context) {
        if (!PECore.isSupportCPUArch()) {
            Log.e(TAG, NOT_SUPPORTED_CPU_ARCH);
            return true;
        }
        if (!mIsInitialized) {
            Log.e(TAG, SDK_NOT_INITIALIZED_INFO);
            return true;
        }
        return !PECore.checkAppKey(context);
    }


    /**
     * 继续导出
     */
    public static void onContinueExport(Context context) {
        onContinueExport(context, true, 0);
    }

    /***
     * 继续导出
     * @param withWatermark true 保留水印；false 清除水印
     */
    public static void onContinueExport(Context context, boolean withWatermark) {
        onContinueExport(context, withWatermark, 0);
    }

    /**
     * 继续导出
     *
     * @param context
     * @param withWatermark true 保留水印;false 清除水印
     * @param minWH         用户手动选择分辨率。<=0时,参数无效(取初始化时ExportConfig中的输出size)
     */
    public static void onContinueExport(Context context, boolean withWatermark, int minWH) {
        Intent intent = new Intent(MSG_EXPORT);
        intent.putExtra(EXPORT_WITH_WATERMARK, withWatermark);
        intent.putExtra(EXPORT_IMAGE_WH, minWH);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    /**
     * 草稿箱视频
     *
     * @param context
     * @return
     */
    public static List<IVirtualImageInfo> getDraftList(Context context) {
        if (appKeyIsInvalid(context)) {
            return null;
        }
        DraftData.getInstance().initilize(context);
        return DraftData.getInstance().getAll(VirtualIImageInfo.INFO_TYPE_DRAFT);

    }

    /**
     * 删除草稿视频
     */
    public static boolean deleteDraft(Context context, final IVirtualImageInfo imageInfo) {
        if (appKeyIsInvalid(context) || null == imageInfo) {
            return false;
        }
        DraftData.getInstance().initilize(context);
        boolean re = DraftData.getInstance().delete(imageInfo.getId()) == 1;
        if (imageInfo instanceof IVirtualImageInfo) {
            ThreadPoolUtils.executeEx(() -> {
                //清理草稿箱文件夹数据
                ((VirtualIImageInfo) imageInfo).deleteData();
            });
        }
        return re;
    }


    /**
     * 导出草稿箱视频
     *
     * @param imageInfo      草稿箱视频
     * @param exportListener 回调导出进度
     * @return 返回目标文件路径
     */
    public static String onExportDraft(Context context, IVirtualImageInfo imageInfo, ExportListener exportListener) {
        return onExportDraft(context, imageInfo, exportListener, true);
    }


    /***
     *导出草稿箱视频
     * @param context
     * @param imageInfo 草稿箱视频
     * @param exportListener  回调导出进度
     * @param withWatermark  是否显示水印
     * @return 返回目标文件路径
     */
    public static String onExportDraft(Context context, IVirtualImageInfo imageInfo, ExportListener exportListener, boolean withWatermark) {
        if (appKeyIsInvalid(context) || null == imageInfo || null == exportListener) {
            return null;
        }
        ModelHelperImp.loadModel(context); //尝试注册抠图模型,不做下载流程
        ((VirtualIImageInfo) imageInfo).restoreData(context);
        return new ExportHelper().export(context, (VirtualIImageInfo) imageInfo, withWatermark, exportListener);
    }


    public static void onExitApp(Context context) {
        ThreadPoolUtils.executeEx(() -> PathUtils.clearTemp());
    }
}
