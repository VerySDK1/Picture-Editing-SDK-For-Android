package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 *
 */
public abstract class AbsBaseFragment extends Fragment {
    protected String TAG = AbsBaseFragment.this.toString();
    protected View mRoot;
    protected Context mContext;
    protected boolean isRunning = false; //判断当前界面是否在栈顶，防止异步更新UI


    protected void onToast(@StringRes int msgId) {
        if (getContext() != null) {
            onToast(getContext().getString(msgId));
        }
    }

    protected void onToast(String msg) {
        if (getContext() != null) {
            Toast toast = Toast.makeText(getContext(), null, Toast.LENGTH_SHORT);
            toast.setText(msg);
            toast.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        isRunning = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        isRunning = true;
        return mRoot;
    }


    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    public void onResume() {
        isRunning = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    public void onDestroyView() {
        isRunning = false;
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        isRunning = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    /**
     * 清理glide内存
     */
    protected void gcGlide() {
        Glide.get(getContext()).clearMemory();
        System.runFinalization();
        System.gc();
    }

    /**
     * 查找fragment内的组件
     */
    public <T extends View> T $(int resId) {
        return mRoot.findViewById(resId);
    }

    public int onBackPressed() {
        return 0;
    }

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
