package com.pesdk.album.uisdk.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.pesdk.album.R
import com.pesdk.album.uisdk.bean.MaterialInfo
import com.pesdk.album.uisdk.bean.Sort
import com.pesdk.album.uisdk.listener.OnMaterialAddListener
import com.pesdk.album.uisdk.ui.adapter.MaterialAdapter
import com.pesdk.album.uisdk.utils.AlbumPathUtils
import com.pesdk.album.uisdk.viewmodel.MaterialViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.download.DownLoadHelper
import com.vesdk.common.download.DownloadManager
import com.vesdk.common.download.DownloadStatue
import com.vesdk.common.helper.bindArgument
import com.vesdk.common.helper.init
import com.vesdk.common.utils.CommonUtils
import kotlinx.android.synthetic.main.album_fragment_material_item.*

/**
 * 素材库
 */
class MaterialItemFragment : BaseFragment(), DownLoadHelper.DownloadListener {

    companion object {

        private const val SORT = "sort"

        @JvmStatic
        fun newInstance(sort: Sort): MaterialItemFragment {
            val fragment = MaterialItemFragment()
            val args = Bundle()
            args.putParcelable(SORT, sort)
            fragment.arguments = args
            return fragment
        }

    }

    /**
     * 回调
     */
    private var mListener: OnMaterialAddListener? = null

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(this).get(MaterialViewModel::class.java) }

    /**
     * 分类
     */
    private val mSort: Sort? by bindArgument(SORT)

    /**
     * 数据
     */
    private lateinit var mMaterialAdapter: MaterialAdapter

    /**
     * 下载列表
     */
    private val mDownList = mutableMapOf<String, MaterialInfo>()

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
                mViewModel.materialList.clear()
                mViewModel.materialList.addAll(elements)
                mMaterialAdapter.notifyDataSetChanged()
            }
        }

        //刷新
        mSort?.let {
            mViewModel.freshMaterialData(it)
        }
    }

    private fun initRecycler() {
        mMaterialAdapter = MaterialAdapter(mViewModel.materialList)
        mMaterialAdapter.setOnItemClickListener { _, _, position ->
            mMaterialAdapter.setLastCheck(position)
            val info = mMaterialAdapter.getItem(position)
            if (AlbumPathUtils.isDownload(info.localPath)) {
                setMaterial(info)
            } else {
                downloadFilter(info)
            }
        }
        rvMaterial.init(mMaterialAdapter, GridLayoutManager(context, 4))
    }

    /**
     * 设置滤镜
     */
    private fun setMaterial(data: MaterialInfo?) {
        mSort?.let { data?.localPath?.let { path -> mListener?.onAddMaterial(path) } }
    }

    /**
     * 设置回调
     */
    fun setListener(listener: OnMaterialAddListener) {
        this.mListener = listener
    }

    /**
     * 布局
     */
    override fun getLayoutId(): Int {
        return R.layout.album_fragment_material_item
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
    private fun downloadFilter(info: MaterialInfo) {
        context?.let {
            //记录下载
            val url = info.url
            val key = CommonUtils.getKey(info.networkData.id, url)
            //下载
            if (DownloadManager.isCanDownload(it, info, key)) {
                info.downStatue = DownloadStatue.DOWN_ING
                mMaterialAdapter.refreshItem(info)
                mDownList[key] = info
                //开始下载文件
                val addDownload = DownloadManager.addDownload(key, url, info.localPath, this)
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
            mMaterialAdapter.refreshItem(key)
            return
        }

        //下载成功
        info.downStatue = DownloadStatue.DOWN_SUCCESS

        //是否当前选中
        mMaterialAdapter.getCurrent()?.let {
            if (CommonUtils.getKey(it.networkData.id, it.networkData.file) == key) {
                setMaterial(it)
            }
        }

        //移除下载记录
        mDownList.remove(key)
        mMaterialAdapter.refreshItem(key)
    }

    override fun downloadFail(key: String, msg: String) {
        //下载失败
        val filterInfo = mDownList[key]
        filterInfo?.let {
            it.failNum++
            it.downStatue = DownloadStatue.DOWN_FAIL
        }
        mDownList.remove(key)
        mMaterialAdapter.refreshItem(key)
    }
}