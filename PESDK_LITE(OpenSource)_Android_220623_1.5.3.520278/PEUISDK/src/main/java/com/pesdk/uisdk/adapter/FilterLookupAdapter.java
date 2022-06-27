package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.Interface.IProgressCallBack;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.net.WebFilterInfo;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;
import com.pesdk.uisdk.widget.RoundProgressBar;
import com.pesdk.utils.glide.GlideUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class FilterLookupAdapter extends BaseRVAdapter<FilterLookupAdapter.ViewHolder> {
    private List<WebFilterInfo> list;
    private LayoutInflater mLayoutInflater;
    private int mColorNormal, mColorSelected;
    private RequestManager mRequestManager;

    /**
     * @param context
     */
    public FilterLookupAdapter(Context context, RequestManager requestManager) {
        mRequestManager = requestManager;
        mColorNormal = ContextCompat.getColor(context, R.color.pesdk_white);
        mColorSelected = ContextCompat.getColor(context, R.color.pesdk_main_color);
        list = new ArrayList<>();
    }


    /**
     * @param tmp
     * @param checked
     */
    public void addAll(List<WebFilterInfo> tmp, int checked) {
        list = tmp;
        lastCheck = checked;
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public FilterLookupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.pesdk_list_item_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new FilterLookupAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateCheckProgress(holder, position);
        }
    }

    /**
     * 更新选中状态和进度
     *
     * @param holder
     * @param position
     */
    private void updateCheckProgress(ViewHolder holder, int position) {
        holder.mText.setTextColor(position == lastCheck ? mColorSelected : mColorNormal);
        WebFilterInfo info = getItem(position);
        holder.mText.setText(info.getName());
        if (position == lastCheck && !TextUtils.isEmpty(info.getLocalPath())) { //选中
            holder.progressLayout.setVisibility(View.GONE);
            holder.mImageView2.setChecked(true);
        } else if (null != mProgressCallBack && mProgressCallBack.getMap() != null && mProgressCallBack.getMap().containsKey(info.getUrl())) { //下载中
            int pro = mProgressCallBack.getMap().get(info.getUrl());
            holder.progressLayout.setVisibility(View.VISIBLE);
            holder.mBar.setProgress(pro);
        } else {
            holder.progressLayout.setVisibility(View.GONE);
            holder.mImageView2.setChecked(false);
        }
        String coverUrl = info.getCover();
        if (!TextUtils.isEmpty(coverUrl)) {
            GlideUtils.setCover(mRequestManager, holder.mImageView2, coverUrl);
        }
    }

    @Override
    public void onBindViewHolder(FilterLookupAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        updateCheckProgress(holder, position);
    }

    /***
     * 设置为选中状态
     */
    public void onItemChecked(int nItemId) {
        lastCheck = nItemId;
        notifyDataSetChanged();
    }

    public void setdownStart(int nItemId) {
        lastCheck = nItemId;
        notifyDataSetChanged();
    }

    public void setdownProgress(int nItemId) {
        lastCheck = nItemId;
        notifyItemChanged(nItemId, nItemId + "");
    }

    public void setdownEnd(int nItemId) {
        lastCheck = nItemId;
        notifyDataSetChanged();
    }

    public void setdownFailed(int nItemId) {
        lastCheck = nItemId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * @param position
     * @return
     */
    public WebFilterInfo getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return list.get(position);
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;
        ExtRoundRectSimpleDraweeView mImageView2;
        View progressLayout;
        RoundProgressBar mBar;

        ViewHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.tvItemCaption);//名称
            mImageView2 = itemView.findViewById(R.id.ivItemImage2);//图片
            mBar = itemView.findViewById(R.id.progressBar);
            progressLayout = itemView.findViewById(R.id.progressLayout);//图片
        }
    }

    class ViewClickListener extends BaseItemClickListener {
        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position || enableRepeatClick) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }

    public void setProgressCallBack(IProgressCallBack progressCallBack) {
        mProgressCallBack = progressCallBack;
    }

    protected IProgressCallBack mProgressCallBack;


}
