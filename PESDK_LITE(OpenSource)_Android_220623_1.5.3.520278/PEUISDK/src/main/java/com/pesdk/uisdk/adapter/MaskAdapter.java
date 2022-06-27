package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.MaskItem;
import com.pesdk.uisdk.util.AppConfiguration;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.manager.MaskManager;
import com.pesdk.uisdk.widget.ExtListItemStyle;
import com.pesdk.utils.glide.GlideUtils;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 蒙版
 */
public class MaskAdapter extends BaseRVAdapter<MaskAdapter.ViewHolder> {

    private ArrayList<MaskItem> mItems = new ArrayList<>();
    private ArrayList<String> mDownList = new ArrayList<>();
    private ArrayList<String> mDownFailList = new ArrayList<>();

    public MaskAdapter(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    private static final int MAX_DOWN = 10;
    private RequestManager mRequestManager;

    public void addAll(List<MaskItem> items, int last) {
        mItems.addAll(items);
        lastCheck = last;
        notifyDataSetChanged();
    }

    public MaskItem getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_mask_sort_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        ViewClickListener viewClickListener = new ViewClickListener();
        viewHolder.llMaskItem.setOnClickListener(viewClickListener);
        viewHolder.llMaskItem.setTag(viewClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //设置数据
        MaskItem item = mItems.get(position);
        if (item.getDrawbleId() != 0) {
            GlideUtils.setCover(mRequestManager, holder.mIcon, item.getDrawbleId());
        } else if (!TextUtils.isEmpty(item.getIcon())) {
            GlideUtils.setCover(mRequestManager, holder.mIcon, item.getIcon());
        }

        //点击
        ViewClickListener listener = (ViewClickListener) holder.llMaskItem.getTag();
        listener.setPosition(position);
        holder.mBorderView.setSelected(lastCheck == position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llMaskItem;
        private ExtListItemStyle mBorderView;
        private ImageView mIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            llMaskItem = itemView.findViewById(R.id.llMaskItem);
            mBorderView = itemView.findViewById(R.id.item_border);
            mIcon = itemView.findViewById(R.id.icon);
        }
    }


    class ViewClickListener extends BaseItemClickListener {

        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position || enableRepeatClick) {
                lastCheck = position;
                MaskItem item = getItem(position);
                if (position == 0) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, item);
                    }
                    notifyDataSetChanged();
                } else {
                    if (FileUtils.isExist(item.getLocalpath())) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(position, item);
                        }
                        notifyDataSetChanged();
                    } else {
                        startDown(view.getContext(), lastCheck);
                    }
                }
            }
        }
    }


    /**
     * 开始下载 poaition下标
     */
    private void startDown(Context mContext, int position) {
        //网络
        if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            Utils.autoToastNomal(mContext, R.string.common_check_network);
        }
        if (mDownList.contains(String.valueOf(position)) || position < 0 || position >= getItemCount()) {
            return;
        }
        if (mDownList.size() >= MAX_DOWN) {
            return;
        }
        //开始下载文件
        MaskItem info = getItem(position);
        if (info == null || FileUtils.isExist(info.getLocalpath())) {
            return;
        }
        String url = info.getUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        mDownList.add(String.valueOf(position));
        notifyDataSetChanged();

        String local = PathUtils.getTempFileNameForSdcard(PathUtils.getMask(), "mask", "zip");
        downFile(mContext, position, url, local);
    }

    /**
     * 结束下载
     */
    private void endDown(int position) {
        mDownList.remove(String.valueOf(position));
        notifyDataSetChanged();
    }

    /**
     * 下载文件
     */
    private void downFile(Context context, int position, String url, String path) {
        DownLoadUtils download = new DownLoadUtils(context, position, url, path);
        download.DownFile(new IDownListener() {

            @Override
            public void onFailed(long mid, int i) {
                //下载失败
                String position = String.valueOf(mid);
                if (!mDownFailList.contains(position)) {
                    mDownFailList.add(position);
                }
                //是选中当前 设置不选择
                if (lastCheck == mid) {
                    lastCheck = -1;
                }
                endDown((int) mid);
                new File(path).delete();
            }

            @Override
            public void onProgress(long mid, int progress) {
                //下载进度
            }

            @Override
            public void Finished(long mid, String localPath) {
                downFinished((int) mid, url, localPath);
            }

            @Override
            public void Canceled(long mid) {
                //下载取消
                endDown((int) mid);
                new File(path).delete();
            }
        });
    }

    /**
     * 下载完成
     */
    private void downFinished(final int position, String url, String localPath) {
        mDownFailList.remove(String.valueOf(position));
        if (position < 0 || position >= getItemCount()) {
            endDown(position);
            return;
        }

        String targetPath = PathUtils.getMaskChildDir(url);
        String dst = null;
        try {
            dst = FileUtils.unzip(null, localPath, targetPath);
            if (!TextUtils.isEmpty(dst)) {//删除临时文件
                FileUtils.deleteAll(localPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtil.i(TAG, "downFinished:" + dst);
        //设置本地路径
        if (!TextUtils.isEmpty(dst)) {
            MaskItem tmp = getItem(position);
            if (MaskManager.getInstance().init(tmp.getName(), dst)) {
                tmp.setLocalpath(dst);
                int id = MaskManager.getInstance().getRegistered(tmp.getName());
                tmp.setMaskId(id);
                mItems.set(position, tmp);
                if (lastCheck == position && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(lastCheck, getItem(lastCheck));
                }
                AppConfiguration.saveMaskList(mItems);
            }
        }
        endDown(position);
    }
}
