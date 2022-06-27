package com.pesdk.uisdk.ui.home;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pesdk.api.SdkEntry;
import com.pesdk.api.manager.ExportConfiguration;
import com.pesdk.net.RetrofitCreator;
import com.pesdk.uisdk.ActivityResultCallback.IResultCallback;
import com.pesdk.uisdk.Interface.PreivewListener;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.analyzer.ExtraPreviewFrameListener;
import com.pesdk.uisdk.analyzer.SkyAnalyzerManager;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.code.SegmentResult;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.data.model.EditModel;
import com.pesdk.uisdk.edit.DraftManager;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.edit.EditDragHandler;
import com.pesdk.uisdk.edit.OFFHandler;
import com.pesdk.uisdk.edit.listener.OnChangeDataListener;
import com.pesdk.uisdk.export.DataManager;
import com.pesdk.uisdk.export.ExportHandler;
import com.pesdk.uisdk.fragment.BlurryFragment;
import com.pesdk.uisdk.fragment.DepthFragment;
import com.pesdk.uisdk.fragment.DoodleFragment;
import com.pesdk.uisdk.fragment.FilterConfigFragment;
import com.pesdk.uisdk.fragment.FrameFragment;
import com.pesdk.uisdk.fragment.MaskFragment;
import com.pesdk.uisdk.fragment.MosaicFragment;
import com.pesdk.uisdk.fragment.OverLayFragment;
import com.pesdk.uisdk.fragment.PipFragment;
import com.pesdk.uisdk.fragment.ProportionFragment;
import com.pesdk.uisdk.fragment.RectAdjustFragment;
import com.pesdk.uisdk.fragment.StickerFragment;
import com.pesdk.uisdk.fragment.SubtitleFragment;
import com.pesdk.uisdk.fragment.callback.IDrag;
import com.pesdk.uisdk.fragment.callback.IFragmentMenuCallBack;
import com.pesdk.uisdk.fragment.canvas.CanvasFragment;
import com.pesdk.uisdk.fragment.canvas.SkyFragment;
import com.pesdk.uisdk.fragment.filter.FilterFragmentLookup;
import com.pesdk.uisdk.fragment.helper.OverLayHandler;
import com.pesdk.uisdk.fragment.main.IClickRange;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.main.MainMenuFragment;
import com.pesdk.uisdk.fragment.main.VirtualImageFragment;
import com.pesdk.uisdk.fragment.main.fg.PipLayerHandler;
import com.pesdk.uisdk.internal.SdkEntryHandler;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.IFixPreviewListener;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.ViewCallback;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.util.helper.MixHelper;
import com.pesdk.uisdk.util.helper.ModelHelperImp;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.util.helper.PipHelper;
import com.pesdk.uisdk.util.helper.ProportionHelper;
import com.pesdk.uisdk.util.helper.ProportionUtil;
import com.pesdk.uisdk.util.helper.ResultHelper;
import com.pesdk.uisdk.util.helper.sky.SkyHandler;
import com.pesdk.uisdk.util.popwind.PopAddHandler;
import com.pesdk.uisdk.util.popwind.PopPipHandler;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.uisdk.widget.edit.DragBorderLineView;
import com.pesdk.uisdk.widget.edit.EditDragView;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.EffectInfo;
import com.vecore.models.PEImageObject;

import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * 主编辑
 */
public class EditActivity extends EditBaseActivity implements IFragmentMenuCallBack, ImageHandlerListener, IEditCallback, IDrag, EditDragView.Callback, ViewCallback {

    private static final String MAIN_TAG = "main_edit_fragment";
    private String TAG = "EditActivity";
    private CanvasFragment mBackgroundFragment;
    private SkyFragment mSkyFragment;
    private OverLayFragment mOverLayFragment;
    private FrameFragment mFrameFragment;
    private MosaicFragment mMosaicFragment;
    private FilterFragmentLookup mFilterFragmentLookup;
    private SubtitleFragment mSubtitleFragment;
    private StickerFragment mStickerFragment;
    private DoodleFragment mDoodleFragment;
    private ProportionFragment mProportionFragment;
    private VirtualImageFragment mVirtualImageFragment;
    private MaskFragment mMaskFragment;
    private DepthFragment mDepthFragment;
    private PipFragment mPipFragment;
    private FilterConfigFragment mConfigFragment;
    private BlurryFragment mBlurryFragment;
    private RectAdjustFragment mRectAdjustFragment;

    private ViewGroup mChildRoot;

    @Override
    public EditDragHandler getEditDragHandler() {
        return mEditDragHandler;
    }

    @Override
    public EditDataHandler getEditDataHandler() {
        return mEditDataHandler;
    }

    @Override
    public PipLayerHandler getPipLayerHandler() {
        return mPipLayerHandler;
    }

    @Override
    public OverLayHandler getOverLayHandler() {
        return mOverLayHandler;
    }

    @Override
    public SkyHandler getSkyHandler() {
        return mSkyHandler;
    }

    private EditDragHandler mEditDragHandler;
    private EditDataHandler mEditDataHandler;//数据管理
    private PipLayerHandler mPipLayerHandler;
    private OverLayHandler mOverLayHandler;
    private SkyHandler mSkyHandler;
    private OFFHandler mOFFHandler;
    private ActivityResultLauncher mTemplateMakeLancher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RetrofitCreator.init(SdkEntry.getSdkService().getUIConfig().baseUrl);
        setContentView(R.layout.pesdk_activity_edit_home_page);
        ActivityResultContract<Void, Boolean> albumContract = SdkEntryHandler.getInstance().getMakeTemplateContract();
        if (albumContract != null) {
            mTemplateMakeLancher = registerForActivityResult(albumContract, result -> {
                if (result) {
                    finish();
                } else {
                    reBuild();
                }
            });
        }


