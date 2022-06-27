package com.vesdk.camera.ui.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.helper.loadImage
import com.pesdk.helper.loadImageCenterCrop
import com.vesdk.camera.R
import com.vesdk.camera.bean.FilterInfo
import com.vesdk.camera.widget.OutsideFrameView
import com.vesdk.common.download.DownloadStatue
import com.vesdk.common.helper.refreshItem
import com.vesdk.common.utils.CommonUtils
import com.vesdk.common.widget.loading.LoadingView

/**
 * 圆角
 */
private const val ROUND = 20

/**
 * 滤镜列表
 */
class FilterAdapter(private val list: MutableList<FilterInfo>) :
    BaseQuickAdapter<FilterInfo, BaseViewHolder>(R.layout.camera_item_filter, list) {

    /**
     * 最后点击选中
     */
    private var mLastCheck = -1

    override fun convert(holder: BaseViewHolder, item: FilterInfo) {
        val fail = holder.getView<ImageView>(R.id.ivFailAgain)
        val down = holder.getView<ImageView>(R.id.ivDown)
        val loading: LoadingView = holder.getView(R.id.loading)
        val frame: OutsideFrameView = holder.getView(R.id.frame)

        val position = holder.layoutPosition

        //封面
        holder.getView<ImageView>(R.id.ivIcon)
            .loadImageCenterCrop(item.networkData.cover, R.drawable.pecom_ic_default, ROUND)
        fail.loadImage(R.drawable.camera_ic_fail_again_big, ROUND)

        //选中
        frame.setBorder(2, ROUND)
        frame.isSelected = position == mLastCheck

        //下载
        when (item.downStatue) {
            DownloadStatue.DOWN_ING -> {
                loading.setRound(ROUND.toFloat())
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
            val oldChecked = mLastCheck
            mLastCheck = position
            refreshItem(oldChecked, mLastCheck)
        }
    }

    /**
     * 刷新单个
     */
    fun refreshItem(info: FilterInfo) {
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

    /**
     * 选中下标
     */
    fun getLastCheck(): Int {
        return mLastCheck
    }

    /**
     * 返回选中
     */
    fun getCurrent(): FilterInfo? {
        return if (mLastCheck < 0 || mLastCheck >= list.size) null else list[mLastCheck]
    }

}