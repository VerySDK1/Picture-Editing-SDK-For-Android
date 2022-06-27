package com.pesdk.album.uisdk.ui.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.uisdk.bean.MaterialInfo
import com.pesdk.helper.loadImage
import com.pesdk.helper.loadImageCenterCrop
import com.vesdk.common.download.DownloadStatue
import com.vesdk.common.helper.refreshItem
import com.vesdk.common.utils.CommonUtils
import com.vesdk.common.widget.loading.LoadingView

/**
 * 素材库列表
 */
class MaterialAdapter(private val list: MutableList<MaterialInfo>) :
    BaseQuickAdapter<MaterialInfo, BaseViewHolder>(R.layout.album_item_material, list) {

    /**
     * 最后点击选中
     */
    private var mLastCheck = 0

    override fun convert(holder: BaseViewHolder, item: MaterialInfo) {
        val fail = holder.getView<ImageView>(R.id.ivFailAgain)
        val down = holder.getView<ImageView>(R.id.ivDown)
        val loading: LoadingView = holder.getView(R.id.loading)

        //封面
        holder.getView<ImageView>(R.id.ivIcon)
            .loadImageCenterCrop(item.networkData.cover, R.drawable.pecom_ic_default)
        fail.loadImage(R.drawable.album_ic_fail_again_big)

        //下载
        when (item.downStatue) {
            DownloadStatue.DOWN_ING -> {
                loading.setRound(0f)
                loading.visibility = View.VISIBLE
                down.visibility = View.GONE
                fail.visibility = View.GONE
            }
            DownloadStatue.DOWN_FAIL -> {
                loading.visibility = View.GONE
                down.visibility = View.VISIBLE
                fail.visibility = View.VISIBLE
            }
            DownloadStatue.DOWN_SUCCESS -> {
                loading.visibility = View.GONE
                down.visibility = View.GONE
                fail.visibility = View.GONE
            }
            DownloadStatue.DOWN_NONE -> {
                loading.visibility = View.GONE
                down.visibility = View.VISIBLE
                fail.visibility = View.GONE
            }
        }
    }

    /**
     * 设置选中
     */
    fun setLastCheck(position: Int) {
        if (position != mLastCheck) {
            val oldCheck = mLastCheck
            mLastCheck = position
            refreshItem(oldCheck, mLastCheck)
        }
    }

    /**
     * 选中下标
     */
    fun getLastCheck(): Int {
        return mLastCheck
    }

    /**
     * 返回选中
     */
    fun getCurrent(): MaterialInfo? {
        return if (mLastCheck < 0 || mLastCheck >= list.size) null else list[mLastCheck]
    }

    /**
     * 刷新单个
     */
    fun refreshItem(info: MaterialInfo) {
        refreshItem(data.indexOfFirst { it.networkData.id == info.networkData.id })
    }

    /**
     * 刷新单个
     */
    fun refreshItem(key: String) {
        refreshItem(data.indexOfFirst {
            CommonUtils.getKey(
                it.networkData.id,
                it.networkData.file
            ) == key
        })
    }
}