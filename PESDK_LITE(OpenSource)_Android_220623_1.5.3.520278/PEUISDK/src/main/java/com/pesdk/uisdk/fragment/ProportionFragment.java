package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.code.Crop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 比例  （指定播放器比例时，自定义换成原始逻辑）
 */
public class ProportionFragment extends BaseFragment {

    public static ProportionFragment newInstance() {

        Bundle args = new Bundle();

        ProportionFragment fragment = new ProportionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_proportion_layout, container, false);
        return mRoot;
    }

    private ImageButton mTvResetAll;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initProportion();
        mTvResetAll = $(R.id.tvResetAll);
        if (mCallback.enableResetAll()) {
            ((TextView) $(R.id.tvTitle)).setText(R.string.pesdk_crop);
            mTvResetAll.setVisibility(View.VISIBLE);
            mTvResetAll.setOnClickListener(v -> mCallback.resetAll());
        } else {
            mRbFree.setVisibility(View.GONE);
            ((TextView) $(R.id.tvTitle)).setText(R.string.pesdk_proportion);
            mTvResetAll.setVisibility(View.GONE);
        }
        mRoot.postDelayed(() -> checkState(mCallback.getCropMode()), 100);
    }

    public void checkState(int mode) {
        if (null == mRoot) {
            Log.e(TAG, "checkState: " + mode);
            return;
        }
        if (mode == Crop.CROP_FREE) {
            mRbFree.setChecked(true);
        } else if (mode == Crop.CROP_1) {
            mRb1x1.setChecked(true);
        } else if (mode == Crop.CROP_169) {
            mRb169.setChecked(true);
        } else if (mode == Crop.CROP_916) {
            mRb916.setChecked(true);
        } else if (mode == Crop.CROP_43) {
            mRb43.setChecked(true);
        } else if (mode == Crop.CROP_34) {
            mRb34.setChecked(true);
        } else if (mode == Crop.CROP_45) {
            mRb45.setChecked(true);
        } else if (mode == Crop.CROP_23) {
            mRb23.setChecked(true);
        } else if (mode == Crop.CROP_32) {
            mRb32.setChecked(true);
        } else if (mode == Crop.CROP_12) {
            mRb12.setChecked(true);
        } else if (mode == Crop.CROP_21) {
            mRb21.setChecked(true);
        } else if (mode == Crop.CROP_67) {
            mRb67.setChecked(true);
        } else {
            mRbOriginal.setChecked(true);
        }
    }


    public boolean isResetEnable() {
        return null != mTvResetAll ? mTvResetAll.isEnabled() : false;
    }

    public void setResetClickable(boolean clickable) {
        mTvResetAll.setEnabled(clickable);
    }

    private RadioButton mRbOriginal, mRbFree, mRb1x1, mRb169, mRb916, mRb43, mRb34, mRb45, mRb23, mRb32, mRb12, mRb21, mRb67;

    private void initProportion() {
        mRbOriginal = $(R.id.rbCropOriginal);
        mRbFree = $(R.id.rbCropFree);
        mRb1x1 = $(R.id.rbProportion1x1);
        mRb169 = $(R.id.rbProportion169);
        mRb916 = $(R.id.rbProportion916);
        mRb43 = $(R.id.rbProportion43);
        mRb34 = $(R.id.rbProportion34);
        mRb45 = $(R.id.rbProportion45);
        mRb23 = $(R.id.rbProportion23);
        mRb32 = $(R.id.rbProportion32);
        mRb12 = $(R.id.rbProportion12);
        mRb21 = $(R.id.rbProportion21);
        mRb67 = $(R.id.rbProportion67);

        mRbOriginal.setOnClickListener(v -> changeMode(Crop.CROP_ORIGINAL));
        mRbFree.setOnClickListener(v -> changeMode(Crop.CROP_FREE));
        mRb1x1.setOnClickListener(v -> changeMode(Crop.CROP_1));
        mRb169.setOnClickListener(v -> changeMode(Crop.CROP_169));
        mRb916.setOnClickListener(v -> changeMode(Crop.CROP_916));
        mRb43.setOnClickListener(v -> changeMode(Crop.CROP_43));
        mRb34.setOnClickListener(v -> changeMode(Crop.CROP_34));
        mRb45.setOnClickListener(v -> changeMode(Crop.CROP_45));
        mRb23.setOnClickListener(v -> changeMode(Crop.CROP_23));
        mRb32.setOnClickListener(v -> changeMode(Crop.CROP_32));
        mRb12.setOnClickListener(v -> changeMode(Crop.CROP_12));
        mRb21.setOnClickListener(v -> changeMode(Crop.CROP_21));
        mRb67.setOnClickListener(v -> changeMode(Crop.CROP_67));
    }

    private void changeMode(@Crop.CropMode int crop) {
        if (null != mCallback) {
            mCallback.changeMode(crop);
        }
    }

    @Override
    public void onCancelClick() {
        mCallback.cancel();
    }

    @Override
    public void onSureClick() {
        mCallback.sure();
    }


    public static interface Callback {

        @Crop.CropMode
        int getCropMode();

        void changeMode(@Crop.CropMode int mode);

        void cancel();

        boolean enableResetAll();

        void resetAll();

        void sure();
    }
}
