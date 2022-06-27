package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.text.TextUtils;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.model.flower.Flower;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.Flower;

/**
 * 花字
 */
public class FlowerVM extends AndroidViewModel {

    private MutableLiveData<List<Flower>> mData;
    private final String type = Flower;

    public FlowerVM(@NonNull Application application) {
        super(application);
        mData = new MutableLiveData<>();
    }


    public MutableLiveData<List<Flower>> getData() {
        return mData;
    }


    public void load() {
        ThreadPoolUtils.executeEx(() -> {
            if (mData.getValue() != null && mData.getValue().size() > 0) {
                mData.postValue(mData.getValue());
            } else {
                DataResult dataResult = PENetworkRepository.getDataList(type, "");
                List<Flower> list = new ArrayList<>();
                if (null != dataResult && null != dataResult.getData()) {
                    int len = dataResult.getData().size();
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        if (!TextUtils.isEmpty(dataBean.getFile())) {
                            Flower tmp = new Flower(dataBean.getFile(), dataBean.getCover());
                            String path = PathUtils.getFlowerChildDir(dataBean.getFile());
                            if (PathUtils.isDownload(path)) {
                                tmp.setLocalPath(path);
                            }
                            list.add(tmp);
                        }
                    }
                }
                mData.postValue(list);
            }
        });
    }
}
