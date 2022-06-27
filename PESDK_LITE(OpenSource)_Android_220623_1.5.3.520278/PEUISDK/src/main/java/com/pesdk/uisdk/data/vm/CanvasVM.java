package com.pesdk.uisdk.data.vm;

import android.app.Application;

import com.pesdk.bean.SortBean;
import com.pesdk.bean.SortResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.widget.AppConfig;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 *
 */
public class CanvasVM extends AndroidViewModel {
    public MutableLiveData<List<SortBean>> getSortData() {
        return mSortData;
    }

    private MutableLiveData<List<SortBean>> mSortData;

    public CanvasVM(@NonNull Application application) {
        super(application);
        mSortData = new MutableLiveData<>();
    }

    public void process() {
        ThreadPoolUtils.execute(() -> {
            List<SortBean> list = new ArrayList<>();
            list.add(new SortBean("0", getApplication().getString(R.string.pesdk_background_color)).setDataList(new ArrayList(Arrays.asList(AppConfig.colors))));
            SortResult sortResult = PENetworkRepository.getSortList(PENetworkApi.Bground);
            if (null != sortResult && sortResult.getData() != null && sortResult.getData().size() > 0) {
                list.addAll(sortResult.getData());
            }
            mSortData.postValue(list);
        });
    }

}
