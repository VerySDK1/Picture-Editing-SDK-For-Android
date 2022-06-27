package com.pesdk.uisdk.fragment.filter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.SortAdapter;
import com.pesdk.uisdk.bean.net.WebFilterInfo;
import com.pesdk.uisdk.data.vm.FilterVM;
import com.pesdk.uisdk.layoutmanager.WrapContentLinearLayoutManager;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 网络滤镜
 */
public class FilterFragmentLookup extends FilterFragmentLookupBase {


    public static FilterFragmentLookup newInstance() {
        return new FilterFragmentLookup();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.pesdk_fragment_lookup_base_layout;
    }

    private FilterVM mVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(FilterVM.class);
        mVM.getSortData().observe(getViewLifecycleOwner(), this::onSortResult);
        mVM.getData().observe(getViewLifecycleOwner(), this::onDataResult);
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_filter);
        LinearLayoutManager manager = new WrapContentLinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSortAdapter = new SortAdapter(getContext());
        mSortAdapter.setOnItemClickListener((OnItemClickListener<SortBean>) (position, item) -> {
            if (position == 0) {
                lastItemId = BaseRVAdapter.UN_CHECK;
                mAdapter.onItemChecked(BaseRVAdapter.UN_CHECK);
                switchFliter(BaseRVAdapter.UN_CHECK);
                onBarEnable(false);
            } else {
                mVM.loadData(item);
            }
        });
        mVM.loadSort();
        rvSort.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSort.setAdapter(mSortAdapter);
        mAdapter.addAll(new ArrayList<>(), BaseRVAdapter.UN_CHECK);
    }

    /**
     * 分组
     */
    private void onSortResult(List<SortBean> sortApis) {
        if (null != sortApis) {
            int index = BaseRVAdapter.UN_CHECK;
            if (null != mFilterInfo) {
                index = IndexHelper.getSortIndex(sortApis, mFilterInfo.getSortId());
                if (sortApis.size() > 1) {
                    mVM.loadData(sortApis.get(Math.max(1, index)));
                }
            } else {
                if (sortApis.size() > 1) {
                    mVM.loadData(sortApis.get(1));
                }
            }
            mSortAdapter.addAll(sortApis, index);
        }
    }

    /**
     * 数据列表
     */
    private void onDataResult(ArrayList<WebFilterInfo> list) {
        SysAlertDialog.cancelLoadingDialog();
        if (null != list) {
            lastItemId = BaseRVAdapter.UN_CHECK;
            int index = BaseRVAdapter.UN_CHECK;
            if (null != mFilterInfo) {
                index = IndexHelper.getFilterIndex(list, mFilterInfo.getMaterialId());
            }
            //更新数据
            mAdapter.addAll(list, index);
            if (index >= 0) {
                showFilterValue();
            }
            onBarEnable(index >= 0);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap.size() > 0) {
            DownLoadUtils.forceCancelAll();
        }
        mRoot = null;

    }

    private int lastItemId = BaseRVAdapter.UN_CHECK;

    @Override
    public void onSelectedImp(int nItemId) {
        if (nItemId >= 0) {
            WebFilterInfo info = mAdapter.getItem(nItemId);
            mSortAdapter.setCurrent(info.getGroupId());
            if (lastItemId != nItemId) {
                if (FileUtils.isExist(info.getLocalPath())) {//已下载的lookup滤镜,直接使用
                    mAdapter.onItemChecked(nItemId);
                    switchFliter(nItemId);
                    lastItemId = nItemId;
                } else {  //此滤镜未注册，下载该filter
                    if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                        mAdapter.onItemChecked(lastItemId);
                    } else {
                        // 下载
                        lastItemId = 0;
                        down(nItemId, info);
                    }
                }
            }
        } else {
            lastItemId = nItemId;
            switchFliter(nItemId);
        }
    }


    /**
     * 下载滤镜
     */
    private void down(int itemId, final WebFilterInfo info) {
        if (!mMap.containsKey(info.getUrl())) {
            /**
             * 支持指定下载文件的存放位置
             */
            String path;
            if (info.getFile().contains("zip")) {
                path = PathUtils.getFilterZip(info.getUrl());
            } else {
                path = PathUtils.getFilterFile(info.getUrl());
            }
            DownLoadUtils download = new DownLoadUtils(getContext(), itemId, info.getUrl(), path);
            LogUtil.i(TAG, "down: " + info);
            download.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    if (isRunning) {
                        mMap.put(info.getUrl(), progress);
                        if (null != mAdapter) {
                            mAdapter.setdownProgress((int) mid);
                        }
                    }
                }

                @Override
                public void Finished(long mid, String localPath) {
                    if (null != mMap) {
                        mMap.remove(info.getUrl());
                    }
                    if (isRunning) {
                        LogUtil.i(TAG, "Finished: " + localPath);
                        if (localPath.endsWith("zip")) {
                            String target = PathUtils.getFilterDir(info.getUrl(), info.getName());
                            try {
                                String tmp = FileUtils.unzip(localPath, target);
                                info.setLocalPath(tmp);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            FileUtils.deleteAll(localPath);
                        } else {
                            info.setLocalPath(localPath);
                        }
                        Log.e(TAG, "Finished: " + info);
                        if (null != mAdapter) {
                            int id = (int) mid;
                            mAdapter.setdownEnd(id);
                            onSelectedImp(id);
                        }

                    }
                }

                @Override
                public void Canceled(long mid) {
                    if (null != mMap) {
                        mMap.remove(info.getUrl());
                    }
                    Log.i(TAG, "Canceled: " + mid);
                    if (isRunning) {
                        if (null != mAdapter) {
                            mAdapter.setdownFailed((int) mid);
                        }
                    }
                }
            });

            if (isRunning) {
                mMap.put(info.getUrl(), 1);
                if (null != mAdapter) {
                    mAdapter.setdownStart(itemId);
                }
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
}
