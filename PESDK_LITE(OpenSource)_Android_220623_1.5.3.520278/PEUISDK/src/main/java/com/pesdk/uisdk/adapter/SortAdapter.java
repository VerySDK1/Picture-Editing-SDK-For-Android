package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.helper.IndexHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SortAdapter extends BaseRVAdapter<SortAdapter.SortHolder> {
    private List<SortBean> mSortBeans = new ArrayList<>();
    private int mColorNormal, nColorP;

    public SortAdapter(Context context) {
        mColorNormal = ContextCompat.getColor(context, R.color.pesdk_main_text_color_n);
        nColorP = ContextCompat.getColor(context, R.color.pesdk_main_press_color);
    }


    @Override
    public SortHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_sort_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new SortHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SortHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateItemHolder(holder, position);
        }
    }

    /**
     * 更新选中状态和进度
     *
     * @param holder
     * @param position
     */
    private void updateItemHolder(SortHolder holder, int position) {
        SortBean sortApi = mSortBeans.get(position);
        if ("0".equals(sortApi.getId())) {
            holder.noneLayout.setVisibility(View.VISIBLE);
            holder.mTvName.setVisibility(View.VISIBLE);
            holder.mFlIcon.setVisibility(View.GONE);
            holder.mTvName.setVisibility(View.GONE);
            try {
                holder.tvNone.setTextColor(lastCheck == position ? nColorP : mColorNormal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.mTvName.setVisibility(View.VISIBLE);
            holder.mFlIcon.setVisibility(View.GONE);
            holder.noneLayout.setVisibility(View.GONE);

            holder.mTvName.setText(sortApi.getName().trim());
            holder.mTvName.setTextColor(lastCheck == position ? nColorP : mColorNormal);
        }

    }

    @Override
    public void onBindViewHolder(SortHolder holder, int position) {
        ViewClickListener clickListener = (ViewClickListener) holder.itemView.getTag();
        clickListener.setPosition(position);
        updateItemHolder(holder, position);
    }


    public void addAll(List<SortBean> icon, int checked) {
        mSortBeans.clear();
        if (null != icon && icon.size() > 0) {
            mSortBeans.addAll(icon);
        }
        lastCheck = checked;
        notifyDataSetChanged();
    }

    public void setPosition(int position) {
        lastCheck = position;
        notifyDataSetChanged();
    }

    private String getItem(int position) {
        return get(position).getId();
    }

    public SortBean get(int position) {
        if (position >= 0 && position < mSortBeans.size()) {
            return mSortBeans.get(position);
        }
        return null;
    }

    /**
     * 获取当前id
     *
     * @return
     */
    public String getCurrent() {
        if (mSortBeans.size() > 0) {
            if (lastCheck == -1 || lastCheck >= mSortBeans.size()) {
                return mSortBeans.get(0).getId();
            }
            return mSortBeans.get(lastCheck).getId();
        }
        return null;
    }

    //设置选中
    public void setCurrent(String sortId) {
        lastCheck = IndexHelper.getSortIndex(mSortBeans, sortId);
        notifyDataSetChanged();
    }

    //加载数据
    public void selectSort(int position) {
        if (lastCheck != position) {
            int tmp = lastCheck;
            lastCheck = position;
            notifyItemChanged(position, position + "");
            if (tmp >= 0) {
                //清理上次选中
                notifyItemChanged(tmp, position + "");
            }
        }
    }

    //加载上一个数据
    public void loadUp() {
        if (lastCheck > 0 && !"0".equals(getItem(lastCheck - 1))) {
            lastCheck--;
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onItemClick(lastCheck, get(lastCheck));
            }
            notifyDataSetChanged();
        }
    }

    //加载下一个
    public void loadDown() {
        if (lastCheck < mSortBeans.size() - 1) {
            lastCheck++;
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onItemClick(lastCheck, get(lastCheck));
            }
            notifyDataSetChanged();
        }
    }


    @Override
    public int getItemCount() {
        return mSortBeans.size();
    }

    class SortHolder extends RecyclerView.ViewHolder {

        private ImageView mSrc;
        private TextView mTvName, tvNone;
        private FrameLayout mFlIcon;
        private ImageView mIvPoint;
        private View noneLayout;

        SortHolder(View itemView) {
            super(itemView);
            noneLayout = itemView.findViewById(R.id.noneLayout);
            mSrc = itemView.findViewById(R.id.sdv_src);
            tvNone = itemView.findViewById(R.id.tvNone);
            mTvName = itemView.findViewById(R.id.tv_name);
            mFlIcon = itemView.findViewById(R.id.fl_icon);
            mIvPoint = itemView.findViewById(R.id.point);
        }
    }


    class ViewClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position) {
                int tmp = lastCheck;
                lastCheck = position;
                notifyItemChanged(position, position + "");

                if (tmp >= 0) {
                    //清理上次选中
                    notifyItemChanged(tmp, position + "");
                }
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, get(position));
                }
            }
        }
    }
}
