package com.pesdk.net.repository;

import com.pesdk.bean.DataResult;
import com.pesdk.bean.PageDataResult;
import com.pesdk.bean.SortResult;
import com.pesdk.bean.UploadResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.RetrofitCreator;
import com.vecore.PECore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.IntRange;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Part;

/**
 *
 */
public class PENetworkRepository {

    private static PENetworkApi networkApi = RetrofitCreator.getInstance().create(PENetworkApi.class);


    public static String getAppkey() {
        return mAppkey;
    }

    private static String mAppkey;
    private static String mLanType;

    /**
     * 语言(中、英)
     */
    public static void setLanType(String lanType) {
        mLanType = lanType;
    }

    /**
     * 应用key
     */
    public static void setAppkey(String appkey) {
        mAppkey = appkey;
    }


    private static Map<String, String> getSortMap(@PENetworkApi.ResourceType String type) {
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        map.put("appkey", mAppkey);
        map.put("os", "android");
        map.put("ver", PECore.getVersionCode() + "");
        map.put("lang", mLanType);
        return map;
    }

    private static Map<String, String> getDataMap(@PENetworkApi.ResourceType String type, String category) {
        Map<String, String> map = getSortMap(type);
        map.put("category", category);
        return map;
    }


    /**
     * 获取分组信息
     */
    public static SortResult getSortList(@PENetworkApi.ResourceType String type) {
        try {
            Response<SortResult> response = networkApi.getSort(getSortMap(type)).execute();
            return response.isSuccessful() ? response.body() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取单列数据
     *
     * @param type
     * @param category
     * @return
     */
    public static DataResult getDataList(@PENetworkApi.ResourceType String type, String category) {
        try {
            Response<DataResult> response = networkApi.getData(getDataMap(type, category)).execute();
            return response.isSuccessful() ? response.body() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 分页查询
     *
     * @param type
     * @param category
     * @param pageNum
     * @return
     */
    public static PageDataResult getDataList(@PENetworkApi.ResourceType String type, String category, @IntRange(from = 1) int pageNum) {
        try {
            Map<String, String> map = getDataMap(type, category);
            map.put("pagination", true + "");
            map.put("page", Integer.toString(pageNum));
            Response<PageDataResult> response = networkApi.getPageData(map).execute();
            return response.isSuccessful() ? response.body() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 搜索数据
     *
     * @param type
     * @param searchText
     * @return
     */
    public static PageDataResult searchDataList(@PENetworkApi.ResourceType String type, String searchText, @IntRange(from = 1) int pageNum) {
        try {
            Map<String, String> map = getSortMap(type);
            map.put("search", searchText);
            map.put("pagination", true + "");
            map.put("page", Integer.toString(pageNum));
            Response<PageDataResult> response = networkApi.getPageData(map).execute();
            return response.isSuccessful() ? response.body() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 上传剪同款
     */
    public static UploadResult upload(@Part List<MultipartBody.Part> list, @Part MultipartBody.Part zip, @Part MultipartBody.Part cover) {
        try {
            Response<UploadResult> response = networkApi.upload(list, zip, cover).execute();
            return response.isSuccessful() ? response.body() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
