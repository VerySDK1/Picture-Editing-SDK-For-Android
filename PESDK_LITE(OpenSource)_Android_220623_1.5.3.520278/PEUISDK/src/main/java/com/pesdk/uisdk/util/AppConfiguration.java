package com.pesdk.uisdk.util;

import android.content.Context;

import com.pesdk.uisdk.bean.MaskItem;
import com.pesdk.uisdk.bean.record.RecordBean;
import com.pesdk.uisdk.bean.record.RecordList;
import com.vecore.base.lib.utils.FileUtils;
import com.vesdk.common.utils.MMKVUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用持久化配置
 */
public class AppConfiguration {


    private static final String KEY_MASK = "maskList";

    public static Context getContext() {
        return mContext;
    }

    private static Context mContext;


    public static void initContext(Context context) {
        mContext = context.getApplicationContext();

    }

    public static List<RecordBean> getMaskList() {
        RecordList list = MMKVUtils.INSTANCE.decodeParcelable(KEY_MASK, RecordList.class);
        return null != list ? list.getlist() : new ArrayList<>();
    }

    /**
     * 已下载的蒙版
     */
    public static void saveMaskList(List<MaskItem> list) {
        if (null == list) {
            return;
        }
        List<RecordBean> beans = new ArrayList<>();
        for (MaskItem item : list) {
            if (FileUtils.isExist(item.getLocalpath())) {
                beans.add(new RecordBean(item.getName(), item.getLocalpath()));
            }
        }
        if (beans.size() > 0) {
            MMKVUtils.INSTANCE.encodeParcelable(KEY_MASK, new RecordList(beans));
        }
    }

    public static RecordBean getMask(String name) {
        List<RecordBean> list = getMaskList();
        if (null == list) {
            return null;
        }
        int len = list.size();
        for (int i = 0; i < len; i++) {
            RecordBean bean = list.get(i);
            if (bean.getName().equals(name)) {
                return bean;
            }
        }
        return null;
    }
}
