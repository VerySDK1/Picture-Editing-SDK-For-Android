package com.vesdk.camera.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vesdk.camera.R
import com.vesdk.camera.bean.FilterInfo
import com.vesdk.camera.bean.Sort
import com.vesdk.camera.listener.OnFilterItemListener
import com.vesdk.camera.ui.adapter.FilterAdapter
import com.vesdk.camera.utils.CameraPathUtils
import com.vesdk.camera.viewmodel.FilterViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.download.DownLoadHelper
import com.vesdk.common.download.DownloadManager
import com.vesdk.common.download.DownloadStatue
import com.vesdk.common.helper.bindArgument
import com.vesdk.common.helper.init
import com.vesdk.common.utils.CommonUtils
import kotlinx.android.synthetic.main.camera_fragment_filter_item.*

/**
 * 素材库
 */
class FilterItemFragment : BaseFragment(), DownLoadHelper.DownloadListener {

    companion object {

        private const val PARAM_SORT = "sort"

        @JvmStatic
        fun newInstance(sort: Sort): FilterItemFragment {
            val fragment = FilterItemFragment()
            val args = Bundle()
            args.putParcelable(PARAM_SORT, sort)
            fragment.arguments = args
            return fragment
        }


    }

    /**
     * 回调
     */
    private var mItemListener: OnFilterItemListener? = null

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(this).get(FilterViewModel::class.java) }

    /**
     * 分类
     */
    private val mSort: Sort? by bindArgument(PARAM_SORT)

    /**
     * 数据
     */
    private lateinit var mFilterAdapter: FilterAdapter

    /**
     * 下载列表
     */
    private val mDownList = mutableMapOf<String, FilterInfo>()

    @SuppressLint("NotifyDataSetChanged")
    override fun init() {
        if (mSort == null) {
            return
        }

        //分类
        initRecycler()

        //viewModel
        mViewModel.dataLiveData.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { elements ->
                //获取
                mViewModel.filterList.clear()
                mViewModel.filterList.addAll(elements)
                mFilterAdapter.notifyDataSetChanged()
            }
        }

        //刷新
        mSort?.let {
            mViewModel.freshFilterData(it)
        }
    }

    private fun initRecycler() {
        mFilterAdapter = FilterAdapter(mViewModel.filterList)
        rvMaterial.init(mFilterAdapter, LinearLayoutManager(context, RecyclerView.HORIZONTAL, false))
        mFilterAdapter.setOnItemClickListener { _, _, position ->
            mFilterAdapter.setLastCheck(position)
            val filter = mFilterAdapter.getItem(position)
            if (CameraPathUtils.isDownload(filter.localPath)) {
                setFilter(filter, position)
            } else {
                downloadFilter(filter)
            }
        }
    }

    /**
     * 设置滤镜
     */
    private fun setFilter(data: FilterInfo, position: Int) {
        mSort?.let { mItemListener?.onFilter(it, data, position) }
    }

    /**
     * 设置回调
     */
    fun setListener(itemListener: OnFilterItemListener) {
        this.mItemListener = itemListener
    }

    /**
     * 设置选中
     */
    fun setChecked(sort: Sort, position: Int) {
        mFilterAdapter.setLastCheck(if (sort.id == mSort?.id) position else -1)
    }

    /**
     * 布局
     */
    override fun getLayoutId(): Int {
        return R.layout.camera_fragment_filter_item
    }

    /**
     * 返回
     */
    override fun onBackPressed(): Int {
        return -1
    }


    /**
     * 下载
     */
    private fun downloadFilter(filterInfo: FilterInfo) {
        context?.let {
            //记录下载
            val url = filterInfo.url
            val key = CommonUtils.getKey(filterInfo.networkData.id, url)
            //下载
            if (DownloadManager.isCanDownload(it, filterInfo, key)) {
                filterInfo.downStatue = DownloadStatue.DOWN_ING
                mFilterAdapter.refreshItem(filterInfo)
                mDownList[key] = filterInfo

                /**
                 * 支持指定下载文件的存放位置
                 */
                //开始下载文件
                val addDownload = DownloadManager.addDownload(key, url, filterInfo.localPath, this)
                addDownload.start()
            }
        }
    }

    override fun downloadProgress(key: String, progress: Float) {
        val info = mDownList[key] ?: return
        info.downloadProgress = progress
    }

    override fun downloadCompleted(key: String, filePath: String) {

        val info = mDownList[key] ?: kotlin.run {
            //移除下载记录
            mDownList.remove(key)
            mFilterAdapter.refreshItem(key)
            return
        }

        //下载成功
        info.downStatue = DownloadStatue.DOWN_SUCCESS

        //是否当前选中
        mFilterAdapter.getCurrent()?.let {
            if (CommonUtils.getKey(it.networkData.id, it.networkData.file) == key) {
                setFilter(it, mFilterAdapter.getLastCheck())
            }
        }

        //移除下载记录
        mDownList.remove(key)
        mFilterAdapter.refreshItem(key)
    }

    override fun downloadFail(key: String, msg: String) {
        //下载失败
        val filterInfo = mDownList[key]
        filterInfo?.let {
            it.failNum++
            it.downStatue = DownloadStatue.DOWN_FAIL
        }
        mDownList.remove(key)
        mFilterAdapter.refreshItem(key)
    }

}