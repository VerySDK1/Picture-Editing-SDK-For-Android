package com.pesdk.uisdk.ui.card.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.fragment.AbsBaseFragment;

import androidx.annotation.Nullable;

/**
 * 2.美颜
 */
public class BeautyMenuFragment extends AbsBaseFragment {
    public static BeautyMenuFragment newInstance() {

        Bundle args = new Bundle();

        BeautyMenuFragment fragment = new BeautyMenuFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_card_beauty_menu, container, false);
        return mRoot;
    }
}
