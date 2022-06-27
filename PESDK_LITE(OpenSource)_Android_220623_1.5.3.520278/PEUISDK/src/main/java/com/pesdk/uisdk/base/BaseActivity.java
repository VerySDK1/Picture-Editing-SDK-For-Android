package com.pesdk.uisdk.base;

import android.os.Bundle;
import android.widget.Toast;

import com.vecore.base.lib.utils.StatusBarUtil;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

/**
 *
 */
public class BaseActivity extends com.pesdk.base.BaseActivity {
    protected String TAG = "BaseActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setImmersiveStatusBar(this, true);
    }

    public void onToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onToast(@StringRes int strId) {
        onToast(getString(strId));
    }


    public void changeFragment(int containerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(containerId, fragment).commitAllowingStateLoss();
    }
}
