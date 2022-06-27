package com.pesdk.uisdk.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.pesdk.api.ActivityResultContract.Intent;
import com.pesdk.api.PEAPI;
import com.pesdk.uisdk.ActivityResultCallback.EditRegisterManger;
import com.pesdk.uisdk.ActivityResultCallback.IResultCallback;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.base.BaseExportActivity;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.fragment.BaseFragment;
import com.pesdk.uisdk.fragment.main.MainMenuFragment;
import com.pesdk.uisdk.fragment.main.MenuCallback;
import com.pesdk.uisdk.util.IntentConstants;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;

import androidx.annotation.Nullable;

/**
 * 抽离部分UI
 */
public abstract class EditBaseActivity extends BaseExportActivity implements MenuCallback {

    protected MainMenuFragment mMainMenuFragment;// Menu
    protected BaseFragment mCurrentChildFragment;
    protected int MAX_ANIM_DURATION = 500;
    protected ViewGroup mFragmentRevokeLayout;
    protected ViewGroup mMainFragmentContainer, mChildFragmentContainer;
    VirtualIImageInfo mVirtualImageInfo;

    private static final String TAG = "EditBaseActivity";


    protected EditRegisterManger mRegisterManger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int draftId = getIntent().getIntExtra(IntentConstants.PARAM_EDIT_DARFT_ID, -1); //草稿Id(编辑草稿)
        if (draftId == -1) {
            String path = getIntent().getStringExtra(IntentConstants.PARAM_EDIT_IMAGE_PATH);
            if (!TextUtils.isEmpty(path)) {//普通图片编辑：新建虚拟图片
                try {
                    PEImageObject peImageObject = new PEImageObject(this, path);
                    Intent.creatShortImage(this, peImageObject.getMediaPath()); //构造草稿
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                    finish();
                    return;
                }
            } else {
                Log.e(TAG, "onCreate: draftId error. ");
                finish();
                return;
            }
        }
        mVirtualImageInfo = PEAPI.getInstance().getShortImageInfo();
        if (mVirtualImageInfo == null) {
            Log.e(TAG, "onCreate: mVirtualImageInfo is null ");
            finish();
            return;
        }

        MAX_ANIM_DURATION = getResources().getInteger(android.R.integer.config_longAnimTime);
        mRegisterManger = new EditRegisterManger(initIResultCallback());
        mRegisterManger.register(this);
    }


    abstract IResultCallback initIResultCallback();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRegisterManger = null;
    }

    protected void initView() {
        mFragmentRevokeLayout = $(R.id.childRevokeLayout);
    }


    /**
     * 退出子fragment
     */
    protected void exitChildFragment(ViewGroup childRoot) {
        childRoot.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pesdk_slide_out));
        if (null != mCurrentChildFragment) {
            getSupportFragmentManager().beginTransaction().remove(mCurrentChildFragment).commitAllowingStateLoss();
            mCurrentChildFragment = null;
        }
        childRoot.setVisibility(View.GONE);
    }

    /**
     * 进入子界面
     */
    protected void enterChildFragment(ViewGroup childRoot, BaseFragment childFragment) {
        childRoot.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pesdk_slide_in));
        getSupportFragmentManager().beginTransaction().replace(mChildFragmentContainer.getId(), childFragment).commitAllowingStateLoss();
        mCurrentChildFragment = childFragment;
    }

    int mTopScrollHeight;

    /**
     * 设置子fragment的高度
     *
     * @param enable true 增加高度
     */
    protected void initChildFragmentVG(ViewGroup childFragmentContainer, boolean enable) {
        if (mTopScrollHeight == 0) {
            mTopScrollHeight = getResources().getDimensionPixelSize(R.dimen.dp_45);
        }
        ViewGroup.LayoutParams lp = childFragmentContainer.getLayoutParams();
        int tmp = getResources().getDimensionPixelSize(R.dimen.pesdk_fragment_main_menu_height) + mTopScrollHeight;
        if (enable) { //多向上平移，字幕界面需保证UI操作区域更大点
            tmp += getResources().getDimensionPixelSize(R.dimen.dp_45);
        }
        lp.height = tmp;
        childFragmentContainer.setLayoutParams(lp);
    }


    Handler mHandler = new Handler(Looper.myLooper()) {

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
}
