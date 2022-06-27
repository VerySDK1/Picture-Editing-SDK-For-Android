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
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.utils.glide.GlideUtils;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.helper.StickerUtils;
import com.pesdk.uisdk.widget.CircleProgressBarView;
import com.pesdk.uisdk.widget.ExtRoundRectSimpleDraweeView;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


/**
 * 贴纸样式
 */
public class StickerAdapter extends BaseRVAdapter<StickerAdapter.StickerDataHolder> {

    private Context mContext;
    private ArrayList<StyleInfo> mArrStyleInfo = new ArrayList<>();
    private RequestManager mRequestManager;

    public StickerAdapter(Context context, RequestManager requestManager) {
        lastCheck = 0;
        mContext = context;
        mRequestManager = requestManager;
        TAG = "StickerAdapter";
    }

    @Override
    public StickerDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_sticker_data_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setTag(viewClickListener);
        return new StickerDataHolder(view);
    }

    @Override
    public void onBindViewHolder(final StickerDataHolder holder, int position) {
        StickerAdapter.ViewClickListener tmp = (StickerAdapter.ViewClickListener) holder.itemView.getTag();
        tmp.setPosition(position);
        tmp.setParent(holder.itemView);
        holder.mBorderView.setOnClickListener(tmp);

        StyleInfo info = mArrStyleInfo.get(position);
        GlideUtils.setCover(mRequestManager, holder.mBorderView, info.icon);
        updateUI(holder, position, info);

    }

    private void updateUI(StickerDataHolder holder, int position, StyleInfo info) {
        if (info.isdownloaded) {
            holder.mBorderView.setChecked(lastCheck == position);
            holder.ivDown.setVisibility(View.GONE);
            holder.mProgressBarView.setVisibility(View.GONE);
        } else {
            holder.mBorderView.setChecked(false);
            if (maps.containsKey(info.pid)) {
                holder.ivDown.setVisibility(View.GONE);
                holder.mProgressBarView.setVisibility(View.VISIBLE);
                holder.mProgressBarView.setProgress(maps.get(info.pid).getProgress());
            } else {
                holder.mProgressBarView.setVisibility(View.GONE);
                holder.ivDown.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onBindViewHolder(StickerDataHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            StyleInfo info = mArrStyleInfo.get(position);
            updateUI(holder, position, info);
        }
    }


    public void addStyles(List<StyleInfo> list, int lastcheck) {
        lastCheck = lastcheck;
        mArrStyleInfo.clear();
        for (StyleInfo info : list) {
            mArrStyleInfo.add(info);
        }
        notifyDataSetChanged();
    }

    public int getPosition(int styleId) {
        int index = BaseRVAdapter.UN_CHECK;
        int len = getItemCount();
        StyleInfo temp;
        for (int i = 0; i < len; i++) {
            temp = getItem(i);
            if (temp.pid == styleId) {
                index = i;
                break;
            }
        }
        return index;
    }


    public StyleInfo getItem(int position) {
        if (position < 0 || position >= mArrStyleInfo.size()) {
            return null;
        }
        return mArrStyleInfo.get(position);
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
                final StyleInfo info = getItem(p);
                if (null != info && !maps.containsKey((long) info.pid)) {
                    String tmpLocal = PathUtils.getTempFileNameForSdcard("Temp_", "zip");
                    DownLoadUtils utils = new DownLoadUtils(mContext, info.pid, info.caption, tmpLocal);
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
                    maps.put((long) info.pid, new LineProgress(p, 1));
                    CircleProgressBarView pbar = Utils.$(itemView, R.id.ttf_pbar);
                    View ivdown = Utils.$(itemView, R.id.ivDown);
                    ivdown.setVisibility(View.GONE);
                    pbar.setVisibility(View.VISIBLE);
                    pbar.setProgress(1);
                    notifyItemChanged(p, PROGRESS);
                } else {
                    Log.e(TAG, "onDown: isdownloading " + info.pid);
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
    private void onItemDownloaded(final StyleInfo info, final int p, final long mid, String localPath) {
        final File zip = new File(localPath);
        if (FileUtils.isExist(zip)) {
            try {
                String targetPath = PathUtils.getStickerChildDir(info.caption);
                // 解压
                final String dirpath = FileUtils.unzip(zip.getAbsolutePath(), targetPath);
                if (!TextUtils.isEmpty(dirpath)) {
                    ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
                        @Override
                        public void onBackground() {
                            String str = StyleInfo.getConfigPath(dirpath);
                            if (!TextUtils.isEmpty(str)) {
                                File config = new File(str);
                                info.mlocalpath = config.getParent();
                                CommonStyleUtils.getConfig(config, info);
                            }

                        }

                        @Override
                        public void onEnd() {
                            super.onEnd();
                            maps.remove(mid);
                            notifyItemChanged(p, PROGRESS);


                            info.isdownloaded = true;
                            zip.delete(); // 删除原mv的临时文件
                            StickerUtils.getInstance().putStyleInfo(info);
                            mArrStyleInfo.set(p, info);
                            setCheckItem(p);

                            mOnItemClickListener.onItemClick(p, info);
                        }
                    });
                } else {
                    maps.remove(mid);
                    notifyItemChanged(p, PROGRESS);
                }
            } catch (IOException e) {
                e.printStackTrace();
                maps.remove(mid);
                notifyItemChanged(p, PROGRESS);
            }
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
        if (null != mArrStyleInfo) {
            mArrStyleInfo.clear();
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
        return mArrStyleInfo.size();
    }

    class StickerDataHolder extends RecyclerView.ViewHolder {

        private ExtRoundRectSimpleDraweeView mBorderView;
        private CircleProgressBarView mProgressBarView;
        private ImageView ivDown;

        StickerDataHolder(View itemView) {
            super(itemView);
            mBorderView = itemView.findViewById(R.id.item_border);
            mProgressBarView = itemView.findViewById(R.id.ttf_pbar);
            ivDown = itemView.findViewById(R.id.ivDown);
        }
    }

    class ViewClickListener extends BaseItemClickListener {

        public void setParent(View parent) {
            this.parent = parent;
        }

        private View parent;

        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position) {
                StyleInfo info = getItem(position);
                if (info.isdownloaded) {
                    setCheckItem(position);
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(position, getItem(position));
                    }
                } else {
                    onDown(position, parent);
                }
            }
        }
    }


}
