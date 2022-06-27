package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.LineProgress;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.widget.CircleProgressBarView;
import com.pesdk.uisdk.widget.ExtListItemStyle;
import com.pesdk.utils.glide.GlideUtils;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.http.MD5;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 字幕样式
 */
public class CaptionStyleAdapter extends BaseRVAdapter<CaptionStyleAdapter.ViewHolder> {

    private Context mContext;
    private List<StyleInfo> mList = new ArrayList<>();
    private String TAG = "CaptionStyleAdapter";
    private LayoutInflater mInflater;
    private RequestManager mRequestManager;
    private int nBgColor;

    /**
     * @param context
     */
    public CaptionStyleAdapter(Context context, RequestManager requestManager) {
        mContext = context;
        mRequestManager = requestManager;
        mInflater = LayoutInflater.from(context);
        nBgColor = ContextCompat.getColor(mContext, R.color.pesdk_flower_item_bg);
    }

    public List<StyleInfo> getList() {
        return mList;
    }

    public int getPosition(int styleId) {
        int index = 0;
        int len = getCount();
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

    public void addAll(List<StyleInfo> list, int index) {
        mList = list;
        lastCheck = index;
        notifyDataSetChanged();
    }


    public int getCount() {
        return mList.size();
    }

    public StyleInfo getItem(int position) {
        if (position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.pesdk_style_item_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new ViewHolder(view);
    }

    class ViewClickListener extends BaseItemClickListener {


        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position) {
                setCheckItem(position);
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateHolder(holder, position);
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) vh.itemView.getTag();
        viewClickListener.setPosition(position);
        onDownLoadListener listener = new onDownLoadListener();
        vh.mState.setOnClickListener(listener);
        listener.setIndex(position);
        updateHolder(vh, position);
    }

    private void updateHolder(ViewHolder vh, int position) {
        vh.mBorderView.setSelected(lastCheck == position);
        StyleInfo info = getItem(position);
        //网络icon
        GlideUtils.setCover(mRequestManager, vh.mSrc, info.icon);
        LineProgress lineProgress = mArray.get(info.pid);
        if (info.isdownloaded) {
            vh.mProgressBarView.setVisibility(View.GONE);
            vh.mState.setVisibility(View.GONE);
        } else {
            if (null != lineProgress) {
                vh.mState.setVisibility(View.GONE);
                vh.mProgressBarView.setVisibility(View.VISIBLE);
                vh.mProgressBarView.setProgress(lineProgress.getProgress());
            } else {
                vh.mState.setVisibility(View.VISIBLE);
                vh.mProgressBarView.setVisibility(View.GONE);
            }

        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void setCheckItem(int index) {
        if (index != lastCheck) {
            int tmp = lastCheck;
            lastCheck = index;
            if (tmp >= 0) {
                notifyItemChanged(tmp, tmp + "");
            }
            notifyItemChanged(index, index + "");
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mState;
        private ImageView mSrc;
        private ExtListItemStyle mBorderView;
        private CircleProgressBarView mProgressBarView;

        public ViewHolder(View itemView) {
            super(itemView);
            mBorderView = Utils.$(itemView, R.id.item_border);
            mState = Utils.$(itemView, R.id.ttf_state);
            mProgressBarView = Utils.$(itemView, R.id.ttf_pbar);
            mSrc = Utils.$(itemView, R.id.sdv_src);
            mBorderView.setBGColor(nBgColor);
            mBorderView.setBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.common_ic_transparent));
        }
    }

    private class onDownLoadListener implements OnClickListener {

        private int mIndex;

        public void setIndex(int index) {
            mIndex = index;
        }

        @Override
        public void onClick(View v) {
            onDown(mIndex);
        }

    }

    private SparseArray<LineProgress> mArray = new SparseArray<>();

    /**
     * 执行下载
     */
    public void onDown(final int p) {
        if (mArray.size() < 2) {
            // 最多同时下载2个
            if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                Utils.autoToastNomal(mContext, R.string.common_check_network);
            } else {
                final StyleInfo info = getItem(p);
                if (null != info && null == mArray.get(info.pid)) {
                    String tmpLocal = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP + "_" + MD5.getMD5(info.caption), "zip");
                    DownLoadUtils utils = new DownLoadUtils(mContext, info.pid, info.caption, tmpLocal);
                    utils.DownFile(new IDownFileListener() {
                        @Override
                        public void onProgress(long mid, int progress) {
                            LineProgress line = mArray.get((int) mid);
                            if (null != line) {
                                line.setProgress(progress);
                                mArray.put((int) mid, line);
                            }
                            notifyItemChanged(p, progress + "");
                        }

                        @Override
                        public void Canceled(long mid) {
                            mArray.remove((int) mid);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void Finished(long mid, String localPath) {
                            onItemDownloaded(info, p, (int) mid, localPath);
                        }
                    });
                    mArray.put(info.pid, new LineProgress(p, 1));
                    notifyItemChanged(p, p + "");
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
    private void onItemDownloaded(final StyleInfo info, final int p, final int mid, String localPath) {
        final File zip = new File(localPath);
        if (FileUtils.isExist(zip)) {
            try {
                // 解压
                String targetPath = PathUtils.getSubChildDir(info.caption);
                final String dirpath = FileUtils.unzip(zip, new File(targetPath));
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
                            mArray.remove(mid);
                            info.isdownloaded = true;
                            zip.delete();
                            notifyDataSetChanged();
                            mList.set(p, info);
                            if (null != mOnItemClickListener) {
                                mOnItemClickListener.onItemClick(p, info);
                            }
                        }
                    });
                } else {
                    mArray.remove(mid);
                    notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
                mArray.remove(mid);
                notifyDataSetChanged();
            }
        }
    }


    /**
     * 清除全部下载
     */
    public void recycle() {
        mOnItemClickListener = null;
        if (null != mArray && mArray.size() > 0) {
            mArray.clear();
            DownLoadUtils.forceCancelAll();
        }
    }

}
