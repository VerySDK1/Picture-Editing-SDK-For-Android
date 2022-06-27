package com.pesdk.uisdk.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.base.BasePlayerActivity;
import com.pesdk.uisdk.data.vm.DemarkVM;
import com.pesdk.uisdk.fragment.child.IRevokeListener;
import com.pesdk.uisdk.fragment.child.RevokeHandler;
import com.pesdk.uisdk.ui.home.erase.ErasePenFragment;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.uisdk.widget.doodle.DoodleView;
import com.pesdk.uisdk.widget.segment.FloatSegmentView;
import com.pesdk.widget.ZoomView;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.ui.PreviewFrameLayout;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.exception.InvalidStateException;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

/**
 * 去水印
 */
public class ErasePenActivity extends BasePlayerActivity implements ErasePenFragment.IDemark {
    private static final String TAG = "ErasePenActivity";
    private ErasePenFragment mErasePenFragment;
    private PEImageObject src;
    private PreviewFrameLayout mPreviewFrameLayout;
    private DoodleView mDoodleView;
    private List<PEImageObject> mRevokeList = null;
    private List<PEImageObject> undoList = null;
    private FloatSegmentView mFloatSegmentView;

    public static Intent createIntent(Context context, PEImageObject mediaObject) {
        Intent intent = new Intent(context, ErasePenActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE, mediaObject);
        return intent;
    }

