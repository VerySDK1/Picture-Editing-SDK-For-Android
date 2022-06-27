package com.pesdk.album.uisdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.pesdk.album.uisdk.bean.MaterialInfo
import com.pesdk.album.uisdk.bean.Sort
import com.pesdk.album.uisdk.viewmodel.source.MaterialSource

class MaterialViewModel : ViewModel() {

    private val materialSource = MaterialSource()

    /**
     * 分类数据
     */
    val sortList = ArrayList<Sort>()

    /**
     * 数据
     */
    val materialList = ArrayList<MaterialInfo>()

    /**
     * 最低版本
     */
    private val _minVer = MutableLiveData("0")

    /**
     * 分类
     */
    private val _sortLiveData = MutableLiveData<Sort>()


    /**
     * 分类数据
     */
    val sortLiveData = Transformations.switchMap(_minVer) {
        materialSource.getMaterialSort(it)
    }

    /**
     * 数据
     */
    val dataLiveData = Transformations.switchMap(_sortLiveData) {
        materialSource.getMaterialData(it.id)
    }


    /**
     * 刷新分类
     */
    fun freshMaterialSort(minVer: String = "0") {
        this._minVer.value = minVer
    }

    /**
     * 刷新数据
     */
    fun freshMaterialData(sort: Sort) {
        this._sortLiveData.value = sort
    }
}