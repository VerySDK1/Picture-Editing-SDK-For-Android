package com.pesdk.uisdk.data.vm;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.bean.SortBean;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.pesdk.net.PENetworkApi.Sky;

/**
 * 背景-网络资源
 */
public class CanvasStyleVM extends ViewModel {
    public LiveData<List<ItemBean>> getAppData() {
        return mAppData;
    }

    private MutableLiveData<List<ItemBean>> mAppData;
    @PENetworkApi.ResourceType
    private final String mType;


    /**
     * 天空|背景
     *
     * @param type
     */
    public CanvasStyleVM(@PENetworkApi.ResourceType String type) {
        mAppData = new MutableLiveData<>();
        mType = type;
    }


    public void process(SortBean sortApi) {
        ThreadPoolUtils.execute(() -> {
            if (mAppData.getValue() != null) {
                mAppData.postValue(mAppData.getValue());
            } else {
                DataResult dataResult = PENetworkRepository.getDataList(mType, sortApi.getId());
                if (null != dataResult && dataResult.getData() != null) {
                    List<ItemBean> styles = new ArrayList<>();
                    int len = dataResult.getData().size();
                    boolean isSky = Sky.equals(mType);
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        ItemBean tmp = new ItemBean(dataBean);
                        String path = isSky ? PathUtils.getSkyFile(tmp.getFile()) : PathUtils.getBGFile(tmp.getFile());
                        if (PathUtils.isDownload(path)) {
                            tmp.setLocalPath(path);
                        }
                        styles.add(tmp);
                    }
                    mAppData.postValue(styles);
                } else {
                    mAppData.postValue(null);
                }
            }
        });
    }
}

