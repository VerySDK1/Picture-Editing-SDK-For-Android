package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.LineProgress;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.utils.glide.GlideUtils;
import com.pesdk.uisdk.widget.CircleProgressBarView;
import com.pesdk.uisdk.widget.ExtListItemStyle;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 字体
 */
public class TTFAdapter extends BaseRVAdapter<TTFAdapter.ViewHolder> {

    private final Context mContext;
    private ArrayList<ItemBean> mList = new ArrayList<>();
    private int textColorN;
    private int bgColor;
    private RequestManager mRequestManager;

    /**
     * @param context
     */
    public TTFAdapter(Context context, RequestManager requestManager) {
        mContext = context;
        mRequestManager = requestManager;
        mList.clear();
        textColorN = ContextCompat.getColor(context, R.color.pesdk_main_text_color_n);
        bgColor = ContextCompat.getColor(context, R.color.pesdk_bg_gray);
    }

    public void add(ArrayList<ItemBean> list, int index) {
        mList = list;
        lastCheck = index;
        mSparseArray.clear();
        notifyDataSetChanged();
    }


    public ItemBean getItem(int position) {
        return mList.get(position);
    }


    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_ttf_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, bgColor);
        onItemClickListener listener = new onItemClickListener();
        viewHolder.itemView.setOnClickListener(listener);
        viewHolder.itemView.setTag(listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateCheckProgress(holder, position);
        }
    }

    private void updateCheckProgress(ViewHolder vh, int position) {
        ItemBean info = getItem(position);
        if (PathUtils.isDownload(info.getLocalPath()) || position == 0) {
            vh.progressBar.setVisibility(View.GONE);
            if (lastCheck == position) {
                if (position == 0) {
                    vh.ivState.setVisibility(View.GONE);
                } else {  //网络字体已下载的被选中状态
                    vh.ivState.setVisibility(View.GONE);
                }
            }
        } else {
            LineProgress lineProgress = mSparseArray.get(info.getMId());
            if (null != lineProgress) {
                vh.ivState.setVisibility(View.GONE);
                vh.progressBar.setVisibility(View.VISIBLE);
                vh.progressBar.setProgress(lineProgress.getProgress());
            } else {
                vh.ivState.setVisibility(View.VISIBLE);
                vh.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        ItemBean info = getItem(position);
        if (position == 0) {
            vh.tv.setVisibility(View.VISIBLE);
            vh.tv.setText(info.getLocalPath());
            vh.cover.setVisibility(View.GONE);
        } else {
            vh.tv.setText("");
            vh.tv.setVisibility(View.GONE);
            vh.cover.setVisibility(View.VISIBLE);
            GlideUtils.setCover(mRequestManager, vh.cover, info.getCover());
        }
        vh.tv.setTextColor(textColorN);
        vh.ivState.setVisibility(View.GONE);
        vh.mBgStyle.setSelected(lastCheck == position);

        updateCheckProgress(vh, position);

        onItemClickListener listener = (onItemClickListener) vh.itemView.getTag();
        listener.setPosition(position);
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    /**
     * 设置选中
     */
    public void setIndex(int index) {
        if (index != lastCheck) {
            lastCheck = index;
            notifyDataSetChanged();
        }
    }


    private class onItemClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            ItemBean info = getItem(position);
            if (PathUtils.isDownload(info.getLocalPath()) || position == 0) {
                onHandlerItem(position);
            } else {
                onDown(position);
            }
        }
    }

    /**
     *
     */
    private void onHandlerItem(int position) {
        if (null != mOnItemClickListener) {
            mOnItemClickListener.onItemClick(position, getItem(position));
        }
    }

    private SparseArray<LineProgress> mSparseArray = new SparseArray<LineProgress>();
    private ArrayList<Integer> mArrPosition = new ArrayList<Integer>();
    private String TAG = "TTFAdapter";

    /**
     * 执行下载
     */
    private void onDown(final int position) {
        if (mSparseArray.size() > 3) { // 最多同时下载3个
            return;
        }
        if (null != mContext && CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            Utils.autoToastNomal(mContext, R.string.common_check_network);
            return;
        }
        for (Integer p : mArrPosition) {
            if (p == position) {
                return;
            }
        }

        final ItemBean info = getItem(position);
        mArrPosition.add(position);
        String localPath = PathUtils.getTTFFile(info.getFile());
        DownLoadUtils utils = new DownLoadUtils(mContext, info.getMId(), info.getFile(), localPath);
        utils.DownFile(new IDownListener() {

            @Override
            public void onFailed(long mid, int i) {
                Log.e(TAG, "onFailed: " + mid + ">>" + i);
                mSparseArray.remove((int) mid);
                notifyDataSetChanged();
            }

            @Override
            public void onProgress(long mid, int progress) {
                LogUtil.i(TAG, "onProgress:" + mid + " >" + progress);
                int key = (int) mid;
                LineProgress line = mSparseArray.get(key);
                if (null != line) {
                    line.setProgress(progress);
                }
                notifyItemChanged(position, position + "");
            }

            @Override
            public void Canceled(long mid) {
                Log.e(TAG, "Canceled: " + mid);
                mSparseArray.remove((int) mid);
                notifyDataSetChanged();
            }

            @Override
            public void Finished(long mid, String localPath) {
                LogUtil.i(TAG, "Finished:" + mid + " >" + localPath + " >" + mContext);
                lastCheck = position;
                mArrPosition.remove((Object) position);
                //字体路径
                info.setLocalPath(localPath); // 更新单个
                mSparseArray.remove((int) mid);
                notifyDataSetChanged();
                onHandlerItem(position);
            }
        });
        mSparseArray.put(info.getMId(), new LineProgress(position, 1));
        notifyItemChanged(position, position + "");
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivState;
        private ImageView cover;
        private TextView tv;
        private CircleProgressBarView progressBar;
        private ExtListItemStyle mBgStyle;

        ViewHolder(View itemView, int color) {
            super(itemView);
            tv = Utils.$(itemView, R.id.ttf_tv);
            cover = Utils.$(itemView, R.id.ttf_img);
            ivState = Utils.$(itemView, R.id.ttf_state);
            progressBar = Utils.$(itemView, R.id.ttf_pbar);
            mBgStyle = Utils.$(itemView, R.id.bg_style);

            mBgStyle.setBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.common_ic_transparent));
        }


    }

    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != mSparseArray && mSparseArray.size() > 0) {
            mSparseArray.clear();
            DownLoadUtils.forceCancelAll();
        }
    }

}
