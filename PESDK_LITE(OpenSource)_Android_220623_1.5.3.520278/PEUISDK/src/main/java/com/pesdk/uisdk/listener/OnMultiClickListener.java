package com.pesdk.uisdk.listener;

import android.view.View;

/**
 * 点击相隔时间
 */
public abstract class OnMultiClickListener implements View.OnClickListener{

    // 两次点击按钮之间的点击间隔不能少于600毫秒
    public static final int MIN_CLICK_DELAY_TIME = 100;

    private long mLastClickTime;

    @Override
    public void onClick(View v) {
        long curClickTime = System.currentTimeMillis();
        if((curClickTime - mLastClickTime) >= MIN_CLICK_DELAY_TIME) {
            // 超过点击间隔后再将lastClickTime重置为当前点击时间
            mLastClickTime = curClickTime;
            onSingleClick(v);
        }
    }

    /**
     * 单击
     */
    protected abstract void onSingleClick(View view);

}