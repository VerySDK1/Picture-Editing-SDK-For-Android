package com.pesdk.album.uisdk.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.pesdk.album.R
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.api.bean.MediaInfo
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.uisdk.bean.Sort
import com.pesdk.album.uisdk.listener.OnAlbumListener
import com.pesdk.album.uisdk.listener.OnMaterialAddListener
import com.pesdk.album.uisdk.ui.adapter.MaterialPagerAdapter
import com.pesdk.album.uisdk.ui.adapter.MaterialTitleAdapter
import com.pesdk.album.uisdk.viewmodel.AlbumViewModel
import com.pesdk.album.uisdk.viewmodel.MaterialViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.helper.init
import kotlinx.android.synthetic.main.album_fragment_material.*

/**
 * 素材库
 */
class MaterialFragment : BaseFragment(), OnMaterialAddListener {

    companion object {

        @JvmStatic
        fun newInstance() = MaterialFragment()

    }

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(requireActivity()).get(MaterialViewModel::class.java) }

    /**
     * viewModel
     */
    private val mAlbumViewModel by lazy { ViewModelProvider(requireActivity()).get(AlbumViewModel::class.java) }

    /**
     * 标题
     */
    private lateinit var mTitleAdapter: MaterialTitleAdapter

    /**
     * 数据
     */
    private var mPagerAdapter: MaterialPagerAdapter? = null

    /**
     * 回调
     */
    private var mListener: OnAlbumListener? = null

    /**
     * 初始化
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun init() {
        //控件
        initView()
        //分类
        initSort()

        //viewModel
        mViewModel.sortLiveData.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { elements ->
                //获取
                mViewModel.sortList.clear()
                mViewModel.sortList.addAll(elements)
                mTitleAdapter.notifyDataSetChanged()
                //pager
                initPager(elements)
                customLoading.visibility = View.GONE
            } ?: kotlin.run {
                if (TextUtils.isEmpty(AlbumSdkInit.getAlbumConfig().baseUrl)) {
                    customLoading.loadError("Url is null")
                } else {
                    customLoading.loadError(result.exceptionOrNull().toString())
                }
            }
        }

        //刷新
        mViewModel.freshMaterialSort()
    }

    /**
     * 控件
     */
    private fun initView() {
        context?.let {
            customLoading.setBackground(ContextCompat.getColor(it, R.color.album_main_bg))
        }
        customLoading.setHideCancel(true)
        customLoading.setListener(object :
            com.pesdk.widget.loading.CustomLoadingView.OnCustomLoadingListener {

            override fun reloadLoading(): Boolean {
                //刷新
                mViewModel.freshMaterialSort()
                return true
            }

            override fun onCancel() {

            }

        })
    }

    /**
     * 初始化列表
     */
    private fun initSort() {
        mTitleAdapter = MaterialTitleAdapter(mViewModel.sortList)
        rvSort.init(mTitleAdapter, requireContext(), LinearLayoutManager.HORIZONTAL)
        mTitleAdapter.setOnItemClickListener { _, _, position ->
            vpMaterial.setCurrentItem(position, true)
            mTitleAdapter.setCheck(position)
        }
    }

    /**
     * 分类
     */
    private fun initPager(sortList: List<Sort>) {
        if (mPagerAdapter != null) {
            val fm: FragmentManager = childFragmentManager
            val ft: FragmentTransaction = fm.beginTransaction()
            val bundle = Bundle()
            var index: Int = vpMaterial.adapter?.count ?: 0
            val key = "index"
            while (index >= 0) {
                bundle.putInt(key, index)
                fm.getFragment(bundle, key)?.let { ft.remove(it) }
                index--
            }
            ft.commit()
        }
        mPagerAdapter = MaterialPagerAdapter(childFragmentManager, sortList, this)
        vpMaterial.adapter = mPagerAdapter
        vpMaterial.offscreenPageLimit = sortList.size
        vpMaterial.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                mTitleAdapter.setCheck(position)
                rvSort.scrollToPosition(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }


    /**
     * 添加回调
     */
    fun setListener(listener: OnAlbumListener) {
        mListener = listener
    }

    /**
     * 添加素材
     */
    override fun onAddMaterial(path: String) {
        mAlbumViewModel.addSelectMedia(MediaInfo(path, 0, MediaType.TYPE_VIDEO))
    }

    /**
     * 布局
     */
    override fun getLayoutId(): Int {
        return R.layout.album_fragment_material
    }

    /**
     * 返回
     */
    override fun onBackPressed(): Int {
        return -1
    }


}