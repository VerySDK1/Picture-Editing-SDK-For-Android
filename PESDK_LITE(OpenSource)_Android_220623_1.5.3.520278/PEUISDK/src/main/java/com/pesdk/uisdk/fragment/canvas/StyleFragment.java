package com.pesdk.uisdk.fragment.canvas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.pesdk.bean.SortBean;
import com.pesdk.net.PENetworkApi;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.ImageAdapter;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.data.vm.CanvasStyleVM;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.fragment.canvas.callback.SkyCallback;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.pesdk.widget.loading.CustomLoadingView;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 背景|天空-样式
 */
public class StyleFragment extends AbsBaseFragment {

    private SortBean mSortApi;

    private RecyclerView rv;
    private ImageAdapter mAdapter;
    private SkyCallback mCallback;

    public void setCallback(SkyCallback callback) {
        mCallback = callback;
    }


    public void setSortApi(SortBean sortApi) {
        mSortApi = sortApi;
    }

    @PENetworkApi.ResourceType
    private String mType;

    private static final String PARAM_TYPE = "_type";

    public static StyleFragment newInstance(@PENetworkApi.ResourceType String type) {
        Bundle args = new Bundle();
        args.putString(PARAM_TYPE, type);
        StyleFragment fragment = new StyleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mType = bundle.getString(PARAM_TYPE);
    }

    private CanvasStyleVM mVM;
    /**
     * loading
     */
    private CustomLoadingView mLoadingView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_canvas_color_layout, container, false);
        mLoadingView = $(R.id.loading);
        mLoadingView.setBackground(ContextCompat.getColor(getContext(), R.color.pesdk_white));
        mLoadingView.setHideCancel(true);
        mVM = new ViewModelProvider(getParentFragment().getViewModelStore(), new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new CanvasStyleVM(mType);
            }
        }).get(mSortApi.getName() + "_" + mSortApi.getId(), CanvasStyleVM.class);
        mVM.getAppData().observe(getViewLifecycleOwner(), this::handleData);
        return mRoot;
    }


    private void handleData(List<ItemBean> list) {
        mSortApi.setDataList(list);
        notifyData();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVM.process(mSortApi);
        rv = $(R.id.rvColor);
        if (PENetworkApi.Sky.equals(mType)) {
            rv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        } else {
            rv.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.HORIZONTAL, false));
        }
        mAdapter = new ImageAdapter(getContext(), Glide.with(this), mType);
        rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((OnItemClickListener<ItemBean>) (position, item) -> {
            if (null != mCallback) {
                mCallback.onStyle(item.getLocalPath());
            }
        });

        if (TextUtils.equals(PENetworkApi.Sky, mType)) {
            int w = CoreUtils.getMetrics().widthPixels;
            mAdapter.setItemSize((w - getResources().getDimensionPixelSize(R.dimen.dp_5) * 2 * 6) / 5); //天空 横向铺满UI
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        rv = null;
        mAdapter = null;
        mVM = null;
        mCallback = null;
    }

    private void notifyData() {
        if (null != mSortApi && null != mAdapter) {
            int tmp = getIndex();
            List<ItemBean> list = (ArrayList<ItemBean>) mSortApi.getDataList();
            if (list != null && list.size() > 0) {
                mAdapter.addAll(list, tmp);
                mLoadingView.setVisibility(View.GONE);
            } else {
                mLoadingView.loadError(getString(R.string.common_pe_loading_error));
            }
        }
    }


    private int getIndex() {
        if (mCallback != null && mSortApi != null) {
            List<ItemBean> list = (ArrayList<ItemBean>) mSortApi.getDataList();
            return IndexHelper.getIndexCanvas(list, mCallback.getStyle());
        }
        return BaseRVAdapter.UN_CHECK;
    }


    public void checkChecked() {
        if (null != mAdapter && null != mCallback) {
            mAdapter.updateCheck(getIndex());
        }
    }


}

