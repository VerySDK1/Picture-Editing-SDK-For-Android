package com.pesdk.uisdk.fragment.canvas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.CanvasColorAdapter;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.fragment.canvas.callback.Callback;
import com.pesdk.uisdk.listener.OnItemClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 背景颜色
 */
public class ColorFragment extends AbsBaseFragment {
    private RecyclerView rvColor;
    private CanvasColorAdapter mAdapter;
    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public static ColorFragment newInstance() {

        Bundle args = new Bundle();

        ColorFragment fragment = new ColorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_canvas_color_layout, container, false);
        $(R.id.loading).setVisibility(View.GONE);
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvColor = $(R.id.rvColor);
        rvColor.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.HORIZONTAL, false));
        mAdapter = new CanvasColorAdapter(mCallback.getBgColor());
        rvColor.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((OnItemClickListener<Integer>) (position, item) -> {
            if (null != mCallback) {
                mCallback.onColor(item);
            }
        });
    }

    public void checkChecked() {
        if (null != mAdapter && null != mCallback) {
            mAdapter.updateCheck(mCallback.getBgColor());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
    }
}
