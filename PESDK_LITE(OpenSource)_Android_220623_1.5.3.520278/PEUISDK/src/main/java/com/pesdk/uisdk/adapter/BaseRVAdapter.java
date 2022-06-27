package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.listener.OnMultiClickListener;


public abstract class BaseRVAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected String TAG = BaseRVAdapter.class.getName();


    public static final int UN_CHECK = -1;

    protected int lastCheck = UN_CHECK;
    private LayoutInflater mLayoutInflater;


    protected LayoutInflater getLayoutInflater(Context context) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(context);
        }
        return mLayoutInflater;
    }

    /**
     * 被选中的项的下标
     */
    public int getChecked() {
        return lastCheck;
    }

    /**
     * 设置被选中下标
     */
    public void setChecked(int checked) {
        if (lastCheck == checked) {
            return;
        }
        lastCheck = checked;
        notifyDataSetChanged();
    }

    public void clearChecked() {
        lastCheck = UN_CHECK;
        notifyDataSetChanged();
    }

    /**
     * 是否允许重复点击
     *
     * @param enableRepeatClick
     */
    public void setEnableRepeatClick(boolean enableRepeatClick) {
        this.enableRepeatClick = enableRepeatClick;
    }

    protected boolean enableRepeatClick = false;

    /**
     * 设置单击事件
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    protected OnItemClickListener mOnItemClickListener;

    /**
     * 查找容器范围内的组件
     */
    protected <T extends View> T $(View mRoot, int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * item单击事件
     */
    protected abstract class BaseItemClickListener extends OnMultiClickListener {
        protected int position;

        public void setPosition(int position) {
            this.position = position;
        }


    }

    //局部刷新
    public int PROGRESS = 100;//进度


}
