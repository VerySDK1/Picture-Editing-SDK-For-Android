package com.pesdk.uisdk.util.popwind;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.AnimUtil;
import com.vecore.base.lib.utils.CoreUtils;

/**
 * 增加
 */
public class PopAddHandler {


    private Activity mActivity;
    private PopupWindow popupWindow;
    private static final String TAG = "LayerHandler";
    private ICallback mCallback;

    public void show(Activity activity, View view, boolean enableLayer, ICallback callback) {
        mActivity = activity;
        mCallback = callback;
        animUtil = new AnimUtil();
        View contentView = LayoutInflater.from(mActivity).inflate(R.layout.pesdk_pop_add_layout, null);
        contentView.findViewById(R.id.pop_add_sticker).setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onSticker();
        });
        contentView.findViewById(R.id.pop_add_text).setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onText();
        });

        contentView.findViewById(R.id.pop_add_overlay).setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onOverlay();
        });

        View layer = contentView.findViewById(R.id.pop_add_pic);
        layer.setVisibility(enableLayer ? View.VISIBLE : View.GONE);
        layer.setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onPic();
        });

        //设置pop获取焦点，如果为false点击返回按钮会退出当前Activity，如果pop中有Editor的话，focusable必须要为true

        int height = activity.getResources().getDimensionPixelSize(R.dimen.dp_50) * (enableLayer ? 3 : 2);
        height = height + CoreUtils.dpToPixel(20);
        popupWindow = new PopupWindow(contentView, activity.getResources().getDimensionPixelSize(R.dimen.dp_100), WindowManager.LayoutParams.WRAP_CONTENT, true);
//        popupWindow = new PopupWindow(contentView, activity.getResources().getDimensionPixelSize(R.dimen.dp_100), height, true);

        // 设置pop可点击，为false点击事件无效，默认为true
        popupWindow.setTouchable(true);
        // 设置点击pop外侧消失，默认为false；在focusable为true时点击外侧始终消失
        popupWindow.setOutsideTouchable(true);

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 设置pop透明效果
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int[] location = new int[2];
        view.getLocationInWindow(location);
//        Log.e(TAG, "show: " + Arrays.toString(location));
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP, CoreUtils.dpToPixel(100), location[1] + CoreUtils.dpToPixel(40));
//        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP, CoreUtils.dpToPixel(100), location[1] - height);

        // 设置pop关闭监听，用于改变背景透明度
        popupWindow.setOnDismissListener(() -> {
            toggleBright();
            mCallback.onDismiss();
        });

        bright = false;
        toggleBright();
    }

    private AnimUtil animUtil;
    private float bgAlpha = 1f;
    private boolean bright = false;

    private static final long DURATION = 500;
    private static final float START_ALPHA = 0.6f;
    private static final float END_ALPHA = 0.8f;

    public void toggleBright() {
        // 三个参数分别为：起始值 结束值 时长，那么整个动画回调过来的值就是从0.5f--1f的
        animUtil.setValueAnimator(START_ALPHA, END_ALPHA, DURATION);
        animUtil.addUpdateListener(progress -> {
            // 此处系统会根据上述三个值，计算每次回调的值是多少，我们根据这个值来改变透明度
            bgAlpha = bright ? progress : (START_ALPHA + END_ALPHA - progress);
            backgroundAlpha(bgAlpha);
        });
        animUtil.addEndListner(animator -> {
            // 在一次动画结束的时候，翻转状态
            bright = !bright;
        });
        animUtil.startAnimator();
    }


    /**
     * 此方法用于改变背景的透明度，从而达到“变暗”的效果
     */
    private void backgroundAlpha(float bgAlpha) {
//        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
//        // 0.0-1.0
//        lp.alpha = bgAlpha;
//        mActivity.getWindow().setAttributes(lp);
//        // everything behind this window will be dimmed.
//        // 此方法用来设置浮动层，防止部分手机变暗无效
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        }
    }


    public static interface ICallback {
        void onOverlay();

        void onText();

        void onSticker();

        void onPic();

        void onDismiss();
    }
}
