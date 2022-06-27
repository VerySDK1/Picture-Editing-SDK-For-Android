package com.pesdk.uisdk.data.vm;

import android.app.Application;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.bean.SortBean;
import com.pesdk.bean.SortResult;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.model.BorderStyleInfo;
import com.pesdk.uisdk.bean.net.TResult;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.FRAME;

/**
 * 边框-样式分组列表
 */
public class FrameVM extends AndroidViewModel {

    private MutableLiveData<List<SortBean>> mLiveData;
    private MutableLiveData<TResult> mData;
    private final String type = FRAME;
    private HashMap<String, TResult> mMap;

    public FrameVM(@NonNull Application application) {
        super(application);
        mLiveData = new MutableLiveData<>();
        mData = new MutableLiveData<>();
        mMap = new HashMap<>();
    }

    public MutableLiveData<List<SortBean>> getLiveData() {
        return mLiveData;
    }

    public MutableLiveData<TResult> getData() {
        return mData;
    }


    public void loadSort() {
        ThreadPoolUtils.execute(() -> {
            SortResult sortResult = PENetworkRepository.getSortList(type);
            if (null != sortResult && sortResult.getData() != null) {
                mLiveData.postValue(sortResult.getData());
            }
        });
    }

    public void load(SortBean sortApi) {
        ThreadPoolUtils.execute(() -> {
            String sortId = sortApi.getId();
            if (mMap.containsKey(sortId)) {
                mData.postValue(mMap.get(sortId));
            } else {
                DataResult dataResult = PENetworkRepository.getDataList(type, sortId);
                if (null != dataResult && dataResult.getData() != null) {
                    List<BorderStyleInfo> list = new ArrayList<>();
                    int len = dataResult.getData().size();
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        String path = PathUtils.getFrameFile(dataBean.getFile());
                        if (!PathUtils.isDownload(path)) {
                            path = null;
                        }
                        BorderStyleInfo styleInfo = new BorderStyleInfo(dataBean.getName(), dataBean.getFile(), dataBean.getCover(), path);
                        styleInfo.setSortBean(sortApi);
                        list.add(styleInfo);
                    }
                    TResult result = new TResult(sortApi.getId(), list);
                    mData.postValue(result);
                    mMap.put(sortId, result);
                } else {
                    mData.postValue(null);
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
