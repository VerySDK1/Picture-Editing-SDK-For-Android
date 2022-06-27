package com.pesdk.uisdk.bean.type;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.Keep;
import androidx.annotation.StringDef;

/**
 * 特效分组
 *
 * @create 2019/9/12
 */
@Keep
public class EffectType {
    public static final String DONGGAN = "动感";
    public static final String FENPING = "分屏";
    public static final String ZHUANCHANG = "转场";
    public static final String DINGGE = "定格";
    public static final String TIME = "时间";

    public static final String DINGGE_EN = "Freeze-Frame";//英文定格type

    @Keep
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({DONGGAN, FENPING, ZHUANCHANG, DINGGE, TIME})
    public @interface Effect {

    }


}
