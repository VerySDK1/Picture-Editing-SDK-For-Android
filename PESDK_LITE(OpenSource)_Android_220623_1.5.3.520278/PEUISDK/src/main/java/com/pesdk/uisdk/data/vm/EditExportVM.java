package com.pesdk.uisdk.data.vm;

import android.app.Application;

import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.export.ExportHelper;
import com.vecore.VirtualImage;
import com.vecore.listener.ExportListener;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * 导出图片文件
 */
public class EditExportVM extends AndroidViewModel {

    private MutableLiveData<String> mLiveData;

    public EditExportVM(@NonNull Application application) {
        super(application);
        mLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<String> getLiveData() {
        return mLiveData;
    }

    private String output;

    public void export(VirtualIImageInfo virtualImageInfo, EditDataHandler editDataHandler, boolean withWatermark) {
        ExportHelper helper = new ExportHelper();
        output = helper.export(getApplication(), virtualImageInfo, editDataHandler, withWatermark, new ExportListener() {
            @Override
            public void onExportStart() {

            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int result, int extra, String info) {
                if (result >= VirtualImage.RESULT_SUCCESS) {
                    mLiveData.postValue(output);
                } else {
                    mLiveData.postValue(null);
                }
            }
        });
    }

}
