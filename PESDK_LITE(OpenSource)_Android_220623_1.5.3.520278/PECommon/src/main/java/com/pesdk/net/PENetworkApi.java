package com.pesdk.net;

import com.pesdk.bean.DataResult;
import com.pesdk.bean.PageDataResult;
import com.pesdk.bean.SortResult;
import com.pesdk.bean.UploadResult;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import androidx.annotation.StringDef;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 图片编辑-网络接口
 */
public interface PENetworkApi {


    String Text = "text"; //文字
    String Sticker = "sticker-pe";//贴纸
    String TYPE_FILTER = "filter";//滤镜
    String Font = "font";//字体
    String Bground = "bground";//背景
    String Sky = "sky";//天空
    String FRAME = "frame";//边框
    String Overlay = "overlay";//叠加
    String Flower = "flower";//花字
    String MASK = "mask";//蒙版
    String HAIR = "hair"; //美发
    String CLOTHES = "clothes"; //换装
    String Templateapi = "templateApiImage"; //图片编辑Api (剪同款)


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Text, Sticker, Font, Bground, Sky, FRAME, Overlay, Flower, MASK, TYPE_FILTER, HAIR, Templateapi, CLOTHES})
    @interface ResourceType {

    }


    /**
     * 获取分类
     */
    @POST("api/v1/type/list")
    @FormUrlEncoded
    Call<SortResult> getSort(@FieldMap Map<String, String> map);


    /**
     * 单个列表
     */
    @POST("api/v1/file/list")
    @FormUrlEncoded
    Call<DataResult> getData(@FieldMap Map<String, String> map);

    /**
     * 单个列表
     */
    @POST("api/v1/file/list")
    @FormUrlEncoded
    Call<PageDataResult> getPageData(@FieldMap Map<String, String> map);

    /**
     * 上传剪同款
     */
    @POST("api/v1/ugc/create")
    @Multipart
    Call<UploadResult> upload(@Part List<MultipartBody.Part> list, @Part MultipartBody.Part zip, @Part MultipartBody.Part cover);

}
