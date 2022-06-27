package com.pesdk.uisdk.util.helper;

import android.util.Log;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.model.IMediaParamImp;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.BeautyInfo;
import com.pesdk.uisdk.util.Utils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.EffectInfo;
import com.vecore.models.PEImageObject;
import com.vecore.models.VisualFilterConfig;

import java.util.ArrayList;
import java.util.List;

import static com.vecore.annotation.EffectApplyRange.Global;

/**
 *
 */
public class FilterUtil {

    private static final String TAG = "FilterUtil";

    public static List<VisualFilterConfig> getFilterList(ImageOb imageOb) {
        return getFilterList(imageOb.getFilter(), imageOb.getAdjust(), imageOb.getBeauty());
    }


    /**
     * 合并滤镜、调色
     *
     * @param filter 滤镜
     * @param adjust 调色
     * @return
     */
    public static List<VisualFilterConfig> getFilterList(FilterInfo filter, FilterInfo adjust, FilterInfo skin) {
        return getFilterList(null != filter ? filter.getLookupConfig() : null, null != adjust ? adjust.getMediaParamImp() : null, null != skin ? skin.getBeauty() : null);
    }


    /**
     * 虚拟图片的滤镜
     *
     * @param filter 滤镜
     * @param adjust 调色
     * @return
     */
    public static List<EffectInfo> getFilterList(FilterInfo filter, FilterInfo adjust) {
        return getFilterList(null != filter ? filter.getLookupConfig() : null, null != adjust ? adjust.getMediaParamImp() : null);
    }

    public static ArrayList<EffectInfo> getFilterList(VisualFilterConfig mLookupConfig, IMediaParamImp mediaParamImp) {
        ArrayList<EffectInfo> effectInfos = new ArrayList<>();
        EffectInfo effectInfo;
        if (mLookupConfig != null) {
            //滤镜
            effectInfo = new EffectInfo();
            effectInfo.setFilterConfig(mLookupConfig).setTimelineRange(Utils.ms2s(0), Utils.ms2s(1000));
            effectInfo.setApplyRange(Global);
            effectInfos.add(effectInfo);
        }
        if (null != mediaParamImp) {         //调色
            List<VisualFilterConfig> toning = getFilterList(mediaParamImp);
            for (int i = 0; i < toning.size(); i++) {
                VisualFilterConfig visualFilterConfig = toning.get(i);
                effectInfo = new EffectInfo();
                effectInfo.setFilterConfig(visualFilterConfig).setTimelineRange(Utils.ms2s(0), Utils.ms2s(1000));
                effectInfo.setApplyRange(Global);
                effectInfos.add(effectInfo);
            }
        }
        return effectInfos;
    }

    /**
     * 调色调色参数到基础滤镜
     *
     * @param config 基础滤镜
     * @param imp    调色参数
     * @return 带调色参数的滤镜
     */
    public static VisualFilterConfig applyAdjustConfig(VisualFilterConfig config, IMediaParamImp imp) {
        if (null == config) {
            config = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
        }
        config.setBrightness(imp.getBrightness());
        config.setContrast(imp.getContrast());
        config.setSaturation(imp.getSaturation());
        config.setSharpen(imp.getSharpen());
        config.setTemperature(imp.getTemperature());
        config.setTint(imp.getTintValue());
        config.setHighlights(imp.getHighlightsValue());
        config.setShadows(imp.getShadowsValue());
        config.setGraininess(imp.getGraininess());
        config.setLightSensation(imp.getLightSensation());
        config.setFade(imp.getFade());
        return config;

    }


    /**
     * 虚拟图片调色
     */
    public static List<VisualFilterConfig> getFilterList(IMediaParamImp mediaParamImp) {
        ArrayList<VisualFilterConfig> configs = new ArrayList<>();

        float defaultValue = Float.NaN;
        VisualFilterConfig config = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
        config = applyAdjustConfig(config, mediaParamImp);
        config.setDefaultValue(defaultValue);
        configs.add(config);

        if (mediaParamImp.getVignetteId() != IMediaParamImp.NO_VIGNETTE_ID) {
            //暗角有效
            VisualFilterConfig vignetted = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_VIGNETTE);
            vignetted.setDefaultValue(mediaParamImp.getVignette());
            configs.add(vignetted);
        }
        return configs;
    }


    /**
     * 构造滤镜列表（画中画） 可能出现5个{ 滤镜+调色、 暗角、 [美颜、瘦脸+大眼、五官]}
     *
     * @param lookup        滤镜
     * @param mediaParamImp 调色参数
     * @param beautyInfo    美颜
     * @return
     */
    private static List<VisualFilterConfig> getFilterList(VisualFilterConfig lookup, IMediaParamImp mediaParamImp, BeautyInfo beautyInfo) {
        ArrayList<VisualFilterConfig> list = new ArrayList<>();
        float defaultValue = Float.NaN;
        VisualFilterConfig config = null;
        if (null != lookup) {
            config = lookup.copy();
            defaultValue = config.getDefaultValue();
            config.resetParams();
        } else {
            config = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
        }
        if (null != mediaParamImp) {
            config = applyAdjustConfig(config, mediaParamImp);
        }

        config.setDefaultValue(defaultValue);
        list.add(config);

        if (null != mediaParamImp && mediaParamImp.getVignetteId() != IMediaParamImp.NO_VIGNETTEDID) {
            //暗角有效
            VisualFilterConfig vignetted = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_VIGNETTE);
            vignetted.setDefaultValue(mediaParamImp.getVignette());
            list.add(vignetted);
        }

        if (beautyInfo != null) {
            //基础美颜
            VisualFilterConfig.SkinBeauty skinBeauty = new VisualFilterConfig.SkinBeauty(beautyInfo.getValueBeautify());
            skinBeauty.setWhitening(beautyInfo.getValueWhitening());
            skinBeauty.setRuddy(beautyInfo.getValueRuddy());
            list.add(skinBeauty);

            //人脸
            List<BeautyFaceInfo> faceList = beautyInfo.getFaceList();
            if (faceList != null && faceList.size() > 0) {
                for (BeautyFaceInfo faceInfo : faceList) {
                    //瘦脸、大眼
                    VisualFilterConfig.FaceAdjustment faceConfig = faceInfo.getFaceConfig();
                    if (faceConfig != null) {
                        list.add(faceConfig);
                    }

                    //五官
                    VisualFilterConfig.FaceAdjustmentExtra fiveSensesConfig = faceInfo.getFiveSensesConfig();
                    if (fiveSensesConfig != null) {
                        list.add(fiveSensesConfig);
                    }
                }
            }
        }


        return list;
    }


    /**
     * 获取背景景深程度
     *
     * @return 0~1.0f
     */
    public static float getBlurValue(List<VisualFilterConfig> list) {
        float value = 0;
        if (null == list) {
            return value;
        }
        for (VisualFilterConfig visualFilterConfig : list) {
            if (visualFilterConfig.getId() == VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR) {
                if (visualFilterConfig.getDefaultValue() == Float.NaN) {
                    value = 0;
                } else {
                    value = visualFilterConfig.getDefaultValue();
                }
                break;
            }
        }
        return value;
    }


    /**
     * 设置滤镜
     */
    public static void applyFilter(PEImageObject object) {
        if (object == null) {
            return;
        }
        ImageOb imageOb = (ImageOb) object.getTag();
        Log.e(TAG, "applyFilter: " + imageOb);
        List<VisualFilterConfig> tmp = getFilterList(imageOb.getFilter(), imageOb.getAdjust(), imageOb.getBeauty());
        try {
            object.changeFilterList(tmp);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

    }
}
