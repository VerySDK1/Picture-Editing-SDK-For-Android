package com.pesdk.uisdk.fragment.sub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.CaptionStyleAdapter;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.data.vm.SubBublleVM;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.widget.loading.CustomLoadingView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.pesdk.net.PENetworkApi.Text;

/**
 * 气泡界面
 */
public class SubtitleBubbleFragment extends AbsBaseFragment {

    public static SubtitleBubbleFragment newInstance() {
        return new SubtitleBubbleFragment();
    }

    private BubbleListener mBubbleListener;

    /**
     * loading
     */
    private CustomLoadingView mLoadingView;
    private String mResourceId;
    private RecyclerView mRvData;
    private CaptionStyleAdapter mAdapter;
    private SubBublleVM mVM;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_edit_subtitle_bubble, container, false);
        mVM = new ViewModelProvider(getParentFragment().getViewModelStore(), new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(SubBublleVM.class);
        mVM.getLiveData().observe(getViewLifecycleOwner(), this::onDataResult);
        mLoadingView = $(R.id.loading);
        mLoadingView.setBackground(ContextCompat.getColor(getContext(), R.color.pesdk_white));
        mLoadingView.setHideCancel(true);
        init();
        return mRoot;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mAdapter) {
            mAdapter.recycle();
            mAdapter = null;
        }
    }

    private void onDataResult(List<StyleInfo> list) {
        if (null == list) {
            SysAlertDialog.cancelLoadingDialog();
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        } else {
            if (mAdapter != null) {
                mAdapter.addAll(list, getIndex(list, mResourceId));
            }
            mLoadingView.setVisibility(View.GONE);
        }
    }


    private int getIndex(List<StyleInfo> list, String resourceId) {
        return IndexHelper.getIndexStyle(list, resourceId);
    }

    private void init() {
        mRvData = $(R.id.rv_data);
        mRvData.setLayoutManager(new GridLayoutManager(getContext(), 4, LinearLayoutManager.VERTICAL, false));
        mAdapter = new CaptionStyleAdapter(mContext, Glide.with(this));
        mAdapter.setOnItemClickListener((OnItemClickListener<StyleInfo>) (position, item) -> {
            if (item.isdownloaded) {
                if (mBubbleListener != null) {
                    mBubbleListener.onSelect(item);
                }
            } else { //未下载
                mAdapter.onDown(position);
            }
        });
        mRvData.setAdapter(mAdapter);
        //获取数据
        mVM.process("", Text);
    }


    /**
     * 根据数据返回下标
     */
    public int getPosition(int id) {
        return mAdapter.getPosition(id);
    }


    /**
     * 恢复选中
     */
    public void resetSelect(String categoryId, String resourceId) {
        mResourceId = resourceId;
        if (null != mAdapter) {
            mAdapter.setCheckItem(getIndex(mAdapter.getList(), resourceId));
        }

    }

    /**
     * 接口
     */
    public void setListener(BubbleListener bubbleListener) {
        mBubbleListener = bubbleListener;
    }

    public interface BubbleListener {

        /**
         * 选中
         */
        void onSelect(StyleInfo styleInfo);

        /**
         * 失败
         */
        void onFailed();

        /**
         * 成功
         */
        void onSuccess();
    }

}