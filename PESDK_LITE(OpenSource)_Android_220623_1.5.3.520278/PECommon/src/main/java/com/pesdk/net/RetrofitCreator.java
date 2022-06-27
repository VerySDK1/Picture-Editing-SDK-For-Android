package com.pesdk.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vecore.base.lib.utils.LogUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 *
 */
public class RetrofitCreator {

    public static String mPeBaseUrl = "http://pesystem.56show.com/";

    public static void init(String url) {
        mPeBaseUrl = url;
    }

    private static RetrofitCreator instance = null;
    private Retrofit peRetrofit;

    /**
     * pe使用接口
     */
    private RetrofitCreator() {
        Gson gson = new GsonBuilder().create();
        peRetrofit = new Retrofit.Builder()
                .baseUrl(mPeBaseUrl)
                .client(new OkHttpClient.Builder()
                                .readTimeout(30, TimeUnit.SECONDS)
                                .connectTimeout(30, TimeUnit.SECONDS)
//                        .addInterceptor(new LogJsonInterceptor())
                                .build()
                )
//                .addConverterFactory(GsonConverterFactory.create(mGson))//标准
                .addConverterFactory(ResponseConverterFactory.create(gson))//添加自定义GSON解析
                .build();
    }


    public static class ResponseConverterFactory extends Converter.Factory {
        private final Gson gson;

        public static ResponseConverterFactory create(Gson gson) {
            return new ResponseConverterFactory(gson);
        }


        private ResponseConverterFactory(Gson gson) {
            if (gson == null)
                throw new NullPointerException("gson == null");
            this.gson = gson;
        }


        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return new GsonResponseBodyConverter<>(gson, type);
        }
    }


    public static class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final Gson gson;
        private final Type type;

        GsonResponseBodyConverter(Gson gson, Type type) {
            this.gson = gson;
            this.type = type;
        }

        /**
         * 针对数据返回成功、错误不同类型字段处理
         */
        @Override
        public T convert(ResponseBody value) throws IOException {
            String response = value.string();
            try {
                return gson.fromJson(response, type);
            } catch (RuntimeException e) {  //结构不一致|字段类型有误
                LogUtil.w(TAG, "convert: " + response);
                e.printStackTrace();
                return null;
            }
        }
    }

    private static final String TAG = "RetrofitCreator";

    public static class LogJsonInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);
            ResponseBody body = response.body();
            String rawJson = body.string();
            LogUtil.i(TAG, "rawJson: " + rawJson);
            return response.newBuilder().body(ResponseBody.create(rawJson, body.contentType())).build();
        }
    }

    public static RetrofitCreator getInstance() {
        if (null == instance) {
            instance = new RetrofitCreator();
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return peRetrofit.create(service);
    }

}
