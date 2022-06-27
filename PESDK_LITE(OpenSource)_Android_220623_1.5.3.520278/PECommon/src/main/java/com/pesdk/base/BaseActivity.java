package com.pesdk.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pesdk.api.ChangeLanguageHelper;
import com.pesdk.net.repository.PENetworkRepository;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 支持语言切换
 */
public class BaseActivity extends AppCompatActivity implements ILanguage {
    protected String TAG = "BaseActivity";

    private boolean isEn = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ChangeLanguageHelper.attachBaseContext(newBase, ChangeLanguageHelper.getAppLanguage(newBase)));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isEn = ChangeLanguageHelper.isEn(this);
        //后台依赖此变量获取翻译;
        PENetworkRepository.setLanType(isEn() ? "en" : "cn");
    }

    public <T extends View> T $(@IdRes int id) {
        return findViewById(id);
    }




    @Override
    public boolean isEn() {
        return isEn;
    }

}