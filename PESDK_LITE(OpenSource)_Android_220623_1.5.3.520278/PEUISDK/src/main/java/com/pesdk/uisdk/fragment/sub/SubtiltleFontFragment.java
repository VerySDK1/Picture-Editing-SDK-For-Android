package com.pesdk.uisdk.fragment.sub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.TTFAdapter;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.data.vm.FontVM;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 字体
 */
public class SubtiltleFontFragment extends AbsBaseFragment {


    private RecyclerView mRecyclerView;
    private TTFAdapter mAdapter;
    private ITTFHandlerListener listener;
    private FontVM mVM;

    public static SubtiltleFontFragment newInstance() {
        Bundle args = new Bundle();
        SubtiltleFontFragment fragment = new SubtiltleFontFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(ITTFHandlerListener listener) {
        this.listener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_subtitle_ttf_layout, container, false);
        mVM = new ViewModelProvider(getParentFragment().getViewModelStore(), new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(FontVM.class);
        mVM.getLiveData().observe(getViewLifecycleOwner(), this::onFontResult);
        return mRoot;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = $(R.id.rvTTF);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mAdapter = new TTFAdapter(getContext(), Glide.with(this));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((OnItemClickListener<ItemBean>) (position, info) -> {
            if (PathUtils.isDownload(info.getLocalPath()) || position == 0) {
                listener.onItemClick(info.getLocalPath(), position);
                mAdapter.setChecked(position);
            }
        });
        mVM.load();
    }

    /**
     * 字体列表
     */
    private void onFontResult(List<ItemBean> _list) {
        ArrayList<ItemBean> list = new ArrayList<>();
        String path = listener.getLocalFile();
        ItemBean defaultTtf = new ItemBean();
        defaultTtf.setLocalPath(getString(R.string.pesdk_default_ttf));
        defaultTtf.setName("defaultttf");
        list.add(defaultTtf);
        if (null != _list && _list.size() > 0) {
            list.addAll(_list);
        }
        mAdapter.add(list, getIndex(list, path));
    }


    private int getIndex(List<ItemBean> list, String path) {
        int index = 0; //默认字体
        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                ItemBean info = list.get(i);
                if (TextUtils.equals(info.getLocalPath(), path)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }


    public interface ITTFHandlerListener {
        /**
         *
         */
        void onItemClick(String ttf, int position);

        String getLocalFile();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.onDestory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        mVM = null;
        listener = null;
        mRecyclerView = null;
    }
}
