package com.pesdk.uisdk.fragment.main.fg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.utils.MiscUtils;

import java.util.List;

/**
 *
 */
class LayerHandlerHelper {
    private static final String TAG = "LayerHandlerHelper";

    /**
     * 生成当前point周围对应的bitmap
     *
     * @param path
     * @param x
     * @param y    0~1.0f
     * @return
     */
    static Bitmap createCropBitmap(String path, float x, float y) {
        Bitmap bitmap = null;
        try {
            bitmap = MiscUtils.getBitmapByMedia(new PEImageObject(path).getInternal(), 640);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }


        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int size = Math.max(100, (int) (Math.min(w, h) * 0.2f));
        int left = (int) Math.max(0, Math.min((w * x) - (size * 0.5f), w - size));
        int top = (int) Math.max(0, Math.min((h * y) - (size * 0.5f), h - size));
        Rect clip = new Rect(left, top, left + size, top + size);


        //获取正方形bitmap,并计算透明区域占比
        Bitmap dst = Bitmap.createBitmap(clip.width(), clip.height(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(dst);
        cv.drawBitmap(bitmap, clip, new RectF(0, 0, dst.getWidth(), dst.getHeight()), null);
        bitmap.recycle();
        return dst;

    }

    /**
     * 倒序查找 可点击的item
     *
     * @param exclude 需排除的item
     * @param pointF  手指point 相对于播放器预览区域
     * @param list    叠加、画中画
     * @return
     */
    static CollageInfo getItem(CollageInfo exclude, PointF pointF, List<CollageInfo> list) {
        CollageInfo dst = null;
        for (int i = list.size() - 1; i >= 0; i--) { //必须倒序
            CollageInfo info = list.get(i);
            if (info == exclude) {
                Log.e(TAG, "getItem: ...");
            } else {
                RectF rectF = info.getImageObject().getShowRectF();
                if (rectF.contains(pointF.x, pointF.y)) {
                    dst = info;
                    break;
                }
            }
        }
        return dst;
    }


    static int getIndex(List<CollageInfo> list, CollageInfo info) {
        return list.indexOf(info);
    }
}
