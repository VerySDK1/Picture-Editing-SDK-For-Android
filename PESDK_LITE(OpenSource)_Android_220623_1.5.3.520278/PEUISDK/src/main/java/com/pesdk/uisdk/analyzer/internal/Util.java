package com.pesdk.uisdk.analyzer.internal;

import android.graphics.Bitmap;

import com.vecore.base.lib.utils.LogUtil;

/**
 *
 */
public class Util {
    private static final String TAG = "Util";

    /**
     * 是否有人像：
     */
    public static boolean hasSegment(Bitmap alphaBmp) {
        //有透明通道: 意味着分割成功有人像|天空
        return hasAlpha(alphaBmp, 0.2f);
    }

    /**
     * 是否有足够的透明像素点
     *
     * @param alphaBmp 带透明通道的图片
     * @param min
     * @return 透明通道count>min
     */
    public static boolean hasAlpha(Bitmap alphaBmp, float min) {
        long st = System.currentTimeMillis();
        int nAlphaCount = 0;
        int width = alphaBmp.getWidth();
        int height = alphaBmp.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //获得Bitmap 图片中每一个点的color颜色值
                //将需要填充的颜色值如果不是
                //在这说明一下 如果color 是全透明 或者全黑 返回值为 0
                //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000
                //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了
                int color = alphaBmp.getPixel(j, i);
                //将颜色值存在一个数组中 方便后面修改
                if (color == 0) { //纯透明
                    nAlphaCount++;
                }
            }
        }

        float tmp = nAlphaCount / (width * height * 1.0f);
        LogUtil.i(TAG, "hasAlpha: -----------> " + tmp + "  > " + nAlphaCount + " / " + (width * height) + "  " + (System.currentTimeMillis() - st));
        return tmp > min;
    }

    /**
     * 叠加需验证所选择区域是否有足够的黑色
     *
     * @param alphaBmp
     * @param min
     * @return
     */
    public static boolean hasBlack(Bitmap alphaBmp, float min) {
        long st = System.currentTimeMillis();
        int nBlackCount = 0;
        int width = alphaBmp.getWidth();
        int height = alphaBmp.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //获得Bitmap 图片中每一个点的color颜色值
                //将需要填充的颜色值如果不是
                //在这说明一下 如果color 是全透明 或者全黑 返回值为 0
                //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000
                //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了
                int color = alphaBmp.getPixel(j, i);
                //将颜色值存在一个数组中 方便后面修改
//                if (color == Color.BLACK) { //纯黑
//                    nAlphaCount++;
//                }
//                https://www.it1352.com/533196.html

//                int alpha = (color & 0xff000000) >>> 24;

                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = (color & 0x000000ff);
                double Y = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
//                Log.e(TAG, "hasBlack: (" + j + "*" + i + ") " + color + "  _-----  " + alpha + "  _" + red + "," + green + "," + blue + "  灰度图值: " + Y);
                if (Y < 128) {
                    nBlackCount++;
                }
            }
        }

        float tmp = nBlackCount / (width * height * 1.0f);
        LogUtil.i(TAG, "hasBlack: -----------> " + tmp + "  > " + nBlackCount + " / " + (width * height) + "  " + (System.currentTimeMillis() - st));
        return tmp > min;
    }

}
