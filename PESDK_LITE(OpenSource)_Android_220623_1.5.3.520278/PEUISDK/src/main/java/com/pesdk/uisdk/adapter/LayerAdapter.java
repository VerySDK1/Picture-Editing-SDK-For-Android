package com.pesdk.uisdk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;
import com.pesdk.utils.glide.GlideUtils;
import com.vecore.base.lib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 已添加图层列表
 */
public class LayerAdapter extends BaseRVAdapter<LayerAdapter.ViewHolder> {
    private String TAG = "LayerAdapter";
    private LayoutInflater mLayoutInflater;
    private List<CollageInfo> list = null;
    private RequestManager mRequestManager;
    private boolean mIsPipUI;

    /**
     * @param requestManager
     * @param pipUI          true  PipFragment; false PipPopWindow   隐藏按钮UI不一致
     */
    public LayerAdapter(RequestManager requestManager, boolean pipUI) {
        mRequestManager = requestManager;
        list = new ArrayList<>();
        mIsPipUI = pipUI;
    }

    /**
     *
     */
    public void addAll(List<CollageInfo> tmp, int index) {
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        lastCheck = index;
        notifyDataSetChanged();
    }


    public CollageInfo getItem(int position) {
        return list.get(position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(mIsPipUI ? R.layout.pesdk_item_pip_layer_layout : R.layout.pesdk_item_pop_pip_layer_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new LayerAdapter.ViewHolder(view);
    }

    public void setIHideClickListener(IHideClickListener IHideClickListener) {
        mIHideClickListener = IHideClickListener;
    }

    private IHideClickListener mIHideClickListener;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        CollageInfo info = getItem(position);
        if (mIsPipUI) {
            if (holder.ivIcon instanceof ExtRoundRectSimpleDraweeView) {
                ((ExtRoundRectSimpleDraweeView) holder.ivIcon).setChecked(position == lastCheck);
            }
            GlideUtils.setCover(mRequestManager, holder.ivIcon, info.getImageObject().getMediaPath());
            holder.ivHide.setVisibility(info.isHide() ? View.VISIBLE : View.GONE);
        } else {
            //封面展示有差异
            GlideUtils.setLayerCover(mRequestManager, holder.ivIcon, info.getImageObject().getMediaPath());
            if (position == lastCheck) {
                holder.mShadow.setVisibility(View.VISIBLE);
                holder.ivHide.setVisibility(View.VISIBLE);
                holder.ivHide.setImageResource(info.isHide() ? R.drawable.pesdk_layer_ic_hide_p : R.drawable.pesdk_layer_ic_hide_n);
                holder.ivHide.setOnClickListener(v -> {
                    if (null != mIHideClickListener) {
                        mIHideClickListener.onHide(position, !info.isHide());
                    }
                });
            } else {
                holder.mShadow.setVisibility(View.GONE);
                holder.ivHide.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIcon;
        ImageView ivHide;
        View mShadow;

        ViewHolder(View itemView) {
            super(itemView);
            ivHide = itemView.findViewById(R.id.ivHided);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            mShadow = itemView.findViewById(R.id.shadow);
        }
    }

    public static interface IHideClickListener {

        void onHide(int position, boolean hide);

    }

    class ViewClickListener extends BaseItemClickListener {


        @Override
        protected void onSingleClick(View view) {
            LogUtil.i(TAG, "onClick: >>" + position + " " + lastCheck);
            if (lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }


}
