package com.pesdk.uisdk.gson;

import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.PipBgParam;
import com.vecore.base.lib.utils.ParcelableUtils;
import com.vecore.models.VisualFilterConfig;
import com.vecore.models.caption.FrameInfo;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * 统一Gson引用
 */
public class Gson {

    private com.google.gson.Gson gson;

    private static class SingletonHolder {
        private static final Gson INSTANCE = new Gson();
    }

    private static final Type sparseArrayType = new TypeToken<SparseArray<FrameInfo>>() {
    }.getType();

    public Gson() {
        IntegerDefault0Adapter integerDefault0Adapter = new IntegerDefault0Adapter();
        LongDefault0Adapter longDefault0Adapter = new LongDefault0Adapter();
        FloatDefault0Adapter floatDefault0Adapter = new FloatDefault0Adapter();
        DoubleDefault0Adapter doubleDefault0Adapter = new DoubleDefault0Adapter();
        ImageObAdapter imageObAdapter = new ImageObAdapter();
        PipBgParamAdapter bgParamAdapter = new PipBgParamAdapter();
        VisualFilterConfigAdapter visualFilterConfigAdapter = new VisualFilterConfigAdapter();

        gson = new GsonBuilder()
//            .setExclusionStrategies(new IgnoreAnnotationExclusionStrategy()) //忽略部分不必要的字段
                .serializeSpecialFloatingPointValues()  //解决NAN
                .registerTypeAdapter(sparseArrayType, new SparseArrayTypeAdapter(FrameInfo.class))

                .registerTypeAdapter(ImageOb.class, imageObAdapter)
                .registerTypeAdapter(PipBgParam.class, bgParamAdapter)

                .registerTypeAdapter(Integer.class, integerDefault0Adapter)
                .registerTypeAdapter(int.class, integerDefault0Adapter)
                .registerTypeAdapter(Long.class, longDefault0Adapter)
                .registerTypeAdapter(long.class, longDefault0Adapter)
                .registerTypeAdapter(Float.class, floatDefault0Adapter)
                .registerTypeAdapter(float.class, floatDefault0Adapter)
                .registerTypeAdapter(Double.class, doubleDefault0Adapter)
                .registerTypeAdapter(double.class, doubleDefault0Adapter)

                //滤镜
                .registerTypeAdapter(VisualFilterConfig.class, visualFilterConfigAdapter)

                .setLenient()
                .create();
    }


    /**
     * 获取RetrofitServiceManager
     *
     * @return
     */
    public static Gson getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public com.google.gson.Gson getGson() {
        return gson;
    }


    class ImageObAdapter implements JsonSerializer<ImageOb>, JsonDeserializer<ImageOb> {
        private static final String TAG = "ImageObAdapter";

        @Override
        public JsonElement serialize(ImageOb src, Type typeOfSrc, JsonSerializationContext context) {
            if (null == src) {
                return null;
            }
            String str = ParcelableUtils.toParcelStr(src);
            return new JsonPrimitive(str);
        }

