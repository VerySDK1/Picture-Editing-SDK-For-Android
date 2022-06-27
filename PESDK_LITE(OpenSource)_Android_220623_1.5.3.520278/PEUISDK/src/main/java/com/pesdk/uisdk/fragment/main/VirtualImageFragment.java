package com.pesdk.uisdk.fragment.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.pesdk.uisdk.Interface.PreivewListener;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.edit.EditDragHandler;
import com.pesdk.uisdk.export.DataManager;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.fragment.callback.IDrag;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.IntentConstants;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.helper.PlayerAspHelper;
import com.pesdk.uisdk.util.helper.RestoreTemplateHelper;
import com.pesdk.uisdk.widget.doodle.DoodleView;
import com.pesdk.uisdk.widget.edit.DragBorderLineView;
import com.pesdk.uisdk.widget.edit.EditDragView;
import com.vecore.PlayerControl;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.ui.PreviewFrameLayout;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.exception.InvalidStateException;
import com.vecore.models.EffectInfo;
import com.vecore.models.PEImageObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
public class VirtualImageFragment extends AbsBaseFragment {

    public static VirtualImageFragment newInstance(boolean isTemplate) {

        Bundle args = new Bundle();

        VirtualImageFragment fragment = new VirtualImageFragment();
        args.putBoolean(IntentConstants.PARAM_IS_TEMPLATE, isTemplate);
        fragment.setArguments(args);

        return fragment;
    }

    private FrameLayout mLlContainer;
    private VirtualImage mVirtualImage;
    private VirtualImageView mVirtualImageView;
    private PreviewFrameLayout mPreviewFrameLayout;
    private DoodleView mPaintView;//涂鸦模式画板

    private IEditCallback mCallback;
    private ImageHandlerListener mVideoHandlerListener;
    private DragBorderLineView mDragBorderView;

