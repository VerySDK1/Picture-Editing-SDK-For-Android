package com.pesdk.uisdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * 屏蔽掉viewpage的左右滑动功能，防止各种冲突
 */
public class ExtViewPager extends ViewPager {

    public ExtViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        enableAutoScroll(true);
    }

    private boolean enableScroll = true;

    /**
     * 是否屏蔽滑动事件
     *
     * @param enable true 允许翻页;false 屏蔽翻页
     */
    public void enableAutoScroll(boolean enable) {
        enableScroll = enable;
    }


    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (enableScroll)
            return super.onTouchEvent(arg0);

        else
            return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (enableScroll)
            return super.onInterceptTouchEvent(arg0);
        else
            return false;
    }

}
