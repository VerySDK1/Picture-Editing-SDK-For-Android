package com.pesdk.uisdk.export;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.util.Log;

import com.pesdk.utils.RUtils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

/**
 * 计算水印位置
 */
public class WatermarkUtil {
    private static final String TAG = "WatermarkUtil";

    /**
     * 计算导出时的水印位置
     *
     * @param context
     * @param path
     * @param minSide
     * @param asp
     * @return
     */
    public static RectF fixExportWaterRectF(Context context, String path, int minSide, float asp, int xAdj, int yAdj) {
        InputStream stream = null;
        RectF rectF = null;
        try {
            Bitmap bitmap;
            if (path.startsWith("asset://")) {
                stream = context.getAssets().open(path.replace("asset://", ""));
                bitmap = BitmapFactory.decodeStream(stream);
            } else {
                FileDescriptor fileDescriptor = RUtils.getFileDescriptor(context, path);
                if (null != fileDescriptor) {
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                } else {
                    bitmap = BitmapFactory.decodeFile(path);
                }
            }
            rectF = fixWatermarkShowRectF(bitmap, minSide, asp, xAdj, yAdj);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
            rectF = null;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return rectF;
    }

    /**
     * 计算水印位置  (可保证等比例输出时与录屏水印保持一致)
     *
     * @param bmp            水印bitmap
     * @param minSide        输出视频的最小边
     * @param mCurProportion 输出视频的比例
     * @return
     */
    private static RectF fixWatermarkShowRectF(Bitmap bmp, int minSide, float mCurProportion, int xAdj, int yAdj) {
        try {
            float baseWidth = 720f;  //水印设计的基准

            float outHeight, outWidth;

            if (mCurProportion >= 1) { //输出size：动态更改
                outHeight = minSide;
                outWidth = outHeight * mCurProportion;
            } else {
                outWidth = minSide;
                outHeight = outWidth / mCurProportion;
            }

            int re = (int) (outWidth % 16);
            if (re != 0) { // 与录屏同步。宽设置为16的倍数（兼容性最好）
                outWidth += (re > 16 / 2) ? (16 - re) : -re; //取最近的16的倍数
            }

            Log.e(TAG, "fixWatermarkShowRectF: " + outWidth + "*" + outHeight);

            //转换为缩放后的 outWidth*outHeight    水印在输出size中的目标大小
            int dstW = (int) (outWidth * bmp.getWidth() / baseWidth);
            int dstH = (int) (dstW / (bmp.getWidth() / (bmp.getHeight() + .0f))); //*****等比例 ：防止在非！9:16时变形


            int x = (int) (outWidth - dstW - xAdj);
            int y = (int) (outHeight - dstH - yAdj);

            // 水印在输出中的位置：
            return new RectF(x / outWidth, y / outHeight, 1 - (xAdj / outWidth), 1 - (yAdj / outHeight));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