        mEditDataHandler = new EditDataHandler(this, $(R.id.btn_revoke), $(R.id.btn_redo));
        mEditDataHandler.setListener(new OnChangeDataListener() {
            @Override
            public void onChange(boolean b) {
                Log.e(TAG, "onChange: " + b);
            }

            @Override
            public boolean isCanUndo() {
                return true;
            }

            @Override
            public void onStepChanged(boolean changed) {
            }


            @Override
            public void onUndoReduction(int build, boolean start) {
                Log.e(TAG, "onUndoReduction: " + build + " " + start);
                if (start) {
                    mVirtualImageFragment.onSelectIndex(-1, -1);
                    onSelectedItem(IMenu.MODE_PREVIEW, -1);
                } else {
                    //刷新时间
                    if (build == EditDataHandler.UNDO_BUILD_ALL) {
                        onRefresh(true);
                        mHandler.post(() -> {
                            mMainMenuFragment.onMainUI(); //菜单UI刷新
                            refreshPopPipHandler();
                        });
                    } else if (build == EditDataHandler.UNDO_BUILD_AUDIO) {
                        onRefresh(false);
                    } else {
                        EditActivity.this.getEditor().refresh();
                    }
                }
            }

            @Override
            public void setAsp(float asp) {
                Log.e(TAG, "setAsp: " + asp);
                //调整字幕、马赛克、水印、画中画
                fixDataSourceAfterReload(asp, () -> {
                    onResult();
                    SysAlertDialog.cancelLoadingDialog();
                });
            }

            @Override
            public VirtualImageView getEditor() {
                return EditActivity.this.getEditor();
            }

            @Override
            public VirtualImage getEditorVideo() {
                return EditActivity.this.getEditorImage();
            }

            @Override
            public FrameLayout getContainer() {
                return EditActivity.this.getContainer();
            }

            @Override
            public void onRefresh(boolean all) {
                EditActivity.this.onRefresh(all);
            }


            @Override
            public void onSelectData(int id) {
                Log.e(TAG, "onSelectData: " + id);
                EditActivity.this.onSelectData(id);
            }

            @Override
            public float getPlayerAsp() {
                return EditActivity.this.getPlayerAsp();
            }
        });

        mEditDragHandler = new EditDragHandler(this);
        mOFFHandler = new OFFHandler(this, mEditDragHandler, this);
        ModelHelperImp.loadModel(this);//加载抠图模型


        mMainFragmentContainer = $(R.id.mainFragmentContainer);
        mChildFragmentContainer = $(R.id.childFragmentContainer);
        mChildRoot = $(R.id.childRoot);
        initView();
        mDepthLayout = $(R.id.depth_bar_layout);
        mFilterLayout = $(R.id.filter_value_layout);
        mEditDataHandler.setShortVideoInfo(mVirtualImageInfo);

        //草稿箱管理初始化
        DraftManager.getInstance().setListener(new DraftManager.OnDraftListener() {

            @Override
            public EditDataHandler getHandler() {
                return mEditDataHandler;
            }


            @Override
            public VirtualImageView getEditor() {
                return EditActivity.this.getEditor();
            }

            @Override
            public void onEnd() {
                if (mIsExit) {
                    runOnUiThread(() -> {//草稿
                        DraftManager.getInstance().onExit();
                        popDismiss();
                        hideLoading();
                        if (TextUtils.isEmpty(mExportPath)) {
                            onToast(R.string.pesdk_auto_save_draft);
                            setResult(RESULT_CANCELED);
                            finish();
                        } else {
                            success(mExportPath);
                        }
                    });
                } else if (mExportSelect == ExportSelect.TEMPLATE_EXPORT) {
                    runOnUiThread(() -> {
                                mTemplateMakeLancher.launch(null);
                            }
                    );
                }
            }
        });
        //设置草稿
        DraftManager.getInstance().setShortInfo(mVirtualImageInfo); //首次需保存为一个新的草稿

