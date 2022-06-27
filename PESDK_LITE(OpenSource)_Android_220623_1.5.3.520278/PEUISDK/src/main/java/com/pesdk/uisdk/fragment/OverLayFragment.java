package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.OverlayAdapter;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.bean.net.IBean;
import com.pesdk.uisdk.data.vm.OverlayVM;
import com.pesdk.uisdk.export.LayerManager;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.helper.OverlayHelper;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.widget.loading.CustomLoadingView;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.utils.MiscUtils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * 叠加: 素材以画中画方式展现，可自由拖动位置
 */
public class OverLayFragment extends BaseFragment {

    public static OverLayFragment newInstance() {
        Bundle args = new Bundle();
        OverLayFragment fragment = new OverLayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ImageHandlerListener mEditorHandler;
    private IEditCallback mEditCallback;
    //数据列表
    private RecyclerView mRvData;
    private OverlayAdapter mDataAdapter; //展示样式列表
    private OverlayVM mVM;
    /**
     * loading
     */
    private CustomLoadingView mLoadingView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getContext();
        mEditorHandler = (ImageHandlerListener) context;
        mEditCallback = (IEditCallback) context;
    }

    private CollageInfo mBk;

    public void edit(CollageInfo info) {
        if (info == null) {
            return;
        }
        mBk = info.copy();
        mAddInfo = info;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_overlay_layout, container, false);
        TAG = "OverLayFragment";
        isAdded = false;
        bChanged = false;
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_overlay);
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(OverlayVM.class);
        mVM.getData().observe(getViewLifecycleOwner(), this::onDataResult);
        mRvData = $(R.id.rv_data);
        mLoadingView = $(R.id.loading);
        mLoadingView.setBackground(ContextCompat.getColor(getContext(), R.color.pesdk_white));
        mLoadingView.setHideCancel(true);
        init();
        return mRoot;
    }


    private void onDataResult(List<IBean> list) {
        SysAlertDialog.cancelLoadingDialog();
        //设置数据
        if (null != list && list.size() > 0) {
            mLoadingView.setVisibility(View.GONE);
            int index = BaseRVAdapter.UN_CHECK;
            if (null != mBk) {
                index = getIndex(list, mBk);
            }
            mDataAdapter.addAll(list, index);
        } else {
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        }
    }

    @Override
    public void onCancelClick() {
        mEditCallback.getOverLayHandler().exitEditMode();
        if (bChanged) {
            showAlert(new AlertCallback() {

                @Override
                public void cancel() {

                }

                @Override
                public void sure() { //放弃新增
                    UndoInfo info = mEditorHandler.getParamHandler().onDeleteStep();
                    if (null != info) {
                        mEditorHandler.getParamHandler().setOverLayList(info.getList());
                    }
                    mEditorHandler.reBuild();
                    mMenuCallBack.onCancel();
                }
            });
        } else {
            mMenuCallBack.onCancel();
        }
    }

    @Override
    public void onSureClick() {
        mEditCallback.getOverLayHandler().exitEditMode();
        mMenuCallBack.onSure();
    }


    private void init() {
        //数据展示
        mDataAdapter = new OverlayAdapter(getContext(), Glide.with(this));
        mRvData.setLayoutManager(new GridLayoutManager(getContext(), 5));
        ((SimpleItemAnimator) mRvData.getItemAnimator()).setSupportsChangeAnimations(false);
        mRvData.setAdapter(mDataAdapter);
        mDataAdapter.setEnableRepeatClick(true);
        mDataAdapter.setOnItemClickListener((OnItemClickListener<IBean>) (position, item) -> {
            if (isRunning) {
                preview(item);  //预览
            } else {
                Log.e(TAG, "onItemClick: give up add effect.. ");
            }
        });
        //获取分类
        mVM.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        mVM = null;
        mBk = null;
        mAddInfo = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gcGlide();
    }

    private boolean isAdded = false; //true 每次进入都是新增 ,false 中途切换是替换
    private final int MENU = IMenu.overlay;

    private CollageInfo mAddInfo;

    private boolean bChanged = false;

    /**
     * 预览
     */
    private void preview(IBean info) {
        try {
            mEditorHandler.getParamHandler().setEditMode(MENU);
            PEImageObject peImageObject = new PEImageObject(info.getLocalPath());
            float asp = mEditorHandler.getPlayerAsp();
            RectF dst = new RectF();
            MiscUtils.fixShowRectFByExpanding(peImageObject.getWidth() / (peImageObject.getHeight() + 0.0f), 720, (int) (720 / asp), dst);
            peImageObject.setShowRectF(dst);
            OverlayHelper.setOverlay(peImageObject);

            if (null == mBk) {
                List<CollageInfo> list = mEditCallback.getEditDataHandler().getParam().getOverLayList();
                if (null != list && null != mAddInfo) {
                    if (!list.contains(mAddInfo)) {//刚增加的叠加已被删除
                        isAdded = false;
                    }
                }
            } else {
                isAdded = true;
            }
            bChanged = true;//样式变化
            if (!isAdded) { //首次新增
                mAddInfo = new CollageInfo(peImageObject);
                isAdded = true;
                mEditorHandler.getParamHandler().addOverlay(mAddInfo, true);
                mEditCallback.getOverLayHandler().edit(mAddInfo, true);
                LayerManager.insertCollage(mAddInfo);
            } else { //替换
                CollageInfo last = mEditCallback.getOverLayHandler().getTopMedia();
                if (null != last) {
                    LayerManager.remove(last);
                    last.setMedia(peImageObject);
                    mEditCallback.getOverLayHandler().edit(last, true);
                    LayerManager.insertCollage(last);
                }
                mEditorHandler.getParamHandler().onSaveDraft(MENU);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        if (isRunning && null != mDataAdapter) {
            mDataAdapter.clearChecked(); //清理被选中
        }
    }

    /**
     * 切换选中的得加时，修改adapter中的选中项
     *
     * @param overlay
     */
    public void restore(CollageInfo overlay) {
        if (isRunning && null != mDataAdapter && null != overlay) {
            mDataAdapter.setChecked(getIndex(mDataAdapter.getList(), overlay));
        }
    }


    private int getIndex(List<com.pesdk.uisdk.bean.net.IBean> list, CollageInfo overlay) {
        if (null == overlay) {
            return BaseRVAdapter.UN_CHECK;
        }
        String path = overlay.getImageObject().getMediaPath();
        if (null != list && !TextUtils.isEmpty(path)) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(path, list.get(i).getLocalPath())) {
                    return i;
                }
            }
        }
        return BaseRVAdapter.UN_CHECK;
    }

}