    private MenuCallback mMenuCallback;
    private EditHandler mEditHandler;
    /**
     * 媒体拖动
     */
    private SparseArray<PreivewListener> mSaListener = new SparseArray<>();
    /**
     * true  模板需要首次build后, 恢复字幕、贴纸等
     */
    private boolean isTemplateRestore = false;
    private boolean prepareEditMedia = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            isTemplateRestore = bundle.getBoolean(IntentConstants.PARAM_IS_TEMPLATE, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_main_virtual_layout, container, false);
        return mRoot;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (IEditCallback) context;
        mMenuCallback = (MenuCallback) context;
        mVideoHandlerListener = (ImageHandlerListener) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        checkContainerVisible();
        mVirtualImageView.setOnInfoListener(mOnInfoListener);
        mVirtualImageView.setOnPlaybackListener(new VirtualImageView.VirtualViewListener() {
            @Override
            public void onPrepared() {
                LogUtil.i(TAG, "onPrepared: " + mVirtualImageView.getPreviewWidth() + "*" + mVirtualImageView.getPreviewHeight());
                if (isFirstBuild) {
                    isFirstBuild = false;
                    float asp = (mVirtualImageView.getPreviewWidth() + 0.0f) / mVirtualImageView.getPreviewHeight();
                    fixContainerAspRatio(asp);
                }
                int len = mSaListener.size();
                for (int nTmp = 0; nTmp < len; nTmp++) {
                    mSaListener.valueAt(nTmp).onEditorPrepred();
                }
                CommonStyleUtils.init(getSubEditorParent().getWidth(), getSubEditorParent().getHeight());

                //第一次加载
                if (isTemplateRestore) { //仅模板恢复时
                    isTemplateRestore = false;
                    mHandler.sendEmptyMessageDelayed(MSG_RESTORE_TEMPLATE, 500);
                } else if (prepareEditMedia) { //准备就绪后,允许拖拽底图
                    prepareEditMedia = false;
                    mHandler.sendEmptyMessageDelayed(MSG_PREPARED_DRAG, 200);
                }
            }

            @Override
            public boolean onError(int what, int extra, String errorInfo) {
                Log.e(TAG, "onError: " + what + " " + extra + " " + errorInfo);
                onToast("code:" + what + "  " + getString(R.string.pesdk_preview_error));
                mHandler.sendEmptyMessage(MSG_PLAYER_FAILED);
                return true;
            }
        });
        initDrag();
        mEditHandler = new EditHandler(mLlContainer, mCallback, mMenuCallback, mVideoHandlerListener, mPreviewFrameLayout, mCallback.getEditDragHandler());
        isFirstBuild = true;
        prepareEditMedia = true;
        rebuild();
    }

    private boolean isFirstBuild = true;

    public void registerListener(PreivewListener listener) {
        mSaListener.append(listener.hashCode(), listener);
    }

    public void unregisterListener(PreivewListener listener) {
        mSaListener.remove(listener.hashCode());
    }

    /**
     * 计算预览比例
     */
    public VirtualImage.Size fixPreviewSize(float asp, PEImageObject base) {
        if (asp == 0) {           //未指定比例(无裁剪)
//            asp = base.getWidth() / (0.0f + base.getHeight());
            Log.e(TAG, "fixPreviewSize: xxxx" + base);
        }
        int maxWH = mVirtualImageView.getPreviewMaxWH();
        if (asp >= 1) {
            return new VirtualImage.Size(maxWH, (int) (maxWH / asp));
        } else {
            return new VirtualImage.Size((int) (maxWH * asp), maxWH);
        }
    }

    /**
     * 修正父容器的比例
     */
    public void fixContainerAspRatio(float asp) {
        mPreviewFrameLayout.setAspectRatio(asp);
    }

    public DoodleView getPaintView() {
        return mPaintView;
    }

    public void checkContainerVisible() {
        if (null != mCallback && null != mLlContainer && null != mPaintView) {
            int menu = mCallback.getMenu();
            mLlContainer.setVisibility(1 == 1 ? View.VISIBLE : View.GONE);
//            mLinearWords.setVisibility((menu == IMenu.addLayer || menu == IMenu.text || menu == IMenu.sticker || menu == IMenu.watermark || menu == IMenu.layer || menu == IMenu.canvas || menu == IMenu.mosaic) ? View.VISIBLE : View.GONE);
            mPaintView.setVisibility(menu == IMenu.graffiti ? View.VISIBLE : View.GONE);
            mPaintView.reset();
        }
    }

    private void initView() {
        mPipGroup = $(R.id.mPipGroup);
        mDragBorderView = $(R.id.dblView);
        mVirtualImageView = $(R.id.mVirtualImageView);
        mVirtualImage = new VirtualImage();
        mPreviewFrameLayout = $(R.id.contentViewLayout);
        mLlContainer = $(R.id.linear_words);
        mPaintView = $(R.id.custom_paint_view);

        //点击屏幕
        mPreviewFrameLayout.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getPointerCount() == 1) {
                float downX = motionEvent.getX();
                float downY = motionEvent.getY();
                Log.e(TAG, "initView: " + downX + "*" + downY);
                onClickVideo(downX, downY);
            }
            return false;
        });
    }


    /**
     * //     * @param enabled true 主界面; false 进入子Fragment时，禁用点击切换元素
     */
    public void setClickEnabled(@IClickRange int clickRange) {
        if (null != mPreviewFrameLayout) {
            if (clickRange == IClickRange.range_none) {
                mPreviewFrameLayout.setEnabled(false);
            } else {
                mPreviewFrameLayout.setEnabled(true);
                mEditHandler.setClickRange(clickRange);
            }
        }
    }


    /**
     * 点击屏幕任意位置，响应元素编辑
     *
     * @param downX
     * @param downY
     */
    private boolean onClickVideo(float downX, float downY) {
        return mEditHandler.onClickVideo(downX, downY);
    }

    public EditHandler getEditHandler() {
        return mEditHandler;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(MSG_RESTORE_TEMPLATE);
        mHandler.removeMessages(MSG_PREPARED_DRAG);
        if (null != mVirtualImageView) {
            mVirtualImageView.cleanUp();
            mVirtualImageView.reset();
            mVirtualImageView = null;
        }
        if (null != mVirtualImage) {
            mVirtualImage.reset();
            mVirtualImage = null;
        }
        mRoot = null;
        mEditHandler = null;
    }

    private EditDragView mEditDragView;


    private void initDrag() {
        //编辑框
        mEditDragView = $(R.id.drag_sticker);
        mEditDragView.setCallback((EditDragView.Callback) getActivity());
        //贴纸、字幕
        mCallback.getEditDragHandler().setDragView(mEditDragView, mDragBorderView);
        mCallback.getEditDragHandler().setFrameListener(new EditDragHandler.OnEditFrameListener() {

            @Override
            public void onDeleted(int menu, int id) {
                Log.e(TAG, "onDelete: " + this);
                mIDrag.deleted(menu, id);
            }

            @Override
            public void onEdit(boolean text) {
                Log.e(TAG, "onEdit: " + text);
                mEditHandler.onEdit(text);
            }

            @Override
            public void onClickPosition(float x, float y) {
                Log.e(TAG, "onClickPosition: " + x + "*" + y);
                if (mCallback.getMenu() == IMenu.MODE_PREVIEW) { //仅主界面时，允许切换选中item
                    if (!onClickVideo(x, y)) { //没有选中，再次确认是否选中了其他item
                        //未选中 取消选中
                    }
                }
            }


            @Override
            public void onKeyframe(boolean show) {
                Log.e(TAG, "onKeyframe: " + show);
            }

            @Override
            public void onExitEdit() {
                Log.e(TAG, "onExitEdit: 退出编辑....");
                mVideoHandlerListener.onSelectedItem(IMenu.MODE_PREVIEW, -1);
            }

            @Override
            public void exitOtherSelectLayer() {
                if (mCallback.getPipLayerHandler().getCurrentCollageInfo() != null) { //退出图层选中
                    mCallback.getPipLayerHandler().exitEditMode();
                } else if (mCallback.getOverLayHandler().getCurrentCollageInfo() != null) { //退出叠加选中
                    mCallback.getOverLayHandler().exitEditMode();
                }
            }
        });
    }

    public void setIDrag(IDrag IDrag) {
        mIDrag = IDrag;
    }

    private IDrag mIDrag;

    public void rebuild() {
        mVirtualImage.reset();
        //比例确认方式: 边框比例>指定比例 > 裁剪比例
        float tmp = DataManager.loadData(mVirtualImage, false, mCallback.getEditDataHandler()); //边框比例
//        Log.e(TAG, "rebuild: biankuang " + tmp + " " + mCallback.getEditDataHandler().getProportionValue());
        if (tmp == 0) { //裁剪
            tmp = PlayerAspHelper.getAsp(mCallback.getEditDataHandler().getProportionValue());
        }
//        Log.e(TAG, "rebuild: " + tmp);
        mVirtualImageView.setPreviewAspectRatio(tmp);
        mVirtualImageView.enableViewBGHolder(true); //解决: 导入水印类型的图片时，图片部分透明底部纯白
        try {
            mVirtualImage.build(mVirtualImageView);
            mVirtualImageView.setBackgroundColor(Color.TRANSPARENT);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    public void changeFilterList(List<EffectInfo> filterList) {
        try {
            mVirtualImage.clearEffects(mVirtualImageView);
            for (EffectInfo effectInfo : filterList) {
                mVirtualImage.addEffect(effectInfo);
            }
            mVirtualImage.updateEffects(mVirtualImageView);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

    }

    private PlayerControl.OnInfoListener mOnInfoListener = (what, extra, obj) -> {
        LogUtil.i(TAG, "onInfo: " + what + " " + extra + " " + obj);
        return false;
    };

    public DragBorderLineView getLineView() {
        return mDragBorderView;
    }


    public FrameLayout getSubEditorParent() {
        return mLlContainer;
    }

    private FrameLayout mPipGroup;

    public FrameLayout getPlayerContainer() {
        return mPipGroup;
    }

    public VirtualImageView getEditor() {
        return mVirtualImageView;
    }

    public VirtualImage getEditorImage() {
        return mVirtualImage;
    }

    public void onSelectData(int id) {
        mEditHandler.setSelectId(id);
    }

    public void onSelectIndex(int index, @IMenu int menu) {
        mEditHandler.setSelectIndex(index, menu);
    }

    public void onSelectId(int id, @IMenu int menu) {
        mEditHandler.setSelectId(id, menu);
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PLAYER_FAILED: {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
                break;
                case MSG_RESTORE_TEMPLATE: { //恢复字幕
                    int width = mLlContainer.getWidth();
                    int height = mLlContainer.getHeight();
                    RestoreTemplateHelper helper = new RestoreTemplateHelper();
                    helper.restoreTemplate(getContext(), mVirtualImage, mVirtualImageView, width, height, mVideoHandlerListener.getParamHandler(), () -> {
                        rebuild(); //恢复字幕后再次build
                    });
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    private final int MSG_PREPARED_DRAG = 801; //首次准备就绪后，自动选中底图
    private final int MSG_PLAYER_FAILED = 802; //播放器失败，主动退出界面
    private final int MSG_RESTORE_TEMPLATE = 803; //恢复主要数据后，继续恢复文字( 模板需要build两次)

}
