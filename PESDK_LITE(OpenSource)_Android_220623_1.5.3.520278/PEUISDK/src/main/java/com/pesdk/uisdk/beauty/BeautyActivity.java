package com.pesdk.uisdk.beauty;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.base.BaseActivity;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.beauty.analyzer.MNNKitFaceManager;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.BeautyInfo;
import com.pesdk.uisdk.beauty.bean.FaceHairInfo;
import com.pesdk.uisdk.beauty.fragment.AdjustFragment;
import com.pesdk.uisdk.beauty.fragment.FaceFragment;
import com.pesdk.uisdk.beauty.fragment.FiveSensesFragment;
import com.pesdk.uisdk.beauty.fragment.HairFragment;
import com.pesdk.uisdk.beauty.listener.OnBeautyListener;
import com.pesdk.uisdk.beauty.widget.DragMediaView;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.fragment.callback.IFragmentMenuCallBack;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.uisdk.widget.edit.EditDragView;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.exception.InvalidStateException;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.models.caption.CaptionLiteObject;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

/**
 * 小功能-美颜
 */
public class BeautyActivity extends BaseActivity implements OnBeautyListener, IFragmentMenuCallBack {
    private static final String TAG = "BeautyActivity";

    /**
     * 美颜
     */
    public static Intent createIntent(Context context, PEImageObject mediaObject, boolean isPip) {
        Intent intent = new Intent(context, BeautyActivity.class);
        intent.putExtra(IntentConstants.PARAM_EDIT_IMAGE, mediaObject);
        intent.putExtra(IntentConstants.PARAM_IMAGE_IS_LAYER, isPip);
        return intent;
    }

    /**
     * 人脸检测最大次数后开始提示
     */
    private static final int MAX_DETECT = 3;

    /**
     * 播放器
     */
    private RelativeLayout mRlVideo;
    private VirtualImage mVirtualImage;
    private VirtualImageView mVirtualImageView;
    /**
     * 媒体操作
     */
    private DragMediaView mDragMediaView;

    /**
     * 菜单ID
     */
    private int mBeautyId;

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
    private HairFragment mHairFragment;

    /**
     * 人脸检测次数
     */
    private int mFaceDetect = 0;

    /**
     * 当前选中的人脸
     */
    private BeautyFaceInfo mFaceInfo;
    /**
     * 美颜参数
     */
    private BeautyInfo mBeautyInfo;

