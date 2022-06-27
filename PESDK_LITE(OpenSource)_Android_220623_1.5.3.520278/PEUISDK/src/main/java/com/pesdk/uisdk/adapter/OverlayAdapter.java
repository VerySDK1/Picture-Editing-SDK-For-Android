package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.LineProgress;
import com.pesdk.uisdk.bean.net.IBean;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.utils.glide.GlideUtils;
import com.pesdk.uisdk.widget.CircleProgressBarView;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownListener;
import com.vecore.base.lib.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 叠加
 */
public class OverlayAdapter extends BaseRVAdapter<OverlayAdapter.VHolder> {

    public List<IBean> getList() {
        return mList;
    }

    private ArrayList<IBean> mList = new ArrayList<>();
    //当前分类ID
    private int currentSortIndex = UN_CHECK;
    //上次选中项的分类索引
    private int lastCheckedSortIndex = UN_CHECK;

    private RequestManager mRequestManager;

    public OverlayAdapter(Context context, RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    @Override
    public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_overlay_layout, parent, false);
        VHolder viewHolder = new VHolder(view);
        ViewClickListener viewClickListener = new ViewClickListener();
        viewHolder.mImageView.setOnClickListener(viewClickListener);
        viewHolder.mImageView.setTag(viewClickListener);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull VHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateCheckProgress(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(VHolder holder, int position) {
        IBean info = mList.get(position);
        ViewClickListener viewClickListener = (ViewClickListener) holder.mImageView.getTag();
        viewClickListener.setPosition(position);
        GlideUtils.setCover(mRequestManager, holder.mImageView, info.getCover());
        updateCheckProgress(holder, position);
    }


    //更新进度
    private void updateCheckProgress(VHolder holder, int position) {
        IBean info = getItem(position);
        holder.mImageView.setChecked(lastCheck == position);
        if (!TextUtils.isEmpty(info.getLocalPath())) {
            holder.ivDown.setVisibility(View.GONE);
        } else {
            LineProgress lineProgress = maps.get(info.getMId());
            if (maps.containsKey(info.getMId())) { //下载中
                holder.ivDown.setVisibility(View.GONE);
                holder.mBar.setProgress(lineProgress.getProgress());
            } else {
                holder.ivDown.setVisibility(View.VISIBLE);
            }
        }
    }

    public void addAll(List<IBean> list,int index) {
        mList.clear();
        if (list != null && list.size() > 0) {
            mList.addAll(list);
        }
        lastCheck=index;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public IBean getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return mList.get(position);
        }
        return null;
    }

    private HashMap<Long, LineProgress> maps = new HashMap<Long, LineProgress>();

    /**
     * 下载
     */
    private void down(final Context context, int position, final IBean info) {
        int itemId = info.getMId();
        if (null == maps.get(itemId)) {
            /**
             * 支持指定下载文件的存放位置
             */
            final DownLoadUtils download = new DownLoadUtils(context, itemId, info.getFile(), PathUtils.getOverlayFile(info.getFile()));
            maps.put((long) itemId, new LineProgress(position, 1));
            download.DownFile(new IDownListener() {

                @Override
                public void onFailed(long mid, int code) {
                    int key = (int) mid;
                    if (code == DownLoadUtils.RESULT_NET_UNCONNECTED) {
                        Utils.autoToastNomal(context, R.string.common_check_network);
                    }
                    if (null != maps) {
                        maps.remove(key);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onProgress(long mid, int progress) {
                    LineProgress line = maps.get(mid);
                    if (null != line) {
                        line.setProgress(progress);
                        maps.put(mid, line);
                        updateProgress(mid);
                    }

                }

                @Override
                public void Finished(final long mid, String localPath) {
                    maps.remove(mid);
                    info.setLocalPath(localPath);
                    mList.set(position, info);
                    lastCheck = position;
                    mOnItemClickListener.onItemClick(position, getItem(position));
                    notifyDataSetChanged();
                }

                @Override
                public void Canceled(long mid) {
                    Log.e(TAG, "Canceled: xxx" + mid);
                    if (null != maps) {
                        maps.remove(mid);
                    }
                    notifyDataSetChanged();
                }
            });
            notifyItemChanged(position, position + "");
        } else {
            Log.e(TAG, "download " + info.getFile() + "  is mDownloading");
        }
    }

    /**
     * 更新下载进度
     */
    private void updateProgress(long key) {
        LineProgress temp = maps.get(key);
        notifyItemChanged(temp.getPosition(), PROGRESS);
    }


    class ViewClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position || enableRepeatClick) {
                //将之前选中的item更新为未选中
                if (lastCheck != UN_CHECK) {
                    if (lastCheckedSortIndex == currentSortIndex) {
                        notifyItemChanged(lastCheck);
                    }
                }
                lastCheck = position;
                lastCheckedSortIndex = currentSortIndex;
                notifyItemChanged(position);
                IBean info = getItem(position);
                if (FileUtils.isExist(info.getLocalPath())) {
                    mOnItemClickListener.onItemClick(position, info);
                } else {
                    //不存在下载，文件
                    down(view.getContext(), position, info);
                }
            }
        }


    }

    class VHolder extends RecyclerView.ViewHolder {

        ImageView ivDown;
        CircleProgressBarView mBar;
        ExtRoundRectSimpleDraweeView mImageView;

        public VHolder(View itemView) {
            super(itemView);
            mImageView = Utils.$(itemView, R.id.ivItemImage);
            ivDown = Utils.$(itemView, R.id.ivDown);
            mBar = Utils.$(itemView, R.id.pbar);
        }
    }

}
