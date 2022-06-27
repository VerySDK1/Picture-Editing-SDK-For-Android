package com.pesdk.album.uisdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.pesdk.album.uisdk.bean.TtfInfo
import com.pesdk.album.uisdk.viewmodel.source.TtfSource

class TtfViewModel : ViewModel() {

    private val ttfSource = TtfSource()

    private val _ttf = MutableLiveData<String>()

    val ttfLiveData = Transformations.switchMap(_ttf) {
        ttfSource.getTtfData()
    }

    private val ttfList = mutableListOf<TtfInfo>()

    fun setTtfList(list: List<TtfInfo>) {
        ttfList.clear()
        ttfList.addAll(list)
    }

    fun getTtfList(): MutableList<TtfInfo> {
        return ttfList
    }

    fun fresh() {
        _ttf.postValue("")
    }


}