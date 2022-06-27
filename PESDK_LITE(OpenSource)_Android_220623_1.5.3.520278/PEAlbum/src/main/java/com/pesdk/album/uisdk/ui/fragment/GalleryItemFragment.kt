package com.pesdk.album.uisdk.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.pesdk.album.R
import com.pesdk.album.api.bean.MediaInfo
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.uisdk.bean.AlbumItem
import com.pesdk.album.uisdk.listener.OnGalleryItemListener
import com.pesdk.album.uisdk.listener.OnGalleryListener
import com.pesdk.album.uisdk.ui.adapter.AlbumAdapter
import com.pesdk.album.uisdk.viewmodel.GalleryViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.helper.bindArgument
import com.vesdk.common.helper.init
import kotlinx.android.synthetic.main.album_fragment_gallery_item.*

/**
 * 图库
 */
class GalleryItemFragment : BaseFragment() {

    companion object {

        private const val TYPE = "type"

        @JvmStatic
        fun newInstance(type: Int): GalleryItemFragment {
            val albumFragment = GalleryItemFragment()
            val args = Bundle()
            args.putInt(TYPE, type)
            albumFragment.arguments = args
            return albumFragment
        }

    }

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(this).get(GalleryViewModel::class.java) }

    /**
     * 全部、视频、图片
     */
    private val mType: Int by bindArgument(TYPE, 0)

    /**
     * adapter
     */
    private lateinit var mAdapter: AlbumAdapter

    /**
     * 回调
     */
    private lateinit var mListener: OnGalleryListener

    /**
     * 没有媒体
     */
    private var noMedia: View? = null

    /**
     * 初始化
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun init() {
        //数据变化
        mViewModel.mediaListData.observe(viewLifecycleOwner) { result ->
            mListener.let {
                if (it.isText(mType)) {
                    result.add(0, AlbumItem(null, 0, MediaType.TYPE_WORD))
                }
            }
            //没有媒体
            if (result.size <= 0) {
                if (noMedia == null) {
                    noMedia = vsNoMedia.inflate()
                } else {
                    noMedia?.visibility = VISIBLE
                }
            } else {
                noMedia?.visibility = GONE
            }
            mViewModel.setAlbumList(result)
            mAdapter.notifyDataSetChanged()
        }

        //列表
        initRecycler()

        //获取数据
        mViewModel.freshMedia(mType)
    }

    /**
     * 初始化列表
     */
    private fun initRecycler() {
        mAdapter = AlbumAdapter(mListener.isEdit(), mViewModel.getAlbumList())
        mAdapter.setListener(object : OnGalleryItemListener {

            override fun onClickWord() {
                //文字版
                mListener.onAddText()
            }

            override fun onClickAdd(position: Int, item: AlbumItem) {
                //添加
                item.media?.let {
                    val num =
                            mListener.onAddAlbum(MediaInfo(it.dataPath, item.duration, item.type))
                    if (num > 0) {
                        mAdapter.setSelect(position, num)
                    }
                }
            }

            override fun onClickEdit(item: AlbumItem) {
                //预览
                item.media?.let {
                    mListener.onEdit(MediaInfo(it.dataPath, item.duration, item.type), false)
                }
            }

        })
        rvAlbum.init(mAdapter, GridLayoutManager(requireContext(), 3))
    }

    /**
     * 添加回调
     */
    fun setListener(listener: OnGalleryListener) {
        mListener = listener
    }

    /**
     * 刷新
     */
    fun freshMedia(id: String) {
        mViewModel.freshMedia(mType, id)
    }

    /**
     * 布局id
     */
    override fun getLayoutId(): Int {
        return R.layout.album_fragment_gallery_item
    }

}