package com.pesdk.api;

import android.os.Parcelable;

import com.pesdk.api.manager.ExportConfiguration;
import com.pesdk.api.manager.UIConfiguration;
import com.vesdk.common.utils.MMKVUtils;


/**
 * Sdk配置服务<br>
 */
public final class SdkService {
    private UIConfiguration mUIConfig;
    private ExportConfiguration mExportConfiguration;
    private static final String UI_CONFIGURATION_KEY = "ui_configuration_key";
    private static final String EXPORT_CONFIGURATION_KEY = "export_configuration_key";

    SdkService() {
        mUIConfig = restoreObject(UI_CONFIGURATION_KEY, new UIConfiguration.Builder().get(), UIConfiguration.class);
        mExportConfiguration = restoreObject(EXPORT_CONFIGURATION_KEY, new ExportConfiguration.Builder().get(), ExportConfiguration.class);
    }

    /**
     * 初始化编辑导出、编辑界面、拍摄录制配置项
     *
     * @param uiConfig 编辑界面配置项
     */
    public void initConfiguration(UIConfiguration uiConfig, ExportConfiguration exportConfiguration) {
        saveObject(UI_CONFIGURATION_KEY, uiConfig);
        saveObject(EXPORT_CONFIGURATION_KEY, exportConfiguration);
        if (null != uiConfig) {
            mUIConfig = uiConfig;
        }
        if (null != uiConfig) {
            mExportConfiguration = exportConfiguration;
        }
    }


    public UIConfiguration getUIConfig() {
        return mUIConfig;
    }

    public ExportConfiguration getExportConfig() {
        return mExportConfiguration;
    }


    /**
     * 持久化保存对象
     *
     * @param key    保存对象关联的key
     * @param object 支持Parcelable的对象
     * @return true代表保存成功
     */
    private static <T extends Parcelable> boolean saveObject(String key, T object) {
        if (object == null) {
            MMKVUtils.INSTANCE.removeKey(key);
            return false;
        }
        MMKVUtils.INSTANCE.encodeParcelable(key, object);
        return true;
    }

    /**
     * 还原持久化保存的对象
     *
     * @param key           保存对象关联的key
     * @param defaultObject 如果key不存在时，返回默认的Parcelable的对象
     * @return 返回还原的持久化保存的对象
     */
    @Deprecated
    public static <T extends Parcelable> T restoreObject(String key, T defaultObject) {
        return restoreObject(key, defaultObject, null);
    }

    /**
     * 还原持久化保存的对象
     *
     * @param key           保存对象关联的key
     * @param defaultObject 如果key不存在时，返回默认的Parcelable的对象
     * @param tClass        Parcelable 对象对应的静态实现方法
     * @return 返回还原的持久化保存的对象
     */
    private static <T extends Parcelable> T restoreObject(String key, T defaultObject, Class<T> tClass) {
        if (tClass == null) {
            return defaultObject;
        }
        T tmp = MMKVUtils.INSTANCE.decodeParcelable(key, tClass);
        if (null == tmp) {
            return defaultObject;
        }
        return tmp;
    }

    public void reset() {
        saveObject(UI_CONFIGURATION_KEY, null);
        saveObject(EXPORT_CONFIGURATION_KEY, null);
    }

}
