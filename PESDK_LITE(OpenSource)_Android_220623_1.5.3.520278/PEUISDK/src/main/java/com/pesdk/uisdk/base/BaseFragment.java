package com.pesdk.uisdk.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 */
public abstract class BaseFragment extends Fragment {

    protected View mRoot;
    protected String TAG = BaseFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(getLayoutId(), container, false);
        initView(mRoot);
        return mRoot;
    }

    public <T extends View> T $(int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    public abstract void initView(View view);

    public abstract int getLayoutId();

    /**
     * @param containerViewId
     * @param fragment
     */
    public void changeFragment(int containerViewId, Fragment fragment) {
        if (containerViewId != 0 && null != fragment) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(containerViewId, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    /**
     * 移除
     *
     * @param fragment
     */
    public void removeFragment(Fragment fragment) {
        if (null != fragment) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commitAllowingStateLoss();
        }
    }


}
