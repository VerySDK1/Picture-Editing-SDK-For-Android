package com.pesdk.uisdk.util.helper;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.pesdk.uisdk.bean.code.Crop;
import com.pesdk.uisdk.fragment.ProportionFragment;
import com.pesdk.uisdk.fragment.callback.IFragmentMenuCallBack;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;

/**
 *
 */
public class ProportionHelper implements ProportionFragment.Callback {

    private IEditCallback mEditCallback;
    private ImageHandlerListener mEditorHandler;
    private IFragmentMenuCallBack mIFragmentMenuCallBack;
    private ProportionFragment mProportionFragment;

    @Override
    public int getCropMode() {
        return mEditorHandler.getParamHandler().getProportionMode();
    }

    @Override
    public void changeMode(@Crop.CropMode int mode) {
        changeModeImp(mode);
    }


    private void changeModeImp(@Crop.CropMode int mode) {
        float asp;
        if (mode == Crop.CROP_1) {
            asp = 1f;
        } else if (mode == Crop.CROP_169) {
            asp = 16 / 9.0f;
        } else if (mode == Crop.CROP_916) {
            asp = 9 / 16.0f;
        } else if (mode == Crop.CROP_43) {
            asp = 4 / 3.0f;
        } else if (mode == Crop.CROP_34) {
            asp = 3 / 4.0f;
        } else if (mode == Crop.CROP_45) {
            asp = 4 / 5f;
        } else if (mode == Crop.CROP_23) {
            asp = 2 / 3f;
        } else if (mode == Crop.CROP_32) {
            asp = 3 / 2f;
        } else if (mode == Crop.CROP_12) {
            asp = 1 / 2f;
        } else if (mode == Crop.CROP_21) {
            asp = 2 / 1f;
        } else if (mode == Crop.CROP_67) {
            asp = 6 / 7f;
        } else { //原始
            asp = mEditorHandler.getVirtualImageInfo().getOriginalProportion(); //原始比例: 导入图片|模板时已固定
        }

        mEditCallback.getEditDataHandler().setProportionMode(mode, asp);
        float lastAsp = mEditorHandler.getPlayerAsp();


        float nextAsp = mEditorHandler.getParamHandler().getNextAsp();
        if (mEditorHandler.getParamHandler().proportionChanged(lastAsp, nextAsp)) {
            mEditorHandler.fixDataSourceAfterReload(nextAsp, () -> {
                mEditorHandler.reBuild();
            });
        } else {
            mEditorHandler.reBuild();
        }
        mHandler.postDelayed(() -> mProportionFragment.checkState(mode), 100);
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: {

                }
                break;
                default: {
                }
                break;
            }
        }
    };


    public void setCallback(ProportionFragment fragment, IEditCallback editCallback, ImageHandlerListener editorHandler, IFragmentMenuCallBack fragmentMenuCallBack) {
        mEditCallback = editCallback;
        mEditorHandler = editorHandler;
        mProportionFragment = fragment;
        mIFragmentMenuCallBack = fragmentMenuCallBack;
    }


    @Override
    public void cancel() {
        mEditCallback.getEditDataHandler().resetoreProportion();
        mIFragmentMenuCallBack.onCancel();
    }

    @Override
    public boolean enableResetAll() {
        return false;
    }

    @Override
    public void resetAll() {

    }

    @Override
    public void sure() {
        //存步骤
        mEditCallback.getEditDataHandler().onSaveProportionStep();
        mIFragmentMenuCallBack.onSure();
    }
}
