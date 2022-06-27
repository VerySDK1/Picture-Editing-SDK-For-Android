package com.pesdk.album.uisdk.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.View.GONE
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.pesdk.album.R
import com.pesdk.album.uisdk.bean.NetworkData
import com.pesdk.album.uisdk.bean.TtfInfo
import com.pesdk.album.uisdk.listener.OnTtfListener
import com.pesdk.album.uisdk.ui.adapter.TtfAdapter
import com.pesdk.album.uisdk.utils.AlbumPathUtils
import com.pesdk.album.uisdk.viewmodel.TtfViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.download.DownLoadHelper
import com.vesdk.common.download.DownloadManager
import com.vesdk.common.download.DownloadStatue
import com.vesdk.common.helper.init
import com.vesdk.common.utils.CommonUtils
import kotlinx.android.synthetic.main.album_fragment_ttf.*

/**
 * 字体
 */
class TtfFragment : BaseFragment(), DownLoadHelper.DownloadListener {

    companion object {

        @JvmStatic
        fun newInstance() = TtfFragment()
    }

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(this).get(TtfViewModel::class.java) }

    /**
     * adapter
     */
    private lateinit var mTtfAdapter: TtfAdapter

    /**
     * listener
     */
    private var mListener: OnTtfListener? = null

    /**
     * 下载列表
     */
    private val mDownList = mutableMapOf<String, TtfInfo>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnTtfListener
    }

    /**
     * 初始化
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun init() {
        //数据变化
        mViewModel.ttfLiveData.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { elements ->
                elements.add(0, TtfInfo(NetworkData("默认字体")))
                mViewModel.setTtfList(elements)
                mTtfAdapter.notifyDataSetChanged()
                loading.visibility = GONE
            } ?: kotlin.run {
                loading.loadError(result.exceptionOrNull().toString())
            }
        }

        //控件
        initView()

        //列表
        initRecycler()

        //获取数据
        mViewModel.fresh()
    }

    /**
     * 控件
     */
    private fun initView() {
        context?.let {
            loading.setBackground(ContextCompat.getColor(it, R.color.album_main_bg))
        }
        loading.setHideCancel(true)
        loading.setListener(object :
            com.pesdk.widget.loading.CustomLoadingView.OnCustomLoadingListener {

            override fun reloadLoading(): Boolean {
                //获取数据
                mViewModel.fresh()
                return true
            }

            override fun onCancel() {

            }

        })
    }

    /**
     * 初始化列表
     */
    private fun initRecycler() {
        mTtfAdapter = TtfAdapter(mViewModel.getTtfList())
        rvTtf.init(mTtfAdapter, GridLayoutManager(context, 4))
        mTtfAdapter.setOnItemClickListener { _, _, position ->
            mTtfAdapter.setLastCheck(position)
            val info = mTtfAdapter.getItem(position)
            //默认字体
            if (info.networkData.id == "0" || AlbumPathUtils.isDownload(info.localPath)) {
                setTtf(info)
            } else {
                downloadFilter(info)
            }
        }
    }

    /**
     * 设置滤镜
     */
    private fun setTtf(data: TtfInfo) {
        data.localPath.let { path ->
            mListener?.onAddTtf(path)
        }
    }

    /**
     * 下载
     */
    private fun downloadFilter(info: TtfInfo) {
        context?.let {
            //记录下载
            val url = info.url
            val key = CommonUtils.getKey(info.networkData.id, url)
            //下载
            if (DownloadManager.isCanDownload(it, info, key)) {
                info.downStatue = DownloadStatue.DOWN_ING
                mTtfAdapter.refreshItem(info)
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
            mTtfAdapter.refreshItem(key)
            return
        }

        //下载成功
        info.downStatue = DownloadStatue.DOWN_SUCCESS

        //是否当前选中
        mTtfAdapter.getCurrent()?.let {
            if (CommonUtils.getKey(it.networkData.id, it.networkData.file) == key) {
                setTtf(it)
            }
        }

        //移除下载记录
        mDownList.remove(key)
        mTtfAdapter.refreshItem(key)
    }

    override fun downloadFail(key: String, msg: String) {
        //下载失败
        val filterInfo = mDownList[key]
        filterInfo?.let {
            it.failNum++
            it.downStatue = DownloadStatue.DOWN_FAIL
        }
        mDownList.remove(key)
        mTtfAdapter.refreshItem(key)
    }


    override fun getLayoutId(): Int {
        return R.layout.album_fragment_ttf
    }
}