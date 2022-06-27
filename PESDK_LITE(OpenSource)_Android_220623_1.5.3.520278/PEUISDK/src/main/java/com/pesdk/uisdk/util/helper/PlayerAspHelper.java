package com.pesdk.uisdk.util.helper;

import com.pesdk.uisdk.bean.FrameInfo;
import com.vecore.models.PEImageObject;

/**
 *
 */
public class PlayerAspHelper {

    /**
     * 获取预览比例
     *
     * @param info          当前边框
     * @param proportionAsp 手动指定的播放器比例
     * @param peImageObject 裁剪比例
     * @return
     */
    public static float getAsp(FrameInfo info, float proportionAsp, PEImageObject peImageObject) {
        if (null != info) {
            return info.getAsp();
        } else {
            return getAsp(proportionAsp, peImageObject);
        }
    }

    /**
     * 没有边框时的比例规则
     *
     * @param proportionAsp 指定比例
     * @param peImageObject 裁剪
     * @return
     */
    @Deprecated
    public static float getAsp(float proportionAsp, PEImageObject peImageObject) {
        if (proportionAsp > 0) {
            return proportionAsp;
        } else if (null != peImageObject) { //正常情况下,不会执行到此行逻辑(因为原始比例 为首次导入主图片时的比例)
            return ProportionUtil.getPlayerAsp(peImageObject);
        } else {
            return 0;
        }
    }

    public static float getAsp(float proportionAsp) {
        if (proportionAsp > 0) {
            return proportionAsp;
        } else {
            return 0;
        }
    }
}

