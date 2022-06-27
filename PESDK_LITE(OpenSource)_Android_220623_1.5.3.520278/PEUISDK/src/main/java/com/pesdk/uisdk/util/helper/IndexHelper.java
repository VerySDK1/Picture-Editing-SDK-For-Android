package com.pesdk.uisdk.util.helper;

import android.text.TextUtils;

import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.bean.model.BorderStyleInfo;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.net.WebFilterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IndexHelper {
    public static int getFilterIndex(ArrayList<WebFilterInfo> list, String resourceId) {
        if (null != list && !TextUtils.isEmpty(resourceId)) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(resourceId, list.get(i).getId())) {
                    return i;
                }
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

    public static int getIndex(List<ItemBean> list, String resourceId) {
        if (null != list && !TextUtils.isEmpty(resourceId)) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(resourceId, list.get(i).getId())) {
                    return i;
                }
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

    /**
     * 获取贴纸分类Index
     */
    public static int getSortIndex(List<SortBean> sortApis, String category) {
        if (null != sortApis && !TextUtils.isEmpty(category)) {
            int len = sortApis.size();
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(sortApis.get(i).getId(), category)) {
                    return i;
                }
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

    /**
     * 获取选中的贴纸项Index
     */
    public static int getStyleIndex(List<StyleInfo> list, String icon) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (TextUtils.equals(list.get(i).icon, icon)) {
                return i;
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

    public static int getIndexFrame(List<BorderStyleInfo> list, String resourceId) {
        if (null != list && !TextUtils.isEmpty(resourceId)) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(resourceId, list.get(i).getLocalpath())) {
                    return i;
                }
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

    public static int getIndexStyle(List<StyleInfo> list, String resourceId) {
        int index = BaseRVAdapter.UN_CHECK;
        if (!TextUtils.isEmpty(resourceId)) {
            for (int i = 0; i < list.size(); i++) {
                StyleInfo tmp = list.get(i);
                if (TextUtils.equals(tmp.resourceId, resourceId)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static int getIndexCanvas(List<ItemBean> list, String path) {
        if (null != list && !TextUtils.isEmpty(path)) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                ItemBean style = list.get(i);
                if (TextUtils.equals(path, style.getLocalPath())) {
                    return i;
                }
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }
}
