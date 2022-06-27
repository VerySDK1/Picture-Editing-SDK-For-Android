package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.text.TextUtils;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * 字体列表
 */
public class FontVM extends AndroidViewModel {


    private MutableLiveData<List<ItemBean>> mData;

    public FontVM(@NonNull Application application) {
        super(application);
        mData = new MutableLiveData<>();
    }


    public MutableLiveData<List<ItemBean>> getLiveData() {
        return mData;
    }


    public void load() {
        ThreadPoolUtils.execute(() -> {
            if (null != mData.getValue() && mData.getValue().size() > 0) {
                mData.postValue(mData.getValue());
            } else {
                DataResult dataResult = PENetworkRepository.getDataList(PENetworkApi.Font, "");
                ArrayList<ItemBean> ttfInfos = new ArrayList<>();
                if (null != dataResult && dataResult.getData() != null) {
                    int len = dataResult.getData().size();
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        if (!TextUtils.isEmpty(dataBean.getFile())) {
                            ItemBean ttfInfo = new ItemBean(dataBean);
                            String path = PathUtils.getTTFFile(dataBean.getFile());
                            if (PathUtils.isDownload(path)) {
                                ttfInfo.setLocalPath(path);
                            }
                            ttfInfos.add(ttfInfo);
                        }
                    }
                }
                mData.postValue(ttfInfos);
            }
        });
    }
}
