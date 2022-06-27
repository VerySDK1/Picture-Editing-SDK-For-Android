package com.pesdk.album.uisdk.ui.adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.bean.template.LockingType
import com.pesdk.bean.template.RReplaceMedia
import com.pesdk.bean.template.ReplaceType
import com.pesdk.helper.loadImage
import com.vecore.models.MediaType
import com.vecore.models.PEImageObject
import com.vesdk.common.helper.refreshItem

/**
 * 选中媒体
 */
class MediaCheckedTemplateAdapter(list: MutableList<RReplaceMedia>) :
    BaseQuickAdapter<RReplaceMedia, BaseViewHolder>(
        R.layout.album_item_media_checked_template, list
    ) {

    override fun convert(holder: BaseViewHolder, item: RReplaceMedia) {
        with(holder) {
            //封面
            if (item.mediaObject == null) {
                setVisible(R.id.mask, true)
                setVisible(R.id.iv_path, true)
                setVisible(R.id.iv_cover, false)
                setVisible(R.id.iv_delete, false)
                getView<ImageView>(R.id.iv_path).loadImage(item.cover)
            } else {
                setVisible(R.id.mask, false)
                setVisible(R.id.iv_path, false)
                setVisible(R.id.iv_cover, true)
                setVisible(R.id.iv_delete, true)
                getView<ImageView>(R.id.iv_cover).loadImage(item.mediaObject.mediaPath)
            }

            //类型
            when (item.lockingType) {
                LockingType.LockingImage -> {
                    setVisible(R.id.iv_type, true)
                    getView<ImageView>(R.id.iv_type).loadImage(R.drawable.edit_item_image)
                }
                LockingType.LockingVideo -> {
                    setVisible(R.id.iv_type, true)
                    getView<ImageView>(R.id.iv_type).loadImage(R.drawable.edit_item_video)
                }
                else -> {
                    setVisible(R.id.iv_type, false)
                }
            }
            when (item.mediaType) {
                ReplaceType.TypeBG -> {
                    setText(R.id.tv_type, context.getString(R.string.album_edit_menu_bg))
                }
                ReplaceType.TypePip -> {
                    setText(R.id.tv_type, context.getString(R.string.album_edit_menu_pip))
                }
                ReplaceType.TypeWater -> {
                    setText(R.id.tv_type, context.getString(R.string.album_edit_menu_watermark))
                }
                else -> {
                    setText(R.id.tv_type, null)
                }
            }

            setText(R.id.tv_position, "${layoutPosition + 1}")
            setGone(R.id.origin, item.group < 0)
        }
    }

    /**
     * 更新
     */
    @SuppressLint("NotifyDataSetChanged")
    fun update(mediaObject: PEImageObject?, position: Int = -1): Int {
        return if (position >= 0) {
            data[position].mediaObject = null
            refreshItem(position)
            -1
        } else {
            mediaObject?.let { media ->
                var index = -1
                var timeShort = true
                val type =
                    if (media.mediaType == MediaType.MEDIA_IMAGE_TYPE) LockingType.LockingImage else LockingType.LockingVideo
                for ((i, info) in data.withIndex()) {
                    if (info.mediaObject == null) {
                        //判断是否限制了类型
                        if (info.lockingType == LockingType.LockingNone || info.lockingType == type) {
                            if (type != LockingType.LockingVideo) {
                                info.mediaObject = mediaObject
                                timeShort = false
                            }
                            index = i
                            //判断 分组
                            if (info.group > 0) {
                                for (j in i + 1 until data.size) {
                                    val info1: RReplaceMedia = data[j]
                                    if (info1.mediaObject == null && info1.group == info.group) {
                                        if (type != LockingType.LockingVideo) {
                                            info1.mediaObject = mediaObject
                                            timeShort = false
                                        }
                                        index = j
                                    }
                                }
                            }
                            if (!timeShort) {
                                notifyDataSetChanged()
                                break
                            }
                        } else {
                            timeShort = false
                        }
                    }
                }
                index
            } ?: kotlin.run { -1 }
        }
    }

    /**
     * 返回数量
     */
    fun getNum(): Int {
        return data.count { it.mediaObject != null }
    }

}