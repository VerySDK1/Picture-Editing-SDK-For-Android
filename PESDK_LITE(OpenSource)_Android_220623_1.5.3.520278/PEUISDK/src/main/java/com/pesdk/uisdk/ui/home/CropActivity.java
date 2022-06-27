package com.pesdk.uisdk.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.base.BasePlayerActivity;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.crop.CropView;
import com.pesdk.uisdk.fragment.ProportionFragment;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.PlayerControl;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.ui.PreviewFrameLayout;
import com.vecore.models.FlipType;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

import static com.vecore.models.FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL;


/**
 * 图片裁切
 */
public class CropActivity extends BasePlayerActivity implements ProportionFragment.Callback {
    private CropView mCvCrop;
    private PreviewFrameLayout mPlayout;
    private PEImageObject mMedia;
    private ImageOb mImageOb;
    private RectF mRectVideoClipBound;
    private RectF mCurDefaultClipBound;
    private int mCurDefaultAngle;
    private float mCurDefaultAspect;
    @Crop.CropMode
    private int mCurDefaultCropMode;
    private FlipType mCurDefaultFlipType;


    @Crop.CropMode
    private int mCropMode;
    private PEImageObject mBackup;

    private ProportionFragment mProportionFragment;
    private static final String TAG = "CropActivity";

    /**
     * 画中画-编辑
     *
     * @param context
     * @param mediaObject 媒体
     * @param previewAsp  当前播放器的预览比例
     * @return
     */
    public static Intent createIntent(Context context, PEImageObject mediaObject, float previewAsp) {
        Intent intent = new Intent(context, CropActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE, mediaObject);
        intent.putExtra(IntentConstants.PARAM_PLAYER_ASP, previewAsp);
        return intent;
    }