    private PEImageObject bk;
    private boolean isLayer;
    private BeautyActivityVM mBeautyActivityVM;
    private String baseMediaPath;
    private FilterInfo filterInfo;
    private boolean isAddBeauty;
    private List<BeautyFaceInfo> mFaceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesdk_activity_beauty);
        mImageObject = getIntent().getParcelableExtra(IntentConstants.PARAM_EDIT_IMAGE);
        if (null == mImageObject) {
            finish();
            return;
        }
        mBeautyActivityVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(BeautyActivityVM.class);
        mBeautyActivityVM.getMergeHairMedia().observe(this, this::onMergeResult);

        isLayer = getIntent().getBooleanExtra(IntentConstants.PARAM_IMAGE_IS_LAYER, false);
        bk = mImageObject.copy();
        mImageObject.setShowRectF(null); //兼容Layer位置
        mImageObject.setShowAngle(0);
        mImageObject.setClipRectF(null);
        ImageOb imageOb = PEHelper.initImageOb(mImageObject);
        filterInfo = imageOb.getBeauty();
        if (null != filterInfo) {
            isAddBeauty = false;//调节美颜
            mBeautyInfo = filterInfo.getBeauty();
            mBeautyInfo.fixHairParam(); //兼容之前的单人头发版本
            //之前单个人脸已经设置了参数
            mFaceList = mBeautyInfo.getFaceList();
        } else { //新增美颜
            isAddBeauty = true;
            mBeautyInfo = new BeautyInfo();
        }

        //1.初始化引擎(异步任务提前准备)
        MNNKitFaceManager.getInstance().createFaceAnalyzer(this);

        if (TextUtils.isEmpty(mBeautyInfo.getBaseMediaPath())) {
            mBeautyInfo.setBaseMediaPath(mImageObject.getMediaPath());
        }

        baseMediaPath = mBeautyInfo.getBaseMediaPath();

        initView();
        init();
    }

    private boolean hasMoreFace = false;

    private void resetFace() {
        if (hasMoreFace) {
            mFaceInfo = null;
        }
    }

    private void facePrepared() {
        if (mDragMediaView.isDrawFace()) {
            hasMoreFace = mFaceList.size() > 1; //多个人脸，每次点击单个功能时，都要重新选择人脸
            if (mFaceList.size() == 1) {
                mFaceInfo = mFaceList.get(0);
                if (mBeautyId == R.id.btn_five_senses) {
                    onClickFive();
                } else if (mBeautyId == R.id.btn_hair) {
                    onClickHair();
                } else {
                    onClickFace();
                }
            } else {
                if (mFaceList.size() <= 0) {
                    mFaceDetect++;
                    if (mFaceDetect > MAX_DETECT) {
                        onToast(R.string.pesdk_face_recognition_failed);
                    } else {
                        detectFace();
                    }
                }
                //刷新
                mDragMediaView.invalidate();
            }
        }
    }

    /**
     * 人脸信息是否可用  （第一次获取人脸信息成功之后，保存防止人脸加了头发之后识别人脸失败）
     *
     * @return
     */
    private boolean faceParamIsAvailable() {
        return null != mFaceList && mFaceList.size() > 0;
    }


    public static interface Callback {
        void prepared(List<BeautyFaceInfo> data);
    }


    /**
     * 初始化
     */
    private void init() {
        // 初始化视频
        mRlVideo.post(() -> {
            float asp = mRlVideo.getWidth() * 1.0f / mRlVideo.getHeight();

            //计算媒体的显示区域
            float mediaAsp = mImageObject.getWidth() * 1.0f / mImageObject.getHeight();
            RectF rectF;
            if (asp > mediaAsp) {
                rectF = new RectF((asp - mediaAsp) / 2 / asp, 0, (asp + mediaAsp) / 2 / asp, 1);
            } else {
                rectF = new RectF(0, (1 / asp - 1 / mediaAsp) / 2 * asp, 1, (1 / asp + 1 / mediaAsp) / 2 * asp);
            }
            mImageObject.setShowRectF(rectF);
            mDragMediaView.setData(new RectF(rectF.left * mRlVideo.getWidth(),
                    rectF.top * mRlVideo.getHeight(),
                    rectF.right * mRlVideo.getWidth(),
                    rectF.bottom * mRlVideo.getHeight()));
            mMediaShow.set(rectF);
            mVirtualImage = new VirtualImage();
            mVirtualImageView.setPreviewAspectRatio(asp);
            rebuild(false);
        });

    }

    /**
     * @param loadHair 加载其他人的头发（进入头发fragment时，若其他人设置了头发，需要等比放大）
     */
    private void rebuild(boolean loadHair) {
        mVirtualImage.reset();
        mVirtualImageView.enableViewBGHolder(true);
        try {
            PEScene scene = new PEScene(mImageObject);
            scene.setBackground(ContextCompat.getColor(this, R.color.pesdk_main_bg));
            mVirtualImage.setPEScene(scene);
            if (loadHair) {//头发
                if (null != mFaceList && mFaceList.size() > 0) {
                    for (BeautyFaceInfo info : mFaceList) {
                        if (null != info && null != mFaceInfo && info.getFaceId() != mFaceInfo.getFaceId() && info.getHairInfo().getHair() != null) {
                            CaptionLiteObject liteObject = info.getHairInfo().getHair();
                            CaptionLiteObject tmp = mBeautyActivityVM.restoreHairInVirtual(liteObject, mImageObject);
                            mVirtualImage.addCaptionLiteObject(tmp);
                        }
                    }
                }
                if (null != mHairObject) {
                    mVirtualImage.addCaptionLiteObject(mHairObject);
                }
            }
            modifyBeauty();
            mVirtualImage.build(mVirtualImageView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private void copyParam(PEImageObject src, PEImageObject dst) {
        dst.setShowRectF(src.getShowRectF());
        dst.setClipRectF(src.getClipRectF());
    }

    private void onMergeResult(String path) {
        try {
            PEImageObject tmp = new PEImageObject(path);
            copyParam(mImageObject, tmp);
            mImageObject = tmp;
            mHairObject = null;
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        rebuild(false);
    }

    public List<BeautyFaceInfo> getBeautyFaceList() {
        return mFaceList;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        //顶部菜单
        $(R.id.btnLeft).setOnClickListener(v -> onBackPressed());
        $(R.id.btnRight).setOnClickListener(v -> save());

        //播放
        mRlVideo = $(R.id.rl_video);
        mVirtualImageView = $(R.id.beauty_video);
        mVirtualImageView.setOnPlaybackListener(new VirtualImageView.VirtualViewListener() {
            @Override
            public void onPrepared() {
                findViewById(R.id.tmpView).setVisibility(View.GONE);
                SysAlertDialog.cancelLoadingDialog();
            }
        });
        //媒体
        mDragMediaView = $(R.id.drag_media);
        mDragMediaView.setListener(new DragMediaView.OnDragListener() {
            RectF showRectF = new RectF();

            @Override
            public List<BeautyFaceInfo> getFaceList() {
                return getBeautyFaceList();
            }

            @Override
            public boolean onRectChange(RectF rectF) {
                updateRectF(rectF);
                mImageObject.refresh();
                return true;
            }

            private void updateRectF(RectF rectF) {
                int w = mRlVideo.getWidth();
                int h = mRlVideo.getHeight();
                showRectF.set(rectF.left / w, rectF.top / h, rectF.right / w, rectF.bottom / h);
                mImageObject.setShowRectF(showRectF);
            }


            @Override
            public void onMove() {

            }


            @Override
            public void onFace(BeautyFaceInfo faceInfo, RectF rectF) {
                mFaceInfo = faceInfo;
                updateRectF(rectF);
                if (mCurrentFragment != null) {
                    mImageObject.refresh();
                    if (mCurrentFragment instanceof FiveSensesFragment) {
                        mFiveSensesFragment.recover();
                        mDragMediaView.setDrawFace(false);
                    } else if (mFaceFragment != null && mCurrentFragment instanceof FaceFragment) {
                        mFaceFragment.recover();
                        mDragMediaView.setDrawFace(false);
                    }
                } else if (mBeautyId == R.id.btn_hair) {
                    onClickHair();          //此处需注意: mImageObject.refresh()
                } else {
                    if (mBeautyId == R.id.btn_five_senses) {
                        mImageObject.refresh();
                        onClickFive();
                    } else {
                        mImageObject.refresh();
                        onClickFace();
                    }
                }
            }
        });
        initHairView();
    }

    private void initHairView() {
        mHairDragView = $(R.id.dragHair);
        mHairDragView.setCallback(() -> false);
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
//                Log.e(TAG, "onRectChange: " + rectF + " " + angle + mShowRectF);
                mHairObject.setShowRectF(mShowRectF);
                mHairObject.setAngle((int) -angle);
                mVirtualImage.updateSubtitleObject(mHairObject);
                return true;
            }

            @Override
            public void onTouchDown() {

            }

            @Override
            public void onTouchUp() {

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
        mDragMediaView.setDrawFace(false);
        if (mBeautyId == R.id.btn_auto) {
            //一键美颜
            onClickAdjust();
        } else if (mBeautyId == R.id.btn_five_senses) {
            resetFace();
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
            resetFace();
            //瘦脸
            onClickFace();
        } else if (mBeautyId == R.id.btn_eyes) {
            resetFace();
            //大眼
            onClickFace();
        } else if (mBeautyId == R.id.btn_hair) {
            resetFace();
            //头发
            onClickHair();
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

    private void prepareFace() {
        if (mFaceInfo == null) {
            if (mFaceList != null) {
                List<BeautyFaceInfo> faceList = mFaceList;
                if (faceList != null && faceList.size() == 1) {
                    mFaceInfo = faceList.get(0);
                }
            }
        }
    }

    /**
     * 选中人脸
     */
    private void onClickFive() {
        prepareFace();
        if (mFaceInfo == null) {
            mDragMediaView.setDrawFace(true);
            detectFace();
        } else {
            BeautyFaceInfo info = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
            mFaceInfo = info;

            mDragMediaView.setDrawFace(false);
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
                        mDragMediaView.setDrawFace(true);
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
        prepareFace();
        if (mFaceInfo == null) {
            mDragMediaView.setDrawFace(true);
            detectFace();
        } else {
            BeautyFaceInfo info = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
            mFaceInfo = info;
            mDragMediaView.setDrawFace(false);
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
                        BeautyFaceInfo info = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
                        mFaceInfo = info;
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
                        mDragMediaView.setDrawFace(true);
                    }
                });
            }
            changeFragment(mFaceFragment);
        }
    }

    private FaceHairInfo mHairInfo;
    private PEImageObject mbkHairBefore = null;
    private BeautyFaceInfo mbkParamHairBefore = null;

    /**
     * 头发
     */
    private void onClickHair() {
        prepareFace();
        if (mFaceInfo == null) {
            mDragMediaView.setDrawFace(true);
            detectFace();
        } else {
            BeautyFaceInfo info = mBeautyInfo.getBeautyFace(mFaceInfo.getFaceId());
            mFaceInfo = info;
            mHairInfo = mFaceInfo.getHairInfo();
            mbkParamHairBefore = mFaceInfo.copy();
            mbkHairBefore = mImageObject.copy();

            mDragMediaView.setDrawFace(false);
            //弹出菜单
            mHairFragment = HairFragment.newInstance();
            mHairFragment.setCallback(new HairFragment.Callback() {

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
                    } else {
                        mVirtualImage.updateSubtitleObject(object);
                        mHairObject = object;
                        initHairControl(object);
                    }
                }

                @Override
                public CaptionLiteObject getLastHair() {
                    return mHairObject;
                }

                @Override
                public void preMergeHair() {
                    //1.退出UI
                    exitHairUI();
                    if (null == mHairObject) {
                        mHairInfo.setHair(null, null);
                        mHairInfo.setHair(null);
                        return;
                    }
                    SysAlertDialog.showLoadingDialog(BeautyActivity.this, R.string.pesdk_process).setCancelable(false);
                    //2.计算头发在原始图片中的位置
                    CaptionLiteObject hair = mBeautyActivityVM.hairInMedia(mImageObject, mHairObject, mVirtualImageView.getPreviewWidth(), mVirtualImageView.getPreviewHeight());
                    mHairInfo.setHair(hair);

                    //3.合并头发
                    if (null != hair) {
                        mBeautyActivityVM.applyHairs(mBeautyInfo.getBaseMediaPath(), mFaceList);
                    }
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

                private void exitHairUI() {
                    hideUI();
                    onSure();
                }
            });

            mHairFragment.setFaceHairInfo(mHairInfo);
            changeFragment(mHairFragment);


            //如果有设置头发，需还原为原始媒体(未指定头发时的路径)
            CaptionLiteObject hair = mHairInfo.getHair();
            if (null != hair) { //有美发信息
                try {
                    PEImageObject tmp = new PEImageObject(baseMediaPath);
                    copyParam(mImageObject, tmp);
                    mImageObject = tmp;
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                mHairObject = mBeautyActivityVM.restoreHairInVirtual(hair, mImageObject);
                initHairControl(mHairObject);
                rebuild(true);
            } else {
                mImageObject.refresh();
            }
        }
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
            mDragMediaView.setData(new RectF(mMediaShow.left * mRlVideo.getWidth(),
                    mMediaShow.top * mRlVideo.getHeight(),
                    mMediaShow.right * mRlVideo.getWidth(),
                    mMediaShow.bottom * mRlVideo.getHeight()));
            mImageObject.setShowRectF(mMediaShow);
            mImageObject.refresh();
        }
    }

    /**
     * 检测人脸
     */
    private void detectFace() {
        resetShow();
        if (faceParamIsAvailable()) {
            facePrepared();
        } else {
            mBeautyActivityVM.processFace(baseMediaPath, data -> {
                mFaceList = data;
                mBeautyInfo.setFaceList(data);
                facePrepared();
            });
        }
    }

    /**
     * 修改美颜
     */
    private void modifyBeauty() {
        mBeautyActivityVM.applyFilter(mImageObject, mBeautyInfo);
    }


    /**
     * 保存图片
     */
    private void save() {
        Intent intent = new Intent();
        if (isLayer) {
            mImageObject.setShowRectF(bk.getShowRectF());
        }
        intent.putExtra(IntentConstants.PARAM_EDIT_BEAUTY_FILTER_RESULT, new FilterInfo(mBeautyInfo));
        intent.putExtra(IntentConstants.PARAM_EDIT_BEAUTY_FILTER_HAIR_MEDIA, mImageObject.getMediaPath()); //兼容美发(多人)
        intent.putExtra(IntentConstants.PARAM_EDIT_BEAUTY_ADD, isAddBeauty);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onCancel() {

    }

    @Override
    public void onSure() {
        //确定
        hideFragment();
        mDragMediaView.setDrawFace(false);
        mHairFragment = null;
    }

    @Override
    public void onBackPressed() {
        if (null != mHairFragment && mHairFragment.onBackPressed() != 0) {
            return;
        } else if (mDragMediaView.isDrawFace()) {
            mDragMediaView.setDrawFace(false);
            return;
        } else if ($(R.id.fragment).getVisibility() == View.VISIBLE) {
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
        MNNKitFaceManager.getInstance().release();
        System.runFinalization();
        System.gc();
    }
}



