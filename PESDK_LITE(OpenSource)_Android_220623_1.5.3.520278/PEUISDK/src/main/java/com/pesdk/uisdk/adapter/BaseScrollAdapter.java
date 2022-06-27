package com.pesdk.uisdk.adapter;


import androidx.recyclerview.widget.RecyclerView;

import com.pesdk.uisdk.bean.model.ITimeLine;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部滚动的Recycleview
 *
 * @param <E>
 * @param <VH>
 */
public abstract class BaseScrollAdapter<E extends ITimeLine, VH extends RecyclerView.ViewHolder> extends BaseRVAdapter<VH> {
    protected List<E> list;
    protected int mProgress;
    private List<Integer> mPosList = new ArrayList<>();
    protected List<Integer> mBackup = new ArrayList<>();

    @Override
    public void setChecked(int checkIndex) {
        lastCheck = checkIndex;
        notifyDataSetChanged();
    }

    public E getItem(int position) {
        if (list.size() == 0) {
            return null;
        }
        return list.get(position);
    }

    /**
     * 当前时间点的开始的item的position
     *
     * @param progress 单位：ms
     * @return 当前时间点的开始的item的position (0~size()-1 )
     */
    public int setProgress(int progress) {
        mProgress = progress;
        int tmp = 0;
        int count = 0;
        int nNearPosition = 0;
        mBackup.clear();
        int len = mPosList.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                mBackup.add(mPosList.get(i));
            }
            mPosList.clear();
        }
        if (!isSame()) {//减少频繁刷新
            notifyDataSetChanged();
        }
        if (tmp > 0) {
            return count / tmp;
        } else {
            return nNearPosition;
        }
    }

    private boolean isSame() {
        if (mBackup.size() != mPosList.size()) {
            return false;
        }
        boolean isSame = true;
        int len = mBackup.size();
        for (int i = 0; i < len; i++) {
            if (!contains(mPosList, mBackup.get(i))) {
                isSame = false;
                break;
            }
        }
        return isSame;
    }

    private boolean contains(List<Integer> list, int dst) {
        return list.contains(dst);
    }


}
