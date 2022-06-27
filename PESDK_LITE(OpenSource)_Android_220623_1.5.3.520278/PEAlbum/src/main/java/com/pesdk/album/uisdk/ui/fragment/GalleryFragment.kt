package com.pesdk.album.uisdk.ui.fragment

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.pesdk.album.R
import com.pesdk.album.api.AlbumConfig
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.api.bean.MediaInfo
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.uisdk.listener.OnAlbumListener
import com.pesdk.album.uisdk.listener.OnGalleryListener
import com.pesdk.album.uisdk.ui.adapter.AlbumPagerAdapter
import com.pesdk.album.uisdk.ui.contract.TextBoardContracts
import com.pesdk.album.uisdk.viewmodel.AlbumViewModel
import com.vesdk.common.base.BaseFragment
import kotlinx.android.synthetic.main.album_fragment_album.*

/**
 * 相册
 */
class GalleryFragment : BaseFragment(), OnGalleryListener {

    companion object {

        @JvmStatic
        fun newInstance(): GalleryFragment {
            return GalleryFragment()
        }

    }

    /**
     * 文字
     */
    private lateinit var mTextBoardContract: ActivityResultLauncher<Void>

    /**
     * 配置
     */
    private lateinit var mAlbumConfig: AlbumConfig

    /**
     * 回调
     */
    private lateinit var mListener: OnAlbumListener

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(requireActivity()).get(AlbumViewModel::class.java) }

    /**
     * pagerAdapter
     */
    private lateinit var mAlbumPagerAdapter: AlbumPagerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnAlbumListener
    }

    override fun init() {

        mAlbumConfig = AlbumSdkInit.getAlbumConfig()

        //fragment
        initFragment()

        //监听
        mViewModel.selectedDir.observe(viewLifecycleOwner) {
            mAlbumPagerAdapter.freshMedia(it.type, it.id)
        }
    }

    /**
     * 跳转
     */
    override fun initRegisterContract() {
        //文字
        mTextBoardContract = registerForActivityResult(TextBoardContracts()) {
            it?.let {
                mViewModel.addSelectMedia(MediaInfo(it, 0, MediaType.TYPE_WORD))
            }
        }
    }

    /**
     * fragment
     */
    private fun initFragment() {
        val photoTab = tabMenu.newTab()
        tabMenu.addTab(photoTab)
        val videoTab = tabMenu.newTab()
        tabMenu.addTab(videoTab)
        val allTab = tabMenu.newTab()
        tabMenu.addTab(allTab)

        val albumSupport = mAlbumConfig.albumSupport
        //支持列表
        mAlbumPagerAdapter = AlbumPagerAdapter(childFragmentManager, albumSupport, this)
        viewPager.adapter = mAlbumPagerAdapter
        viewPager.offscreenPageLimit = mAlbumPagerAdapter.count
        viewPager.post {
            if (mAlbumPagerAdapter.count >= 3) {
                when (mAlbumConfig.firstShow) {
                    AlbumConfig.FIRST_ALL -> {
                        viewPager.setCurrentItem(0, false)
                    }
                    AlbumConfig.FIRST_VIDEO -> {
                        viewPager.setCurrentItem(1, false)
                    }
                    AlbumConfig.FIRST_IMAGE -> {
                        viewPager.setCurrentItem(2, false)
                    }
                    else -> {
                        viewPager.setCurrentItem(0, false)
                    }
                }
            } else {
                viewPager.setCurrentItem(0, false)
            }
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mListener.selectMenu(AlbumConfig.ALBUM_SUPPORT_DEFAULT)
                    }
                    1 -> {
                        //显示
                        mListener.selectMenu(AlbumConfig.ALBUM_SUPPORT_VIDEO_ONLY)
                    }
                    2 -> {
                        mListener.selectMenu(AlbumConfig.ALBUM_SUPPORT_IMAGE_ONLY)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        //关联显示
        tabMenu.setupWithViewPager(viewPager, false)
        //菜单
        if (albumSupport != AlbumConfig.ALBUM_SUPPORT_DEFAULT) {
            tabMenu.visibility = ViewPager.GONE
        } else {
            allTab.text = getString(R.string.album_media_all)
            videoTab.text = getString(R.string.album_video)
            photoTab.text = getString(R.string.album_photo)
        }
    }


    /**
     * 显示文字版
     */
    override fun isText(type: Int): Boolean {
        return type == AlbumConfig.ALBUM_SUPPORT_IMAGE_ONLY && !mAlbumConfig.hideText
    }

    /**
     * 显示编辑
     */
    override fun isEdit(): Boolean {
        return !mAlbumConfig.hideEdit
    }


    /**
     * 添加
     */
    override fun onAddAlbum(mediaInfo: MediaInfo): Int {
        mViewModel.addSelectMedia(mediaInfo)
        return mViewModel.getSelectedList().size
    }

    /**
     * 编辑
     */
    override fun onEdit(mediaInfo: MediaInfo, selected: Boolean) {
        mListener.onEdit(mediaInfo, selected)
    }

    /**
     * 文字
     */
    override fun onAddText() {
        mTextBoardContract.launch(null)
    }


    override fun getLayoutId(): Int {
        return R.layout.album_fragment_album
    }

    fun refresh(type:Int,id:String="0"){
        mAlbumPagerAdapter.freshMedia(type,id)
    }

}