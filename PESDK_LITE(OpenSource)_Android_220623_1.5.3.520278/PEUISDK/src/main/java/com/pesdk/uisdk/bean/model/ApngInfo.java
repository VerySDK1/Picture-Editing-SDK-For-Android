package com.pesdk.uisdk.bean.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.util.LruCache;

import com.pesdk.uisdk.util.ApngExtractFrames;
import com.vecore.base.lib.utils.BitmapUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.com.hjg.pngj.PngReaderApng;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkACTL;
import ar.com.hjg.pngj.chunks.PngChunkFCTL;


/**
 * 单个apng资源
 * 原始的apng解析成png 之后，单个图片的大小不一致（单个图片并不是代表单个帧画面，需要依赖baseBitmap），需转换成 一致的 (依赖basebitmap从新生成新的 帧画面)
 */
public class ApngInfo {
    private String TAG = "ApngInfo";
    private File sourceUri;
    private static final float DELAY_FACTOR = 1000F;
    private ArrayList<PngChunkFCTL> fctlArrayList = new ArrayList<>();
    private Bitmap baseBitmap;
    private Paint paint;
    private String workingPath;
    private int baseWidth;
    private int baseHeight;
    private int numFrames;
    private int numPlays;

    public int getBaseWidth() {
        return baseWidth;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public int getNumFrames() {
        return numFrames;
    }

    /**
     * 单帧画面的持续时间
     */
    private float itemDuration = 0.1f;
    /**
     * 总的持续时间
     */
    private float duration = 1f;

    public float getItemDuration() {
        return itemDuration;
    }

    public float getDuration() {
        return duration;
    }


    public List<String> getFrameList() {
        return frameList;
    }


    @Override
    public String toString() {
        return "ApngInfo{" +
                "itemDuration=" + itemDuration +
                ", duration=" + duration +
                ", workingPath='" + workingPath + '\'' +
                ", baseWidth=" + baseWidth +
                ", baseHeight=" + baseHeight +
                ", numFrames=" + numFrames +
                ", numPlays=" + numPlays +
                ", baseFile=" + baseFile +
                ", frameList=" + frameList +
                ", sourceUri=" + sourceUri +
                '}';
    }

    private float mScaling;
    private File baseFile;

    /**
     * 新的帧画面的图片
     */
    private List<String> frameList = null;
    private LruCache<String, Bitmap> mMemoryCache;
    //分配缓存内存5
    private final int MAX_CACHE_SIZE = 1024 * 1024 * 5;

    /**
     * 构造缓存器
     */
    private void initCache() {
        mMemoryCache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {

            // 必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (null != value && !value.isRecycled()) {
                    return value.getByteCount();
                }
                return 0;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if (null != oldValue) {
                    synchronized (oldValue) {
                        oldValue.recycle();
                    }
                }
            }
        };
    }

    private String mBaseFileName;

    ApngInfo(File apngFile, String baseFileName) {
        frameList = new ArrayList<>();
        mBaseFileName = baseFileName;
        mScaling = 0F;
        paint = new Paint();
        paint.setAntiAlias(true);
        workingPath = apngFile.getParent();

        prepare(apngFile);
        parseFrame();
        recycle();
        fctlArrayList.clear();
    }

    /**
     * 解析所有的帧效果，并保存成文件
     */
    private void parseFrame() {
        boolean encodeBmp = false;
        int len = fctlArrayList.size();  //是否需要每帧导出bitmap再保存
        for (int i = 0; i < len; i++) {
            //每帧画面
            PngChunkFCTL pngChunk = fctlArrayList.get(i);
            if (i == 0) {
                int delayNum = pngChunk.getDelayNum();
                int delayDen = pngChunk.getDelayDen();
                int delay = Math.round(delayNum * DELAY_FACTOR / delayDen);
                itemDuration = delay / 1000.0f;
                duration = itemDuration * len;
            }
            //参照 普通贴纸的序列规则，用来循环
            String path = new File(workingPath, mBaseFileName + i + ".png").getAbsolutePath();
            if (i == 0 && !com.vecore.base.lib.utils.FileUtils.isExist(path)) {
                encodeBmp = true;
                initCache();
            }
            if (encodeBmp) { //每次都导出png太耗时，只有当下载完成时，第一次需要导出成图片；
                Bitmap frameBmp = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(frameBmp);
                if (i == 0) { //基础图片
                    drawBaseBitmap(canvas);
                } else {
                    drawAnimateBitmap(canvas, i);
                }
                //保存当前帧的完整画面
                try {
                    BitmapUtils.saveBitmapToFile(frameBmp, true, 100, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                frameBmp.recycle();
            }
            frameList.add(path);
        }
    }

    /**
     * @param apngFile     原始的apng文件
     * @param baseFileName 帧图片序列的基础名字
     */
    public static ApngInfo createApng(File apngFile, String baseFileName) {
        return new ApngInfo(apngFile, baseFileName);
    }


    private void readApngInformation(File baseFile) {
        PngReaderApng reader = new PngReaderApng(baseFile);
        reader.end();
        List<PngChunk> pngChunks = reader.getChunksList().getChunks();
        PngChunk chunk;
        for (int i = 0; i < pngChunks.size(); i++) {
            chunk = pngChunks.get(i);
            if (chunk instanceof PngChunkACTL) {
                numFrames = ((PngChunkACTL) chunk).getNumFrames();
                if (numPlays > 0) {
                } else {
                    numPlays = ((PngChunkACTL) chunk).getNumPlays();
                }
            } else if (chunk instanceof PngChunkFCTL) {
                fctlArrayList.add((PngChunkFCTL) chunk);
            }
        }
    }

    private void drawBaseBitmap(Canvas canvas) {
        if (mScaling == 0F) {
            float scalingByWidth = ((float) canvas.getWidth()) / baseWidth;
            float scalingByHeight = ((float) canvas.getHeight()) / baseHeight;
            mScaling = scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
        }
        RectF dst = new RectF(0, 0, mScaling * baseWidth, mScaling * baseHeight);
        canvas.drawBitmap(baseBitmap, null, dst, paint);
        cacheBitmap(0, baseBitmap);
    }

    private void drawAnimateBitmap(Canvas canvas, int frameIndex) {
        Bitmap bitmap = getCacheBitmap(frameIndex);
        if (bitmap == null) {
            bitmap = createAnimateBitmap(frameIndex);
            cacheBitmap(frameIndex, bitmap);
        }
        if (bitmap == null) return;

        RectF dst = new RectF(0, 0, mScaling * bitmap.getWidth(), mScaling * bitmap.getHeight());
        canvas.drawBitmap(bitmap, null, dst, paint);
    }

    private Bitmap createAnimateBitmap(int frameIndex) {
        Bitmap bitmap = null;
        PngChunkFCTL previousChunk = frameIndex > 0 ? fctlArrayList.get(frameIndex - 1) : null;
        if (previousChunk != null) {
            bitmap = handleDisposeOperation(frameIndex, baseFile, previousChunk);
        }
        String path = new File(workingPath, ApngExtractFrames.getFileName(baseFile, frameIndex)).getPath();
        Bitmap frameBitmap = loadImageSync(path);
        PngChunkFCTL chunk = fctlArrayList.get(frameIndex);
        byte blendOp = chunk.getBlendOp();
        int offsetX = chunk.getxOff();
        int offsetY = chunk.getyOff();
        Bitmap tmp = handleBlendingOperation(offsetX, offsetY, blendOp, frameBitmap, bitmap);
        if (tmp != frameBitmap && tmp != null) {
            frameBitmap.recycle();
        }
        return tmp;
    }

    public ArrayList<PngChunkFCTL> getFctlArrayList() {
        return fctlArrayList;
    }

    private Bitmap handleDisposeOperation(int frameIndex, File baseFile, PngChunkFCTL previousChunk) {
        Bitmap bitmap = null;

        byte disposeOp = previousChunk.getDisposeOp();
        int offsetX = previousChunk.getxOff();
        int offsetY = previousChunk.getyOff();

        Canvas tempCanvas;
        Bitmap frameBitmap = null;
        Bitmap tempBitmap;
        String tempPath;

        switch (disposeOp) {
            case PngChunkFCTL.APNG_DISPOSE_OP_NONE:
                // Get bitmap from the previous frame
                bitmap = frameIndex > 0 ? getCacheBitmap(frameIndex - 1) : null;
                break;

            case PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND:
                // Get bitmap from the previous frame but the drawing region is needed to be cleared
                bitmap = frameIndex > 0 ? getCacheBitmap(frameIndex - 1) : null;
                if (bitmap == null) break;

                tempPath = new File(workingPath, ApngExtractFrames.getFileName(baseFile, frameIndex - 1)).getPath();
                frameBitmap = loadImageSync(tempPath);
                tempBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
                tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(bitmap, 0, 0, null);

                tempCanvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
                tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                tempCanvas.clipRect(0, 0, baseWidth, baseHeight);

                bitmap = tempBitmap;
                break;

            case PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS:
                if (frameIndex > 1) {
                    PngChunkFCTL tempPngChunk;

                    for (int i = frameIndex - 2; i >= 0; i--) {
                        tempPngChunk = fctlArrayList.get(i);
                        int tempDisposeOp = tempPngChunk.getDisposeOp();
                        int tempOffsetX = tempPngChunk.getxOff();
                        int tempOffsetY = tempPngChunk.getyOff();

                        tempPath = new File(workingPath, ApngExtractFrames.getFileName(baseFile, i)).getPath();
                        frameBitmap = loadImageSync(tempPath);

                        if (tempDisposeOp != PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS) {

                            if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_NONE) {
                                bitmap = getCacheBitmap(i);
                                if (bitmap == null) {
                                    Log.e(TAG, "handleDisposeOperation:  Can't retrieve previous APNG_DISPOSE_OP_NONE frame: please try to increase memory cache size!");
                                }

                            } else if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND) {
                                tempBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
                                tempCanvas = new Canvas(tempBitmap);
                                tempCanvas.drawBitmap(getCacheBitmap(i), 0, 0, null);

                                tempCanvas.clipRect(tempOffsetX, tempOffsetY, tempOffsetX + frameBitmap.getWidth(), tempOffsetY + frameBitmap.getHeight());
                                tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                tempCanvas.clipRect(0, 0, baseWidth, baseHeight);

                                bitmap = tempBitmap;
                            }
                            break;
                        }
                    }
                }
                break;
        }
        if (null != frameBitmap) {
            frameBitmap.recycle();
        }
        return bitmap;
    }


    private void prepare(File sourceUri) {
        this.sourceUri = sourceUri;
        String imagePath = getImagePathFromUri();
        baseFile = new File(imagePath);
//        int re = ApngExtractFrames.process(baseFile);
//        Log.e(TAG, "build: " + baseFile + ">" + re);

        readApngInformation(baseFile);
        String path = new File(workingPath, ApngExtractFrames.getFileName(baseFile, 0)).getPath();
        baseBitmap = loadImageSync(path);   //基础数据不能放到缓存中
        baseWidth = baseBitmap.getWidth();
        baseHeight = baseBitmap.getHeight();
    }


    private String getImagePathFromUri() {
        if (sourceUri == null) return null;
        String imagePath = null;
        try {
            String filename = sourceUri.getName();
            File file = new File(workingPath, filename);
            if (!file.exists()) {
                FileUtils.copyFile(new File(sourceUri.getPath()), file);
            }
            imagePath = file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imagePath;
    }

    /**
     * Process Blending operation, and handle a final draw for this frame
     */
    private Bitmap handleBlendingOperation(int offsetX, int offsetY, byte blendOp, Bitmap frameBitmap, Bitmap baseBitmap) {
        Bitmap redrawnBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(redrawnBitmap);
        if (baseBitmap != null) {
            canvas.drawBitmap(baseBitmap, 0, 0, null);
            if (blendOp == PngChunkFCTL.APNG_BLEND_OP_SOURCE) {
                canvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.clipRect(0, 0, baseWidth, baseHeight);
            }
        }
        canvas.drawBitmap(frameBitmap, offsetX, offsetY, null);
        return redrawnBitmap;
    }

    private String getCacheKey(int frameIndex) {
        return String.format("%s-base-%s", sourceUri.toString(), frameIndex);
    }

    private void cacheBitmap(int frameIndex, Bitmap bitmap) {
        if (bitmap == null) return;
        mMemoryCache.put(getCacheKey(frameIndex), bitmap);
    }

    private Bitmap getCacheBitmap(int frameIndex) {
        return mMemoryCache.get(getCacheKey(frameIndex));
    }

    private final Object bitmapHashLock = new Object();

    private void recycle() {
        if (null != mMemoryCache) {
            synchronized (bitmapHashLock) {
                mMemoryCache.evictAll();
            }
        }
        if (null != baseBitmap) {
            baseBitmap.recycle();
            baseBitmap = null;
        }
    }

    private Bitmap loadImageSync(String pngPath) {
        return BitmapFactory.decodeFile(pngPath);
    }

}
