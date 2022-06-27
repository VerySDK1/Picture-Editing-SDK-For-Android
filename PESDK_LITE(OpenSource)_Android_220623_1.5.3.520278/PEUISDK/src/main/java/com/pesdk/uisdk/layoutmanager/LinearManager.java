package com.pesdk.uisdk.layoutmanager;

import android.content.Context;
import android.view.View;

import com.pesdk.uisdk.Interface.OnViewPagerListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RV上下翻页播放
 */
public class LinearManager extends LinearLayoutManager implements RecyclerView.OnChildAttachStateChangeListener {

    private PagerSnapHelper pagerSpaner;
    private final String TAG = "LinearManager";

    private OnViewPagerListener viewPagerListener;
    private int diffY = 0;

    public LinearManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        pagerSpaner = new PagerSnapHelper();
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        view.addOnChildAttachStateChangeListener(this);
        pagerSpaner.attachToRecyclerView(view);
    }


    @Override
    public void onChildViewDetachedFromWindow(View view) {
        int position = getPosition(view);
        if (0 < diffY) {
            viewPagerListener.onPageRelease(true, position);
        } else {
            viewPagerListener.onPageRelease(false, position);
        }
    }


    @Override
    public void onChildViewAttachedToWindow(View view) {
        int position = getPosition(view);
        if (mState != RecyclerView.SCROLL_STATE_DRAGGING) { //排除正在滑动中...
            viewPagerListener.onPageSelected(position, false);
        }
    }

    private int mState = RecyclerView.SCROLL_STATE_IDLE;

    @Override
    public void onScrollStateChanged(int state) {
        mState = state;
        if (RecyclerView.SCROLL_STATE_IDLE == state) {
            View view = pagerSpaner.findSnapView(this);
            int position = getPosition(view);
            viewPagerListener.onPageSelected(position, position == getItemCount() - 1);
        }
        super.onScrollStateChanged(state);
    }

    public void setOnViewPagerListener(OnViewPagerListener listener) {
        viewPagerListener = listener;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        diffY = dy;
        return super.scrollVerticallyBy(dy, recycler, state);
    }
}
