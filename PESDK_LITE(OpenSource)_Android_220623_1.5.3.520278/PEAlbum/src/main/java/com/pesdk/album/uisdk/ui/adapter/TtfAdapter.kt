package com.pesdk.album.uisdk.ui.adapter

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.uisdk.bean.TtfInfo
import com.pesdk.helper.loadImage
import com.vesdk.common.download.DownloadStatue
import com.vesdk.common.helper.refreshItem
import com.vesdk.common.utils.CommonUtils
import com.vesdk.common.widget.loading.LoadingView

/**
 * 字体
 */
class TtfAdapter(val list: MutableList<TtfInfo>) :
    BaseQuickAdapter<TtfInfo, BaseViewHolder>(R.layout.album_item_ttf, list) {

    /**
     * 最后点击选中
     */
    private var mLastCheck = 0

    override fun convert(holder: BaseViewHolder, item: TtfInfo) {
        val fail = holder.getView<ImageView>(R.id.ivFailAgain)
        val down = holder.getView<ImageView>(R.id.ivDown)
        val loading: LoadingView = holder.getView(R.id.loading)

        val position = holder.layoutPosition

        //封面
        if (item.networkData.cover == "") {
            holder.getView<ImageView>(R.id.ivIcon)
                .loadImage(R.drawable.album_ic_default_ttf, R.drawable.pecom_ic_default)
        } else {
            holder.getView<ImageView>(R.id.ivIcon)
                .loadImage(item.networkData.cover, R.drawable.pecom_ic_default)
        }
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

        //选中
        holder.setBackgroundColor(
            R.id.rlItem,
            if (position == mLastCheck) ContextCompat.getColor(
                context,
                R.color.album_item_bg_n
            ) else ContextCompat.getColor(context, R.color.album_item_bg)
        )

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
     * 返回选中
     */
    fun getCurrent(): TtfInfo? {
        return if (mLastCheck < 0 || mLastCheck >= list.size) null else list[mLastCheck]
    }

    /**
     * 刷新单个
     */
    fun refreshItem(info: TtfInfo) {
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