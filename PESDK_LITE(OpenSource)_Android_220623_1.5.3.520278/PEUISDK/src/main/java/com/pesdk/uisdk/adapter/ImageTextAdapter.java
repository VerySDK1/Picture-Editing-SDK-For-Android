package com.pesdk.uisdk.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.CoverInfo;
import com.pesdk.utils.glide.GlideUtils;
import com.pesdk.uisdk.widget.ExtCircleSimpleDraweeView;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 图片圆形正方形、文字     不带下载进度
 */
public class ImageTextAdapter extends BaseRVAdapter<ImageTextAdapter.ImageHolder> {

    private boolean mCircle = false;
    private boolean mHideText = false;
    private ArrayList<CoverInfo> mCoverData = new ArrayList<>();
    private long mLastClickTime = 0;
    private RequestManager mRequestManager;

    public ImageTextAdapter(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_image_text, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, final int position) {
        TAG = "ImageTextAdapter";
        CoverInfo data = mCoverData.get(position);
        holder.mImageView.setChecked(position == lastCheck);
        holder.mImageView2.setChecked(position == lastCheck);
        if (mCircle) {
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.mImageView2.setVisibility(View.GONE);
            if (data.getDrawableIcon() == 0) {
                GlideUtils.setCover(mRequestManager,holder.mImageView, data.getPath());
            } else {
                GlideUtils.setCover(mRequestManager,holder.mImageView, data.getDrawableIcon());
            }

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickItem(position);
                }
            });
        } else {
            holder.mImageView.setVisibility(View.GONE);
            holder.mImageView2.setVisibility(View.VISIBLE);
            if (data.getDrawableIcon() == 0) {
                GlideUtils.setCover(mRequestManager,holder.mImageView2, data.getPath());
            } else {
                GlideUtils.setCover(mRequestManager,holder.mImageView2, data.getDrawableIcon());
            }
            holder.mImageView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickItem(position);
                }
            });
        }
        if (mHideText || TextUtils.isEmpty(data.getName())) {
            holder.mText.setVisibility(View.GONE);
        } else {
            holder.mText.setVisibility(View.VISIBLE);
            holder.mText.setSelected(lastCheck == position);
            holder.mText.setText(data.getName());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItem(position);
            }
        });
    }

    private void onClickItem(int position) {
        if (mOnItemClickListener != null) {
            //防止频繁点击
//            if (System.currentTimeMillis() - mLastClickTime > (CaptionAnimHandler.CAPTION_ANIM_MAX_DURATION * 1000)) { //保证单次动画预览完全
            setChecked(position);
            mOnItemClickListener.onItemClick(position, null);
            mLastClickTime = System.currentTimeMillis();
//            }
        }
    }

    public void addCoverData(List<CoverInfo> coverData) {
        addCoverData(coverData, -1);
    }

    public void addCoverData(List<CoverInfo> coverData, int lastcheck) {
        mCoverData.clear();
        if (coverData != null && coverData.size() > 0) {
            mCoverData.addAll(coverData);
        }
        lastCheck = lastcheck;
        notifyDataSetChanged();
    }

    public void setChecked(int lastcheck) {
        lastCheck = lastcheck;
        notifyDataSetChanged();
    }

    /**
     * 设置圆形还是放心 默认方
     *
     * @param b
     */
    public void setCircle(boolean b) {
        this.mCircle = b;
    }

    /**
     * 是否显示名字 默认显示
     *
     * @param b
     */
    public void setHideText(boolean b) {
        this.mHideText = b;
    }

    @Override
    public int getItemCount() {
        return mCoverData.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {

        TextView mText;
        ExtRoundRectSimpleDraweeView mImageView2;//
        ExtCircleSimpleDraweeView mImageView;

        public ImageHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.tvItemCaption);
            mImageView2 = itemView.findViewById(R.id.ivItemImage2);
            mImageView = itemView.findViewById(R.id.ivItemImage);
        }
    }
}

