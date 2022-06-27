package com.pesdk.uisdk.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.flower.Flower;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.widget.ExtListItemStyle;
import com.pesdk.utils.glide.GlideUtils;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 花字列表
 */
public class FlowerAdapter extends BaseRVAdapter<FlowerAdapter.FlowerHolder> {

    private static final int MAX_DOWN = 10;
    private Context mContext;
    private ArrayList<Flower> mFlowerList = new ArrayList<>();
    private ArrayList<String> mDownList = new ArrayList<>();
    private ArrayList<String> mDownFailList = new ArrayList<>();
    private int nBgColor;
    private RequestManager mRequestManager;

    public FlowerAdapter(Context context, RequestManager requestManager) {
        this.mContext = context;
        mRequestManager = requestManager;
        nBgColor = ContextCompat.getColor(mContext, R.color.pesdk_flower_item_bg);
    }

    @Override
    @NonNull
    public FlowerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pesdk_item_flower_text_layout, parent, false);
        FlowerHolder viewHolder = new FlowerHolder(view);
        ViewClickListener viewClickListener = new ViewClickListener();
        viewHolder.mIvIcon.setOnClickListener(viewClickListener);
        viewHolder.mIvIcon.setTag(viewClickListener);
        return viewHolder;
    }

    class ViewClickListener extends BaseItemClickListener {


        @Override
        protected void onSingleClick(View view) {
            if (lastCheck != position || enableRepeatClick) {
                lastCheck = position;
                if (position == 0) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, null);
                    }
                    notifyDataSetChanged();
                } else {
                    Flower item = getItem(position);
                    if (FileUtils.isExist(item.getLocalPath())) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(position, item);
                        }
                        notifyDataSetChanged();
                    } else {
                        startDown(lastCheck);
                    }
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(FlowerHolder holder, int position) {
        //点击
        ViewClickListener listener = (ViewClickListener) holder.mIvIcon.getTag();
        listener.setPosition(position);
        //设置数据
        Flower Flower = mFlowerList.get(position);
        //图标
        if (Flower.getIcon() == null) {
            GlideUtils.setCover(mRequestManager, holder.mIvIcon, Flower.getIconId());
        } else {
            GlideUtils.setCover(mRequestManager, holder.mIvIcon, Flower.getIcon());
        }
        //选中
        holder.mBorderView.setSelected(lastCheck == position);
    }

    @Override
    public int getItemCount() {
        return mFlowerList.size();
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

    /**
     * 设置选中
     */
    public void setCheckItem(String id) {
        for (int i = 0; i < mFlowerList.size(); i++) {
            if (mFlowerList.get(i).getId().equals(id)) {
                lastCheck = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 添加数据
     */
    public void addAll(ArrayList<Flower> flowerList, int check) {
        mFlowerList = flowerList;
        lastCheck = check;
        notifyDataSetChanged();
    }

    /**
     * 取数据
     */
    public Flower getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return mFlowerList.get(position);
        }
        return null;
    }


    /**
     * 开始下载 poaition下标
     */
    private void startDown(int position) {
        //网络
        if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            Utils.autoToastNomal(mContext, R.string.common_check_network);
        }
        if (mDownList.contains(String.valueOf(position)) || position < 0 || position >= mFlowerList.size()) {
            return;
        }
        if (mDownList.size() >= MAX_DOWN) {
            return;
        }
        //开始下载文件
        Flower info = getItem(position);
        if (info == null || FileUtils.isExist(info.getLocalPath())) {
            return;
        }
        String url = info.getUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        mDownList.add(String.valueOf(position));
        notifyDataSetChanged();

        String local = PathUtils.getTempFileNameForSdcard(PathUtils.getFlower(), "flower", "zip");
        downFile(position, url, local);
    }

    /**
     * 结束下载
     */
    private void endDown(int position) {
        mDownList.remove(String.valueOf(position));
        notifyDataSetChanged();
    }

    /**
     * 退出 取消全部下载
     */
    public void closeDown() {
        mDownList.clear();
        DownLoadUtils.forceCancelAll();
    }

    /**
     * 下载文件
     */
    private void downFile(int position, String url, String path) {
        DownLoadUtils download = new DownLoadUtils(mContext, position, url, path);
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
            }
        });
    }

    /**
     * 下载完成
     */
    private void downFinished(final int position, String url, String localPath) {
        mDownFailList.remove(String.valueOf(position));
        if (position < 0 || position >= mFlowerList.size()) {
            endDown(position);
            return;
        }

        String targetPath = PathUtils.getFlowerChildDir(url);
        String dst = null;
        try {
            dst = FileUtils.unzip(mContext, localPath, targetPath);
            if (!TextUtils.isEmpty(dst)) {//删除临时文件
                FileUtils.deleteAll(localPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //设置本地路径
        if (!TextUtils.isEmpty(dst)) {
            Flower tmp = getItem(position);
            tmp.setLocalPath(dst);
            mFlowerList.set(position, tmp);
            if (lastCheck == position && mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(lastCheck, getItem(lastCheck));
            }
        }
        endDown(position);
    }


    class FlowerHolder extends RecyclerView.ViewHolder {

        ImageView mIvIcon;
        ExtListItemStyle mBorderView;

        FlowerHolder(View itemView) {
            super(itemView);
            mIvIcon = Utils.$(itemView, R.id.iv_icon);
            mBorderView = Utils.$(itemView, R.id.item_border);
            mBorderView.setBGColor(nBgColor);
            mBorderView.setBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.common_ic_transparent));
        }
    }

}
