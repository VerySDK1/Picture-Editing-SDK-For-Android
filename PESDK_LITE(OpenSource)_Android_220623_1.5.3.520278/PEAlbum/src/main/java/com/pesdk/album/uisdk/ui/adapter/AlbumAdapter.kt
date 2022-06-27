package com.pesdk.album.uisdk.ui.adapter

import android.annotation.SuppressLint
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.uisdk.bean.AlbumItem
import com.pesdk.album.uisdk.listener.OnGalleryItemListener
import com.pesdk.album.uisdk.utils.AlbumUtils
import com.pesdk.api.ChangeLanguageHelper
import com.pesdk.helper.loadGif
import com.pesdk.helper.loadImage
import com.pesdk.helper.loadImageCenterCrop
import com.pesdk.helper.loadWebpImage
import com.vesdk.common.utils.CommonUtils
import com.vesdk.common.utils.DateTimeUtils.millisecond2String

/**
 * 相册列表
 */
class AlbumAdapter(private val edit: Boolean, list: MutableList<AlbumItem>?) :
    BaseMultiItemQuickAdapter<AlbumItem, BaseViewHolder>(list) {

    /**
     * 宽高
     */
    private val width = CommonUtils.dip2px(80f)
    private val height = CommonUtils.dip2px(80f)

    private var mEnabledUse = true

    init {
        // 绑定 layout 对应的 type
        addItemType(MediaType.TYPE_WORD.value, R.layout.album_item_album_word)
        addItemType(MediaType.TYPE_IMAGE.value, R.layout.album_item_album_image)
        addItemType(MediaType.TYPE_VIDEO.value, R.layout.album_item_album_video)
    }

    override fun convert(holder: BaseViewHolder, item: AlbumItem) {
        when (holder.itemViewType) {
            MediaType.TYPE_WORD.value -> onBindWord(holder)
            MediaType.TYPE_IMAGE.value -> onBindImage(holder, item)
            MediaType.TYPE_VIDEO.value -> onBindVideo(holder, item)
        }
        holder.setEnabled(R.id.mask, mEnabledUse)
    }

    private fun onBindWord(holder: BaseViewHolder) {
        val zh = ChangeLanguageHelper.isZh(context)
        if (zh) {
            holder.getView<ImageView>(R.id.ivWord).loadImage(R.drawable.album_ic_word_broad)
        } else {
            holder.getView<ImageView>(R.id.ivWord).loadImage(R.drawable.album_ic_word_broad_en)
        }
    }

    private fun onBindImage(holder: BaseViewHolder, item: AlbumItem) {
        //封面
        val image = item.media?.dataPath
        image?.let {
            if (AlbumUtils.isUri(it)) {
                AlbumUtils.getAbsolutePath(context, it)
            } else {
                it
            }?.let { path ->
                if (path.contains("webp")) {
                    holder.getView<ImageView>(R.id.ivIcon)
                        .loadWebpImage(image, width, height, R.drawable.pecom_ic_default)
                } else if (path.contains("gif")) {
                    holder.getView<ImageView>(R.id.ivIcon)
                        .loadGif(image, true, R.drawable.pecom_ic_default)
                } else {
                    holder.getView<ImageView>(R.id.ivIcon)
                        .loadImageCenterCrop(image, R.drawable.pecom_ic_default)
                }
            } ?: kotlin.run {
                holder.getView<ImageView>(R.id.ivIcon)
                    .loadImageCenterCrop(image, R.drawable.pecom_ic_default)
            }
        } ?: kotlin.run {
            holder.getView<ImageView>(R.id.ivIcon)
                .loadImageCenterCrop(image, R.drawable.pecom_ic_default)
        }
        //编辑
        holder.setVisible(R.id.ivEdit, edit)
    }

    private fun onBindVideo(holder: BaseViewHolder, item: AlbumItem) {
        //封面
        holder.getView<ImageView>(R.id.ivIcon)
            .loadImageCenterCrop(item.media?.dataPath, R.drawable.pecom_ic_default)
        holder.setText(R.id.tvDuration, millisecond2String(item.duration))
        //编辑
        holder.setVisible(R.id.ivEdit, edit)
    }

    /**
     * 设置接口
     */
    fun setListener(listener: OnGalleryItemListener) {
        //子控件点击事件
        addChildClickViewIds(R.id.ivEdit)
        setOnItemChildClickListener { _, _, position ->
            listener.onClickEdit(data[position])
        }

        //点击事件
        setOnItemClickListener { adapter, _, position ->
            when (adapter.getItemViewType(position)) {
                MediaType.TYPE_WORD.value -> listener.onClickWord()
                MediaType.TYPE_IMAGE.value -> listener.onClickAdd(position, data[position])
                MediaType.TYPE_VIDEO.value -> listener.onClickAdd(position, data[position])
            }
        }
    }

    /**
     * 可点击
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setEnabledUse(enabled: Boolean) {
        mEnabledUse = enabled
        notifyDataSetChanged()
    }

    /**
     * 设置选中
     */
    fun setSelect(position: Int, num: Int) {
        val rlNum = getViewByPosition(position, R.id.rlNum)
        val tvNum = getViewByPosition(position, R.id.tvNum)
        if (rlNum != null && tvNum != null) {
            tvNum as TextView
            tvNum.text = num.toString()
            rlNum.visibility = VISIBLE
            val aniSlideOut = AnimationUtils.loadAnimation(context, R.anim.album_center_show)
            aniSlideOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    rlNum.visibility = GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {

                }

            })
            tvNum.startAnimation(aniSlideOut)
        }
    }


}