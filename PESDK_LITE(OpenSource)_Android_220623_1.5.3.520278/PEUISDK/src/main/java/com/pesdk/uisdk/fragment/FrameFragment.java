package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.FrameAdapter;
import com.pesdk.uisdk.adapter.SortAdapter;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.model.BorderStyleInfo;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.bean.net.TResult;
import com.pesdk.uisdk.data.vm.FrameVM;
import com.pesdk.uisdk.layoutmanager.WrapContentLinearLayoutManager;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.pesdk.uisdk.widget.ParallaxRecyclerView;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.widget.loading.CustomLoadingView;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * 边框: 样式画中画固定，背景可拖动
 */
public class FrameFragment extends BaseFragment {

    public static FrameFragment newInstance() {
        Bundle args = new Bundle();
        FrameFragment fragment = new FrameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ImageHandlerListener mEditorHandler;
    //种类列表
    private RecyclerView mRvSort;
    private SortAdapter mSortAdapter;
    //数据列表
    private ParallaxRecyclerView mRvData;
    private boolean bChanged = false; //是否设置的特效
    private FrameVM mVM;
    /**
     * loading
     */
    private CustomLoadingView mLoadingView;
    private FrameAdapter mDataAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getContext();
        mEditorHandler = (ImageHandlerListener) context;
    }

