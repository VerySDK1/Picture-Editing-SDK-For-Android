package com.pesdk.uisdk.data.vm;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.pesdk.bean.DataBean;
import com.pesdk.bean.DataResult;
import com.pesdk.net.PENetworkApi;
import com.pesdk.net.repository.PENetworkRepository;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.MaskItem;
import com.pesdk.uisdk.fragment.mask.CircularRender;
import com.pesdk.uisdk.fragment.mask.FiveStarRender;
import com.pesdk.uisdk.fragment.mask.LinearRender;
import com.pesdk.uisdk.fragment.mask.LoveRender;
import com.pesdk.uisdk.fragment.mask.MirrorRender;
import com.pesdk.uisdk.fragment.mask.QuadrilateralRender;
import com.pesdk.uisdk.fragment.mask.RectangleRender;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.manager.MaskManager;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.pesdk.net.PENetworkApi.MASK;

/**
 * 蒙版
 */
public class MaskVM extends AndroidViewModel {

    private MutableLiveData<List<MaskItem>> mData;
    @PENetworkApi.ResourceType
    private final String type = MASK;

    public MaskVM(@NonNull Application application) {
        super(application);
        mData = new MutableLiveData<>();
    }


    public MutableLiveData<List<MaskItem>> getData() {
        return mData;
    }


    public void load() {
        ThreadPoolUtils.executeEx(() -> {
            DataResult dataResult = PENetworkRepository.getDataList(type, "");
            List<MaskItem> list = new ArrayList<>();
            if (null != dataResult && null != dataResult.getData()) {
                int len = dataResult.getData().size();
                for (int i = 0; i < len; i++) {
                    DataBean dataBean = dataResult.getData().get(i);
                    if (!TextUtils.isEmpty(dataBean.getFile())) {
                        MaskItem tmp = new MaskItem(dataBean.getName(), dataBean.getFile(), dataBean.getCover());
                        String path = PathUtils.getMaskChildDir(dataBean.getFile());
                        if (PathUtils.isDownload(path)) {
                            int id = MaskManager.getInstance().getRegistered(tmp.getName());
                            if (id > 0) {
                                tmp.setMaskId(id);
                                tmp.setLocalpath(path);
                            }
                        }
                        list.add(tmp);
                    }
                }
                Context context = getApplication();
                for (MaskItem tmp : list) {
                    if (getApplication().getString(R.string.pesdk_mask_line).equals(tmp.getName())) {
                        tmp.setMaskRender(new LinearRender(context));
                    } else if (context.getString(R.string.pesdk_mask_parallel).equals(tmp.getName())) {
                        tmp.setMaskRender(new MirrorRender(context));
                    } else if (context.getString(R.string.pesdk_mask_circle).equals(tmp.getName())) {
                        tmp.setMaskRender(new CircularRender(context));
                    } else if (context.getString(R.string.pesdk_mask_rectangle).equals(tmp.getName())) {
                        tmp.setMaskRender(new RectangleRender(context));
                    } else if (context.getString(R.string.pesdk_mask_star).equals(tmp.getName())) {
                        tmp.setMaskRender(new FiveStarRender(context));
                    } else if (context.getString(R.string.pesdk_mask_love).equals(tmp.getName())) {
                        tmp.setMaskRender(new LoveRender(context));
                    } else if (context.getString(R.string.pesdk_mask_rect).equals(tmp.getName())) {
                        tmp.setMaskRender(new QuadrilateralRender(context));
                    }
                }
            }
            mData.postValue(list);
        });
    }

}
