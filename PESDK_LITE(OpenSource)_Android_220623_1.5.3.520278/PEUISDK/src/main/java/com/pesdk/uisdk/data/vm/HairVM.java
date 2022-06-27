package com.pesdk.uisdk.data.vm;

import android.app.Application;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.bean.SortBean;
import com.pesdk.bean.SortResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.HAIR;

/**
 * 头发列表
 */
public class HairVM extends AndroidViewModel {

    @PENetworkApi.ResourceType
    private String type = HAIR;
    private MutableLiveData<List<ItemBean>> mData;
    private MutableLiveData<List<SortBean>> mSortData;
    private HashMap<String, List<ItemBean>> mMap;

    public HairVM(@NonNull Application application) {
        super(application);
        mData = new MutableLiveData<>();
        mSortData = new MutableLiveData<>();
        mMap = new HashMap<>();
    }

    public MutableLiveData<List<ItemBean>> getLiveData() {
        return mData;
    }


    public MutableLiveData<List<SortBean>> getSortData() {
        return mSortData;
    }


    public void loadSort() {
        ThreadPoolUtils.execute(() -> {
            if (mSortData.getValue() != null && mSortData.getValue().size() > 1) {
                mSortData.postValue(mSortData.getValue());
            } else {
                SortResult sortResult = PENetworkRepository.getSortList(type);
                if (null != sortResult) {
                    List<SortBean> list = sortResult.getData();
                    if (null == list) {
                        list = new ArrayList<>();
                    }
                    list.add(0, new SortBean("0", Integer.toString(R.drawable.pesdk_none), Integer.toString(R.drawable.pesdk_none)));
                    mSortData.postValue(list);
                } else {
                    List<SortBean> list = new ArrayList<>();
                    list.add(new SortBean("0", Integer.toString(R.drawable.pesdk_none), Integer.toString(R.drawable.pesdk_none)));
                    mSortData.postValue(list);
                }
            }
        });

    }

    public void loadData(SortBean sortApi) {
        ThreadPoolUtils.execute(() -> {
            String sortId = sortApi.getId();
            if (mMap.containsKey(sortId)) {
                mData.postValue(mMap.get(sortId));
            } else {
                DataResult dataResult = PENetworkRepository.getDataList(type, sortId);
                ArrayList<ItemBean> beans = new ArrayList<>();
                if (null != dataResult && dataResult.getData() != null) {
                    int len = dataResult.getData().size();
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        ItemBean itemBean = new ItemBean(dataBean);
                        itemBean.setSortId(sortId);
                        itemBean.setName(sortApi.getName() + (i + 1));
                        String path = PathUtils.getHairChildDir(itemBean.getFile());
                        if (PathUtils.isDownload(path)) {
                            itemBean.setLocalPath(new File(path, "hair").getAbsolutePath());
                        }
                        beans.add(itemBean);
                    }
                }
                mData.postValue(beans);
                if (beans.size() > 0) {
                    mMap.put(sortId, beans);
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
