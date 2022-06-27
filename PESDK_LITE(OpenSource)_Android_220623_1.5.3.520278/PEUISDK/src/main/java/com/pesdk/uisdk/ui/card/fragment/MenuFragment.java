package com.pesdk.uisdk.ui.card.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.ui.card.listener.Callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 *
 */
public class MenuFragment extends AbsBaseFragment {

    private Callback mCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_card_menu, container, false);
        initView();
        return mRoot;
    }

    private RadioGroup rgMenu;
    private BeautyMenuFragment mBeauty;
    private BGFragment mBG;
    private ClothesFragment mClothesFragment;

    private void initView() {
        rgMenu = $(R.id.rgMenu);
        rgMenu.setOnCheckedChangeListener((group, checkedId) -> {
            onSwitch(checkedId);
        });
        onSwitch(rgMenu.getCheckedRadioButtonId());
    }

    private void onSwitch(int checkedId) {
        if (checkedId == R.id.rbBG) {
            onBG();
        } else if (checkedId == R.id.rbBeauty) {
            onBeauty();
        } else if (checkedId == R.id.rbClothes) {
            onClothes();
        }
    }

    private void onBG() {
        if (mBG == null) {
            mBG = BGFragment.newInstance();
        }
        mCallback.goneEarse();
        saveClothes();
        change(mBG);
    }

    private void saveClothes() {
        if (null != mClothesFragment && mClothesFragment.isVisible()) {
            mCallback.getClothesCallback().onClothesSure();
        }

    }

    private void onBeauty() {
        if (mBeauty == null) {
            mBeauty = BeautyMenuFragment.newInstance();
        }
        mCallback.goneEarse();
        saveClothes();
        change(mBeauty);
    }

    private void onClothes() {
        if (mClothesFragment == null) {
            mClothesFragment = ClothesFragment.newInstance();
        }
        mClothesFragment.setCallback(mCallback.getClothesCallback());
        mCallback.onPreClothes();
        mClothesFragment.setFaceHairInfo(mCallback.getHairInfo());
        change(mClothesFragment);
    }


    private void change(Fragment fragment) {
        changeFragment(R.id.childfragment, fragment);
    }


    public boolean isCothes() {
        return null != rgMenu && rgMenu.getCheckedRadioButtonId() == R.id.rbClothes;
    }
}