    private float mPlayerAsp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesdk_activity_rotate_crop);
        mProportionFragment = (ProportionFragment) getSupportFragmentManager().findFragmentById(R.id.proportionFragment);
        Intent intent = getIntent();
        PEImageObject mediaObject = intent.getParcelableExtra(IntentConstants.PARAM_EDIT_IMAGE);
        mPlayerAsp = intent.getFloatExtra(IntentConstants.PARAM_PLAYER_ASP, 1f);
        if (null == mediaObject) {
            finish();
            return;
        }
        mMedia = mediaObject;

        mBackup = mMedia.copy();
        mMedia.setShowAngle((mBackup.getShowAngle() / 90) * 90);

        mImageOb = PEHelper.initImageOb(mMedia);
        Log.e(TAG, "onCreate: " + mImageOb);
        mCvCrop = $(R.id.cvVideoCrop);
        mCropMode = mImageOb.getCropMode();
        changeCropMode(mCropMode);
        initViews();
        initPlayer();
    }


    @Override
    public int getCropMode() {
        return mCropMode;
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(0, 0);
    }


    private void initViews() {
        mVirtualImageView = $(R.id.vvMediaPlayer);
        mPlayout = $(R.id.rlVideoCropFramePreview);
        //该控件使用硬件加速
        mCvCrop.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mCvCrop.setIcropListener(new CropView.ICropListener() {

            @Override
            public void onPlayState() {
            }

            @Override
            public void onMove() {
                if (!mProportionFragment.isResetEnable()) {
                    if (mCvCrop.getCrop().width() != mMedia.getWidth() || mCvCrop.getCrop().height() != mMedia.getHeight()) {
                        mProportionFragment.setResetClickable(true);
                    }
                }
            }
        });

        if (checkIsLandRotate()) {
            mCurDefaultAspect = (float) mMedia.getHeight() / mMedia.getWidth();
        } else {
            mCurDefaultAspect = (float) mMedia.getWidth() / mMedia.getHeight();
        }
        setPreviewAsp(mCurDefaultAspect);

    }

    private void setPreviewAsp(float asp) {
        mVirtualImageView.setPreviewAspectRatio(asp);
        mPlayout.setAspectRatio(asp);
    }

    private boolean checkIsLandRotate() {
        return mMedia.checkIsLandRotate();
    }


    @Override
    public void changeMode(@Crop.CropMode int mode) {
        mRectVideoClipBound.setEmpty();
        mCropMode = mode;
        changeCropMode(mCropMode);
        mProportionFragment.setResetClickable(true);
    }


    @Override
    public void cancel() {
        onBackPressed();
    }

    @Override
    public boolean enableResetAll() {
        return true;
    }


    @Override
    public void resetAll() {
        mCropMode = mCurDefaultCropMode;
        mRectVideoClipBound = new RectF(mCurDefaultClipBound);

        mMedia.setShowAngle(mCurDefaultAngle);
        mMedia.setFlipType(mCurDefaultFlipType);
        mMedia.setShowRectF(null);
        mMedia.setClipRect(null);
        mProportionFragment.setResetClickable(false);
        build();
    }

    @Override
    public void sure() {
        RectF crop = mCvCrop.getCrop();
        RectF rcCrop = null;
        int tmpW = mMedia.getWidth();
        int tmpH = mMedia.getHeight();
//        Log.e(TAG, "mMedia.getShowAngle()==>" + mMedia.getShowAngle() + " >" + mMedia.getFlipType() + " crop：" + crop);
        int showAngle = mMedia.getShowAngle();

        //判断镜像
        FlipType flipType = mMedia.getFlipType();
        if (flipType == FLIP_TYPE_VERTICAL_HORIZONTAL) {
            showAngle = (showAngle + 180) % 360;
        }

        if (showAngle == 90) {
            float left = tmpW - crop.bottom;
            float top = crop.left;
            rcCrop = new RectF(left, top, left + crop.height(), top + crop.width());
        } else if (showAngle == 180) {
            float left = tmpW - crop.right;
            float top = tmpH - crop.bottom;
            rcCrop = new RectF(left, top, left + crop.width(), top + crop.height());
        } else if (showAngle == 270) {
            float left = crop.top;
            float top = tmpH - crop.right;
            rcCrop = new RectF(left, top, left + crop.height(), top + crop.width());
        } else {
            rcCrop = new RectF(crop.left, crop.top, crop.right, crop.bottom);
        }
        mMedia.setClipRectF(rcCrop);
        RectF tmp = mBackup.getShowRectF();
        if (!rcCrop.isEmpty() && tmp != null && !(tmp.width() == tmp.height() && tmp.width() == 1f)) {
            int pW = 960;
            int pH = (int) (960 / mPlayerAsp);
            //有自定义显示位置 ，依据新的裁剪区域，重新计算显示位置
            float centerX = tmp.centerX() * pW;
            float twPX = (tmp.height() * pH) * (rcCrop.width() / rcCrop.height()); //保证中心点和显示高不变，重新算显示宽
            float left = centerX - (twPX / 2);
            tmp.left = left / (pW + 0.0f);
            tmp.right = (left + twPX) / (pW + 0.0f);
            mMedia.setShowRectF(tmp);
        } else {
            mMedia.setShowRectF(null);
        }
        Intent intent = new Intent();
        intent.putExtra(IntentConstants.PARAM_EDIT_RESULT_CLIPRECTF, mMedia.getClipRectF());
        intent.putExtra(IntentConstants.PARAM_EDIT_RESULT_CROPMODE, mCropMode);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, 0);
    }


    private PlayerControl.OnInfoListener mInfoListener = (what, extra, obj) -> {
        Log.i(TAG, "onInfo: " + what + " >" + extra);
        return false;
    };


    private final int DELAY_UPDATE = 0;

    private void initPlayer() {
        mVirtualImage = new VirtualImage();
        mVirtualImageView.setOnPlaybackListener(new VirtualImageView.VirtualViewListener() {
            @Override
            public void onPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                mPlayout.removeCallbacks(mUpdateCrop);
                changeCropMode(mCropMode);
                mPlayout.postDelayed(mUpdateCrop, DELAY_UPDATE); //显示裁剪组件
            }
        });
        RectF clipRectF = mMedia.getClipRectF();
        if (mMedia.getShowAngle() != 0) {
            int tmpW = mMedia.getWidth();
            int tmpH = mMedia.getHeight();
            RectF clip = new RectF();
            if (mMedia.getShowAngle() == 90) {
                float left = clipRectF.top;
                float top = tmpW - clipRectF.right;
                clip.set(left, top, left + clipRectF.height(), top + clipRectF.width());
            } else if (mMedia.getShowAngle() == 180) {
                float left = tmpW - clipRectF.right;
                float top = tmpH - clipRectF.bottom;
                clip.set(left, top, left + clipRectF.width(), top + clipRectF.height());
            } else if (mMedia.getShowAngle() == 270) {
                float left = tmpH - clipRectF.bottom;
                float top = clipRectF.left;
                clip.set(left, top, left + clipRectF.height(), top + clipRectF.width());
            } else {
                clip.set(clipRectF);
            }
            mRectVideoClipBound = new RectF(clip);
        } else {
            mRectVideoClipBound = new RectF(clipRectF);
        }
        mCurDefaultClipBound = new RectF(mRectVideoClipBound);
        mProportionFragment.setResetClickable(false);

        mCurDefaultAngle = mMedia.getShowAngle();
        mCurDefaultCropMode = mImageOb.getCropMode();

        mCurDefaultFlipType = mMedia.getFlipType();
        mMedia.setClipRect(null);
        mMedia.setShowRectF(null);
        build();
        mVirtualImageView.setOnInfoListener(mInfoListener);
    }

    /**
     * 加载媒体资源
     */
    @Override
    public void reload(VirtualImage virtualImage) {
        virtualImage.reset();
        PEScene scene = new PEScene(mMedia);
        virtualImage.setPEScene(scene);
    }

    private void changeCropMode(@Crop.CropMode int mode) {
        RectF videoBound = null;
        if (checkIsLandRotate()) {
            videoBound = new RectF(0, 0, mMedia.getHeight(), mMedia.getWidth());
        } else {
            videoBound = new RectF(0, 0, mMedia.getWidth(), mMedia.getHeight());
        }
        if (mRectVideoClipBound == null) {
            mRectVideoClipBound = new RectF(mMedia.getClipRectF());
        }
        if (mRectVideoClipBound.isEmpty()) {
            mRectVideoClipBound = new RectF(videoBound);
        }
        mCvCrop.initialize(mRectVideoClipBound, videoBound, 0);
        mImageOb.setCropMode(mode);
        if (mode == Crop.CROP_ORIGINAL) {// 原始
            mCvCrop.applyAspect(1, 1 / (videoBound.width() / (videoBound.height())));
        } else if (mode == Crop.CROP_1) {
            mCvCrop.applySquareAspect(); // 方格，1:1
        } else if (mode == Crop.CROP_169) {
            mCvCrop.applyAspect(1, 9f / 16);
        } else if (mode == Crop.CROP_916) {
            mCvCrop.applyAspect(1, 16 / 9.0f);
        } else if (mode == Crop.CROP_43) {
            mCvCrop.applyAspect(1, 3.0f / 4);
        } else if (mode == Crop.CROP_34) {
            mCvCrop.applyAspect(1, 4 / 3.0f);
        } else if (mode == Crop.CROP_45) {
            mCvCrop.applyAspect(1, 5 / 4.0f);
        } else if (mode == Crop.CROP_23) {
            mCvCrop.applyAspect(1, 3 / 2.0f);
        } else if (mode == Crop.CROP_32) {
            mCvCrop.applyAspect(1, 2 / 3.0f);
        } else if (mode == Crop.CROP_12) {
            mCvCrop.applyAspect(1, 2 / 1.0f);
        } else if (mode == Crop.CROP_21) {
            mCvCrop.applyAspect(1, 1 / 2.0f);
        } else if (mode == Crop.CROP_67) {
            mCvCrop.applyAspect(1, 7 / 6.0f);
        } else {
            mCvCrop.applyFreeAspect();
        }
        mHandler.postDelayed(() -> mProportionFragment.checkState(mode), 100);
    }


    //显示Crop组件
    private Runnable mUpdateCrop = new Runnable() {
        @Override
        public void run() {
            mCvCrop.setVisibility(View.VISIBLE);
            mCvCrop.setUnAbleBorder();
        }
    };

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: {

                }
                break;
                default: {
                }
                break;
            }
        }
    };

}
