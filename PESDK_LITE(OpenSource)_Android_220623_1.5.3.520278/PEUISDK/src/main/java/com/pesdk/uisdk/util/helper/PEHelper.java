package com.pesdk.uisdk.util.helper;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.vecore.models.PEImageObject;

/**
 *
 */
public class PEHelper {

    public static ImageOb initImageOb(PEImageObject object) {
        Object tmp = object.getTag();
        if (!(tmp instanceof ImageOb)) {
            tmp = new ImageOb();
            object.setTag(tmp);
        }
        return (ImageOb) tmp;
    }

    /**
     * 参数是否一致
     */
    public static boolean isSame(ImageOb src, ImageOb dst) {
        if (src != null)
            return src.equals(dst);
        return null == dst;

    }


    /***
     * 参数是否一致（显示位置&&旋转角度）
     * @param src
     * @param dst
     * @return true 没有发生变化
     */
    public static boolean isSameParam(CollageInfo src, CollageInfo dst) {
        if (src != null && dst != null) {
            PEImageObject srcImageObject = src.getImageObject();
            PEImageObject dstImageObject = dst.getImageObject();
            boolean isSame = (srcImageObject.getShowRectF().equals(dstImageObject.getShowRectF())) && //显示位置
                    srcImageObject.getShowAngle() == dstImageObject.getShowAngle() && //旋转角度
                    (srcImageObject.getClipRect().equals(dstImageObject.getClipRect())) && //裁剪区域
                    (srcImageObject.getFlipType() == dstImageObject.getFlipType()) //裁剪区域
                    ;
            if (!isSame) {
                return false;
            } else {
                //滤镜、调色、美颜
                if (isSame(PEHelper.initImageOb(srcImageObject), PEHelper.initImageOb(dstImageObject))) {
                    return true;
                }
            }
        }
        return false;
    }

}
