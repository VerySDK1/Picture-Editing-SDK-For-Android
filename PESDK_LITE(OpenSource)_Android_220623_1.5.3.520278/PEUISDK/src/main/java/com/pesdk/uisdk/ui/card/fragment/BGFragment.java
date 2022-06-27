package com.pesdk.uisdk.ui.card.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.ui.card.listener.ColorListener;
import com.pesdk.uisdk.widget.ColorBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 1.背景色
 */
public class BGFragment extends AbsBaseFragment {

    public static BGFragment newInstance() {

        Bundle args = new Bundle();

        BGFragment fragment = new BGFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ColorListener mColorListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mColorListener = (ColorListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_card_bg, container, false);
        initView();
        return mRoot;
    }

    private ColorBar mColorBar;

    private void initView() {
        mColorBar = $(R.id.colorBar);
        mColorBar.setColor(mColorListener.getColor());
        mColorBar.setCallback(new ColorBar.Callback() {
            @Override
            public void onNone() {
                mColorListener.onBgNone();
            }

            @Override
            public void onColor(int color) {
                mColorListener.onColor(color);
            }
        });
    }


}
