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
import com.pesdk.uisdk.bean.model.BorderStyleInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.utils.glide.GlideUtils;
import com.pesdk.uisdk.widget.CircleProgressBarView;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


/**
 * 边框样式
 */
public class FrameAdapter extends BaseRVAdapter<FrameAdapter.ViewHolder> {

    private Context mContext;
    private List<BorderStyleInfo> mList = new ArrayList<>();
    private RequestManager mRequestManager;

    public FrameAdapter(Context context, RequestManager requestManager) {
        TAG = "FrameAdapter";
        mContext = context;
        mRequestManager = requestManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_sticker_data_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setTag(viewClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ViewClickListener tmp = (ViewClickListener) holder.itemView.getTag();
        tmp.setPosition(position);
        tmp.setParent(holder.itemView);
        holder.mBorderView.setOnClickListener(tmp);
        BorderStyleInfo info = mList.get(position);
        GlideUtils.setCover(mRequestManager, holder.mBorderView, info.getIcon());
        updateUI(holder, position, info);
    }

    private void updateUI(ViewHolder holder, int position, BorderStyleInfo info) {
        holder.mBorderView.setChecked(lastCheck == position);
        if (!TextUtils.isEmpty(info.getLocalpath())) {
            holder.ivDown.setVisibility(View.GONE);
            holder.mBar.setVisibility(View.GONE);
        } else {
            if (maps.containsKey(info.getId())) {
                holder.ivDown.setVisibility(View.GONE);
                holder.mBar.setVisibility(View.VISIBLE);
                holder.mBar.setProgress(maps.get(info.getId()).getProgress());
            } else {
                holder.mBar.setVisibility(View.GONE);
                holder.ivDown.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            BorderStyleInfo info = mList.get(position);
            updateUI(holder, position, info);
        }
    }


    public void addStyles(List<BorderStyleInfo> list, int lastcheck) {
        lastCheck = lastcheck;
        mList = list;
        notifyDataSetChanged();
    }


    private BorderStyleInfo getItem(int position) {
        if (position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    public void setCheckItem(int nposition) {
        if (nposition != lastCheck) {
            lastCheck = nposition;
            notifyDataSetChanged();
        }
    }


    private HashMap<Long, LineProgress> maps = new HashMap<Long, LineProgress>();

    /**
     * 执行下载
     */
    public void onDown(final int p, View itemView) {
        if (maps.size() < 2) {
            // 最多同时下载2个
            if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                Utils.autoToastNomal(mContext, R.string.common_check_network);
            } else {
                final BorderStyleInfo info = getItem(p);
                if (null != info && !maps.containsKey((long) info.getId())) {
                    String tmpLocal = PathUtils.getFrameFile(info.getUrl());
                    DownLoadUtils utils = new DownLoadUtils(mContext, info.getId(), info.getUrl(), tmpLocal);
                    utils.setMethod(false);
                    utils.DownFile(new IDownFileListener() {
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
                        public void Canceled(long mid) {
                            maps.remove(mid);
                            notifyItemChanged(p, PROGRESS);
                        }

                        @Override
                        public void Finished(long mid, String localPath) {
                            onItemDownloaded(info, p, mid, localPath);
                        }
                    });
                    maps.put((long) info.getId(), new LineProgress(p, 0));
                    CircleProgressBarView pbar = Utils.$(itemView, R.id.ttf_pbar);
                    View ivdown = Utils.$(itemView, R.id.ivDown);
                    ivdown.setVisibility(View.GONE);
                    pbar.setVisibility(View.VISIBLE);
                    pbar.setProgress(1);
                    notifyItemChanged(p, PROGRESS);
                } else {
                    Log.e(TAG, "onDown: isdownloading " + info.getId());
                    Utils.autoToastNomal(mContext, R.string.pesdk_dialog_download_ing);
                }
            }
        } else {
            Utils.autoToastNomal(mContext, R.string.pesdk_download_thread_limit_msg);
        }
    }

    /**
     * 下载完成
     *
     * @param info
     * @param p
     * @param mid
     * @param localPath
     */
    private void onItemDownloaded(final BorderStyleInfo info, final int p, final long mid, String localPath) {
        final File file = new File(localPath);
        if (FileUtils.isExist(file)) {
            maps.remove(mid);
            info.setLocalpath(localPath);
            mList.set(p, info);

            setCheckItem(p);
            mOnItemClickListener.onItemClick(p, info);
        } else {
            maps.remove(mid);
            lastCheck = BaseRVAdapter.UN_CHECK;
            notifyDataSetChanged();
        }

    }

    /**
     * 更新下载进度
     */
    private void updateProgress(long key) {
        LineProgress temp = maps.get(key);
        notifyItemChanged(temp.getPosition(), PROGRESS);
    }

    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != mList) {
            mList.clear();
        }
        clearDownloading();
    }

    /**
     * 关闭当前清除全部下载
     */
    public void clearDownloading() {
        if (null != maps && maps.size() > 0) {
            maps.clear();
            DownLoadUtils.forceCancelAll();
        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ExtRoundRectSimpleDraweeView mBorderView;
        private CircleProgressBarView mBar;
        private ImageView ivDown;

        ViewHolder(View itemView) {
            super(itemView);
            mBorderView = itemView.findViewById(R.id.item_border);
            mBar = itemView.findViewById(R.id.ttf_pbar);
            ivDown = itemView.findViewById(R.id.ivDown);
        }
    }

    class ViewClickListener extends BaseItemClickListener {

        private View parent;

        public void setParent(View parent) {
            this.parent = parent;
        }

        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position) {
                BorderStyleInfo tmp = getItem(position);
                if (!TextUtils.isEmpty(tmp.getLocalpath())) {
                    setCheckItem(position);
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(position, tmp);
                    }
                } else {
                    onDown(position, parent);
                }
            }
        }
    }


}
