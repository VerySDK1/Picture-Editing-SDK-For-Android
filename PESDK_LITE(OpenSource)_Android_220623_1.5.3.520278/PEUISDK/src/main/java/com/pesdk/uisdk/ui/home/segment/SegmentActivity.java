package com.pesdk.uisdk.ui.home.segment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.SeekBar;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.analyzer.ExtraPreviewFrameListener;
import com.pesdk.uisdk.base.BasePlayerActivity;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.fragment.helper.PaintHandler;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.util.helper.PersonSegmentHelper;
import com.pesdk.uisdk.widget.segment.FloatSegmentView;
import com.pesdk.uisdk.widget.segment.SegmentView;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.ui.PreviewFrameLayout;
import com.vecore.exception.InvalidStateException;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * 抠图
 */
public class SegmentActivity extends BasePlayerActivity {
    private static final String TAG = "SegmentActivity";

    /**
     *
     */
    public static Intent createIntent(Context context, PEImageObject peImageObject, boolean isPip) {
        Intent intent = new Intent(context, SegmentActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE, peImageObject);
        intent.putExtra(IntentConstants.PARAM_IMAGE_IS_LAYER, isPip);
        return intent;
    }

    /**
     * 场景
     */
    private PEImageObject mPEImageObject;

    /**
     * 播放器
     */
    private VirtualImage mVirtualImage;
    private VirtualImageView mVirtualImageView;
    private PreviewFrameLayout mPreviewFrameLayout;
    private View mDiff;
    private ImageOb bkOb;
    private SegmentView mSegmentView;
    private PaintHandler mPaintHandler;
    private CheckedTextView cbRubber, cbFast;
    private View tvReset;
    private FloatSegmentView mFloatSegmentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesdk_activity_segment);
        mPEImageObject = getIntent().getParcelableExtra(IntentConstants.PARAM_EDIT_IMAGE);
        if (null == mPEImageObject) {
            finish();
            return;
        }

        mRedoList.clear();

        ImageOb ob = PEHelper.initImageOb(mPEImageObject);
        bkOb = ob.copy();
        initView();

        ob.setSegment(Segment.NONE);  //清理之前的抠图
        ob.setMaskPath(null);
        mPEImageObject.setShowRectF(null); //兼容Layer位置
        mPEImageObject.setShowAngle(0);
        mPEImageObject.setClipRectF(null);
        mVirtualImage = new VirtualImage();
        isFirst = true;
        reload(mVirtualImage);
    }


    @Override
    public void reload(VirtualImage virtualImage) {
        mVirtualImage.reset();
        mVirtualImageView.enableViewBGHolder(true);
        try {
            PEScene scene = new PEScene(mPEImageObject);
            AnalyzerManager.getInstance().extraMaskMedia(mPEImageObject, false);
            mVirtualImage.setPEScene(scene);
            mVirtualImage.build(mVirtualImageView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private boolean isFirst = false;
    private View mUndoLayout;
    private View mRevoke, mUndo;

    /**
     * 初始化控件
     */
    private void initView() {
        //顶部菜单
        $(R.id.btnLeft).setOnClickListener(v -> onBackPressed());
        $(R.id.btnRight).setOnClickListener(v -> save());
        mDiff = $(R.id.btnChildDiff);
        mDiff.setVisibility(View.VISIBLE);
        mRevoke = $(R.id.btnChildRevoke);
        mUndo = $(R.id.btnChildUndo);
        mUndoLayout = $(R.id.childRevokeLayout);
        cbRubber = $(R.id.btn_rubber);
        cbFast = $(R.id.btn_segment_fast);
        tvReset = $(R.id.btn_reset);
        //播放
        mPreviewFrameLayout = $(R.id.preview);
        mVirtualImageView = $(R.id.beauty_video);
        mVirtualImageView.setOnPlaybackListener(new VirtualImageView.VirtualViewListener() {
            @Override
            public void onPrepared() {
                mPreviewFrameLayout.setAspectRatio(mVirtualImageView.getPreviewWidth() * 1.0f / mVirtualImageView.getPreviewHeight());
                if (isFirst) {
                    isFirst = false;
                    mHandler.postDelayed(() -> doLastMask(), 200);
                }
            }
        });
        mFloatSegmentView = $(R.id.floatSegmentView);
        mSegmentView = $(R.id.doodleView);
        mSegmentView.setCallback(new SegmentView.Callback() {


            @Override
            public void startTouch(Bitmap bitmap, Rect rect) {
                mFloatSegmentView.setVisibility(View.VISIBLE);
                mFloatSegmentView.setBitmap(bitmap, rect);
            }

            @Override
            public void moveTouch(Bitmap bitmap, Rect rect) {
                mFloatSegmentView.setBitmap(bitmap, rect);
            }

            @Override
            public void endTouch(boolean hasMask) {
                mSegmentView.postInvalidate();
                mFloatSegmentView.setVisibility(View.GONE);
                mFloatSegmentView.recycle();
                mRedoList.clear();
                checkUIStatus();
            }
        });
        mSegmentView.setBaseMedia(mPEImageObject.getMediaPath());
        mDiff.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onDiffBegin();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                onDiffEnd();
            }
            return false;
        });

        mRevoke.setOnClickListener(v -> onRevoke());
        mUndo.setOnClickListener(v -> onUndo());

        SeekBar seekBar = $(R.id.sbStrokeWdith);
        mPaintHandler = new PaintHandler(seekBar, new PaintHandler.Callback() {
            @Override
            public void onStartTrackingTouch() {
                mSegmentView.beginPaintSizeMode();
            }

            @Override
            public void onProgressChanged(float value) {
                mSegmentView.setPaintWidth(value);
            }

            @Override
            public void onStopTrackingTouch(float value) {
                mSegmentView.setPaintWidth(value);
                mSegmentView.endPaintSizeMode();
            }
        });
        mPaintHandler.init();
    }

    private List<SegmentView.DrawPathBean> mRedoList = new ArrayList<>();

    private void checkUIStatus() {
        List<SegmentView.DrawPathBean> tmp = mSegmentView.getRevokeList();
        mRevoke.setEnabled(tmp.size() > 0);
        mUndo.setEnabled(mRedoList.size() > 0);
        mDiff.setEnabled(tmp.size() > 0);
        tvReset.setEnabled(tmp.size() > 0);
        if (tmp.size() > 0 || mRedoList.size() > 0) {
            mUndoLayout.setVisibility(View.VISIBLE);
        }
    }


    private void onRevoke() {
        List<SegmentView.DrawPathBean> list = mSegmentView.getRevokeList();
        if (list.size() > 0) {
            SegmentView.DrawPathBean tmp = list.remove(list.size() - 1);
            mRedoList.add(tmp);
        }
        mSegmentView.invalidate();
        checkUIStatus();
    }

    private void onUndo() {
        if (mRedoList.size() > 0) {
            SegmentView.DrawPathBean tmp = mRedoList.remove(mRedoList.size() - 1);
            mSegmentView.getRevokeList().add(tmp);
        }
        mSegmentView.invalidate();
        checkUIStatus();
    }

    private void doLastMask() {
        String tmp = bkOb.getMaskPath();
        if (!TextUtils.isEmpty(tmp)) {
            mSegmentView.setMask(BitmapFactory.decodeFile(tmp));
        }
        checkUIStatus();
    }


    public void onSegment(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_auto) {
            //基于原始图识别
            showLoading(getString(R.string.pesdk_segment_ing));
            onAutoSegment();
        } else if (viewId == R.id.btn_reset) {
            mSegmentView.reset();
            mRedoList.clear();
            cbFast.setChecked(false);
            cbRubber.setChecked(false);
            mSegmentView.enableRubber(false);
            checkUIStatus();
        } else {
            cbFast.setChecked(false);
            cbRubber.setChecked(false);
            if (viewId == R.id.btn_segment_fast) {
                mSegmentView.enableRubber(false);
                cbFast.setChecked(true);
            } else if (viewId == R.id.btn_rubber) {
                mSegmentView.enableRubber(true);
                cbRubber.setChecked(true);
            }
        }
    }

    /**
     * 自动抠图
     */
    private void onAutoSegment() {
        PersonSegmentHelper helper = new PersonSegmentHelper();
        helper.process(mPEImageObject, bitmap -> mHandler.obtainMessage(MSG_PERSON_RESULT, bitmap).sendToTarget());
    }

    private final int MSG_PERSON_RESULT = 100;

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PERSON_RESULT: {
                    hideLoading();
                    Bitmap tmp = (Bitmap) msg.obj;
                    mSegmentView.postDelayed(() -> {
                        mSegmentView.setMask(tmp);
                        mSegmentView.postDelayed(() -> checkUIStatus(), 100);
                    }, 200);
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    private void onDiffEnd() {
        ImageOb imageOb = PEHelper.initImageOb(mPEImageObject);
        imageOb.setSegment(Segment.NONE);
        reload(mVirtualImage);
        mSegmentView.setVisibility(View.VISIBLE);
    }

    private void onDiffBegin() {
        String path = doMask();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        reload(mVirtualImage);
        mSegmentView.setVisibility(View.GONE);
    }

    private String doMask() {
        Bitmap bitmap = mSegmentView.save();
        if (null != bitmap) {
            ImageOb imageOb = PEHelper.initImageOb(mPEImageObject);
            ExtraPreviewFrameListener.bindMask(bitmap, imageOb);
            imageOb.setSegment(Segment.SEGMENT_PERSON);
            bitmap.recycle();
            return imageOb.getMaskPath();
        } else {
            onToast(R.string.pesdk_segment_no_mask);
            return null;
        }
    }

    private void save() {
        String path = doMask();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(IntentConstants.PARAM_SEGMENT_RESULT, path);
        setResult(RESULT_OK, intent);
        finish();
    }

}
