package com.vesdk.camera.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.vecore.models.VisualFilterConfig
import com.vesdk.camera.bean.FilterInfo
import com.vesdk.camera.bean.Sort
import com.vesdk.camera.viewmodel.source.FilterSource
import java.lang.invoke.MethodHandles

class FilterViewModel : ViewModel() {

    private val filterSource = FilterSource()


    /**
     * 分类数据
     */
    val sortList = ArrayList<Sort>()

    /**
     * 数据
     */
    val filterList = ArrayList<FilterInfo>()

    /**
     * 滤镜选中配置
     */
    var config = VisualFilterConfig(0)


    /**
     * 最低版本
     */
    private val _minVer = MutableLiveData("0")

    /**
     * 分类
     */
    private val _sortLiveData = MutableLiveData<Sort>()


    /**
     * 配置
     */
    val configLiveData = MutableLiveData("0")

    /**
     * 分类数据
     */
    val sortLiveData = Transformations.switchMap(_minVer) {
        filterSource.getFilterSort(it)
    }

    /**
     * 数据
     */
    val dataLiveData = Transformations.switchMap(_sortLiveData) {
        filterSource.getFilterData(it.id)
    }


    /**
     * 设置默认值
     */
    fun setDefaultValue(value: Float) {
        config.defaultValue = value
        configLiveData.postValue("0")
    }

    /**
     * 普通lookup
     */
    fun setFilterPath(path: String) {
        config = VisualFilterConfig(path)
        configLiveData.postValue("0")
    }

    /**
     * 马赛克
     */
    fun setFilter(visualFilterConfig: VisualFilterConfig) {
        config = visualFilterConfig
        configLiveData.postValue("0")
    }

    /**
     * 存在滤镜
     */
    fun isExitFilter(): Boolean {
        return config.filterFilePath != null && config.filterFilePath != ""
    }

    /**
     * 刷新分类
     */
    fun freshFilterSort(minVer: String = "0") {
        this._minVer.value = minVer
    }

    /**
     * 刷新数据
     */
    fun freshFilterData(sort: Sort) {
        this._sortLiveData.value = sort
    }

}