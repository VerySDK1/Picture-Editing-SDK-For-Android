package com.pesdk.uisdk.fragment.sub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.Interface.OnFlowerListener;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.FlowerAdapter;
import com.pesdk.uisdk.bean.model.flower.Flower;
import com.pesdk.uisdk.data.vm.FlowerVM;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.widget.loading.CustomLoadingView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class FlowerFragment extends AbsBaseFragment {
    public static FlowerFragment newInstance() {

        Bundle args = new Bundle();

        FlowerFragment fragment = new FlowerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private OnFlowerListener mFlowerListener;
    /**
     * 列表
     */
    private RecyclerView mRvFlower;
    private FlowerAdapter mFlowerTextAdapter;
    private FlowerVM mVM;
    private CustomLoadingView mLoadingView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_subtitle_flower_layout, container, false);
        mRvFlower = $(R.id.rv_flower_text);
        mRvFlower.setLayoutManager(new GridLayoutManager(mContext, 4, LinearLayoutManager.VERTICAL, false));
        mFlowerTextAdapter = new FlowerAdapter(mContext, Glide.with(this));
        mRvFlower.setAdapter(mFlowerTextAdapter);


        mLoadingView = $(R.id.loading);
        mLoadingView.setBackground(ContextCompat.getColor(getContext(), R.color.pesdk_white));
        mLoadingView.setHideCancel(true);


        mVM = new ViewModelProvider(getParentFragment().getViewModelStore(), new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(FlowerVM.class);
        mVM.getData().observe(getViewLifecycleOwner(), this::onDataResult);
        mFlowerTextAdapter.setOnItemClickListener((position, item) -> {
            if (position == 0) {
                mFlowerListener.onSelect(null);
            } else {
                mFlowerListener.onSelect((Flower) item);
            }
        });
        //获取数据
        mVM.load();
        return mRoot;
    }

    private void onDataResult(List<Flower> list) {
        SysAlertDialog.cancelLoadingDialog();
        //设置数据
        if (null != list && list.size() > 0) {
            mLoadingView.setVisibility(View.GONE);
            ArrayList<Flower> flowers = new ArrayList<>();
            flowers.add(new Flower(R.drawable.none_gray));
            flowers.addAll(list);
            mFlowerTextAdapter.addAll(flowers, 0);
        } else {
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        }
    }


    /**
     * 设置选中
     */
    public void setCheck(Flower info) {
        if (mFlowerTextAdapter == null || mRvFlower == null) {
            return;
        }
        if (info == null) {
            mFlowerTextAdapter.setIndex(0);
        } else {
            mFlowerTextAdapter.setCheckItem(info.getId());
        }
        mRvFlower.scrollToPosition(mFlowerTextAdapter.getChecked());
    }


    /**
     * 设置返回接口
     */
    public void setFlowerListener(OnFlowerListener flowerListener) {
        mFlowerListener = flowerListener;
    }

}
