package com.pesdk.uisdk.ui.card.vm;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.beauty.BeautyActivity;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.BeautyInfo;
import com.pesdk.uisdk.beauty.listener.ExtraPreviewFrameListener;
import com.pesdk.uisdk.ui.card.widget.TestDrawView;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.vecore.VirtualImage;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.listener.ExportListener;
import com.vecore.models.ImageConfig;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.models.VisualFilterConfig;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 *
 */
public class CardActivityVM extends AndroidViewModel {


    public MutableLiveData<String> getFloatMedia() {
        return mFloatMedia;
    }

    private MutableLiveData<String> mFloatMedia;

    public MutableLiveData<Integer> getBgColor() {
        return mbgColor;
    }

    private MutableLiveData<Integer> mbgColor;


    public CardActivityVM(@NonNull Application application) {
        super(application);
        mbgColor = new MutableLiveData<>();
        mFloatMedia = new MutableLiveData<>();
    }

    private static final String TAG = "CardActivityVM";


    /**
     * 计算头发在原始图片中的位置
     *
     * @param srcMedia   原始人像
     * @param hairObject 头发对象
     * @param previewW   播放器预览size
     * @param previewH
     */
    public CaptionLiteObject hairInMedia(PEImageObject srcMedia, CaptionLiteObject hairObject, int previewW, int previewH) {
        //显示位置转换：预览时,上下左右有空白,生成图时需转换成相对于原始媒体，生成图片时需保证输出size为原图，比例可能不一致
        return hairObject.copy();
    }


    /**
     * 恢复头发在虚拟图片中的位置
     *
     * @param hair        头发在原始媒体中的位置
     * @param mediaObject 原始媒体的显示位置
     * @return
     */
    public CaptionLiteObject restoreHairInVirtual(CaptionLiteObject hair, PEImageObject mediaObject) {
        CaptionLiteObject dstHair = hair.copy();
        return dstHair;
    }


    /**
     * 识别人脸信息
     */
    public void processFace(String baseMedia, BeautyActivity.Callback callback) {
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            List<BeautyFaceInfo> data;

            @Override
            public void onBackground() { //2.读取人脸
                try {
                    PEImageObject tmp = new PEImageObject(baseMedia);
                    Bitmap bitmap = MiscUtils.getBitmapByMedia(tmp.getInternal(), Math.min(Math.max(tmp.getWidth(), tmp.getHeight()), 1280));
                    data = new ArrayList<>();
                    ExtraPreviewFrameListener.processFace(bitmap, data);
                    bitmap.recycle();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
//                Log.e(TAG, "onEnd: " + data);
                if (null != callback)
                    callback.prepared(data);
            }
        });
    }

    /**
     * 修改美颜
     */
    public void applyFilter(PEImageObject imageObject, BeautyInfo beautyInfo) {
        ArrayList<VisualFilterConfig> configs = new ArrayList<>();
        //美颜
        VisualFilterConfig.SkinBeauty skinBeauty = new VisualFilterConfig.SkinBeauty(beautyInfo.getValueBeautify());
        skinBeauty.setWhitening(beautyInfo.getValueWhitening());
        skinBeauty.setRuddy(beautyInfo.getValueRuddy());
        configs.add(skinBeauty);

        //人脸
        List<BeautyFaceInfo> faceList = beautyInfo.getFaceList();
        if (faceList != null && faceList.size() > 0) {
            for (BeautyFaceInfo faceInfo : faceList) {
                //瘦脸、大眼
                VisualFilterConfig.FaceAdjustment faceConfig = faceInfo.getFaceConfig();
                if (faceConfig != null) {
                    configs.add(faceConfig);
                }

                //五官
                VisualFilterConfig.FaceAdjustmentExtra fiveSensesConfig = faceInfo.getFiveSensesConfig();
                if (fiveSensesConfig != null) {
                    configs.add(fiveSensesConfig);
                }
            }
        }
        try {
            imageObject.changeFilterList(configs);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    public void updateBgColor(int color) {
        mbgColor.setValue(color);
    }

    /**
     * 合并自动的抠图和手动抠图
     *
     * @param showAngle  原始媒体的显示角度
     * @param handMask   手动抠图
     * @param baseMask   自动抠图
     * @param mediaRectF 原媒体的显示位置
     * @return
     */
    public String mergeMask(int showAngle, Bitmap handMask, Bitmap baseMask, RectF mediaRectF) {
        int w = baseMask.getWidth();
        int h = baseMask.getHeight();
        Bitmap tmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tmp);

        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setAntiAlias(true);
        Rect dst = new Rect(0, 0, w, h);
        canvas.drawBitmap(baseMask, null, dst, p);


        Rect rect = getHairRectInBaseMask(mediaRectF, baseMask, new RectF(0, 0, 1, 1));


        //2.rotate(0.5f,0.5f)
        if (showAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(showAngle, 0.5f * w, 0.5f * h);
            canvas.setMatrix(matrix);
        }

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT)); //srcMask中的白色区域，为要消除的部分
        canvas.drawBitmap(handMask, null, rect, p);


        String path = PathUtils.getTempFileNameForSdcard("mask_mix_" + handMask.hashCode(), "png");
