package com.pesdk.demo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.pesdk.api.IVirtualImageInfo
import com.pesdk.api.SdkEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 */
class LocalDraftViewModel(application: Application) : AndroidViewModel(application) {
    var liveData = MutableLiveData<List<IVirtualImageInfo>>()


    /**
     * 当前编辑的草稿
     */
    var currentDraft = MutableLiveData<IVirtualImageInfo>()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            liveData.postValue(SdkEntry.getDraftList(getApplication()))
        }
    }


    /**
     * 设置当前
     */
    fun setCurrent(info: IVirtualImageInfo?) {
        currentDraft.postValue(info)
    }

    /**
     * 删除草稿
     */
    fun deleteDraft(info: IVirtualImageInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            SdkEntry.deleteDraft(getApplication(), info)
            liveData.postValue(SdkEntry.getDraftList(getApplication()))
        }
    }

    /**
     * 更新
     */
//    fun updateDraft(info: Draft) {
////        viewModelScope.launch(Dispatchers.IO) {
////            mDraftSource.updateDraft(info)
////        }
//    }

}