package com.pesdk.helper

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import jp.wasabeef.glide.transformations.CropTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * 图片加载 圆角
 */
fun <T> ImageView.loadImage(image: T?, rounded: Int = 1) {
    Glide.with(this)
        .load(image)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(rounded)))
        .into(this)
}

/**
 * 图片加载 占位图 圆角
 */
fun <T> ImageView.loadImage(image: T?, @DrawableRes holder: Int, rounded: Int = 1) {
    Glide.with(this)
        .load(image)
        .apply(
            RequestOptions
                .placeholderOf(holder)
                .error(holder)
                .transform(RoundedCorners(rounded))
        )
        .into(this)
}

/**
 * 图片加载 回调
 */
fun ImageView.loadImage(uri: String?, requestListener: RequestListener<Drawable?>) {
    Glide.with(this)
        .load(uri)
        .listener(requestListener)
        .into(this)
}

/**
 * 图片加载 宽高
 */
fun <T> ImageView.loadImage(
    image: T?,
    width: Int,
    height: Int,
    @DrawableRes holder: Int,
    rounded: Int = 1
) {
    val multi = MultiTransformation(CropTransformation(width, height))
    Glide.with(this)
        .load(image)
        .apply(
            RequestOptions
                .placeholderOf(holder)
                .error(holder)
                .transform(multi, RoundedCorners(rounded))
        )
        .into(this)
}

fun <T> ImageView.loadWebpImage(
    image: T?,
    width: Int,
    height: Int,
    @DrawableRes holder: Int,
    rounded: Int = 1
) {
    val roundedCorners = if (rounded > 0) {
        RoundedCorners(rounded)
    } else {
        RoundedCorners(0)
    }
    val multi = MultiTransformation(CropTransformation(width, height), CenterCrop(), roundedCorners)
    Glide.with(this)
        .load(image)
        .apply(
            RequestOptions
                .placeholderOf(holder)
                .error(holder)
                .transform(multi, RoundedCorners(rounded))
        )
        .optionalTransform(
            WebpDrawable::class.java,
            WebpDrawableTransformation(multi)
        )
        .into(this)
}

/**
 * 图片加载 中心缩放
 */
fun <T> ImageView.loadImageCenterCrop(
    image: T?,
    @DrawableRes holder: Int? = null,
    rounded: Int = 1
) {
    Glide.with(this)
        .load(image)
        .apply(RequestOptions()
            .dontAnimate()
            .dontTransform()
            .transform(CenterCrop(), RoundedCorners(rounded))
            .apply {
                holder?.let {
                    placeholder(it)
                    error(it)
                }
            })
        .into(this)
}

/**
 * 图片加载 GIF
 */
fun <T> ImageView.loadGif(
    image: T?,
    centerCrop: Boolean = false,
    @DrawableRes holder: Int? = null,
    rounded: Int = 1,
    requestListener: RequestListener<GifDrawable?>? = null
) {
    var requestOptions = RequestOptions().dontTransform()
    requestOptions = if (centerCrop) {
        requestOptions.transform(CenterCrop(), RoundedCorners(rounded))
    } else {
        requestOptions.transform(RoundedCorners(rounded))
    }
    holder?.let {
        requestOptions = requestOptions.placeholder(it).error(it)
    }
    if (requestListener != null) {
        Glide.with(this)
            .asGif()
            .load(image)
            .apply(requestOptions)
            .listener(requestListener)
            .into(this)
    } else {
        Glide.with(this)
            .asGif()
            .load(image)
            .apply(requestOptions)
            .into(this)
    }
}

/**
 * 图片加载 GIF
 */
fun <T> ImageView.loadGif(
    image: T?,
    width: Int,
    height: Int,
    @DrawableRes holder: Int? = null,
    rounded: Int = 1
) {
    var requestOptions = RequestOptions()
        .dontTransform()
        .transform(
            MultiTransformation(CropTransformation(width, height)),
            CenterCrop(),
            RoundedCorners(rounded)
        )
    holder?.let {
        requestOptions = requestOptions.placeholder(it).error(it)
    }
    Glide.with(this)
        .asGif()
        .load(image)
        .apply(requestOptions)
        .into(this)
}

/**
 * 圆形
 */
fun ImageView.loadCircleImage(uri: String?, @DrawableRes holder: Int? = null) {
    if (uri.isNullOrBlank()) {
        if (holder != null) {
            setImageResource(holder)
        }
    } else if (holder == null) {
        Glide.with(this).load(uri).apply(RequestOptions().circleCrop()).into(this)
    } else {
        Glide.with(this)
            .load(uri)
            .apply(
                RequestOptions()
                    .placeholder(holder)
                    .circleCrop()
            )
            .into(this)
    }
}

