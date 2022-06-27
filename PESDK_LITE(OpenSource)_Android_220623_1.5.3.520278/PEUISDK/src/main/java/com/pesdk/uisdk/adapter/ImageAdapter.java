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
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.utils.glide.GlideUtils;
import com.pesdk.uisdk.widget.CircleProgressBarView;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import static com.pesdk.net.PENetworkApi.Sky;

/**
 * 背景-样式
 */
public class ImageAdapter extends BaseRVAdapter<ImageAdapter.ImageHolder> {
    private List<ItemBean> mList = new ArrayList<>();
    private Context mContext;
    private RequestManager mRequestManager;
    private String mType;
    private int mItemSize;

    public ImageAdapter(Context context, RequestManager requestManager, String type) {
        this.mContext = context;
        mRequestManager = requestManager;
        mType = type;
    }

    /**
     * 指定单个item大小
     *
     * @param itemSize
     */
    public void setItemSize(int itemSize) {
        mItemSize = itemSize;
    }

    /**
     * 切换选中的item
     */
    public void updateCheck(int index) {
        lastCheck = index;
        notifyDataSetChanged();
    }


    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pesdk_item_sticker_data_layout, parent, false);
        ViewClickListener listener = new ViewClickListener();
        view.setOnClickListener(listener);
        view.setTag(listener);
        ImageHolder holder = new ImageHolder(view);
        if (mItemSize > 0) {
            ViewGroup.LayoutParams lp = holder.mCardView.getLayoutParams();
            lp.width = mItemSize;
            lp.height = mItemSize;
            holder.mCardView.setLayoutParams(lp);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        ItemBean style = getItem(position);
        GlideUtils.setCover(mRequestManager, holder.mImage, style.getCover());
        refreshUI(holder, position, style);
    }

    private void refreshUI(ImageHolder holder, int position, ItemBean style) {
        holder.mImage.setChecked(position == lastCheck);
        if (TextUtils.isEmpty(style.getLocalPath())) { //未下载
            long key = style.getMId();
            if (maps.containsKey(key)) { //下载中
                holder.ivDown.setVisibility(View.GONE);
                holder.mProgressBarView.setVisibility(View.VISIBLE);
                holder.mProgressBarView.setProgress(maps.get(key).getProgress());
            } else { //未下载
                holder.ivDown.setVisibility(View.VISIBLE);
                holder.mProgressBarView.setVisibility(View.GONE);
            }
        } else { //已下载
            holder.mProgressBarView.setVisibility(View.GONE);
            holder.ivDown.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {   //局部刷新
            refreshUI(holder, position, getItem(position));
        }
    }

    public void addAll(List<ItemBean> beans, int check) {
        mList = beans;
        lastCheck = check;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    private ItemBean getItem(int position) {
        return mList.get(position);
    }

    class ImageHolder extends RecyclerView.ViewHolder {

        private ExtRoundRectSimpleDraweeView mImage;
        private CircleProgressBarView mProgressBarView;
        private ImageView ivDown;
        private CardView mCardView;

        public ImageHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.itemLayout);
            mImage = itemView.findViewById(R.id.item_border);
            mProgressBarView = itemView.findViewById(R.id.ttf_pbar);
            ivDown = itemView.findViewById(R.id.ivDown);
        }

    }

    class ViewClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position) {
                ItemBean tmp = getItem(position);
                if (!PathUtils.isDownload(tmp.getLocalPath())) {
                    //下载
                    download(position);
                } else {
                    onItemClick(position);
                }
            }
        }
    }


    private HashMap<Long, LineProgress> maps = new HashMap<Long, LineProgress>();

    /**
     * 执行下载
     */
    public void download(final int p) {
        if (maps.size() < 2) {
            // 最多同时下载2个
            if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                Utils.autoToastNomal(mContext, R.string.common_check_network);
            } else {
                final ItemBean info = getItem(p);
                long Mid = info.getFile().hashCode();
                if (null != info && !maps.containsKey(Mid)) {
                    boolean isSky = Sky.equals(mType);
                    String path = isSky ? PathUtils.getSkyFile(info.getFile()) : PathUtils.getBGFile(info.getFile());
                    DownLoadUtils downLoadUtils = new DownLoadUtils(mContext, Mid, info.getFile(), path);
                    downLoadUtils.DownFile(new IDownFileListener() {
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
                            maps.remove(mid);
                            onItemDownloaded(p, localPath);
                        }
                    });
                    maps.put(Mid, new LineProgress(p, 0));
                    Log.e(TAG, "download: xxx" + p);
                    notifyItemChanged(p, PROGRESS);
                } else {
                    Utils.autoToastNomal(mContext, R.string.pesdk_dialog_download_ing);
                }
            }
        } else {
            Utils.autoToastNomal(mContext, R.string.pesdk_download_thread_limit_msg);
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
     * 下载完成
     */
    private void onItemDownloaded(int p, String localPath) {
        ItemBean tmp = getItem(p);
        tmp.setLocalPath(localPath);
        mList.set(p, tmp);
        onItemClick(p);
    }

    /**
     * 响应选中
     *
     * @param position
     */
    private void onItemClick(int position) {
        lastCheck = position;
        notifyDataSetChanged();
        if (null != mOnItemClickListener) {
            mOnItemClickListener.onItemClick(position, getItem(position));
        }
    }


}