//        Log.e(TAG, "mergeMask: " + rect + " " + w + "*" + h + " " + mediaRectF + " " + path + "  asp:" + (rect.width() * 1f / rect.height()) + " =? " + (handMask.getWidth() * 1f / handMask.getHeight()));
        try {
            BitmapUtils.saveBitmapToFile(tmp, true, 100, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


    /**
     * 切换衣服时,依据基础人像和衣服的mask显示位置自动抠图合并
     *
     * @param baseMask   基础人像
     * @param mediaRectF 原始媒体的显示位置
     * @param showAngle  原始媒体的显示角度
     * @param clothMask  衣服mask
     * @param liteObject 播放器中的衣服信息
     */
    public Bitmap applyAutoSegment(TestDrawView testDrawView, Bitmap baseMask, RectF mediaRectF, int showAngle, String clothMask, CaptionLiteObject liteObject) {
        int w = baseMask.getWidth();
        int h = baseMask.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setAntiAlias(true);


        Rect dst = new Rect(0, 0, w, h);
        canvas.drawBitmap(baseMask, null, dst, p);


        Bitmap bmpHair = BitmapFactory.decodeFile(clothMask);
        RectF hairRectF = liteObject.getShowRectF();

//        Log.e(TAG, "applyAutoSegment: " + hairRectF + "  mediaRectF:" + mediaRectF + "  bmp:" + w + "*" + h);
        //1.需转换为相对于原始图
        Rect rect = getHairRectInBaseMask(mediaRectF, baseMask, hairRectF);
//        Log.e(TAG, "applyAutoSegment: rect in base" + rect);
//        Log.e(TAG, "applyAutoSegment: " + showAngle  );


//        testDrawView.setCallback1(cv -> {
//            int a = cv.getWidth(), b = cv.getHeight();
//            Paint paint = new Paint();
//            paint.setColor(Color.BLACK);
//            paint.setStyle(Paint.Style.STROKE);
//            RectF dst1 = new RectF(mediaRectF.left * a, mediaRectF.top * b, mediaRectF.right * a, mediaRectF.bottom * b);
//            cv.drawRect(dst1, paint); //媒体原始显示位置
//
////                float x = 0.5f * w, y = 0.5f * h;
//
////                int layer = canvas.saveLayer(0, 0, a, b, paint);
//            Matrix tmp = new Matrix();
//            tmp.postRotate(-showAngle, dst1.centerX(), dst1.centerY());
//            cv.setMatrix(tmp);
//
//
////                canvas.save();
//            paint.setColor(Color.rgb(255, 100, 100));
//            cv.drawRect(dst1, paint); //旋转后的媒体区域
//
//
//            paint.setColor(Color.rgb(0, 0, 255));
//            paint.setStrokeWidth(5);
//
//            RectF rectF = new RectF(rect.left * 1.0f / w, rect.top * 1.0f / h, rect.right * 1.0f / w, rect.bottom * 1.0f / h);
//            Log.e(TAG, "draw: " + rectF);
//
//
//            float l = dst1.left + (dst1.width() * rectF.left);
//            float t = dst1.top + (dst1.height() * rectF.top);
//
//            float r = l + (dst1.width() * rectF.width());
//            float bb = t + (dst1.height() * rectF.height());
//
//
////                Matrix matrix = new Matrix();
////                RectF rect1 = new RectF(l, t, r, bb);
////                RectF d2 = new RectF();
////                matrix.postRotate(showAngle, rect1.centerX(), rectF.centerY());
////                matrix.mapRect(d2, rect1);
//
//
//            RectF aa = new RectF(l, t, r, bb);
//            cv.drawRect(aa, paint);//头发
//
//            int layer = cv.saveLayer(0, 0, a, b, null);
//
//            cv.rotate(showAngle, dst1.centerX(), dst1.centerY());
//            paint.setColor(Color.rgb(255, 0, 255));
//            paint.setStrokeWidth(5);
//            cv.drawRect(aa, paint);
//            cv.restoreToCount(layer);
//        });

        {
            float x = 0.5f * w, y = 0.5f * h;
            canvas.rotate(showAngle, x, y);
            canvas.rotate(-liteObject.getAngle(), rect.centerX(), rect.centerY());


            {//根据衣服的显示位置, 自动消除衣服边缘的区域
                p.reset();
                p.setColor(Color.TRANSPARENT);
                p.setStyle(Paint.Style.FILL);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN)); //srcMask中的白色区域，为要消除的部分
//                p.setStyle(Paint.Style.STROKE);

                Rect rect1 = new Rect(-w, rect.top, rect.left, h * 2);
                Rect rect2 = new Rect(rect.right, rect.top, 2 * w, h * 2); //拆分成左右两个，防止脖子被消除
                canvas.drawRect(rect1, p); //填充3个方向要消除的部分
                canvas.drawRect(rect2, p);
                if (rect.bottom < h) { //衣服的底边
                    Rect rect3 = new Rect(-w, rect.bottom, 2 * w, h * 2); //拆分成左右两个，防止脖子被消除
                    canvas.drawRect(rect3, p);
                }
            }

            p.reset();
            p.setColor(Color.RED);
            p.setAntiAlias(true);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawBitmap(bmpHair, null, rect, p);


//            p.reset();
//            p.setStrokeWidth(10);
//            p.setColor(Color.GREEN);
//            p.setStyle(Paint.Style.STROKE);
//            canvas.drawRect(rect, p);
//


        }


//        testDrawView.postInvalidateDelayed(500);


        return bmp;
    }


    /**
     * 计算衣服在原始图中的显示位置
     *
     * @param mediaRectF 媒体的显示位置
     * @param baseMask   原始人像
     * @param hairRectF  衣服|手动涂抹的区域
     * @return hairRectF的内容在原始人像中的裁剪位置
     */
    private Rect getHairRectInBaseMask(RectF mediaRectF, Bitmap baseMask, RectF hairRectF) {
        int w = baseMask.getWidth();
        int h = baseMask.getHeight();

        float pw = mediaRectF.width(), ph = mediaRectF.height();
//        Log.e(TAG, "getHairRectInBaseMask:mediaRectF:  " + mediaRectF + " " + hairRectF + " pWH:" + pw + "*" + ph);
        RectF clipRect = new RectF(); //基础Mask在播放器中展示的像素区域 （被保留的clip  显示区域内的人像裁剪）

        clipRect.left = mediaRectF.left >= 0 ? 0 : (Math.abs(mediaRectF.left) / pw * w);
        clipRect.right = mediaRectF.right <= 1 ? w : ((1 - mediaRectF.left) / pw * w);


        clipRect.top = mediaRectF.top >= 0 ? 0 : (Math.abs(mediaRectF.top) / ph * h);
        clipRect.bottom = mediaRectF.bottom <= 1 ? h : ((1 - mediaRectF.top) / ph * h);

        float clipW = clipRect.width();
        float clipH = clipRect.height();

        //衣服应用到BaseMask时需注意:  有可能上下左右有间隙 如: rectF(-0.1f,-0.2f ,0.8f,0.8f )
//        Log.e(TAG, "getHairRectInBaseMask: clipRect:" + clipRect + "  " + clipW + "*" + clipH);

        //播放器中的WH 0~1f 分别对应clip中的像素值 (可能大于clipW*clipH  原因:mediaRectF(0.1f,0.1f,0.8f,0.8f))
        float mw = clipW / (Math.min(1, mediaRectF.right) - Math.max(0, mediaRectF.left)); //显示区域可能
        float mh = clipH / (Math.min(1, mediaRectF.bottom) - Math.max(0, mediaRectF.top));


        Rect rect = new Rect();
        int left = (int) (clipRect.left + (hairRectF.left - Math.max(mediaRectF.left, 0)) * mw);
        int top = (int) (clipRect.top + (hairRectF.top - Math.max(mediaRectF.top, 0)) * mh);
        rect.left = left;
        rect.top = top;

        rect.right = (int) (left + (hairRectF.width() * mw));
        rect.bottom = (int) (top + (hairRectF.height() * mh));

//        Log.e(TAG, "getHairRectInBaseMask: " + rect + " mWH: " + mw + "*" + mh);
        return rect;
    }


    /**
     * 创建涂抹时，左上方的缩放图片(1个透明 无旋转的图片)
     *
     * @param imageObject 原始媒体
     * @param displayAsp  目标显示比例
     * @param beautyInfo  美颜信息
     */
    public void createFloatMedia(PEImageObject imageObject, float displayAsp, BeautyInfo beautyInfo) {
        VirtualImage virtualImage = new VirtualImage();
        ImageOb src = PEHelper.initImageOb(imageObject);
        PEImageObject peImageObject = imageObject.copy();
        ImageOb dst = src.copy();
        dst.setSegment(Segment.SEGMENT_PERSON);
        peImageObject.setTag(dst);

        PEScene scene = new PEScene(peImageObject);
        scene.setBackground(Color.TRANSPARENT);


        AnalyzerManager.getInstance().extraMedia(peImageObject, true);
        virtualImage.setPEScene(scene);
        applyFilter(peImageObject, beautyInfo);

        String path = PathUtils.getTempFileNameForSdcard("float_base", "png");
        ImageConfig config = new ImageConfig(720, (int) (720 / displayAsp), Color.TRANSPARENT);
        virtualImage.export(getApplication().getApplicationContext(), path, config, new ExportListener() {
            @Override
            public void onExportStart() {

            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int result, int extra, String info) {
                virtualImage.release();
                if (result >= VirtualImage.RESULT_SUCCESS) {
                    //缩放
                    mFloatMedia.postValue(path);
                } else {
                    mFloatMedia.postValue(null);
                }
            }
        });
    }
}
