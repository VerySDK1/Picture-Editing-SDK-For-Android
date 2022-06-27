package com.pesdk.uisdk.ui.card.vm;

import android.app.Application;
import android.graphics.Rect;

import com.pesdk.uisdk.export.ExportHelper;
import com.pesdk.uisdk.ui.card.listener.ExportCallback;
import com.vecore.VirtualImage;
import com.vecore.listener.ExportListener;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * 证件照导出
 */
public class ExportVM extends AndroidViewModel {

    private MutableLiveData<String> mLiveData;

    public ExportVM(@NonNull Application application) {
        super(application);
        mLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<String> getLiveData() {
        return mLiveData;
    }

    private String output;

    public void export(ExportCallback callback, Rect size) {
        ExportHelper helper = new ExportHelper();
        VirtualImage virtualImage = new VirtualImage();
        callback.loadData(virtualImage);
        //证件照无水印
        output = helper.export(getApplication(), virtualImage, new int[]{size.width(), size.height()}, null, new ExportListener() {
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
