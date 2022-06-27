package com.pesdk.utils.glide;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.pesdk.R;

public class GlideUtils {
    private static final RequestOptions options = new RequestOptions()
            .error(R.drawable.pecom_ic_default)
            //关键是这个占位图，使用<animated-rotate/>标签;
            //设置帧动画资源也可以，不过需要代码开启动画;
            .placeholder(R.drawable.pecom_ic_default);

    private static final int SIZE = 150;

    public static void setCover(RequestManager manager, ImageView draweeView, String pathVideoConver) {
        setCover(manager, draweeView, pathVideoConver, true, SIZE, SIZE, 1, R.drawable.pecom_ic_default, false);
    }

    public static void setCoverRound(RequestManager manager, ImageView draweeView, String pathVideoConver, int roundedCorner) {
        setCover(manager, draweeView, pathVideoConver, true, SIZE, SIZE, roundedCorner, R.drawable.pecom_ic_default, false);
    }

    public static void setCover(RequestManager manager, ImageView draweeView, String pathVideoConver, int w, int h) {
        setCover(manager, draweeView, pathVideoConver, true, Math.max(w, SIZE), Math.max(h, SIZE), 1, R.drawable.pecom_ic_default, false);
    }

    public static void setCoverWithPlaceholder(RequestManager manager, ImageView draweeView, String pathVideoConver, int placeholderId) {
        setCover(manager, draweeView, pathVideoConver, true, SIZE, SIZE, 1, placeholderId, false);
    }

    public static void setCover(RequestManager manager, ImageView imageView, String url, boolean placeholder, int width, int height, int roundedCorner,
                                int placeholderId, boolean argb) {
        RoundedCorners roundedCorners = new RoundedCorners(Math.max(1, roundedCorner));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners)
                .override(width, height)
                .centerCrop();
        if (placeholder) {
            if (placeholderId != 0) {
                options.placeholder(placeholderId).error(R.drawable.pecom_ic_default);
            } else {
                options.error(R.drawable.pecom_ic_default);
            }

        }
        manager.load(url)
                .format(argb ? DecodeFormat.PREFER_ARGB_8888 : DecodeFormat.PREFER_RGB_565)
                .apply(options)
                .into(imageView);
    }

    public static void setLayerCover(RequestManager manager, ImageView imageView, String url) {
        RoundedCorners roundedCorners = new RoundedCorners(1);
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners)
                .override(SIZE, SIZE)
                .fitCenter();
        options.error(R.drawable.pecom_ic_default);
        manager.load(url)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .apply(options)
                .into(imageView);
    }

    /***
     *
     * @param imageView
     * @param drawable
     */
    public static void setCover(RequestManager requestManager, ImageView imageView, int drawable) {
        setCover(requestManager, imageView, drawable, 1);
    }

    public static void setCover(RequestManager requestManager, ImageView imageView, int drawable, int roundedCorner) {
        RequestOptions options = null;
        if (roundedCorner > 0) {
            RoundedCorners roundedCorners = new RoundedCorners(roundedCorner);
            options = RequestOptions.bitmapTransform(roundedCorners);
        } else {
            options = new RequestOptions();
        }
        requestManager.load(drawable)
                .apply(options)
                .into(imageView);
    }

    public static void setCover(RequestManager requestManager, ImageView imageView, String url, int roundedCorner) {
        RequestOptions options = null;
        if (roundedCorner > 0) {
            RoundedCorners roundedCorners = new RoundedCorners(roundedCorner);
            options = RequestOptions.bitmapTransform(roundedCorners);
        } else {
            options = new RequestOptions();
        }
        requestManager.load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     *
     */
    public static void setCover(RequestManager requestManager, ImageView imageView, Uri uri) {
        requestManager.load(uri)
                .apply(options)
                .into(imageView);
    }
}