        mVirtualImageFragment = VirtualImageFragment.newInstance(getIntent().getBooleanExtra(IntentConstants.PARAM_IS_TEMPLATE, false));
        mVirtualImageFragment.setIDrag(this);
        getSupportFragmentManager().beginTransaction().add(R.id.mainFragment, mVirtualImageFragment).commitAllowingStateLoss();
        mPipLayerHandler = new PipLayerHandler(this, new PipLayerHandler.Callback() {
            @Override
            public void prepared() {
                mMainMenuFragment.onMainUI();
            }

            @Override
            public void delete() {
                CollageInfo info = mPipLayerHandler.getTopMedia();
                if (null != info) {//删除之后更改UI,响应选中下一个Layer
                } else {
                    //没有图层，切换到背景
                    mMainMenuFragment.onMainUI();
                }
                refreshPopPipHandler();
            }

            @Override
            public void onAngleChanged() {
                if (null != mRectAdjustFragment) {
                    mRectAdjustFragment.onAngleChanged();
                }
            }
        });
        onMainFragment();
        mOverLayHandler = new OverLayHandler(this, new OverLayHandler.Callback() {

            @Override
            public void restoreItem(CollageInfo uiData) {
                if (null != mOverLayFragment) {
                    mOverLayFragment.restore(uiData);
                }
            }

            @Override
            public void delete() {
                Log.e(TAG, "delete: " + this);
                if (null != mOverLayFragment) {
                    mOverLayFragment.reset();
                }
            }

            @Override
            public boolean isMainFragment() {
                return mCurrentChildFragment == null;
            }

            @Override
            public void onAngleChanged() {

            }
        });
        mSkyHandler = new SkyHandler(this, new SkyHandler.Callback() {
            @Override
            public void prepared() {

            }

            @Override
            public void delete() {

            }

            @Override
            public boolean isMainFragment() {
                return mCurrentChildFragment == null;
            }
        });
    }

    @Override
    IResultCallback initIResultCallback() {
        return new IResultCallback() {
            @Override
            public void addLayer(String path) {
                if (!TextUtils.isEmpty(path)) {
                    preMenu(); //确认要新增需要先保存当前选中的item
                    try {
                        CollageInfo collageInfo = mPipLayerHandler.addItem(new PEImageObject(path)); //新增数据
                        mVirtualImageFragment.onSelectId(collageInfo.getId(), IMenu.pip); //编辑选中项

                        refreshPopPipHandler();
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }

            }


            @Override
            public void onEditResult(RectF clipRectF, @Crop.CropMode int mode) {
                CollageInfo collageInfo = getCurrentCollage();
                if (null != collageInfo) { //图层-裁剪
                    PEImageObject peImageObject = collageInfo.getImageObject();
                    RectF srcShowRectF = peImageObject.getShowRectF();


                    int w = mVirtualImageFragment.getEditor().getPreviewWidth();
                    int h = mVirtualImageFragment.getEditor().getPreviewHeight();
                    RectF tmp = new RectF(srcShowRectF.left * w, srcShowRectF.top * h, srcShowRectF.right * w, srcShowRectF.bottom * h);


                    if (clipRectF.width() / clipRectF.height() != tmp.width() / tmp.height()) {//可能比例变了
                        mEditDataHandler.onSaveAdjustStep(IMenu.pip);

                        RectF showRectF = ResultHelper.fixPipShowRectF(srcShowRectF, clipRectF, mVirtualImageFragment.getSubEditorParent());
                        peImageObject.setShowRectF(showRectF); //显示
                        peImageObject.setClipRectF(new RectF(clipRectF));//裁剪

                        PEImageObject bg = collageInfo.getBG();
                        if (null != bg) { //重置背景
                            bg.setShowRectF(showRectF);
                            PipHelper.applyBgClip(peImageObject, bg);
                        }

                        ImageOb ob = PEHelper.initImageOb(peImageObject);
                        ob.setCropMode(mode);

                        DataManager.upInsertCollage(getEditorImage(), collageInfo);
                        mPipLayerHandler.reEdit(collageInfo, enablePipDeleteMenu(), true); //再次编辑
                    }
                }
            }

            @Override
            public void onBeautyResult(boolean isAdd, FilterInfo filterInfo, String hairMedia) {
                if (mEditDataHandler.getEditMode() == IMenu.pip) {
                    mEditDataHandler.onSaveAdjustStep(IMenu.pip); //记录一个临时步骤
                    CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
                    if (null != collageInfo) {
                        ResultHelper.fixPipBeauty(collageInfo, filterInfo, hairMedia);
                    }
                }
                onResult();
            }

            @Override
            public void onSegmentResult(String maskPath) {
                CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
                if (null != collageInfo) {
                    mEditDataHandler.onSaveAdjustStep(IMenu.pip); //记录一个临时步骤
                    ImageOb imageOb = PEHelper.initImageOb(collageInfo.getImageObject());
                    imageOb.setMaskPath(maskPath);
                    imageOb.setSegment(Segment.SEGMENT_PERSON);
                    imageOb.setPersonResult(SegmentResult.AI_SUCCESS);
                }
                onResult();
            }


            @Override
            public void onEraseResult(PEImageObject imageObject) {
                CollageInfo collageInfo = getCurrentCollage();
                if (null != collageInfo) { //图层-消除笔
                    mEditDataHandler.onSaveAdjustStep(IMenu.pip); //记录一个临时步骤
                    DataManager.removeCollage(getEditorImage(), collageInfo); //删除旧的
                    PEImageObject peImageObject = collageInfo.getImageObject();
                    ImageOb imageOb = PEHelper.initImageOb(peImageObject);
                    RectF srcShowRectF = peImageObject.getShowRectF();
                    RectF clipRectF = imageObject.getClipRectF();
                    imageObject.setClipRectF(clipRectF);
                    imageObject.setShowRectF(srcShowRectF);

                    imageOb.setMaskPath(null); //必须清理抠图缓存
                    imageObject.setTag(imageOb);

                    collageInfo.setMedia(imageObject);

                    reBuild(); //必须rebuild(图片变了),防止抠图绑定不了
                    mPipLayerHandler.reEdit(collageInfo, enablePipDeleteMenu(), true); //再次编辑
                }
            }

            @Override
            public void replaceLayer(String path) { //替换画中画
                CollageInfo collageInfo = getCurrentCollage();
                if (null != collageInfo) { //替换
                    mEditDataHandler.onSaveAdjustStep(IMenu.pip); //记录一个临时步骤
                    DataManager.removeCollage(getEditorImage(), collageInfo); //删除旧的
                    PEImageObject peImageObject = collageInfo.getImageObject();


                    RectF srcShowRectF = peImageObject.getShowRectF();
                    try {
                        PEImageObject tmp = new PEImageObject(path);
                        ImageOb ob = PEHelper.initImageOb(peImageObject);
                        ob.setMaskPath(null);
                        ob.setCropMode(Crop.CROP_ORIGINAL);
                        tmp.setTag(ob);

                        RectF showRectF = ResultHelper.fixReplacePipShowRectF(srcShowRectF, tmp.getWidth() * 1f / tmp.getHeight(), mVirtualImageFragment.getSubEditorParent());
                        tmp.setShowRectF(showRectF);


                        collageInfo.setMedia(tmp);
                        DataManager.upInsertCollage(getEditorImage(), collageInfo);
                        updateSegment(collageInfo, tmp);
                        //替换画中画，再抠图异常
                        mPipLayerHandler.reEdit(collageInfo, enablePipDeleteMenu(), true); //再次编辑

                        refreshPopPipHandler();
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 更新抠图对象
     *
     * @param collageInfo
     * @param dst
     */
    private void updateSegment(CollageInfo collageInfo, PEImageObject dst) {
        ExtraPreviewFrameListener listener = AnalyzerManager.getInstance().getFrameListener(collageInfo);
        if (null != listener) {
            listener.update(dst); //更新抠图对象
        }
    }

    @IMenu
    @Override
    public int getMenu() {
        return mMainMenuFragment.getMenu();
    }

    @Override
    public View getDepth() {
        return mDepthLayout;
    }

    @Override
    public View getFilterValueGroup() {
        return mFilterLayout;
    }


    /**
     * 导出选项
     */
    private enum ExportSelect {
        //默认导出、模板导出、示例导出、封面
        DEFAULT_EXPORT, TEMPLATE_EXPORT, EXAMPLE_EXPORT, EDIT_COVER
    }

    /**
     * 导出选项
     */
    private ExportSelect mExportSelect = ExportSelect.DEFAULT_EXPORT;

    private PopAddHandler mPopAddHandler;
    private PopPipHandler mPopPipHandler;
    private ImageView mBtnAdd;

    private void popDismiss() {
        if (null != mPopPipHandler) {
            mPopPipHandler.dismiss();
            mPopPipHandler = null;
            mBtnAdd.setImageResource(R.drawable.pesdk_ic_layer_un);
        }
    }

    @Override
    public void initView() {
        super.initView();
        $(R.id.save_btn).setOnClickListener(v -> {//保存按钮
            preMenu();
            prepareExport();
        });
        mBtnAdd = $(R.id.btn_add);
        mBtnAdd.setOnClickListener(v -> {
            if (null != mPopPipHandler && mPopPipHandler.isShowing()) { //关闭pop
                popDismiss();
                return;
            }
            mPopPipHandler = new PopPipHandler();
            mPopPipHandler.show(this, v, this, new PopPipHandler.ICallback() {
                @Override
                public void onAddLayer() {
                    onPopAdd();
                }

                @Override
                public void onMerge() {
                    onMergeLayer();
                }
            });
            mBtnAdd.setImageResource(R.drawable.pesdk_ic_layer_n);
        });
        View tmp = $(R.id.btnExportTemplate);
        if (null != mTemplateMakeLancher) {
            tmp.setVisibility(View.VISIBLE);
            tmp.setOnClickListener(v -> {//保存按钮
                preMenu();
                mExportSelect = ExportSelect.TEMPLATE_EXPORT;
                DraftManager.getInstance().onSaveDraftAll();
            });
        } else {
            tmp.setVisibility(View.GONE);
        }

        // 退出按钮
        $(R.id.back_btn).setOnClickListener(v -> onBackPressed());
        mMainMenuFragment = MainMenuFragment.newInstance();
    }


    /***
     * 新增
     */
    private void onPopAdd() {
        if (null == mPopAddHandler) {
            mPopAddHandler = new PopAddHandler();
        }
        mPopAddHandler.show(this, mBtnAdd, true, new PopAddHandler.ICallback() {
            @Override
            public void onOverlay() {
                mMainMenuFragment.addOverlay();
            }

            @Override
            public void onText() {
                mMainMenuFragment.addText();
            }

            @Override
            public void onSticker() {
                mMainMenuFragment.addSticker();
            }

            @Override
            public void onPic() {
                mMainMenuFragment.addLayer();
            }

            @Override
            public void onDismiss() {
                mPopPipHandler.reset();
            }
        });

    }

    @Override
    public void onFilterChange() {
        mVirtualImageFragment.changeFilterList(mEditDataHandler.getEffectList());
    }


    @Override
    public EditDataHandler getParamHandler() {
        return mEditDataHandler;
    }


    @Override
    public PipLayerHandler getForeground() {
        return mPipLayerHandler;
    }

    @Override
    public OverLayHandler getOverLayerHandler() {
        return mOverLayHandler;
    }

    @Override
    public void reBuild() {
        getCurrentChildFragment().rebuild();
    }


    @Override
    public void onExitSelect() {
        mMainMenuFragment.onSelectItem(IMenu.MODE_PREVIEW, -1);
        if (null != mOverLayFragment) {
            mOverLayFragment.reset();
        }
        refreshPopPipHandler();
    }

    @Override
    public int getDuration() {
        return 1000;
    }


    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void onBack() {
        if (mCurrentChildFragment instanceof CanvasFragment || mCurrentChildFragment instanceof SkyFragment) {
            onMainFragment();
        }
    }

    /**
     * 退出按钮
     */
    @Override
    public void onBackPressed() {
        if (null != mCurrentChildFragment) {
            mCurrentChildFragment.onCancelClick();
            return;
        } else {
            onBackPressedImp();
        }
    }

    private void onBackPressedImp() {
        // 图片还未被保存
        preMenu();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pesdk_exit_without_save).setCancelable(false).setPositiveButton(R.string.pesdk_confirm, (dialog, id) -> {
            showLoading(R.string.pesdk_isDrafting);
            onExitDraft();
        }).setNegativeButton(R.string.pesdk_cancel, (dialog, id) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean mIsExit = false;
    private String mExportPath;

    /**
     * 保存到草稿箱 退出
     */
    private void onExitDraft() {
        mIsExit = true;
        DraftManager.getInstance().onSaveDraftAll();
    }

    private void showLoading(@StringRes int strId) {
        SysAlertDialog.showLoadingDialog(this, strId);
    }

    private void hideLoading() {
        SysAlertDialog.cancelLoadingDialog();
    }


    @Override
    public void preMenu() {
        if (mEditDragHandler.onSave()) {//退出选中:文字、贴纸
            resetSelectedItem();
        } else if (mVirtualImageFragment.getEditHandler() != null && mVirtualImageFragment.getEditHandler().exitOverPip()) {//退出选中:图层、叠加
            resetSelectedItem();
            refreshPopPipHandler();
        }
    }

    @Override
    public void onAddLayer() {
        getCurrentChildFragment().checkContainerVisible();
        mRegisterManger.onAddLayer(this);
    }

    @Override
    public void onSticker(StickerInfo info) {
        mMainMenuFragment.setMenuEdit(IMenu.sticker);
        mStickerFragment = StickerFragment.newInstance();
        if (null != info) { //保存当前贴纸效果
            getParamHandler().onSaveAdjustStep(IMenu.sticker);
        }
        mStickerFragment.setEditInfo(info);
        swtichFragment(mStickerFragment);
    }

    @Override
    public void onText(WordInfoExt infoExt) {
        mMainMenuFragment.setMenuEdit(IMenu.text);
        mSubtitleFragment = SubtitleFragment.newInstance();
        mSubtitleFragment.setEditInfo(infoExt);
        mSubtitleFragment.setFragmentContainer(mChildRoot);
        getCurrentChildFragment().checkContainerVisible();
        new Handler().postDelayed(() -> swtichFragment(mSubtitleFragment), 200);
    }

    private View mFilterLayout;

    @Override
    public void onFilter() {
        mMainMenuFragment.setMenuEdit(IMenu.filter);
        mFilterFragmentLookup = FilterFragmentLookup.newInstance();
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //图层-滤镜
            mPipLayerHandler.lockItem();
            mEditDataHandler.setEditMode(IMenu.pip);
            mFilterFragmentLookup.setLayerInfo(collageInfo.getImageObject());
        } else { //背景-滤镜
            mFilterFragmentLookup.setFilterInfo(mEditDataHandler.getExtImage().getFilter());
        }
        swtichFragment(mFilterFragmentLookup);
    }

    @Override
    public void onAdjust() {
        mConfigFragment = FilterConfigFragment.newInstance();
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //图层-调色
            mPipLayerHandler.lockItem();
            mEditDataHandler.setEditMode(IMenu.pip);
            mConfigFragment.setPIP(collageInfo.getImageObject());
        } else { //背景-调色
            mConfigFragment.setFilterInfo(mEditDataHandler.getExtImage().getAdjust());
        }
        swtichFragment(mConfigFragment);
    }

    @Override
    public void onBeauty() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //图层-美颜
            mEditDataHandler.setEditMode(IMenu.pip);
            mRegisterManger.onBeauty(this, collageInfo.getImageObject(), true);
        }
    }


    @Override
    public void onBlur() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //图层-模糊度
            mPipLayerHandler.lockItem();
            mBlurryFragment = BlurryFragment.newInstance();
            mBlurryFragment.setData(collageInfo);
            swtichFragment(mBlurryFragment);
        } else { //背景-模糊
            PEImageObject bg = getParamHandler().getExtImage().getBackground();
            if (null != bg) {
                mBlurryFragment = BlurryFragment.newInstance();
                mBlurryFragment.setBGData(bg);
                swtichFragment(mBlurryFragment);
            } else {
                onToast(R.string.pesdk_no_bg_blur);
            }
        }
    }

    @Override
    public void onProportion() {
        { //保存当前信息
            preMenu();
        }
        mProportionFragment = ProportionFragment.newInstance();
        ProportionHelper helper = new ProportionHelper();
        helper.setCallback(mProportionFragment, this, this, this);
        mProportionFragment.setCallback(helper);
        swtichFragment(mProportionFragment);
    }

    @Override
    public void onCrop() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //图层-裁剪
            PEImageObject peImageObject = collageInfo.getImageObject();
            mRegisterManger.onEdit(this, peImageObject, peImageObject.getWidth() * 1.0f / peImageObject.getHeight());
        } else {
            Log.e(TAG, "onCrop: null");
        }
    }

    @Override
    public void onGraffiti() {
        mDoodleFragment = DoodleFragment.newInstance();
        mDoodleFragment.setDoodleView(getCurrentChildFragment().getPaintView());
        mDoodleFragment.setMenuRevokeLayout(mFragmentRevokeLayout);
        swtichFragment(mDoodleFragment);
    }


    @Override
    public void onWatermark() {
//        if (null == mWatermarkFragment) {
//            mWatermarkFragment = new WatermarkFragment();
//        }
//        mWatermarkFragment.setContainer(getContainer());
//        swtichFragment(mWatermarkFragment);
    }

    @Override
    public void onPip() {
        preMenu();
        mPipLayerHandler.exitEditMode();
        mMainMenuFragment.setMenuEdit(IMenu.pip);
        mPipFragment = PipFragment.newInstance();
        swtichFragment(mPipFragment);
    }


    @Override
    public void onErase() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //图层-消除笔
            mRegisterManger.onErase(this, collageInfo.getImageObject());
        } else {
            Log.e(TAG, "onErase: null");
        }
    }


    @Override
    public void onCanvas() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        mBackgroundFragment = CanvasFragment.newInstance();
        if (null != collageInfo) { //图层-背景(必须加载抠图)
            ModelHelperImp.checkAnalyzer(this, () -> {
                mPipLayerHandler.lockItem();
                mBackgroundFragment.setCollageInfo(collageInfo);
                swtichFragment(mBackgroundFragment);
            });
        } else {
            preMenu();
            mMainMenuFragment.setMenuEdit(IMenu.canvas);
            mBackgroundFragment.setPEScene(mEditDataHandler.getExtImage());
            swtichFragment(mBackgroundFragment);
        }

    }

    private View mDepthLayout;


    @Override
    public void onSky() {
        ModelHelperImp.checkSkyAnalyzer(this, () -> onSkyImp());
    }

    private void onSkyImp() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        PEImageObject imageObject = null;
        mSkyFragment = SkyFragment.newInstance();
        if (null != collageInfo) { //图层-天空
            imageObject = collageInfo.getImageObject();
            mPipLayerHandler.lockItem();
            mSkyFragment.setCollageInfo(collageInfo);
        }
        if (null == imageObject) {
            Log.e(TAG, "onSkyImp: null");
            return;
        }
        ImageOb imageOb = PEHelper.initImageOb(imageObject);
        if (imageOb.getSkyResult() == SegmentResult.None) {
            SkyAnalyzerManager.getInstance().hasSegment(this, imageObject, exist -> {
                imageOb.setSkyResult(exist ? SegmentResult.AI_SUCCESS : SegmentResult.AI_FAILED);
                if (!exist) {
                    onToast(R.string.pesdk_toast_sky_segment);
                }
            });
        }
        swtichFragment(mSkyFragment);
    }

    @Override
    public void onKoutu() {
        ModelHelperImp.checkAnalyzer(this, () -> onKoutuImp());
    }


    private CollageInfo getCurrentCollage() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null == collageInfo) {
            collageInfo = mEditDataHandler.getBaseCollage();
        }
        return collageInfo;
    }

    private void onKoutuImp() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        PEImageObject imageObject;
        if (null != collageInfo) { //图层-背景
            imageObject = collageInfo.getImageObject();
            mRegisterManger.onSegment(this, imageObject, true);
        } else {
            Log.e(TAG, "onKoutuImp: null");
        }
    }


    @Override
    public void onOverlay() {
        mOverLayFragment = OverLayFragment.newInstance();
        swtichFragment(mOverLayFragment);
    }

    @Override
    public void onFrame() {
        mPipLayerHandler.lockItem();
        mFrameFragment = FrameFragment.newInstance();
        swtichFragment(mFrameFragment);
    }

    @Override
    public void onMosaic() {
        if (null == mMosaicFragment) {
            mMosaicFragment = MosaicFragment.newInstance();
        }
        swtichFragment(mMosaicFragment);
    }


    @Override
    public void onMask() {
        mMaskFragment = MaskFragment.newInstance();
        mPipLayerHandler.lockItem();
        mPipLayerHandler.setUnavailable(true);//屏蔽所有touch事件，与蒙版事件冲突
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) {
            mMaskFragment.setPEImage(collageInfo.getImageObject());
            mMaskFragment.setLinearWords(getContainer());
            swtichFragment(mMaskFragment);
        }
    }

    @Override
    public void onDepth() {
        mDepthFragment = DepthFragment.newInstance();
        mDepthFragment.setMenuRevokeLayout(mFragmentRevokeLayout);
        mPipLayerHandler.exitEditMode();
        swtichFragment(mDepthFragment);
    }

    @Override
    public void onMirrorLeftright() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //画中画
            getEditDataHandler().onSaveAdjustStep(IMenu.pip);
            EditModel editModel = new EditModel();
            editModel.applyMirror(true, collageInfo.getImageObject());
        }
    }

    @Override
    public void onMirrorUpDown() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //画中画
            getEditDataHandler().onSaveAdjustStep(IMenu.pip);
            EditModel editModel = new EditModel();
            editModel.applyMirror(false, collageInfo.getImageObject());
        }
    }

    @Override
    public void onReplace() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) { //替换图层
            mRegisterManger.replaceLayer();
        } else if (mOverLayHandler.getCurrentCollageInfo() != null) { //替换叠加
            collageInfo = mOverLayHandler.getCurrentCollageInfo();
            if (null != collageInfo) {
                getEditDataHandler().onSaveAdjustStep(IMenu.overlay);
                mOverLayFragment = OverLayFragment.newInstance();
                mOverLayFragment.edit(collageInfo);
                swtichFragment(mOverLayFragment);
            }
        } else if (mEditDragHandler.getEditWord() != null) { //替换文字
            WordInfoExt word = mEditDragHandler.getEditWord();
            if (null != word) {
                onText(word);
            }
        } else if (mEditDragHandler.getEditSticker() != null) { //替换贴纸
            StickerInfo sticker = mEditDragHandler.getEditSticker();
            if (null != sticker) {
                onSticker(sticker);
            }
        }
    }

    @Override
    public void onRectAdjust() { //仅画中画支持
        mRectAdjustFragment = RectAdjustFragment.newInstance();
        mRectAdjustFragment.setFg(mOFFHandler);
        swtichFragment(mRectAdjustFragment);
    }

    @Override
    public void onBack2Main() {
        CollageInfo collageInfo = mPipLayerHandler.getCurrentCollageInfo();
        if (null != collageInfo) {
            mPipLayerHandler.exit2Main();
        } else if (null != mOverLayHandler.getCurrentCollageInfo()) {
            mOverLayHandler.exit2Main();
        }
    }

    /**
     * 响应图片合成
     */
    private void onMergeLayer() {
        preMenu(); //退出贴纸选中..
        MixHelper mixHelper = new MixHelper();
        mixHelper.onMix(this, mEditDataHandler, path -> { //合并成功
            mEditDataHandler.onMixStep(path);
            reBuild();
            refreshPopPipHandler();
        });
    }

    /**
     * 根据新的预览比例，修正（字幕、贴纸、画中画 、水印）
     */
    @Override
    public void fixDataSourceAfterReload(float newAsp, IFixPreviewListener fixPreviewListener) {
        //旧的比例
        int oldW = mVirtualImageFragment.getSubEditorParent().getWidth();
        int oldH = mVirtualImageFragment.getSubEditorParent().getHeight();
        float oldAsp = (float) oldW / oldH;
        Log.e(TAG, "fixDataSourceAfterReload: " + oldW + "*" + oldH + " " + oldAsp + " <> " + newAsp);
        //重新修正比例
        if (mEditDataHandler.proportionChanged(oldAsp, newAsp)) {
            //重新计算预览size
            VirtualImage.Size size = mVirtualImageFragment.fixPreviewSize(newAsp, null);
//            VirtualImage.Size size = mVirtualImageFragment.fixPreviewSize(newAsp, mEditDataHandler.getPEScene().getPEImageObject());
            Log.e(TAG, "fixDataSourceAfterReload: " + oldW + "*" + oldH + " " + oldAsp + " <> " + newAsp + "  " + size);
            //修正容器比例
            mVirtualImageFragment.fixContainerAspRatio(size.width / (size.height + 0.0f));
            SysAlertDialog.showLoadingDialog(this, R.string.pesdk_loading);
            mMainFragmentContainer.postDelayed(() -> {
                int[] old = new int[]{oldW, oldH};
                int[] dst = new int[]{mVirtualImageFragment.getSubEditorParent().getWidth(), mVirtualImageFragment.getSubEditorParent().getHeight()};

//                //修正媒体显示位置
//                ProportionFragmentModel model = new ProportionFragmentModel();
//                model.fixMediaShowRectF(this, mEditDataHandler.getPEScene(), old, dst);

                //修正贴纸等显示位置
                float aspOld = old[0] * 1.0f / old[1];
                ProportionUtil.onFixResources(aspOld, dst, size, mEditDataHandler, getCurrentChildFragment().getEditorImage(), getCurrentChildFragment().getEditor(), () -> {
                    SysAlertDialog.cancelLoadingDialog();
                    fixPreviewListener.onComplete();
                });
            }, 200);
        } else {
            fixPreviewListener.onComplete();
        }
    }

    @Override
    public float getPlayerAsp() {
        return getEditor().getPreviewWidth() / (getEditor().getPreviewHeight() + 0.0f);
    }


    private void onMainFragment() {
        mPipLayerHandler.unLockItem();
        mPipLayerHandler.setUnavailable(false); //恢复所有touch事件
        mMainMenuFragment.resetMainEdit();
        swtichFragment(mMainMenuFragment);
        restoreMenu();
    }


    private void restoreMenu() {
//            mPipLayerHandler.exitEditMode();
//            boolean enableDelete = getEditDataHandler().enablePipDeleteMenu();
//            CollageInfo tmp = mPipLayerHandler.restoreFg(enableDelete, true);
//            Log.e(TAG, "restoreMenu: " + enableDelete + " >" + tmp);
//        Log.e(TAG, "restoreMenu: " + mPipLayerHandler.getCurrentCollageInfo());
        if (null != mPipLayerHandler.getCurrentCollageInfo()) {
            mMainMenuFragment.onPipSelected();
        } else {
            mMainMenuFragment.onMainUI();
        }
        if (null != mPopPipHandler) {
            mPopPipHandler.restore();
            refreshPopPipHandler();
        }
    }

    private void swtichFragment(Fragment childFragment) {
        getCurrentChildFragment().checkContainerVisible();
        if (childFragment instanceof MainMenuFragment) { //主界面
            mVirtualImageFragment.setClickEnabled(IClickRange.range_all);
            mFragmentRevokeLayout.setVisibility(View.GONE);
            mEditDataHandler.setEditMode(IMenu.MODE_PREVIEW);
            if (mMainFragmentContainer.getVisibility() != View.VISIBLE) {
                mMainFragmentContainer.setVisibility(View.VISIBLE);
            }
            if (mChildRoot.getVisibility() != View.GONE) {
                exitChildFragment(mChildRoot);
            }
            ObjectAnimator animation = ObjectAnimator.ofFloat($(R.id.scrollFrame), "translationY", 0);
            animation.setDuration(MAX_ANIM_DURATION);
            animation.start();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(MAIN_TAG);
            Log.e(TAG, "swtichFragment: " + fragment + " dst:" + childFragment);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (null != fragment) {
                transaction.show(fragment); //显示
            } else {
                transaction.add(R.id.mainFragmentContainer, childFragment, MAIN_TAG);
            }
            transaction.commitAllowingStateLoss();
            ((MainMenuFragment) childFragment).setProportionEanble(mEditDataHandler.getParam().getFrameList().size() == 0);
            mHandler.post(() -> recycleChildFragment());
        } else { //子功能界面
            if (null != mPopPipHandler) {
                mPopPipHandler.hide();
            }
            mVirtualImageFragment.setClickEnabled(childFragment instanceof OverLayFragment ? IClickRange.range_overlay : IClickRange.range_none); //叠加允许点击，用于切换选中
            initChildFragmentVG(mChildFragmentContainer, (childFragment instanceof SubtitleFragment ||
                    childFragment instanceof CanvasFragment || childFragment instanceof OverLayFragment ||
                    childFragment instanceof BlurryFragment ||
                    childFragment instanceof MaskFragment)); //字幕、背景、天空、叠加、图层 区域需要更大
            ObjectAnimator animation = ObjectAnimator.ofFloat($(R.id.scrollFrame), "translationY", -mTopScrollHeight);
            animation.setDuration(MAX_ANIM_DURATION);
            animation.start();
            mChildRoot.setVisibility(View.VISIBLE);

            enterChildFragment(mChildRoot, (com.pesdk.uisdk.fragment.BaseFragment) childFragment);
            if (childFragment instanceof DoodleFragment || childFragment instanceof DepthFragment) {
                mFragmentRevokeLayout.setVisibility(View.VISIBLE);
                mFragmentRevokeLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pesdk_slide_in));
            } else {
                mFragmentRevokeLayout.setVisibility(View.GONE);
            }
            getSupportFragmentManager().beginTransaction().hide(mMainMenuFragment).commitAllowingStateLoss(); //隐藏主菜单
        }
    }


    @Override
    public void onCancel() {
        int tmp = mMainMenuFragment.getMenu();
        if (tmp == IMenu.graffiti) {//涂鸦
            getCurrentChildFragment().getPaintView().reset();
        }
        onMainFragment();
    }


    @Override
    public void onSure() {
        onMainFragment();
    }

    private void onResult() {
        getCurrentChildFragment().rebuild();
        onMainFragment();
    }


    private VirtualImageFragment getCurrentChildFragment() {
        return mVirtualImageFragment;
    }


    @Override
    public void onSure(boolean fresh) {
        onSure();
    }

    @Override
    public void onRefresh(boolean all) {
        getCurrentChildFragment().rebuild();
    }


    @Override
    public VirtualImageView getEditor() {
        return getCurrentChildFragment().getEditor();
    }

    @Override
    public VirtualImage getEditorImage() {
        return getCurrentChildFragment().getEditorImage();
    }


    @Override
    public FrameLayout getContainer() {
        VirtualImageFragment fragment = getCurrentChildFragment();
        if (null != fragment) {
            return fragment.getSubEditorParent();
        }
        return null;
    }

    @Override
    public FrameLayout getPlayerContainer() {
        VirtualImageFragment fragment = getCurrentChildFragment();
        if (null != fragment) {
            return fragment.getPlayerContainer();
        }
        return null;
    }

    @Override
    public DragBorderLineView getLineView() {
        VirtualImageFragment fragment = getCurrentChildFragment();
        if (null != fragment) {
            return fragment.getLineView();
        }
        return null;
    }


    @Override
    public void onSelectData(int id) {
        Log.e(TAG, "onSelectData: " + id);
        mVirtualImageFragment.onSelectData(id);
    }

    @Override
    public void onSelectedItem(@IMenu int mode, int index) {
        Log.e(TAG, "onSelectedItem: " + mode + " >" + index);
        if (mode == IMenu.MODE_PREVIEW) {
            resetSelectedItem();
        } else {
            mMainMenuFragment.onSelectItem(mode, index);
            if (mode == IMenu.text) { //文字
                mOFFHandler.onSelectedItem(mEditDataHandler.getWordNewInfo(index));
            } else if (mode == IMenu.sticker) {//素材
                mOFFHandler.onSelectedItem(mEditDataHandler.getStickerInfo(index));
            } else if (mode == IMenu.pip) {//图层
                mOFFHandler.onSelectedItem(mPipLayerHandler);
            } else if (mode == IMenu.overlay) {//叠加
                mOFFHandler.onSelectedItem(mOverLayHandler);
            }
        }
        refreshPopPipHandler();
    }

    private void refreshPopPipHandler() {
        if (null != mPopPipHandler) { //刷新选中项
            mPopPipHandler.refresh();
        }
    }

    @Override
    public int getIndex(@IMenu int mode, int id) {
        return mVirtualImageFragment.getEditHandler().getIndex(mode, id);
    }

    /**
     * 退出选中
     */
    private void resetSelectedItem() {
        mOFFHandler.reset();
        mMainMenuFragment.onSelectItem(IMenu.MODE_PREVIEW, -1);
    }


    @Override
    public void onChangeEffectFilter() {
        onFilterChange();
    }


    @Override
    public VirtualIImageInfo getVirtualImageInfo() {
        return mVirtualImageInfo;
    }


    /**
     * 预览特效
     */
    @Override
    public void onEffect(EffectInfo effectInfo) {
        //清理播放器中已加载的全部特效 （实时预览必须先删除再新增）
        getCurrentChildFragment().getEditorImage().clearEffects(getCurrentChildFragment().getEditor());
        //特效
        ArrayList<EffectInfo> tmp = mEditDataHandler.getEffectAndFilter();
        tmp.add(effectInfo);
        DataManager.loadEffects(getCurrentChildFragment().getEditorImage(), tmp);
        getCurrentChildFragment().getEditorImage().updateEffects(getCurrentChildFragment().getEditor());
    }

    @Override
    public void registerListener(PreivewListener listener) {
        mVirtualImageFragment.registerListener(listener);
    }

    @Override
    public void unregisterListener(PreivewListener listener) {
        mVirtualImageFragment.unregisterListener(listener);
    }

    @Override
    public void deleted(int type, int id) {
        if (getMenu() != IMenu.MODE_PREVIEW) {
            onMainFragment();
        }
        mMainMenuFragment.onSelectItem(IMenu.MODE_PREVIEW, -1); //删除对象后，退出选中状态
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        popDismiss();
        getSupportFragmentManager().beginTransaction().remove(mVirtualImageFragment).remove(mMainMenuFragment).commitAllowingStateLoss();
        recycleChildFragment();

        mChildRoot = null;
        mChildFragmentContainer = null;
        mEditDataHandler = null;
        mEditDragHandler = null;
        mOFFHandler = null;


        mVirtualImageInfo = null;
        mVirtualImageFragment = null;
        mMainMenuFragment = null;

        mFilterLayout = null;
        mDepthLayout = null;

        AnalyzerManager.getInstance().release();
        SkyAnalyzerManager.getInstance().release();
        Glide.get(this).clearMemory();
        System.runFinalization();
        System.gc();
    }

    @Override
    public ExportConfiguration getExportConfig() {
        return SdkEntry.getSdkService().getExportConfig();
    }

    @Override
    public void onExport() {
        ExportHandler exportHandler = new ExportHandler(this, mVirtualImageInfo, path -> {
            mExportPath = path;
            onExitDraft();
        });
        exportHandler.export(mEditDataHandler, withWatermark);
    }

    private void success(String path) {
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, path);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


    /**
     * 退出子Fragment时，清理变量。
     */
    private void recycleChildFragment() {
        mConfigFragment = null;
        mSkyFragment = null;
        mBackgroundFragment = null;
        mSubtitleFragment = null;
        mStickerFragment = null;
        mFilterFragmentLookup = null;
        mOverLayFragment = null;
        mDepthFragment = null;
        mDoodleFragment = null;
        mMosaicFragment = null;
        mMaskFragment = null;
        mPipFragment = null;
        mFrameFragment = null;
        mBlurryFragment = null;
    }

    @Override
    public boolean enablePipDeleteMenu() {
        return getEditDataHandler().enablePipDeleteMenu();
    }

    @Override
    public boolean enableAutoExit() {
        return mCurrentChildFragment == null;
    }
}
