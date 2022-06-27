package com.pesdk.uisdk.util.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pesdk.utils.RUtils;
import com.vecore.models.PEImageObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class BitmapUtil {
    public static byte[] BitmapToBytes(Bitmap bmp) {
        int bytes = bmp.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bmp.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
        return buffer.array(); //Get the bytes ar
    }

    public static Bitmap Bytes2Bimap(byte[] b, int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = ByteBuffer.wrap(b);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }


    /**
     * 获取内容
     *
     * @param context
     * @param src
     * @param maxWH
     * @return
     */
    public static Bitmap getBitmap(Context context, PEImageObject src, int maxWH) {
        int inSampleSize = 1;
        if (src.getWidth() > maxWH || src.getHeight() > maxWH) {
            int width, height;
            float asp = (float) src.getWidth() / src.getHeight();
            if (src.getWidth() >= src.getHeight()) {
                width = maxWH;
                height = (int) (width / (asp));
            } else {
                height = maxWH;
                width = (int) (height * asp);
            }
            inSampleSize = calculateInSampleSize(src.getWidth(), src.getHeight(), width, height);
        }
        String srcPath = src.getMediaPath();
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inSampleSize = inSampleSize;
        return RUtils.decodeFile(context, srcPath, op);
    }


    private static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
//            算法二：取目标长宽的最大值来计算，这样会减少过度的尺寸压缩，
//使用需要的宽高的最大值来计算比率
            final int suitedValue = reqHeight > reqWidth ? reqHeight : reqWidth;
            final double heightRatio = Math.ceil((float) height / suitedValue);
            final double widthRatio = Math.ceil((float) width / suitedValue);
            inSampleSize = (int) (heightRatio > widthRatio ? heightRatio : widthRatio);//用最大
//            版权声明：本文为CSDN博主「AndSync」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
//            原文链接：https://blog.csdn.net/mylizhimin/article/details/53462653
        }
        return inSampleSize;
    }


    private byte[] IntToByteArray(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    private void bytes2File(byte[] bytes, String path) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                }

            }
        }


    }
}
