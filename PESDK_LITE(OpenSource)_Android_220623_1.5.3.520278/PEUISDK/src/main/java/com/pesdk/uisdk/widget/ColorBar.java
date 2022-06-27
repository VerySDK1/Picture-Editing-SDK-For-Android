package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.ColorListAdapter;
import com.pesdk.uisdk.listener.OnItemClickListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 字幕颜色选中器
 */
public class ColorBar extends LinearLayout {
    private View mView;
    private RecyclerView mColorListView;
    private ColorListAdapter mColorAdapter;

    public ColorBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mView = LayoutInflater.from(context).inflate(R.layout.pesdk_widget_color_bar_layout, null);

        TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.pesdk_ColorBar);
        boolean hide = tArray.getBoolean(R.styleable.pesdk_ColorBar_hideNone, false);
        int px = tArray.getDimensionPixelSize(R.styleable.pesdk_ColorBar_itemSize, -1);
        tArray.recycle();

        addView(mView);
        mColorListView = mView.findViewById(R.id.paint_color_list);
        View mNone = mView.findViewById(R.id.btn_color_none);

        if (hide) {
            mNone.setVisibility(GONE);
        } else {
            ViewGroup.LayoutParams lp = mNone.getLayoutParams();
            if (px > 0) {
                lp.width = px;
                lp.height = px;
                mNone.setLayoutParams(lp);
            }
            mNone.setOnClickListener(v -> {
                mColorAdapter.setChecked(BaseRVAdapter.UN_CHECK);
                mCallback.onNone();
            });
        }
        initColorListView(px);
    }


    /**
     * 初始化颜色列表
     */
    private void initColorListView(int size) {
        mColorListView.setHasFixedSize(false);
        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(getContext());
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mColorListView.setLayoutManager(stickerListLayoutManager);
        mColorAdapter = new ColorListAdapter(size);
        mColorAdapter.setOnItemClickListener((OnItemClickListener<Integer>) (position, item) -> mCallback.onColor(item));
        mColorListView.setAdapter(mColorAdapter);
    }

    public void setColor(int labelColor) {
        if (null != mColorAdapter) {
            mColorAdapter.setCheckedColor(labelColor);
        }

    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Callback mCallback;

    public int getColor() {
        return null != mColorAdapter ? mColorAdapter.getItem(mColorAdapter.getChecked()) : Color.BLACK;
    }


    public static interface Callback {

        void onNone();

        void onColor(int color);
    }
}