    private int nType = 0; // 0 新增边框 ; 1 二次进入时,存在边框，本次修改边框
    private FrameInfo mEditFrameInfo; //二次进入时,存在边框，本次修改边框,编辑此边框

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_frame_layout, container, false);
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(FrameVM.class);
        mVM.getLiveData().observe(getViewLifecycleOwner(), this::onSortResult);
        mVM.getData().observe(getViewLifecycleOwner(), this::onDataResult);
        TAG = "FrameFragment";
        bChanged = false;
        List<FrameInfo> tmp = mEditorHandler.getParamHandler().getParam().getFrameList();
        if (tmp.size() == 0) {
            mEditFrameInfo = null;
            nType = 0;
        } else {
            nType = 1;
            mEditFrameInfo = tmp.get(0);
        }
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_frame);
        mRvSort = $(R.id.rv_sort);
        mRvData = $(R.id.rv_data);
        mLoadingView = $(R.id.loading);
        mLoadingView.setBackground(ContextCompat.getColor(getContext(), R.color.pesdk_white));
        mLoadingView.setHideCancel(true);
        init();
        return mRoot;
    }


    private void onSortResult(List<SortBean> sortApis) {
        if (sortApis != null && sortApis.size() > 0) {
            sortApis.add(0, new SortBean("0", getString(R.string.pesdk_none)));
            int index = BaseRVAdapter.UN_CHECK;
            if (null != mEditFrameInfo) {
                index = IndexHelper.getSortIndex(sortApis, mEditFrameInfo.getSortId());
            }
            mSortAdapter.addAll(sortApis, index);
            //获取第一个分类
            mVM.load(sortApis.get(Math.max(1, index)));
        } else {
            SysAlertDialog.cancelLoadingDialog();
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        }

    }

    private void onDataResult(TResult result) {
        SysAlertDialog.cancelLoadingDialog();
        if (null != result.getList() && result.getList().size() > 0) {
            mLoadingView.setVisibility(View.GONE);
            int index = BaseRVAdapter.UN_CHECK;
            if (null != mEditFrameInfo) {
                index = IndexHelper.getIndexFrame(result.getList(), mEditFrameInfo.getPEImageObject().getMediaPath());
            }
            mDataAdapter.addStyles(result.getList(), index);
        } else {
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        }
    }

    @Override
    public void onCancelClick() {
        if (bChanged) {
            showAlert(new AlertCallback() {

                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    float lastAsp = mEditorHandler.getPlayerAsp();
                    if (null != mEditFrameInfo) {   //放弃编辑
                        UndoInfo info = mEditorHandler.getParamHandler().onDeleteStep();
                        FrameInfo tmp = info.getList().size() > 0 ? (FrameInfo) info.getList().get(0) : null;
                        mEditorHandler.getParamHandler().editFrame(tmp, false);
                    } else { //放弃新增
                        UndoInfo info = mEditorHandler.getParamHandler().onDeleteStep();
                        if (null != info) {
                            FrameInfo tmp = null;
                            if (info.getList().size() > 0) {
                                Object obj = info.getList().get(0);
                                if (obj instanceof FrameInfo) {
                                    tmp = (FrameInfo) obj;
                                }
                            }
                            mEditorHandler.getParamHandler().addFrame(tmp, false);
                        } else {
                            mEditorHandler.getParamHandler().clearFrame(false);
                        }
                    }
                    float asp = mEditorHandler.getParamHandler().getNextAsp();
                    if (mEditorHandler.getParamHandler().proportionChanged(lastAsp, asp)) {
                        mEditorHandler.fixDataSourceAfterReload(asp, () -> {
                            mEditorHandler.reBuild();
                            mMenuCallBack.onCancel();
                        });
                    } else {
                        mEditorHandler.reBuild();
                        mMenuCallBack.onCancel();
                    }
                }
            });
        } else {
            mMenuCallBack.onCancel();
        }
    }

    @Override
    public void onSureClick() {
        mMenuCallBack.onSure();
    }


    private void init() {
        mSortAdapter = new SortAdapter(getContext());
        mRvSort.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRvSort.setAdapter(mSortAdapter);
        mSortAdapter.setOnItemClickListener((OnItemClickListener<SortBean>) (position, item) -> {
            if (position == 0) {
                noneFrame();
            } else {
                mVM.load(item);
            }
        });
        //数据展示
        mDataAdapter = new FrameAdapter(getContext(), Glide.with(this));
        mRvData.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        ((SimpleItemAnimator) mRvData.getItemAnimator()).setSupportsChangeAnimations(false);
        mRvData.setAdapter(mDataAdapter);
        mRvData.setListener(new ParallaxRecyclerView.OnLoadListener() {
            @Override
            public void onPull() {
                mSortAdapter.loadUp();
            }

            @Override
            public void onPush() {
                mSortAdapter.loadDown();
            }

            @Override
            public void firstItem(int firstItem) {

            }
        });
        mDataAdapter.setEnableRepeatClick(false);
        mDataAdapter.setOnItemClickListener((OnItemClickListener<BorderStyleInfo>) (position, item) -> {
            mSortAdapter.setCurrent(item.getSortBean().getId());
            previewBorder(item);
        });
        mVM.loadSort();
    }

    /**
     * 清除边框
     */
    private void noneFrame() {
        mDataAdapter.setChecked(BaseRVAdapter.UN_CHECK);
        float lastAsp = mEditorHandler.getPlayerAsp();
        List<FrameInfo> tmp = mEditorHandler.getParamHandler().getParam().getFrameList();
        if (tmp != null && tmp.size() > 0) {
            mEditorHandler.getParamHandler().clearFrame(false);
        }
        mEditFrameInfo = null;

        float asp = mEditorHandler.getParamHandler().getNextAsp();
        if (mEditorHandler.getParamHandler().proportionChanged(lastAsp, asp)) {
            mEditorHandler.fixDataSourceAfterReload(asp, () -> {
                mEditorHandler.reBuild();
                bChanged = true;
            });
        } else {
            mEditorHandler.reBuild();
            bChanged = true;
        }
    }

    /**
     * 加边框(固定画中画，且比例锁定)
     */
    private void previewBorder(BorderStyleInfo item) {
        try {
            float lastAsp = mEditorHandler.getPlayerAsp();
            PEImageObject peImageObject = new PEImageObject(item.getLocalpath());
            FrameInfo frameInfo = new FrameInfo(peImageObject, item.getSortBean().getId());
            float asp = frameInfo.getAsp();
            if (nType == 0) {
                List<FrameInfo> tmp = mEditorHandler.getParamHandler().getParam().getFrameList();
                mEditorHandler.getParamHandler().addFrame(frameInfo, tmp.size() == 0);
            } else {  //编辑
                mEditorHandler.getParamHandler().editFrame(frameInfo, nType == 1); //仅第一次替换保存状态
                nType++;
            }

            if (mEditorHandler.getParamHandler().proportionChanged(lastAsp, asp)) {
                mEditorHandler.fixDataSourceAfterReload(asp, () -> {
                    mEditorHandler.reBuild();
                    bChanged = true;
                });
            } else {
                mEditorHandler.reBuild();
                bChanged = true;
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        mVM = null;
    }

}
