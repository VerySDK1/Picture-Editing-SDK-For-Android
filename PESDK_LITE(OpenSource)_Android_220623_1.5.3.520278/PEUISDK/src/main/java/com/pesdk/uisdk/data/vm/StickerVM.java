package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.text.TextUtils;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.bean.SortBean;
import com.pesdk.bean.SortResult;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.net.TResult;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.helper.StickerUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.Sticker;

/**
 * 贴纸-样式分组列表
 */
public class StickerVM extends AndroidViewModel {

    private MutableLiveData<List<SortBean>> mLiveData;


    private MutableLiveData<TResult> mData;
    private final String type = Sticker;
    private HashMap<String, TResult> mMap;

    public StickerVM(@NonNull Application application) {
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
        ThreadPoolUtils.executeEx(() -> {
            SortResult sortResult = PENetworkRepository.getSortList(type);
            mLiveData.postValue(null != sortResult ? sortResult.getData() : null);
        });

    }

    public void load(SortBean sortApi) {
        ThreadPoolUtils.execute(() -> {
            String sortId = sortApi.getId();
            if (mMap.containsKey(sortId)) {
                mData.postValue(mMap.get(sortId));
            } else {
                ArrayList<StyleInfo> list = new ArrayList<>();
                DataResult dataResult = PENetworkRepository.getDataList(type, sortId);
                if (null != dataResult && dataResult.getData() != null) {
                    int len = dataResult.getData().size();
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        if (!TextUtils.isEmpty(dataBean.getFile())) {
                            StyleInfo tmp = new StyleInfo(false, dataBean);
                            tmp.category = sortId;
                            String path = PathUtils.getStickerChildDir(tmp.caption);
                            if (PathUtils.isDownload(path)) {
                                String str = StyleInfo.getConfigPath(path);
                                if (null != str) {
                                    tmp.isdownloaded = true;
                                    File file = new File(str);
                                    tmp.mlocalpath = file.getParent();
                                    CommonStyleUtils.checkStyle(new File(tmp.mlocalpath), tmp);
                                }
                            }
                            list.add(tmp);
                            StickerUtils.getInstance().putStyleInfo(tmp);
                        }
                    }
                    TResult result = new TResult(sortId, list);
                    mData.postValue(result);
                    mMap.put(sortId, result);
                } else {
                    mData.postValue(new TResult(sortId, list));
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