        @Override
        public ImageOb deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The value error ");
            }
            String str = json.getAsString();
            if (null != typeOfT && typeOfT == new TypeToken<ImageOb>() {
            }.getType()) {
                if (!TextUtils.isEmpty(str)) {
                    return ParcelableUtils.toParcelObj(str, ImageOb.CREATOR);
                }
            }
            return null;
        }
    }

    class PipBgParamAdapter implements JsonSerializer<PipBgParam>, JsonDeserializer<PipBgParam> {
        private static final String TAG = "PipBgParamAdapter";

        @Override
        public JsonElement serialize(PipBgParam src, Type typeOfSrc, JsonSerializationContext context) {
            if (null == src) {
                return null;
            }
            String str = ParcelableUtils.toParcelStr(src);
            return new JsonPrimitive(str);
        }

        @Override
        public PipBgParam deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The value error ");
            }
            String str = json.getAsString();
            if (null != typeOfT && typeOfT == new TypeToken<PipBgParam>() {
            }.getType()) {
                if (!TextUtils.isEmpty(str)) {
                    return ParcelableUtils.toParcelObj(str, PipBgParam.CREATOR);
                }
            }
            return null;
        }
    }



    class IntegerDefault0Adapter implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (json.getAsString().equals("")) {
                    return 0;
                }
            } catch (Exception ignore) {
            }
            try {
                return json.getAsInt();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    class LongDefault0Adapter implements JsonSerializer<Long>, JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (json.getAsString().equals("")) {
                    return 0L;
                }
            } catch (Exception ignore) {
            }
            try {
                return json.getAsLong();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    class FloatDefault0Adapter implements JsonSerializer<Float>, JsonDeserializer<Float> {
        @Override
        public Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (json.getAsString().equals("")) {
                    return 0.0f;
                }
            } catch (Exception ignore) {
            }
            try {
                return json.getAsFloat();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Float src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    class DoubleDefault0Adapter implements JsonSerializer<Double>, JsonDeserializer<Double> {
        @Override
        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (json.getAsString().equals("")) {
                    return 0.0;
                }
            } catch (Exception ignore) {
            }
            try {
                return json.getAsDouble();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src);
        }
    }

    class VisualFilterConfigAdapter implements JsonSerializer<VisualFilterConfig>, JsonDeserializer<VisualFilterConfig> {

        private Map<String, Class> mMap;
        private com.google.gson.Gson mGson;

        public VisualFilterConfigAdapter() {

            IntegerDefault0Adapter integerDefault0Adapter = new IntegerDefault0Adapter();
            LongDefault0Adapter longDefault0Adapter = new LongDefault0Adapter();
            FloatDefault0Adapter floatDefault0Adapter = new FloatDefault0Adapter();
            DoubleDefault0Adapter doubleDefault0Adapter = new DoubleDefault0Adapter();
            ImageObAdapter imageObAdapter = new ImageObAdapter();

            mGson = new GsonBuilder()
                    .serializeSpecialFloatingPointValues()  //解决NAN
                    .registerTypeAdapter(sparseArrayType, new SparseArrayTypeAdapter(FrameInfo.class))
                    .registerTypeAdapter(ImageOb.class, imageObAdapter)
                    .registerTypeAdapter(Integer.class, integerDefault0Adapter)
                    .registerTypeAdapter(int.class, integerDefault0Adapter)
                    .registerTypeAdapter(Long.class, longDefault0Adapter)
                    .registerTypeAdapter(long.class, longDefault0Adapter)
                    .registerTypeAdapter(Float.class, floatDefault0Adapter)
                    .registerTypeAdapter(float.class, floatDefault0Adapter)
                    .registerTypeAdapter(Double.class, doubleDefault0Adapter)
                    .registerTypeAdapter(double.class, doubleDefault0Adapter)
                    .setLenient()
                    .create();

            mMap = new TreeMap<>();
            mMap.put(VisualFilterConfig.class.getName(), VisualFilterConfig.class);
            mMap.put(VisualFilterConfig.ChromaKey.class.getName(), VisualFilterConfig.ChromaKey.class);
            mMap.put(VisualFilterConfig.SkinBeauty.class.getName(), VisualFilterConfig.SkinBeauty.class);
            mMap.put(VisualFilterConfig.FaceAdjustment.class.getName(), VisualFilterConfig.FaceAdjustment.class);
            mMap.put(VisualFilterConfig.FaceAdjustmentExtra.class.getName(), VisualFilterConfig.FaceAdjustmentExtra.class);

            mMap.put(VisualFilterConfig.Pixelate.class.getName(), VisualFilterConfig.Pixelate.class);
            mMap.put(VisualFilterConfig.AutoKeying.class.getName(), VisualFilterConfig.AutoKeying.class);
            mMap.put(VisualFilterConfig.BlurConfig.class.getName(), VisualFilterConfig.BlurConfig.class);
        }

        @Override
        public VisualFilterConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String type = json.getAsJsonObject().get("type").getAsString();
            Class c = mMap.get(type);
            if (c == null) {
                throw new RuntimeException("Unknow class: " + type);
            }
            return (VisualFilterConfig) mGson.fromJson(json, c);
        }

        @Override
        public JsonElement serialize(VisualFilterConfig src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                Class c = mMap.get(src.type);
                if (c == null) {
                    throw new RuntimeException("Unknow class: " + src.type);
                }
                return mGson.toJsonTree(src, c);
            }
            return null;
        }
    }

}