    private PEImageObject mBuildMedia = null;
    private DemarkVM mDemarkModel;
    private boolean isFirst = false;
    private RevokeHandler mRevokeHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesdk_activity_dewatermark_layout);
        src = getIntent().getParcelableExtra(IntentConstants.PARAM_EDIT_IMAGE);


        initView();
        mRevokeList = new ArrayList<>();
        undoList = new ArrayList<>();
        isFirst = true;
        mVirtualImageView.setOnPlaybackListener(new VirtualImageView.VirtualViewListener() {
            @Override
            public void onPrepared() {
                if (isFirst) {
                    isFirst = false;
                    mPreviewFrameLayout.postDelayed(() -> {
                        mPreviewFrameLayout.setAspectRatio((float) mVirtualImageView.getPreviewWidth() / mVirtualImageView.getPreviewHeight());
                        mDoodleView.setVisibility(View.VISIBLE);
                    }, 150);
                }
            }
        });
        mFloatSegmentView = $(R.id.floatSegmentView);
        mDemarkModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(DemarkVM.class);
        mDemarkModel.getMaskLiveData().observe(this, this::handleMask);
        mDemarkModel.getBuildPath().observe(this, this::handlePath);
        mDemarkModel.initBase(src.getMediaPath());   //若图片太大需要压缩, 防止消除时buffer造成OOM


        mVirtualImage = new VirtualImage();

        ViewGroup view = findViewById(R.id.childRevokeLayout);
        view.setVisibility(View.VISIBLE);
        mRevokeHandler = new RevokeHandler(view, true, true, new IRevokeListener() {
            @Override
            public void onRevoke() {
                revoke();
                checkUIStatus();
            }

            @Override
            public void onUndo() {
                undo();
                checkUIStatus();
            }

            @Override
            public void onReset() {
                reset();
                checkUIStatus();
            }

            @Override
            public void onDiffBegin() {
                diffBegin();
            }

            @Override
            public void onDiffEnd() {
                diffEnd();
            }
        });

        mZoomView = $(R.id.zoomLayout);
        mZoomView.setListener(new ZoomView.OnFlowTouchListener() {

            RectF tempRectF = new RectF();
            Matrix matrix = new Matrix();


            @Override
            public void onMove(float x, float y) {
                tempRectF.set(mBuildMedia.getShowRectF());
                tempRectF.offset(x, y);
                restrictedArea(mBuildMedia, true);
            }

            /**
             * 矫正区域
             */
            private void restrictedArea(PEImageObject media, boolean refresh) {
                //区间
                if (tempRectF.left > 0) {
                    tempRectF.offset(-tempRectF.left, 0f);
                } else if (tempRectF.right < 1) {
                    tempRectF.offset(1 - tempRectF.right, 0f);
                }
                if (tempRectF.top > 0) {
                    tempRectF.offset(0f, -tempRectF.top);
                } else if (tempRectF.bottom < 1) {
                    tempRectF.offset(0f, 1 - tempRectF.bottom);
                }
                media.setShowRectF(tempRectF);
                if (refresh) {
                    media.refresh();
                }
            }

            @Override
            public void onZoom(float zoom, float x, float y) {
                int w = mPreviewFrameLayout.getWidth();
                int h = mPreviewFrameLayout.getHeight();

                RectF oldRectF = mBuildMedia.getShowRectF();

                tempRectF.set(
                        oldRectF.left * w,
                        oldRectF.top * h,
                        oldRectF.right * w,
                        oldRectF.bottom * h
                );
                matrix.reset();
                matrix.postScale(
                        zoom,
                        zoom,
                        x * w,
                        y * h
                );
                matrix.mapRect(tempRectF, tempRectF);
                tempRectF.set(
                        tempRectF.left / w,
                        tempRectF.top / h,
                        tempRectF.right / w,
                        tempRectF.bottom / h
                );

                int scaleMax = 5;
                int scaleMin = 1;
                //限制大小
                if (tempRectF.width() > scaleMax) {
                    float scale = scaleMax / tempRectF.width();
                    matrix.reset();
                    matrix.postScale(
                            scale,
                            scale,
                            tempRectF.centerX(),
                            tempRectF.centerY()
                    );
                    matrix.mapRect(tempRectF, tempRectF);
                }
                if (tempRectF.height() > scaleMax) {
                    float scale = scaleMax / tempRectF.height();
                    matrix.reset();
                    matrix.postScale(
                            scale,
                            scale,
                            tempRectF.centerX(),
                            tempRectF.centerY()
                    );
                    matrix.mapRect(tempRectF, tempRectF);
                }
                if (tempRectF.width() < scaleMin) {
                    float scale = scaleMin / tempRectF.width();
                    matrix.reset();
                    matrix.postScale(
                            scale,
                            scale,
                            tempRectF.centerX(),
                            tempRectF.centerY()
                    );
                    matrix.mapRect(tempRectF, tempRectF);
                }
                if (tempRectF.height() < scaleMin) {
                    float scale = scaleMin / tempRectF.height();
                    matrix.reset();
                    matrix.postScale(
                            scale,
                            scale,
                            tempRectF.centerX(),
                            tempRectF.centerY()
                    );
                    matrix.mapRect(tempRectF, tempRectF);
                }
                restrictedArea(mBuildMedia, false);
            }
        });

    }

    private String mBasePath;

    private void handlePath(String path) {
        try {
            mBasePath = path;
            mBuildMedia = new PEImageObject(path);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        mBuildMedia.setShowRectF(new RectF(0, 0, 1f, 1f));
        mRevokeList.add(mBuildMedia);
        mVirtualImageView.post(() -> reload());
    }

    private ZoomView mZoomView;

    private final int LIMIT = 1;

    private void checkUIStatus() {
        mRevokeHandler.setRevokeEnable(revokeEnable());
        mRevokeHandler.setUndoEnable(undoList.size() > 0);
        boolean resetEanable = (mRevokeList.size() + undoList.size()) > LIMIT;
        mRevokeHandler.setResetEnable(resetEanable);
        mRevokeHandler.setDiffEnable(resetEanable);
    }


    private void initView() {
        mVirtualImageView = $(R.id.virtualImageView);
        mPreviewFrameLayout = $(R.id.contentViewLayout);
        mDoodleView = $(R.id.doodleView);
        mErasePenFragment = (ErasePenFragment) getSupportFragmentManager().findFragmentById(R.id.demarkFragment);
        mErasePenFragment.setDoodleView(mDoodleView);
        mErasePenFragment.setCallback(new ErasePenFragment.Callback() {

            @Override
            public void startTouch() {
                mFloatSegmentView.setVisibility(View.VISIBLE);
                drawFrame();
            }

            private void drawFrame() {
                Bitmap mask = mDoodleView.getBitmap();
                Bitmap bitmap = mDemarkModel.getLiveBaseBmp().getValue();
                int w = bitmap.getWidth(), h = bitmap.getHeight();
                RectF tmp = mBuildMedia.getShowRectF();
//                Log.e(TAG, "drawFrame: " + tmp + " " + mBuildMedia.getClipRect());

                float fw = tmp.width(), fh = tmp.height();

                int l = (int) (w * Math.abs(tmp.left) / fw);
                int t = (int) (h * Math.abs(tmp.top) / fh);


                int r = (int) (w * (1 - tmp.left) / fw);
                int b = (int) (h * (1 - tmp.top) / fh);

                Rect clip = new Rect(l, t, r, b);

                //基于播放器clip,二次裁剪，最小化创建bitmap

                {

                    int pw = mDoodleView.getWidth(), ph = mDoodleView.getHeight();
                    Rect zoomClip = mDoodleView.getClipRect();
                    RectF zoomClipF = new RectF(zoomClip.left * 1f / pw, zoomClip.top * 1f / ph, zoomClip.right * 1f / pw, zoomClip.bottom * 1f / ph);


                    Rect clip2 = new Rect();
                    clip2.left = (int) (clip.left + (clip.width() * zoomClipF.left));
                    clip2.top = (int) (clip.top + (clip.height() * zoomClipF.top));

                    clip2.right = (int) (clip.left + (clip.width() * zoomClipF.right));
                    clip2.bottom = (int) (clip.top + (clip.height() * zoomClipF.bottom));


                    int bmpW = clip2.width(), bmpH = clip2.height();
                    Rect rect = new Rect(0, 0, bmpW, bmpH);
                    Bitmap dst = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888); //最小化的bitmap
                    Canvas cv = new Canvas(dst);
                    cv.drawBitmap(bitmap, clip2, rect, null);
                    cv.drawBitmap(mask, zoomClip, rect, null);

//                    Log.e(TAG, "drawFrame: " + w + "*" + h + " " + zoomClipF + " clip2:" + clip2 + " clip:" + clip + " showRectF:" + tmp + "bmp:" + dst.getWidth() + "*" + dst.getHeight() + " " + rect);
                    mFloatSegmentView.setBitmap(dst, rect);
                }
            }

            @Override
            public void moveTouch() {
                drawFrame();
            }

            @Override
            public void endTouch() {
                mFloatSegmentView.setVisibility(View.GONE);
                mFloatSegmentView.recycle();
            }
        });
    }


    /**
     * 生成去水印图片成功
     */
    private void handleMask(PEImageObject mask) {
        SysAlertDialog.cancelLoadingDialog();
        if (null == mask) {
            onToast(R.string.pesdk_save_error);
        } else {
//            ivGo.setVisibility(View.GONE);
            mBuildMedia = mask;
            mRevokeList.add(mBuildMedia);
            if (bSave) {
                success();
            } else {
                reload();
            }
            checkUIStatus();
        }
    }

    @Override
    public void onCancel() {
        onBackPressed();
    }

    private boolean bSave = false;

    @Override
    public void onSure() {
        if (mDoodleView.isEmpty()) {
            success();
        } else {
            bSave = true;
            doMask();
            preMask();
        }
    }

    @Override
    public void preMask() {
//        ivGo.setVisibility(View.VISIBLE);
        doMask();
    }

    @Override
    public void doMask() {
        SysAlertDialog.showLoadingDialog(this, R.string.pesdk_process).setCancelable(false);
        Bitmap bitmap = mDoodleView.getBitmap();
        mDoodleView.reset();
        mDoodleView.postInvalidate();

        RectF rectF = mBuildMedia.getShowRectF();
        if (!rectF.isEmpty()) {
            //主图有放大，需要把bitmap缩放
            int w = mBuildMedia.getWidth();
            int h = mBuildMedia.getHeight();
            Bitmap tmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(tmp);
            Rect dst = new Rect();
            float a = rectF.width();
            float b = rectF.height();
            dst.left = (int) (Math.abs(rectF.left) / a * w);
            dst.top = (int) (Math.abs(rectF.top) / b * h);
            dst.right = (int) (dst.left + (w * 1 / a));
            dst.bottom = (int) (dst.top + (h * 1 / b));

//            Log.e(TAG, "doMask: " + rectF + " " + dst + " " + w + "*" + h);
            canvas.drawBitmap(bitmap, null, dst, null);
            mDemarkModel.processMask(mBuildMedia, tmp);
            bitmap.recycle();
        } else {
            mDemarkModel.processMask(mBuildMedia, bitmap);
        }
    }


    private void revoke() {
        if (revokeEnable()) {
            undoList.add(mRevokeList.remove(mRevokeList.size() - 1));
            mBuildMedia = mRevokeList.get(mRevokeList.size() - 1);
            reload();
        }
    }

    private void undo() {
        if (undoList.size() > 0) {
            mRevokeList.add(undoList.remove(undoList.size() - 1));
            mBuildMedia = mRevokeList.get(mRevokeList.size() - 1);
            reload();
        }
    }

    private void reset() {
        mBuildMedia = mRevokeList.get(0);
        mRevokeList.clear();
        undoList.clear();
        mRevokeList.add(mBuildMedia);
        reload();
    }

    private void diffBegin() {
        PEImageObject tmp = mRevokeList.get(0);
        PEImageObject cp = tmp.copy();
        cp.setShowRectF(mBuildMedia.getShowRectF());
        mBuildMedia = cp;
        reload();
    }

    private void diffEnd() {
        mBuildMedia = mRevokeList.get(mRevokeList.size() - 1);
        reload();
    }


    private boolean revokeEnable() {
        return mRevokeList.size() > LIMIT;
    }


    private void reload() {
        reload(mVirtualImage);
    }

    @Override
    public void reload(VirtualImage virtualImage) {
        mVirtualImage.reset();
        mVirtualImageView.reset();
        PEScene scene = new PEScene(mBuildMedia);
        mVirtualImage.setPEScene(scene);
        try {
            mVirtualImage.build(mVirtualImageView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private void success() {
        PEImageObject dst = null;
        float scaleX = mBuildMedia.getWidth() * 1f / src.getWidth();
        if (scaleX != 1 && !TextUtils.equals(mBasePath, mBuildMedia.getMediaPath())) {  //有压缩且有消除操作
            try {//重新计算裁剪区域
                dst = new PEImageObject(mBuildMedia.getMediaPath());
                RectF clip = new RectF(src.getClipRect()); //重新设置裁剪,图片可能被压缩了
                if (!clip.isEmpty()) {
                    RectF zoomClip = new RectF(clip.left * scaleX, clip.top * scaleX, clip.right * scaleX, clip.bottom * scaleX);
                    Rect tmp = new Rect();
                    tmp.left = (int) Math.max(0, zoomClip.left);
                    tmp.top = (int) Math.max(0, zoomClip.top);
                    tmp.right = (int) Math.min(mBuildMedia.getWidth(), Math.ceil(zoomClip.right));
                    tmp.bottom = (int) Math.min(mBuildMedia.getHeight(), Math.ceil(zoomClip.bottom));
                    dst.setClipRect(tmp);
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else { //原图
            try {
                dst = new PEImageObject(src.getMediaPath());
                dst.setClipRect(src.getClipRect());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }

        //拷贝之前的所有参数
        try {
            dst.changeFilterList(src.getFilterList());
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        dst.setFlipType(src.getFlipType());
        dst.setMaskObject(src.getMaskObject());
        dst.setShowAngle(src.getShowAngle());
        dst.setAngle(src.getAngle());
        dst.setShowRectF(src.getShowRectF());
        dst.setTag(src.getTag());
        Intent intent = new Intent();
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE, dst);
        setResult(RESULT_OK, intent);
        finish();
    }
}
