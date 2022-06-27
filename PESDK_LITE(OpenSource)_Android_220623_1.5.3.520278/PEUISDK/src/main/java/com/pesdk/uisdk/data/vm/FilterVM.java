package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.text.TextUtils;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.bean.SortBean;
import com.pesdk.bean.SortResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.net.WebFilterInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.TYPE_FILTER;

/**
 * 滤镜
 */
public class FilterVM extends AndroidViewModel {

    private MutableLiveData<List<SortBean>> mSortData;

    private MutableLiveData<ArrayList<WebFilterInfo>> mData;

    @PENetworkApi.ResourceType
    private String type = TYPE_FILTER;

    private HashMap<String, ArrayList<WebFilterInfo>> mMap;

    public FilterVM(@NonNull Application application) {
        super(application);
        mMap = new HashMap<>();
        mSortData = new MutableLiveData<>();
        mData = new MutableLiveData<>();
    }

    public MutableLiveData<List<SortBean>> getSortData() {
        return mSortData;
    }

    public MutableLiveData<ArrayList<WebFilterInfo>> getData() {
        return mData;
    }

    public void loadSort() {
        ThreadPoolUtils.execute(() -> {
            SortResult sortResult = PENetworkRepository.getSortList(type);
            List<SortBean> list = sortResult.getData();
            if (null == list) {
                list = new ArrayList<>();
            }
            list.add(0, new SortBean("0", Integer.toString(R.drawable.pesdk_none), Integer.toString(R.drawable.pesdk_none)));
            mSortData.postValue(list);
        });

    }

    public void loadData(SortBean sortApi) {
        ThreadPoolUtils.execute(() -> {
            String sortId = sortApi.getId();
            if (mMap.containsKey(sortId)) {
                mData.postValue(mMap.get(sortId));
            } else {
                DataResult dataResult = PENetworkRepository.getDataList(type, sortId);
                ArrayList<WebFilterInfo> list = new ArrayList<>();
                if (null != dataResult && dataResult.getData() != null) {
                    int len = dataResult.getData().size();
                    boolean isCustom = "精选".equals(sortApi.getName());
                    for (int i = 0, j = 1; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        String name = isCustom ? dataBean.getName() : sortApi.getName() + j;

                        String url = dataBean.getFile();
                        if (!TextUtils.isEmpty(url)) {
                            String local = null;
                            if (url.contains("zip")) { //黑白马赛克
                                local = PathUtils.getFilterDir(url, name);
                                if (!PathUtils.isDownload(local)) {
                                    local = null;
                                }
                            } else {
                                local = PathUtils.getFilterFile(url);
                                if (!PathUtils.isDownload(local)) {
                                    local = null;
                                }
                            }
                            WebFilterInfo filterInfo = new WebFilterInfo(sortId, dataBean.getId(), url, dataBean.getCover(), name, local, dataBean.getUpdatetime());
                            filterInfo.setMId(Utils.getHash(url));
                            list.add(filterInfo);
                            j++;
                        }
                    }
                }
                mData.postValue(list);
                if (list.size() > 0) {
                    mMap.put(sortId, list);
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mMap.clear();
    }
}
