package com.pesdk.uisdk.util;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.pesdk.uisdk.bean.EffectsTag;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.ITimeLine;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.util.helper.BitmapUtil;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.EffectInfo;
import com.vecore.models.FlipType;
import com.vecore.models.MediaObject;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.utils.MiscUtils;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public class Utils {


    public static void autoBindLifecycle(Context context, LifecycleObserver observer) {
        if (context == null) {
            return;
        }
        if (context instanceof AppCompatActivity) {
            // 宿主是activity
            ((AppCompatActivity) context).getLifecycle().addObserver(observer);
            return;
        }
        // 宿主是fragment
        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner) context).getLifecycle().addObserver(observer);
            return;
        }
    }


    /**
     * 查找容器范围内的组件
     */
    public static <T extends View> T $(View mRoot, int resId) {
        return mRoot.findViewById(resId);
    }

    private static final StringBuilder m_sbFormator = new StringBuilder();
    private static final Formatter m_formatter = new Formatter(m_sbFormator,
            Locale.getDefault());

    /**
     * 根据时间获取唯一ID
     */
    public static int getRandomId() {
        String time = String.valueOf(System.currentTimeMillis());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }


    /**
     * 判断是否存在虚拟导航键
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        return CoreUtils.checkDeviceVirtualBar(context);
    }

    /**
     * 时长单位的转换 秒->毫秒
     */
    public static int s2ms(float s) {
        return (int) (s * 1000);
    }

    /**
     * 时长单位的转换 毫秒->秒
     */
    public static float ms2s(int ms) {
        return (ms / 1000.0f);
    }

    /**
     * 时长单位的转换 毫秒->秒
     *
     * @param ms 时长单位的转换 毫秒->秒
     */
    public static float ms2s(long ms) {
        return (ms / 1000.0f);
    }

    private static final String TAG = "Utils";


    /**
     * 可以左右滑动时 露出部分
     */
    public static int increaseDistance(int num, int width, int paddingLeft, int padding) {
        int w = (num - 1) * (width + padding) + width + paddingLeft;
        int mWidthPixels = CoreUtils.getMetrics().widthPixels;
        if (w < mWidthPixels) {
            return padding;
        }
        int index = 0;
        for (int i = 1; i <= num; i++) {
            w = (i - 1) * (width + padding) + width + paddingLeft;
            if (w > mWidthPixels) {
                index = i;
                break;
            }
        }
        if (index > 2) {
            float factor = (mWidthPixels - (index - 2) * (width + padding) - width - paddingLeft) * 1.0f / (padding + width);
            if (factor <= 0.4f) {
                factor = 0.4f;
            } else if (factor >= 0.6f) {
                factor = 0.6f;
            }
            padding = (int) (((mWidthPixels - width - paddingLeft) - (index - 2) * width - width * (1 - factor)) / (index - 1));
        }
        return padding;
    }

    private static Matrix mMatrix = new Matrix();

    /**
     * 计算旋转后的裁剪区域
     *
     * @param clipRectF 当前的裁剪区域
     * @param viewRectF 当前容器宽高
     * @return
     */
    public static RectF rotateClipRecF(RectF clipRectF, RectF viewRectF) {
        mMatrix.reset();
        mMatrix.postRotate(-90, viewRectF.centerX(), viewRectF.centerY());
        RectF dst = new RectF();
        mMatrix.mapRect(dst, clipRectF); //旋转后的裁剪区域
        RectF dst2 = new RectF();
        mMatrix.mapRect(dst2, viewRectF); //整体容器旋转后的宽高
//        Log.e(TAG, "rotateMatrix: " + clipRectF + " dst：" + dst + " " + clipRectF.width() + "*" + clipRectF.height() + "  -- " + dst.width() + "*" + dst.height() + " viewRectF:" + viewRectF);
//
//
//        Log.e(TAG, "rotateMatrix: " + dst2 + " " + dst2.width() + "*" + dst2.height());

        //坐标系转化（相对于旋转后的容器size）
        mMatrix.reset();
        mMatrix.setTranslate((dst2.width() - viewRectF.width()) / 2f, (dst2.height() - viewRectF.height()) / 2f);
        RectF dst3 = new RectF();
        mMatrix.mapRect(dst3, dst); //旋转后的裁剪区域在view中的位置

//        RectF dst4 = new RectF();
//        mMatrix.mapRect(dst4, dst2); //view
//        Log.e(TAG, "rotateMatrix: mapRect: " + dst3 + " " + dst3.width() + "*" + dst3.height() + " dst4:" + dst4);
        return dst3;
    }

    /**
     * 旋转时，背景以90的整数倍旋转
     *
     * @param mediaShowAngle
     * @return
     */
    public static int getBGShowAngle(int mediaShowAngle) {
        int tmp = mediaShowAngle / 90 * 90;
        return tmp + (90 * ((mediaShowAngle - tmp) > 45 ? 1 : 0));
    }

    /**
     * 当前状态执行上下镜像
     *
     * @return 新的镜像类型 （不一定是上下)
     */
    public static FlipType onVerticalFlipType(MediaObject mediaObject) {
        FlipType tmp = mediaObject.getFlipType();
        if (mediaObject.checkIsLandRotate()) {
            if (tmp == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
                tmp = FlipType.FLIP_TYPE_VERTICAL;
            } else if (tmp == FlipType.FLIP_TYPE_VERTICAL) {
                tmp = FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL;
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == tmp) {
                tmp = FlipType.FLIP_TYPE_NONE;
            } else {
                tmp = (FlipType.FLIP_TYPE_HORIZONTAL);
            }
        } else {
            if (tmp == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
                tmp = (FlipType.FLIP_TYPE_HORIZONTAL);
            } else if (tmp == FlipType.FLIP_TYPE_HORIZONTAL) {
                tmp = (FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL);
            } else if (tmp == FlipType.FLIP_TYPE_VERTICAL) {
                tmp = (FlipType.FLIP_TYPE_NONE);
            } else {
                tmp = (FlipType.FLIP_TYPE_VERTICAL);
            }
        }
        return tmp;
    }

    /**
     * 当前状态执行左右镜像
     *
     * @return 新的镜像类型 (不一定是左右)
     */
    public static FlipType onHorizontalFlipType(MediaObject mediaObject) {
        FlipType tmp = mediaObject.getFlipType();
        if (mediaObject.checkIsLandRotate()) {
            if (tmp == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
                tmp = (FlipType.FLIP_TYPE_HORIZONTAL);
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == tmp) {
                tmp = (FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL);
            } else if (FlipType.FLIP_TYPE_VERTICAL == tmp) {
                tmp = (FlipType.FLIP_TYPE_NONE);
            } else {
                tmp = (FlipType.FLIP_TYPE_VERTICAL);
            }
        } else {
            if (tmp == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
                tmp = (FlipType.FLIP_TYPE_VERTICAL);
            } else if (tmp == FlipType.FLIP_TYPE_VERTICAL) {
                tmp = (FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL);
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == tmp) {
                tmp = (FlipType.FLIP_TYPE_NONE);
            } else {
                tmp = (FlipType.FLIP_TYPE_HORIZONTAL);
            }
        }
        return tmp;
    }

    public static void autoToastNomal(Context context, @StringRes int strId) {
        autoToastNomal(context, context.getString(strId));
    }

    public static void autoToastNomal(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 震动
     *
     * @param context
     */
    public static void onVibrator(Context context) {
        if (null != context) {
            try {
                Vibrator vibrator = (Vibrator) context.getSystemService(Activity.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 字幕的id
     *
     * @return id
     */
    public static int getWordId() {
        String time = String.valueOf(System.currentTimeMillis());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }

    /**
     * 返回一个资源id
     */
    public static int getId() {
        //当前时间 后6位（毫秒+微妙）
        String time = String.valueOf(System.nanoTime());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }

    /**
     * 特效
     *
     * @return
     */
    public static int getEffectId() {
        String time = String.valueOf(System.nanoTime());
        int len = time.length();
        return Integer.parseInt(time.substring(len - 6, len));
    }

    /**
     * 当前在集合的索引
     *
     * @param id 当前的Id
     * @return
     */
    public static int getIndex(List<? extends ITimeLine> list, int id) {
        int index = -1;
        if (null != list) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                ITimeLine timeLine = list.get(i);
                if (timeLine != null && id == timeLine.getId()) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }


    /**
     * 判断当前点击的坐标，是否在字幕预览框内
     *
     * @param mListPointF 字幕预览框
     * @param fx
     * @param fy
     * @return
     */
    public static boolean isContains(List<PointF> mListPointF, float fx, float fy) {
        if (null != mListPointF && mListPointF.size() == 4) {
            RectF r = new RectF();
            Path path = new Path();
            path.moveTo(mListPointF.get(0).x, mListPointF.get(0).y);
            path.lineTo(mListPointF.get(1).x, mListPointF.get(1).y);
            path.lineTo(mListPointF.get(2).x, mListPointF.get(2).y);
            path.lineTo(mListPointF.get(3).x, mListPointF.get(3).y);
            path.close();
            path.computeBounds(r, true);
            Region region = new Region();
            region.setPath(path, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
            return region.contains((int) fx, (int) fy);
        }
        return false;
    }

    /***
     * 获取时间范围内最顶层的元素 因为：后加载的在上层
     * @param list
     * @param progress  播放器位置
     * @param dstIndex  用户强制指定了选中项
     * @return
     */
    public static ITimeLine getTopItem(List<? extends ITimeLine> list, int progress, int dstIndex) {
        if (dstIndex >= 0 && dstIndex < list.size()) {
            ITimeLine current = list.get(dstIndex);
            if (current != null) {
                return current;
            }
        }
        ITimeLine current = null;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ITimeLine timeLine = list.get(i);
            if (timeLine != null) {
            }
        }
        return current;
    }


    /**
     * 构建媒体的缩率图
     *
     * @param context
     * @param mediaObject
     * @return
     */
    @WorkerThread
    public static String fixThumb(@NonNull Context context, @NonNull PEImageObject mediaObject) {
        return fixThumb(context, mediaObject, 300);
    }

    @WorkerThread
    public static String fixThumb(@NonNull Context context, @NonNull PEImageObject mediaObject, int maxWH) {
        String path = PathUtils.getTempFileNameForSdcard("Temp_imix_thumb", ".png");
        return fixThumb(context, mediaObject, path, maxWH);
    }


    /**
     * 画中画缩略图
     *
     * @param context
     * @param mediaObject
     * @param dstThumbPath
     * @return
     */
    private static String fixThumb(@NonNull Context context, @NonNull PEImageObject mediaObject, String dstThumbPath, int maxWH) {
        Bitmap bitmap = null;
        bitmap = MiscUtils.getBitmapByMedia(mediaObject.getInternal(), maxWH);
        if (null != bitmap) {
            try {
                BitmapUtils.saveBitmapToFile(bitmap, 100, dstThumbPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != bitmap) {
            bitmap.recycle();
        }
        return dstThumbPath;
    }


    private static final int MAX_IMAGE_WH = 1920;


    /**
     * 导入图片媒体时，缩放大图
     *
     * @return
     */
    public synchronized static String scaleMediaObject(Context context, String srcPath) {
        PEImageObject src = null;
        try {
            src = new PEImageObject(context, srcPath);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        if (null != src && ((src.getWidth() > MAX_IMAGE_WH || src.getHeight() > MAX_IMAGE_WH))) //需要缩放
//        if (null != src && (src.getMediaType() == MediaType.MEDIA_IMAGE_TYPE && (src.getWidth() > MAX_IMAGE_WH || src.getHeight() > MAX_IMAGE_WH) && !src.isMotionImage())) //需要缩放
        {

            Bitmap bmp = BitmapUtil.getBitmap(context, src, MAX_IMAGE_WH);
            if (null != bmp && !bmp.isRecycled()) {
                int angle = src.getOriginalAngle(); //防止部分图片有内置角度
                if (angle != 0) {
                    Bitmap tmp = com.vecore.base.lib.utils.BitmapUtils.rorateBmp(bmp, angle);
                    if (tmp != null && tmp != bmp) {
                        bmp.recycle();
                        bmp = tmp;
                    }
                }
                String mime = src.getMime();
                boolean isPng = mime.toLowerCase().contains("png") || mime.toLowerCase().contains("webp");
                String path = PathUtils.getCacheMediaPath(bmp.hashCode(), isPng);
                try {
                    com.vecore.base.lib.utils.BitmapUtils.saveBitmapToFile(bmp, isPng, 95, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bmp.recycle();
                return path;
            }
        }
        return srcPath;
    }


    /**
     * 文本转哈希值
     *
     * @param text
     * @return
     */
    public static int getHash(String text) {
        int arraySize = 11113; // 数组大小一般取质数
        int hashCode = 0;
        for (int i = 0; i < text.length(); i++) { // 从字符串的左边开始计算
            int letterValue = text.charAt(i) - 96;// 将获取到的字符串转换成数字，比如a的码值是97，则97-96=1
            // 就代表a的值，同理b=2；
            hashCode = ((hashCode << 5) + letterValue) % arraySize;// 防止编码溢出，对每步结果都进行取模运算
        }
        return hashCode;
    }

    /***
     * 获取时间范围内最顶层的元素 因为：后加载的在上层
     * @param list
     * @param progress  播放器位置
     * @param dstIndex  用户强制指定了选中项
     * @return
     */
    public static EffectInfo getTopItemByEffectList(List<EffectInfo> list, int progress, int dstIndex) {
        float proS = Utils.ms2s(progress);
        if (dstIndex >= 0 && dstIndex < list.size()) {
            EffectInfo effectInfo = list.get(dstIndex);
            if (effectInfo != null && effectInfo.getStartTime() <= proS
                    && proS < effectInfo.getEndTime()) {
                Object object = effectInfo.getTag();
                if (object instanceof EffectsTag) {
                    EffectsTag effectsTag = (EffectsTag) object;
                    effectsTag.setIndex(dstIndex);
                    return effectInfo;
                }

            }
        }
        EffectInfo current = null;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            EffectInfo timeLine = list.get(i);
            if (timeLine.getStartTime() <= proS && proS < timeLine.getEndTime()) {
                current = timeLine;
                Object object = current.getTag();
                if (object instanceof EffectsTag) {
                    EffectsTag effectsTag = (EffectsTag) object;
                    effectsTag.setIndex(i);
                    break;
                }
            }
        }
//        Log.e(TAG, "getTopItemByEffectList: " + list + ">" + progress + " >" + proS + (null == current));
        return current;
    }

    /**
     * 修正比例
     *
     * @param oldRectF 原始显示区域0~1
     * @param mediaAsp 原始的比例 媒体的
     * @param oldAsp   旧的比例
     * @param newAsp   新比例
     * @return 新的显示区域
     */
    public static RectF correctionRatio(RectF oldRectF, float mediaAsp, float oldAsp, float newAsp) {
        if (oldRectF == null || oldRectF.isEmpty()) {
            return oldRectF;
        }
        //原始的比例
        float scale;
        if (oldAsp > mediaAsp) {
            scale = oldRectF.height();
        } else {
            scale = oldRectF.width();
        }

        //计算现在的区域
        RectF newRectF = new RectF();
        if (newAsp > mediaAsp) {
            newRectF.set(0, 0, mediaAsp / newAsp, 1);
        } else {
            newRectF.set(0, 0, 1, newAsp / mediaAsp);
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, newRectF.centerX(), newRectF.centerY());
        //中心偏移
        matrix.postTranslate(oldRectF.centerX() - newRectF.centerX(), oldRectF.centerY() - newRectF.centerY());
        matrix.mapRect(newRectF, newRectF);
        return newRectF;
    }

    /**
     * 退出编辑时，判断数据是否有变化，提示是否退出
     *
     * @param list     当前页面编辑的数据   (元素对象需事先equals( T ) )
     * @param saveList 上传保存的数据
     */
    public static <T extends Object> boolean isEqualsSource(List<T> list, List<T> saveList) {
        if (list.size() != saveList.size()) {
            return false;
        }
        boolean isEquals = true;
        for (int i = 0; i < list.size(); i++) {
            T info = list.get(i);
            if (!info.equals(saveList.get(i))) {
                isEquals = false;
                break;
            }
        }
        return isEquals;
    }


    public static PEScene copy(PEScene src) {
        ImageOb tag = (ImageOb) src.getPEImageObject().getTag();
        PEScene clone = src.copy();
        if (tag != null) {
            clone.getPEImageObject().setTag(tag.copy());
        }
        return clone;
    }


    /**
     * 抠图
     *
     * @param imageObject
     * @param type        人像、天空
     */
    public static void setSegment(PEImageObject imageObject, @Segment.Type int type) {
        Object object = imageObject.getTag();
        ImageOb imageOb = null;
        if (object instanceof ImageOb) {
            imageOb = (ImageOb) object;
        } else {
            imageOb = new ImageOb();
            imageObject.setTag(imageOb);
        }
        imageObject.setBlendEnabled(true);
        imageOb.setSegment(type);

    }

    /**
     * 获取UUId
     */
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private static final int COVER_WH = 200;

    public static String getCover(PEImageObject peImageObject, String basePath) {
        Bitmap bmp = MiscUtils.getBitmapByMedia(peImageObject.getInternal(), COVER_WH);
        if (null != bmp && !bmp.isRecycled()) {
            String path = PathUtils.getTempFileNameForSdcard(basePath, "cover", "jpg");
            try {
                com.vecore.base.lib.utils.BitmapUtils.saveBitmapToFile(bmp, false, 100, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bmp.recycle();
            return path;
        }
        return null;
    }
}
