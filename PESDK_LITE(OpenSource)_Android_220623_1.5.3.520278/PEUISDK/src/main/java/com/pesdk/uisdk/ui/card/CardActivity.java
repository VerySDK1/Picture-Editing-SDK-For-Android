package com.pesdk.uisdk.ui.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.base.BaseActivity;
import com.pesdk.uisdk.bean.CardImportResult;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.beauty.analyzer.MNNKitFaceManager;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.BeautyInfo;
import com.pesdk.uisdk.beauty.bean.FaceHairInfo;
import com.pesdk.uisdk.beauty.fragment.AdjustFragment;
import com.pesdk.uisdk.beauty.fragment.FaceFragment;
import com.pesdk.uisdk.beauty.fragment.FiveSensesFragment;
import com.pesdk.uisdk.beauty.listener.OnBeautyListener;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.fragment.callback.IFragmentMenuCallBack;
import com.pesdk.uisdk.ui.card.child.IRevokeListener;
import com.pesdk.uisdk.ui.card.child.RevokeHandler;
import com.pesdk.uisdk.ui.card.export.ExportHandler;
import com.pesdk.uisdk.ui.card.fragment.CardErasePenFragment;
import com.pesdk.uisdk.ui.card.fragment.ClothesFragment;
import com.pesdk.uisdk.ui.card.fragment.MenuFragment;
import com.pesdk.uisdk.ui.card.listener.Callback;
import com.pesdk.uisdk.ui.card.listener.ColorListener;
import com.pesdk.uisdk.ui.card.vm.CardActivityVM;
import com.pesdk.uisdk.ui.card.widget.ClothesView;
import com.pesdk.uisdk.ui.card.widget.TestDrawView;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.ModelHelperImp;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.util.helper.PersonSegmentHelper;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.uisdk.widget.edit.EditDragView;
import com.pesdk.uisdk.widget.segment.FloatSegmentView;
import com.pesdk.uisdk.widget.segment.SegmentView;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.ui.PreviewFrameLayout;
import com.vecore.base.lib.utils.BitmapUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.exception.InvalidStateException;
import com.vecore.models.AspectRatioFitMode;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.utils.MiscUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

/**
 * 证件照
 */
public class CardActivity extends BaseActivity implements OnBeautyListener, IFragmentMenuCallBack, Callback, ColorListener {
    private static final String TAG = "CardActivity";

    private static final String PARAM_SIZE = "_out_size";

    public static Intent createCard(Context context, String path, VirtualImage.Size size) {
        Intent intent = new Intent(context, CardActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE, path);
        intent.putExtra(PARAM_SIZE, new Rect(0, 0, size.width, size.height));
        return intent;
    }

    private int mBeautyId;


    /**
     * 播放器
     */
    private PreviewFrameLayout mRlVideo;
    private VirtualImage mVirtualImage;
    private VirtualImageView mVirtualImageView;


    /**
     * 起始图片显示区域
     */
    private final RectF mMediaShow = new RectF();

    /**
     * 场景
     */
    private PEImageObject mImageObject;

    /**
     * 菜单
     */
    private AbsBaseFragment mCurrentFragment;
    /**
     * 调节
     */
    private AdjustFragment mAdjustFragment;
    /**
     * 五官
     */
    private FiveSensesFragment mFiveSensesFragment;
    /**
     * 人脸
     */
    private FaceFragment mFaceFragment;


    /**
     * 当前选中的人脸
     */
    private BeautyFaceInfo mFaceInfo;
    /**
     * 美颜参数
     */
    private BeautyInfo mBeautyInfo;

    private CardActivityVM mCardActivityVM;
    private String baseMediaPath, buildMedia;
    private FilterInfo filterInfo;
    private List<BeautyFaceInfo> mFaceList;
    private Rect size;
    private ViewStub mVsClothes;
    private SegmentView mSegmentView;
    private MenuFragment menuFragment;
    private FrameLayout mUIClothLayout;
    private float mDisplayAsp = 1f;
    private TestDrawView mTestDrawView;
    private RadioGroup mRadioGroup;
    private RadioButton rbShortHair;

    /**
     * true 启用自动消除功能
     */
    private boolean enableAutoRemove() {
        return rbShortHair.isChecked();
    }

