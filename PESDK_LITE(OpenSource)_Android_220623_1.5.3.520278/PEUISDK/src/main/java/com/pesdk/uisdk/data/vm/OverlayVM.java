package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.text.TextUtils;

import com.pesdk.bean.DataResult;
import com.pesdk.bean.DataBean;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.net.IBean;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.Overlay;

/**
 * 叠加
 */
public class OverlayVM extends AndroidViewModel {

    private static final String TAG = "OverlayVM";
    private MutableLiveData<List<IBean>> mData;
    private final String type = Overlay;

    public OverlayVM(@NonNull Application application) {
        super(application);
        mData = new MutableLiveData<>();
    }


    public MutableLiveData<List<IBean>> getData() {
        return mData;
    }


    public void load() {
        ThreadPoolUtils.executeEx(() -> {
            DataResult dataResult = PENetworkRepository.getDataList(type, "");
            List<IBean> list = new ArrayList<>();
            if (null != dataResult && dataResult.getData() != null) {
                int len = dataResult.getData().size();
                for (int i = 0; i < len; i++) {
                    DataBean dataBean = dataResult.getData().get(i);
                    String url = dataBean.getFile();
                    if (!TextUtils.isEmpty(url)) {
                        IBean tmp = new IBean(dataBean);
                        tmp.setMId(Utils.getHash(url));
                        String path = PathUtils.getOverlayFile(url);
                        if (PathUtils.isDownload(path)) {
                            tmp.setLocalPath(path);
                        }
                        list.add(tmp);
                    }
                }
            }
            mData.postValue(list);
        });
    }

}
