package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.text.TextUtils;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.helper.SubUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * 字幕->样式列表
 */
public class SubBublleVM extends AndroidViewModel {

    private MutableLiveData<ArrayList<StyleInfo>> mLiveData;


    public SubBublleVM(@NonNull Application application) {
        super(application);
        mLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<StyleInfo>> getLiveData() {
        return mLiveData;
    }


    public void process(String sortId, @PENetworkApi.ResourceType String mType) {
        ThreadPoolUtils.execute(() -> {
            if (null != mLiveData.getValue() && mLiveData.getValue().size() > 0) {
                mLiveData.postValue(mLiveData.getValue());
            } else {
                ArrayList<StyleInfo> list = new ArrayList<>();
                DataResult dataResult = PENetworkRepository.getDataList(mType, sortId);
                if (null != dataResult && dataResult.getData() != null) {
                    int len = dataResult.getData().size();
                    for (int i = 0; i < len; i++) {
                        DataBean dataBean = dataResult.getData().get(i);
                        if (!TextUtils.isEmpty(dataBean.getFile())) {
                            StyleInfo tmp = new StyleInfo(true, dataBean);
                            tmp.category = sortId;
                            if (SubUtils.DEFAULT_STYLE_CODE.equals(tmp.code) && FileUtils.isExist(SubUtils.getInstance().getAssetSmapleLocalPath())) {
                                //默认样式：导出asset标记为已下载
                                SubUtils.getInstance().initStyle(tmp, SubUtils.getInstance().getAssetSmapleLocalPath());
                                SubUtils.getInstance().replaceOAdd(tmp); //替换内置模板信息（信息更详细）
                            } else {  //其他字幕
                                String path = PathUtils.getSubChildDir(tmp.caption);
                                if (PathUtils.isDownload(path)) {
                                    String str = StyleInfo.getConfigPath(path);
                                    if (null != str) {
                                        tmp.isdownloaded = true;
                                        File file = new File(str);
                                        tmp.mlocalpath = file.getParent();
                                        CommonStyleUtils.checkStyle(new File(tmp.mlocalpath), tmp);
                                    }
                                }
                                SubUtils.getInstance().putStyleInfo(tmp);
                            }
                            list.add(tmp);
                        }
                    }
                }
                mLiveData.postValue(list);
            }
        });
    }
}