    private Runnable mRbChange = new Runnable() {
        @Override
        public void run() {
            //切换时应恢复默认
            resetImp();
            if (mRadioGroup.getCheckedRadioButtonId() == R.id.rbShortHair) { //短发
                if (null != mHairObject) {
                    doAutoClipSegment(mHairObject);
                }
            } else { //长发
                if (null != mBitmapAutoSegment) {
                    mBitmapAutoSegment.recycle();
                    mBitmapAutoSegment = null;
                }
                if (null != mHairObject) {
                    bindMask(mBaseMaskPath);
                    reAutoClip(mBaseMaskPath);
                }
            }
        }
    };
    private FloatSegmentView mFloatSegmentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesdk_activity_card);
        String path = getIntent().getStringExtra(IntentConstants.PARAM_EDIT_IMAGE);
        size = getIntent().getParcelableExtra(PARAM_SIZE);
        mDisplayAsp = size.width() * 1f / size.height();
        try {
            mImageObject = new PEImageObject(path);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        if (null == mImageObject) {
            finish();
            return;
        }
        mFloatSegmentView = $(R.id.floatSegmentView);
        mRadioGroup = $(R.id.rgHairGroup);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mRadioGroup.removeCallbacks(mRbChange);
            mRadioGroup.postDelayed(mRbChange, 100);
        });


        rbShortHair = $(R.id.rbShortHair);
        mTestDrawView = $(R.id.testView);
        mCardActivityVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(CardActivityVM.class);
        mCardActivityVM.getFloatMedia().observe(this, this::onFloatResult);

        menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.menuFragment);


        mUIClothLayout = $(R.id.clothesLayout);

        SysAlertDialog.showLoadingDialog(this, R.string.pesdk_process).setCancelable(false);
        initView();


        mImageObject.setShowAngle(0);
        mImageObject.setClipRectF(null);
        RectF rectF = new RectF();
        mImageObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
        MiscUtils.fixShowRectFByExpanding(mImageObject.getWidth() * 1f / mImageObject.getHeight(), 1080, (int) (1080 / (mDisplayAsp)), rectF);
        mImageObject.setShowRectF(rectF);

        ImageOb imageOb = PEHelper.initImageOb(mImageObject);
        filterInfo = imageOb.getBeauty();
        if (null != filterInfo) {
            mBeautyInfo = filterInfo.getBeauty();
            mFaceList = mBeautyInfo.getFaceList();
        } else { //新增美颜
            mBeautyInfo = new BeautyInfo();
        }

        if (TextUtils.isEmpty(mBeautyInfo.getBaseMediaPath())) {
            mBeautyInfo.setBaseMediaPath(mImageObject.getMediaPath());
        }

        baseMediaPath = mBeautyInfo.getBaseMediaPath();

        buildMedia = baseMediaPath;


        mVsClothes = $(R.id.mVsClothesMenu);
        mSegmentView = $(R.id.doodleView);

        mRevokeList = new ArrayList<>();
        undoList = new ArrayList<>();
        //1.初始化引擎(异步任务提前准备)
        MNNKitFaceManager.getInstance().createFaceAnalyzer(this, () -> {
            mHandler.postDelayed(() -> processFace(), 1000);
        });
        //媒体
        initHairView();

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
                mFloatSegmentView.setVisibility(View.GONE);
                mFloatSegmentView.recycle();

                mDemark.doMask();
            }
        });
    }


    private Handler mHandler = new Handler() {

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

    private Bitmap mBaseMask;
    private String mBaseMaskPath;

    private void processFace() {
        mCardActivityVM.processFace(baseMediaPath, data -> {
            if (null == data || data.size() == 0) { //识别失败
                SysAlertDialog.cancelLoadingDialog();
                onToast(R.string.pesdk_card_recognition_failed);
                finish();
                return;
            } else if (data.size() > 1) {//多人脸不支持
                SysAlertDialog.cancelLoadingDialog();
                onToast(R.string.pesdk_card_recognition_multi);
                finish();
                return;
            } else {
                //保证人像需居中(人脸在钟中间位置)
                mFaceList = data;
                mBeautyInfo.setFaceList(data);
                facePrepared();

                //依据人脸: 计算默认的显示位置、角度
                CardImportResult result = mFaceInfo.applyAutoCenter(mTestDrawView, BitmapFactory.decodeFile(mImageObject.getMediaPath()), mImageObject.getWidth(), mImageObject.getHeight(), mDisplayAsp);

                Log.e(TAG, "processFace: " + result);

                mImageObject.setShowAngle(result.getAngle());
                mImageObject.setShowRectF(result.getRectF());

                //1.主动segment
                autoSegment();
            }
        });
    }

    private void autoSegment() {
        ModelHelperImp.checkAnalyzer(this, () -> {//检查模型
            PersonSegmentHelper helper = new PersonSegmentHelper();
            helper.process(mImageObject, bitmap -> {
                mBaseMask = bitmap; //基础的人像mask
                if (bitmap != null) {
                    mBaseMaskPath = PathUtils.getTempFileNameForSdcard("base_mask", "png");
                    try {
                        BitmapUtils.saveBitmapToFile(bitmap, true, 100, mBaseMaskPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mRevokeList.add(mBaseMaskPath);
                }

                mHandler.post(() -> {
                    if (bitmap == null) {
                        SysAlertDialog.cancelLoadingDialog();
                        finish();
                        return;
                    }
                    mCardActivityVM.createFloatMedia(mImageObject, mDisplayAsp, mBeautyInfo);
                });
            });
        });
    }

    private void onFloatResult(String path) {

        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        //真实的build
        nCount = 1;
        mCardActivityVM.updateBgColor(Color.WHITE);
        init();

    }


    private void facePrepared() {
        mFaceInfo = mFaceList.get(0);
    }


    /**
     * 初始化
     */
    private void init() {
        // 初始化视频
        mRlVideo.post(() -> {
            mMediaShow.set(mImageObject.getShowRectF());
            mVirtualImageView.setPreviewAspectRatio(mDisplayAsp);
            rebuild(false);
        });

    }

    /**
     * @param loadHair 加载其他人的头发
     */
    private void rebuild(boolean loadHair) {
        mVirtualImage.reset();
        mVirtualImageView.enableViewBGHolder(true);
        mVirtualImageView.setPreviewAspectRatio(size.width() * 1f / size.height());
        try {
            load(mVirtualImage, loadHair, false);
            mVirtualImage.build(mVirtualImageView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private void load(VirtualImage virtualImage, boolean loadClothes, boolean export) {
        PEScene scene = new PEScene(mImageObject);
        Integer tmp = mCardActivityVM.getBgColor().getValue();
        if (tmp != null) {
            int color = tmp;
            if (color != PEScene.UNKNOWN_COLOR) {
                scene.setBackground(color);
                ImageOb ob = PEHelper.initImageOb(mImageObject);
                ob.setSegment(Segment.SEGMENT_PERSON);
                AnalyzerManager.getInstance().extraMedia(mImageObject, export);
            } else {
                mImageObject.setExtraDrawListener(null);
            }
        } else {
            mImageObject.setExtraDrawListener(null);
        }
        virtualImage.setPEScene(scene);
        modifyBeauty();

        if (loadClothes) {//头发
            if (null != mFaceList && mFaceList.size() > 0) {
                for (BeautyFaceInfo info : mFaceList) {
                    if (info.getHairInfo().getHair() != null && mHairObject == null) {
                        CaptionLiteObject liteObject = info.getHairInfo().getHair();
                        CaptionLiteObject hair = mCardActivityVM.restoreHairInVirtual(liteObject, mImageObject);
                        virtualImage.addCaptionLiteObject(hair);
                    }
                }
            }
            if (null != mHairObject) {
                virtualImage.addCaptionLiteObject(mHairObject);
            }
        }
    }


    private void copyParam(PEImageObject src, PEImageObject dst) {
        dst.setShowRectF(src.getShowRectF());
        dst.setShowAngle(src.getShowAngle());
        dst.setClipRectF(src.getClipRectF());
        dst.setTag(src.getTag());
    }


    public List<BeautyFaceInfo> getBeautyFaceList() {
        return mFaceList;
    }

    private int nCount = 0;

    /**
     * 初始化控件
     */
    private void initView() {
        //顶部菜单
        $(R.id.btnLeft).setOnClickListener(v -> onBackPressed());
        $(R.id.btnRight).setOnClickListener(v -> save());

        //播放
        mRlVideo = $(R.id.rl_video);
        mRlVideo.setAspectRatio(size.width() * 1f / size.height());
        mVirtualImageView = $(R.id.beauty_video);
        mVirtualImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.pesdk_main_bg));
        mVirtualImageView.setOnPlaybackListener(new VirtualImageView.VirtualViewListener() {
            @Override
            public void onPrepared() {
                if (nCount == 1) {
                    nCount++;
                    SysAlertDialog.cancelLoadingDialog();
                } else if (nCount > 1) {
                    SysAlertDialog.cancelLoadingDialog();
                }

            }
        });

        mVirtualImage = new VirtualImage();

        buildEmpty();
    }

    /**
     * 构造一个空的，解决背景色
     */
    private void buildEmpty() {
        Bitmap bitmap = Bitmap.createBitmap(640, (int) (640 / (size.width() * 1f / size.height())), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bitmap);
        cv.drawColor(Color.WHITE);
        try {
            PEImageObject peImageObject = new PEImageObject(bitmap);
            PEScene peScene = new PEScene();
            peScene.setPEImage(peImageObject);
            mVirtualImage.setPEScene(peScene);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        try {
            mVirtualImage.build(mVirtualImageView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

    }

    private void initHairView() {
        mHairDragView = $(R.id.dragHair);
        mHairDragView.setCallback(() -> false);
        mHairDragView.setHideRect(true);
        mHairDragView.setListener(new EditDragView.OnDragListener() {
            @Override
            public void onClickOther(int position) {

            }

            @Override
            public void onClick(boolean in, float x, float y) {

            }

            @Override
            public void onDelete() {

            }

            @Override
            public void onAlign(int align) {

            }

            @Override
            public void onCopy() {

            }

            @Override
            public void onEdit() {

            }

            @Override
            public boolean onRectChange(RectF rectF, float angle) {
                RectF mShowRectF = new RectF(rectF.left / getWidth(), rectF.top / getHeight(), rectF.right / getWidth(), rectF.bottom / getHeight());
                mHairObject.setShowRectF(mShowRectF);
                mHairObject.setAngle((int) -angle);
                mVirtualImage.fastCaptionLite(mHairObject);
                mVirtualImageView.refresh();
                return true;
            }

            @Override
            public void onTouchDown() {
                if (enableAutoRemove()) {
                    bindMask(mBaseMaskPath);
                }
            }

            @Override
            public void onTouchUp() {
                if (enableAutoRemove()) {
                    String autoMask = autoClipSegment(mHairObject);
                    if (!TextUtils.isEmpty(autoMask)) {
                        reAutoClip(autoMask);
                    }
                }
            }

            @Override
            public void onExitEdit() {

            }

            @Override
            public float getWidth() {
                return mRlVideo.getWidth();
            }

            @Override
            public float getHeight() {
                return mRlVideo.getHeight();
            }
        });
    }

    private EditDragView mHairDragView;


    /**
     * 菜单点击
     */
    public void onBeauty(View view) {
        mBeautyId = view.getId();
        if (mBeautyId == R.id.btn_auto) {
            //一键美颜
            onClickAdjust();
        } else if (mBeautyId == R.id.btn_five_senses) {
            //五官
            onClickFive();
        } else if (mBeautyId == R.id.blue) {
            //磨皮
            onClickAdjust();
        } else if (mBeautyId == R.id.btn_color) {
            //美白
            onClickAdjust();
        } else if (mBeautyId == R.id.btn_ruddy) {
            //红润
            onClickAdjust();
        } else if (mBeautyId == R.id.btn_face) {
            //瘦脸
            onClickFace();
        } else if (mBeautyId == R.id.btn_eyes) {
            //大眼
            onClickFace();
        }
    }


    /**
     * 调节
     */
    private void onClickAdjust() {
        if (mAdjustFragment == null) {
            mAdjustFragment = AdjustFragment.newInstance();
            mAdjustFragment.setAdjustListener(new AdjustFragment.OnAdjustListener() {

                @Override
                public void onChange(float value) {
                    if (mBeautyId == R.id.blue) {
                        mBeautyInfo.setValueBeautify(value);
                    } else if (mBeautyId == R.id.btn_color) {
                        mBeautyInfo.setValueWhitening(value);
                    } else if (mBeautyId == R.id.btn_ruddy) {
                        mBeautyInfo.setValueRuddy(value);
                    } else if (mBeautyId == R.id.btn_auto) {
                        //调节所有美颜参数
                        mBeautyInfo.setValueRuddy(value);
                        mBeautyInfo.setValueBeautify(value);
                        mBeautyInfo.setValueWhitening(value);
                    }
                    modifyBeauty();
                }

                @Override
                public float getDefault() {
                    if (mBeautyId == R.id.blue) {
                        return mBeautyInfo.getValueBeautify();
                    } else if (mBeautyId == R.id.btn_color) {
                        return mBeautyInfo.getValueWhitening();
                    } else if (mBeautyId == R.id.btn_ruddy) {
                        return mBeautyInfo.getValueRuddy();
                    } else if (mBeautyId == R.id.btn_auto) { //一键美颜
                        return (mBeautyInfo.getValueBeautify() + mBeautyInfo.getValueRuddy() + mBeautyInfo.getValueWhitening()) / 3;
                    }
                    return 0;
                }

                @Override
                public String getTitle() {
                    if (mBeautyId == R.id.blue) {
                        return getString(R.string.pesdk_fu_blue);
                    } else if (mBeautyId == R.id.btn_color) {
                        return getString(R.string.pesdk_fu_whitening);
                    } else if (mBeautyId == R.id.btn_ruddy) {
                        return getString(R.string.pesdk_fu_ruddy);
                    } else if (mBeautyId == R.id.btn_auto) {
                        return getString(R.string.pesdk_beauty_auto);
                    }
                    return null;
                }
            });
        }
        changeFragment(mAdjustFragment);
    }


    /**
     * 选中人脸
     */
    private void onClickFive() {
        if (mFaceInfo == null) {
            detectFace();
        } else {
            mFaceInfo = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
            //弹出菜单
            if (mFiveSensesFragment == null) {
                mFiveSensesFragment = FiveSensesFragment.newInstance();
                mFiveSensesFragment.setFiveListener(new FiveSensesFragment.OnFiveSensesListener() {
                    @Override
                    public BeautyFaceInfo getFace() {
                        BeautyFaceInfo info = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
                        return mFaceInfo = info;
                    }

                    @Override
                    public void onChange() {
                        modifyBeauty();
                    }

                    @Override
                    public int getFaceNum() {
                        List<BeautyFaceInfo> list = getBeautyFaceList();
                        if (list != null) {
                            return list.size();
                        }
                        return 0;
                    }

                    @Override
                    public void onSwitchFace() {
                        resetShow();
                    }
                });
            }
            changeFragment(mFiveSensesFragment);
        }
    }


    /**
     * 人脸
     */
    private void onClickFace() {
        if (mFaceInfo == null) {
            detectFace();
        } else {
            mFaceInfo = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
            //弹出菜单
            if (mFaceFragment == null) {
                mFaceFragment = FaceFragment.newInstance();
                mFaceFragment.setFaceListener(new FaceFragment.OnFaceListener() {
                    @Override
                    public String getTitle() {
                        if (mBeautyId == R.id.btn_face) {
                            return getString(R.string.pesdk_fu_facelift);
                        } else if (mBeautyId == R.id.btn_eyes) {
                            return getString(R.string.pesdk_fu_bigeye);
                        }
                        return null;
                    }

                    @Override
                    public int getCurrent() {
                        if (mBeautyId == R.id.btn_eyes) {
                            return 0;
                        } else if (mBeautyId == R.id.btn_face) {
                            return 1;
                        }
                        return 0;
                    }

                    @Override
                    public BeautyFaceInfo getFace() {
                        mFaceInfo = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
                        return mFaceInfo;
                    }

                    @Override
                    public void onChange() {
                        modifyBeauty();
                    }

                    @Override
                    public int getFaceNum() {
                        List<BeautyFaceInfo> list = getBeautyFaceList();
                        if (list != null) {
                            return list.size();
                        }
                        return 0;
                    }

                    @Override
                    public void onSwitchFace() {
                        resetShow();
                    }
                });
            }
            changeFragment(mFaceFragment);
        }
    }

    private FaceHairInfo mHairInfo;
    private PEImageObject mbkHairBefore = null;
    private BeautyFaceInfo mbkParamHairBefore = null;
    private RevokeHandler mRevokeHandler;
    private CardErasePenFragment mErasePenFragment;


    @Override
    public void onPreClothes() {
        if (mFaceInfo == null) {
            detectFace();
        }
        BeautyFaceInfo info = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
        mFaceInfo = info;

        mHairInfo = mFaceInfo.getHairInfo();
        mbkParamHairBefore = mFaceInfo.copy();
        mbkHairBefore = mImageObject.copy();
        //如果有设置头发，需还原为原始媒体(未指定头发时的路径)
        CaptionLiteObject hair = mHairInfo.getHair();
        if (null != hair) { //有美发信息
            try {
                PEImageObject tmp = new PEImageObject(buildMedia);
                copyParam(mImageObject, tmp);
                mImageObject = tmp;
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            mHairObject = mCardActivityVM.restoreHairInVirtual(hair, mImageObject);
            initHairControl(mHairObject);
            rebuild(true);
            if (null != mRevokeHandler) {
                mRevokeHandler.setEarseVisibility(View.VISIBLE);
            }
        } else {
            mHairObject = null;
            mImageObject.refresh();
            if (null != mRevokeHandler) {
                mRevokeHandler.setEarseVisibility(View.GONE);
            }
        }

    }

    @Override
    public void goneEarse() {
        if (null != mRevokeHandler) {
            mRevokeHandler.setEarseVisibility(View.GONE);
        }
    }

    private void initRevoke() {
        if (null == mRevokeHandler) {
            mRevokeHandler = new RevokeHandler(this, false, false, new IRevokeListener() {
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

                @Override
                public void onEarse() {
                    //1.退出换装编辑
                    onClothesSureImp(false);


//                    1.1衣服放在UI层显示，防止涂抹交叉异常
                    mUIClothLayout.setVisibility(View.VISIBLE);
                    ClothesView view = new ClothesView(CardActivity.this, null);
                    CaptionLiteObject hair = mHairInfo.getHair();
                    RectF rectF = hair.getShowRectF();
                    String tmp = hair.getPath();


                    int w = mVirtualImageView.getWidth();
                    int h = mVirtualImageView.getHeight();


                    Rect rect = new Rect();
                    rect.left = (int) (w * rectF.left);
                    rect.top = (int) (h * rectF.top);
                    rect.right = (int) (w * rectF.right);
                    rect.bottom = (int) (h * rectF.bottom);


                    view.setData(BitmapFactory.decodeFile(tmp), hair.getAngle(), rect);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    mUIClothLayout.addView(view, lp);


                    $(R.id.tmp).setVisibility(View.INVISIBLE);


                    int bgColor = getBgColor();
                    //1.2媒体和当前mask 生成一个图片(不含服装)
                    mSegmentView.setBaseMediaFile(mCardActivityVM.getFloatMedia().getValue());
                    mSegmentView.setMaskColor(bgColor);


                    //2.涂抹
                    mErasePenFragment = CardErasePenFragment.newInstance();
                    mErasePenFragment.setPaintColor(bgColor);
                    mErasePenFragment.setDemark(mDemark);
                    changeFragment(mErasePenFragment);

                    mSegmentView.setVisibility(View.VISIBLE);
                    mErasePenFragment.setSegmentView(mSegmentView);

                    mRevokeHandler.setEarseVisibility(View.GONE);
                    mRevokeHandler.setUndoVisibility(View.VISIBLE);
                    checkUIStatus();
                }
            });
        }
    }

    private int getBgColor() {
        return mCardActivityVM.getBgColor() != null ? mCardActivityVM.getBgColor().getValue() : PEScene.UNKNOWN_COLOR;

    }


    private Bitmap mBitmapAutoSegment;

    private String autoClipSegment(CaptionLiteObject hair) {
        String path = hair.getPath();
        File file = new File(path);
        File mask = new File(file.getParent(), "mask.png");
        if (mask.exists()) { //2.0 有Mask图
            String clothMask = mask.getAbsolutePath();
            Bitmap tmp = mCardActivityVM.applyAutoSegment(mTestDrawView, mBaseMask, mImageObject.getShowRectF(), mImageObject.getShowAngle(), clothMask, hair);
            String maskPath = PathUtils.getTempFileNameForSdcard("mask_mix2_" + tmp.hashCode(), "png");
            try {
                BitmapUtils.saveBitmapToFile(tmp, true, 100, maskPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap last = mBitmapAutoSegment;
            mBitmapAutoSegment = tmp;
            bindMask(maskPath);
            ThreadPoolUtils.executeEx(() -> {
                if (null != last) {
                    last.recycle();
                }
            });
            return maskPath;
        } else { //1.0 没有mask图时,
            mBitmapAutoSegment = null;
            bindMask(mBaseMaskPath);
            return mBaseMaskPath;
        }
    }

    @Override
    public FaceHairInfo getHairInfo() {
        return mHairInfo;
    }

    @Override
    public ClothesFragment.Callback getClothesCallback() {
        return mCallback;
    }

    /**
     * 头发控制组件
     */
    private void initHairControl(CaptionLiteObject object) {
        int w = mRlVideo.getWidth();
        int h = mRlVideo.getHeight();

        RectF tmp = new RectF(object.getShowRectF());
        tmp.left *= w;
        tmp.right *= w;
        tmp.top *= h;
        tmp.bottom *= h;

        mHairDragView.setData(tmp, -object.getAngle());
        mHairDragView.setCtrRotation(true);
        mHairDragView.setCtrDelete(false);
        mHairDragView.setCtrCopy(false);
        mHairDragView.setCtrEdit(false);
        mHairDragView.setControl(false);
        mHairDragView.setEnabledAngle(true);
        mHairDragView.setEnabledProportion(false);
        mHairDragView.onSticker();
        mHairDragView.setVisibility(View.VISIBLE);
    }

    private CaptionLiteObject mHairObject = null;

    /**
     * 切换fragment
     */
    private void changeFragment(AbsBaseFragment fragment) {
        if (fragment == null) {
            return;
        }
        if (mCurrentFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mCurrentFragment)
                    .commitAllowingStateLoss();
        }
        if (!fragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, fragment)
                    .show(fragment)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(fragment)
                    .commitAllowingStateLoss();
        }
        //显示出来
        View fragmentView = $(R.id.fragment);
        fragmentView.setVisibility(View.VISIBLE);
        //动画
        Animation aniSlideIn = AnimationUtils.loadAnimation(this, R.anim.pesdk_slide_in);
        fragmentView.startAnimation(aniSlideIn);
        mCurrentFragment = fragment;

    }

    /**
     * 隐藏菜单
     */
    public void hideFragment() {
        if (mCurrentFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mCurrentFragment)
                    .commitAllowingStateLoss();
        }
        mCurrentFragment = null;
        $(R.id.fragment).setVisibility(View.GONE);
    }


    /**
     * 恢复图片显示位置
     */
    private void resetShow() {
        if (mImageObject != null) {
            mImageObject.setShowRectF(mMediaShow);
            mImageObject.refresh();
        }
    }

    /**
     * 检测人脸
     */
    private void detectFace() {
        resetShow();
        facePrepared();
    }

    /**
     * 修改美颜
     */
    private void modifyBeauty() {
        mCardActivityVM.applyFilter(mImageObject, mBeautyInfo);
    }


    /**
     * 保存图片
     */
    private void save() {
        ExportHandler exportHandler = new ExportHandler(this);
        exportHandler.export(virtualImage -> load(virtualImage, true, true), size);
    }


    @Override
    public void onCancel() {

    }

    @Override
    public void onSure() {
        //确定
        hideFragment();
    }

    @Override
    public void onBackPressed() {
        if ($(R.id.fragment).getVisibility() == View.VISIBLE) {
            if (mCurrentFragment != null && mCurrentFragment.onBackPressed() != -1) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVirtualImageView) {
            mVirtualImageView.cleanUp();
            mVirtualImageView = null;
        }
        if (null != mVirtualImage) {
            mVirtualImage.release();
            mVirtualImage = null;
        }
        AnalyzerManager.getInstance().release();
        MNNKitFaceManager.getInstance().release();
        System.runFinalization();
        System.gc();
    }

    private void doAutoClipSegment(CaptionLiteObject object) {
        String autoMask = autoClipSegment(object);
        if (!TextUtils.isEmpty(autoMask)) {
            reAutoClip(autoMask);
        }

    }

    private ClothesFragment.Callback mCallback = new ClothesFragment.Callback() {

        @Override
        public PEImageObject getBase() {
            return mImageObject;
        }

        @Override
        public float getAsp() {
            return mVirtualImageView.getPreviewWidth() * 1f / mVirtualImageView.getPreviewHeight();
        }

        @Override
        public void updateCaption(CaptionLiteObject object) {
            if (null != mHairObject) {
                mVirtualImage.deleteSubtitleObject(mHairObject);
            }
            if (null == object) {//none
                mVirtualImageView.refresh();
                mHairObject = null;
                mHairDragView.setVisibility(View.GONE);
                mHairInfo.setHair(null);

                mVsClothes.setVisibility(View.GONE);
                bindMask(mBaseMaskPath);
                mBitmapAutoSegment = null;
                reset();
            } else {
                mVirtualImage.updateSubtitleObject(object);
                mHairObject = object;
                initHairControl(object);

                //可以涂抹修改
                mVsClothes.setVisibility(View.VISIBLE);
                initRevoke();
                if (enableAutoRemove()) {
                    doAutoClipSegment(object);
                }
            }
        }

        @Override
        public CaptionLiteObject getLastHair() {
            return mHairObject;
        }


        @Override
        public void hideUI() {
            mHairDragView.setVisibility(View.GONE);
        }

        @Override
        public BeautyFaceInfo getBeautyFace() {
            return mFaceInfo;
        }

        @Override
        public void onCancelHair() {
            //1.恢复进入头发界面前的参数
            mImageObject = mbkHairBefore;
            mHairObject = null;
            mHairInfo = new FaceHairInfo(mbkParamHairBefore.getHairInfo().getHairSortId(), mbkParamHairBefore.getHairInfo().getHairMaterialId());
            mHairInfo.setHair(mbkParamHairBefore.getHairInfo().getHair());
            mFaceInfo.setHairInfo(mHairInfo);
            //2.再次build
            rebuild(false);
            exitHairUI();
        }

        @Override
        public void onClothesSure() {
            onClothesSureImp(true);
        }


    };

    private void exitHairUI() {
        mCallback.hideUI();
    }

    private void onClothesSureImp(boolean loadHair) {
        //1.退出编辑
        exitHairUI();

        //2.重新生成衣服
        if (null != mHairObject) {
            //2.计算头发在原始图片中的位置
            CaptionLiteObject hair = mCardActivityVM.hairInMedia(mImageObject, mHairObject, mVirtualImageView.getPreviewWidth(), mVirtualImageView.getPreviewHeight());
            mHairInfo.setHair(hair);
        } else {
            mHairInfo.setHair(null);
        }
        mHairObject = null;

        //3.build
        rebuild(loadHair);
    }

    @Override
    public void onBgNone() {
        mCardActivityVM.updateBgColor(PEScene.UNKNOWN_COLOR);
    }


    @Override
    public void onColor(int color) {
        ModelHelperImp.checkAnalyzer(this, () -> {
            mCardActivityVM.updateBgColor(color);
            rebuild(true);
        });
    }

    @Override
    public int getColor() {
        if (mCardActivityVM.getBgColor() != null) {
            return mCardActivityVM.getBgColor().getValue();
        } else {
            return PEScene.UNKNOWN_COLOR;
        }
    }


    private CardErasePenFragment.IDemark mDemark = new CardErasePenFragment.IDemark() {
        @Override
        public void onCancel() {
            try {
                PEImageObject tmp = new PEImageObject(buildMedia);
                copyParam(mImageObject, tmp);
                mImageObject = tmp;
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

            mSegmentView.setVisibility(View.GONE);
            hideFragment();

            mRevokeHandler.setUndoVisibility(View.GONE);
            mRevokeHandler.setEarseVisibility(View.VISIBLE);

            $(R.id.tmp).setVisibility(View.VISIBLE);


            mUIClothLayout.removeAllViews();
            mUIClothLayout.setVisibility(View.GONE);

            if (menuFragment.isCothes()) {
                onPreClothes();
            }

            removeUIClothes();
        }

        @Override
        public void onSure() {
            buildMedia = mImageObject.getMediaPath(); //已经涂抹过
            mSegmentView.setVisibility(View.GONE);
            hideFragment();


            mRevokeHandler.setEarseVisibility(View.VISIBLE);
            mRevokeHandler.setUndoVisibility(View.GONE);

            $(R.id.tmp).setVisibility(View.VISIBLE);
            if (menuFragment.isCothes()) {
                onPreClothes();
            }
            removeUIClothes();
        }

        /**
         * 清除UI上的衣服
         */
        private void removeUIClothes() {
            mHandler.postDelayed(() -> {
                int len = mUIClothLayout.getChildCount();
                for (int i = 0; i < len; i++) {
                    View view = mUIClothLayout.getChildAt(i);
                    if (view instanceof ClothesView) {
                        ((ClothesView) view).recycle();
                    }
                }
                mUIClothLayout.removeAllViews();
                mUIClothLayout.setVisibility(View.GONE);
            }, 100);
        }


        @Override
        public void doMask() {
            mSegmentView.setEnableShowRevokeList(true);
            mSegmentView.invalidate();
            Bitmap userMask = mSegmentView.save(true); //这里是用户自定义需要扣除的部分（颜色需转换成mask一致的透明色）
            //1.这里基础mask,需要换成自动换装后依据位置消除了一次的mask
            Bitmap tmp = null != mBitmapAutoSegment ? mBitmapAutoSegment : mBaseMask;
            //合并抠图mask
            String mask = mCardActivityVM.mergeMask(mImageObject.getShowAngle(), userMask, tmp, mImageObject.getShowRectF());

            mRevokeList.add(mask);
            userMask.recycle();
            bindMask(mask);

            //延迟刷新，防止界面闪烁 （待播放器刷新之后界面再刷）
            mHandler.postDelayed(() -> {
                mSegmentView.setEnableShowRevokeList(false);
                mSegmentView.postInvalidate();
            }, 50);
            checkUIStatus();
        }
    };


    private void bindMask(String mask) {
        ImageOb ob = PEHelper.initImageOb(mImageObject);
        ob.setMaskPath(mask);

        AnalyzerManager.getInstance().force();
        mVirtualImageView.refresh();

    }


    private List<String> mRevokeList = null;
    private List<String> undoList = null;


    private void revoke() {
        if (revokeEnable()) {
            undoList.add(mRevokeList.remove(mRevokeList.size() - 1));
            String mask = mRevokeList.get(mRevokeList.size() - 1);
            mBitmapAutoSegment = BitmapFactory.decodeFile(mask);
            mSegmentView.revoke();
            bindMask(mask);
        }
    }

    private void undo() {
        if (undoList.size() > 0) {
            mRevokeList.add(undoList.remove(undoList.size() - 1));
            String mask = mRevokeList.get(mRevokeList.size() - 1);
            mBitmapAutoSegment = BitmapFactory.decodeFile(mask);
            mSegmentView.undo();
            bindMask(mask);
        }
    }

    private void reset() {
        resetImp();
        bindMask(mBaseMaskPath);
    }

    private void resetImp() {
        mRevokeList.clear();
        undoList.clear();
        mRevokeList.add(mBaseMaskPath);
        mSegmentView.reset();
        mSegmentView.postInvalidate();
    }

    /**
     * 重新拖动位置|更改衣服后，需重新自动消除
     */
    private void reAutoClip(String mask) {
        mRevokeList.clear();
        undoList.clear();
        if (!TextUtils.equals(mBaseMaskPath, mask)) {
            mRevokeList.add(mBaseMaskPath); //为了撤销时，可以恢复原始
        }
        mRevokeList.add(mask);
        mSegmentView.reset();
    }

    private void diffBegin() {
        bindMask(mBaseMaskPath);
    }

    private void diffEnd() {
        String mask = mRevokeList.get(mRevokeList.size() - 1);
        bindMask(mask);
    }


    private final int LIMIT = 1;

    private void checkUIStatus() {
        mRevokeHandler.setRevokeEnable(revokeEnable());
        mRevokeHandler.setUndoEnable(undoList.size() > 0);
        boolean resetEanable = (mRevokeList.size() + undoList.size()) > LIMIT;
        mRevokeHandler.setResetEnable(resetEanable);
        mRevokeHandler.setDiffEnable(resetEanable);
    }


    private boolean revokeEnable() {
        return mRevokeList.size() > LIMIT;
    }

}