/**
 * 圆角
 */
fun ImageView.loadRoundImage(uri: String?, radius: Int, @DrawableRes holder: Int? = null) {
    if (uri.isNullOrBlank() && holder != null) {
        setImageResource(holder)
    } else if (holder == null) {
        Glide.with(this).load(uri)
            .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(radius, 0)))
            .into(this)
    } else {
        Glide.with(this)
            .load(uri)
            .apply(
                RequestOptions()
                    .transform(RoundedCornersTransformation(radius, 0))
                    .placeholder(holder)
            )
            .into(this)
    }
}


/**
 * 图片加载 GIF Mark标记
 */
fun ImageView.loadGifMark(uri: String?, holder: String?, mark: Int) {
    Glide.with(this)
        .asGif()
        .load(uri)
        .apply(RequestOptions()
            .dontTransform()
            .signature(ObjectKey("$uri$mark"))
            .apply {
                holder?.let { placeholder(it.toDrawable()) }
            })
        .into(this)
}

fun ImageView.loadImageMark(uri: String?, holder: String?, mark: Int) {
    Glide.with(this)
        .load(uri)
        .apply(RequestOptions()
            .dontAnimate()
            .signature(ObjectKey("$uri$mark")).apply {
                if (holder != null) {
                    this.placeholder(holder.toDrawable())
                }
            })
        .into(this)
}

fun ImageView.loadImageMark(uri: String?, mark: Int) {
    Glide.with(this)
        .load(uri)
        .apply(
            RequestOptions()
                .dontAnimate()
                .signature(ObjectKey("$uri$mark"))
        )
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                setImageDrawable(resource)
                return true
            }
        })
        .submit(layoutParams.width, layoutParams.height)
}

fun ImageView.loadLongImageMark(uri: String?, holder: String?, mark: Int) {
    Glide.with(this)
        .load(uri)
        .apply(RequestOptions
            .bitmapTransform(
                CropTransformation(
                    0,
                    layoutParams.height,
                    CropTransformation.CropType.TOP
                )
            )
            .dontAnimate()
            .signature(ObjectKey("$uri$mark")).apply {
                if (holder != null) {
                    this.placeholder(holder.toDrawable())
                }
            })
        .into(this)
}

fun ImageView.loadLongImageMark(uri: String?, mark: Int) {
    Glide.with(this)
        .load(uri)
        .apply(
            RequestOptions
                .bitmapTransform(
                    CropTransformation(
                        0,
                        layoutParams.height,
                        CropTransformation.CropType.TOP
                    )
                )
                .dontAnimate()
                .signature(ObjectKey("$uri$mark"))
        )
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                setImageDrawable(resource)
                return true
            }
        })
        .submit(layoutParams.width, layoutParams.height)
}

fun ImageView.loadVideoMark(uri: String?, holder: String?, mark: Int) {
    Glide.with(this)
        .load(uri)
        .apply(RequestOptions()
            .frame(0)
            .signature(ObjectKey("$uri$mark"))
            .centerCrop()
            .dontAnimate()
            .apply {
                if (holder != null) {
                    this.placeholder(holder.toDrawable())
                }
            }
        )
        .into(this)
}

fun ImageView.loadVideoMark(uri: String?, mark: Int) {
    Glide.with(this)
        .load(uri)
        .apply(
            RequestOptions()
                .frame(0)
                .signature(ObjectKey("$uri$mark"))
                .centerCrop()
                .dontAnimate()
        )
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                setImageDrawable(resource)
                return true
            }
        })
        .submit(layoutParams.width, layoutParams.height)
}

fun ImageView.loadVideo(uri: String, @DrawableRes holder: Int) {
    Glide.with(this)
        .load(uri)
        .apply(
            RequestOptions()
                .frame(0)
                .centerCrop()
                .placeholder(holder)
                .dontAnimate()
        )
        .into(this)
}

fun ImageView.loadBase64(uri: ByteArray?, width: Int, height: Int, mark: Int) {
    val multi = MultiTransformation(CropTransformation(width, height))
    Glide.with(this)
        .load(uri)
        .apply(
            RequestOptions()
                .centerCrop()
                .transform(multi)
                .signature(ObjectKey("$uri$mark"))
                .dontAnimate()
        )
        .into(this)
}

fun String.toDrawable(): Drawable? {
    val img = Base64.decode(this, Base64.DEFAULT);
    if (img != null) {
        return BitmapDrawable(BitmapFactory.decodeByteArray(img, 0, img.size))
    }
    return null
}