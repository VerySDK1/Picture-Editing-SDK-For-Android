package com.pesdk.uisdk.data.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.BitmapUtil;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.utils.PEUtils;

import java.nio.ByteBuffer;

/**
 *
 */
public class WatermarkModel {
    private static final String TAG = "WatermarkModel";


    public String onDewatermark(Bitmap bitmap, Bitmap mask) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        byte[] data = BitmapUtil.BitmapToBytes(bitmap);
        Bitmap maskBmp;
        if (width != mask.getWidth() && height != mask.getHeight()) {
            maskBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(maskBmp);
            cv.drawBitmap(mask, null, new Rect(0, 0, width, height), null);
        } else {
            maskBmp = mask;
        }


        byte[] maskBuffer = BitmapUtil.BitmapToBytes(maskBmp); //Get the bytes ar
//        Log.e(TAG, "onDewatermark: mask: " + maskBuffer.length + " buffer: " + Arrays.toString(maskBuffer));
        ByteBuffer buffer = ByteBuffer.allocate(width * height);
        byte[] maskBytes = buffer.array();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                maskBytes[idx] = maskBuffer[idx * 4 + 3];
            }
        }
//        Log.e(TAG, "onDewatermark: " + maskBytes.length + " >" + (maskBytes.length * 4) + "   mask: " + (maskBytes[maskBytes.length - 1]) + " >" + (maskBytes[0]));
        long st = System.currentTimeMillis();

        int size = width * height;
        int pitch = width * 4;
//        Log.e(TAG, "onDewatermark: " + width + "*" + height + " pitch:" + pitch + " data:" + size + " =? " + data.length + " " +
//                "mask: " + maskBytes.length + "  =?" + size + "  " + ((data.length + 0.0f) / size));

        int result = PEUtils.deLogoWithMask(width, height, pitch, data, width, height, width, maskBytes);

        try {
            Bitmap tmp = BitmapUtil.Bytes2Bimap(data, width, height);
            String dst = PathUtils.getTempFileNameForSdcard("watermark_pic", "png");
            BitmapUtils.saveBitmapToFile(tmp, true, 100, dst);
            return dst;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (maskBmp != mask) {
                maskBmp.recycle();
            }
        }
        return null;
    }


}
