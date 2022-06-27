package com.pesdk.uisdk.util.popwind;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.LayerAdapter;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.AnimUtil;
import com.vecore.base.lib.utils.CoreUtils;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 增加
 */
public class PopPipHandler {


    private Activity mActivity;
    private PopupWindow popupWindow;
    private static final String TAG = "PopPipHandler";

    private ImageView mIvAdd;
    private RecyclerView rvPipList;
    private LayerAdapter adapter;
    private ImageHandlerListener mImageHandlerListener;
    private View mTargetView;
    private int topY;
    private View mMixLayer;
    private ICallback mICallback;

    public void show(Activity activity, View view, ImageHandlerListener imageHandlerListener, ICallback callback) {
        mActivity = activity;
        animUtil = new AnimUtil();
        mTargetView = view;
        mICallback = callback;
        mImageHandlerListener = imageHandlerListener;
        View contentView = LayoutInflater.from(mActivity).inflate(R.layout.pesdk_pop_layer_layout, null);
        mIvAdd = contentView.findViewById(R.id.pop_add);
        mIvAdd.setOnClickListener(v -> {
            mIvAdd.setImageResource(R.drawable.pesdk_layer_add_close);
            callback.onAddLayer();
        });
        mMixLayer = contentView.findViewById(R.id.btn_layer_merge);

        //设置pop获取焦点，如果为false点击返回按钮会退出当前Activity，如果pop中有Editor的话，focusable必须要为true
        popupWindow = new PopupWindow(contentView, activity.getResources().getDimensionPixelSize(R.dimen.dp_100), WindowManager.LayoutParams.WRAP_CONTENT, false);

        // 设置pop可点击，为false点击事件无效，默认为true
        popupWindow.setTouchable(true);
        // 设置点击pop外侧消失，默认为false；在focusable为true时点击外侧始终消失
//        popupWindow.setOutsideTouchable(true);

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 设置pop透明效果
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int[] location = new int[2];
        view.getLocationInWindow(location);
//        Log.e(TAG, "show: " + Arrays.toString(location));
        topY = location[1] + CoreUtils.dpToPixel(30);
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP, CoreUtils.dpToPixel(10), topY);

        // 设置pop关闭监听，用于改变背景透明度
        popupWindow.setOnDismissListener(() -> {
            toggleBright();
        });

        bright = false;

        rvPipList = contentView.findViewById(R.id.rvPipList);
        adapter = new LayerAdapter(Glide.with(mActivity), false);
        adapter.setIHideClickListener((position, hide) -> {
            adapter.getItem(position).setHide(hide);
            adapter.notifyDataSetChanged();
        });
        rvPipList.setAdapter(adapter);
        refresh();
        adapter.setOnItemClickListener((OnItemClickListener<CollageInfo>) (position, item) -> {
            onItemCheck(position, item);
        });

        toggleBright();
    }

    private List<CollageInfo> initData() {
        return mImageHandlerListener.getParamHandler().getParam().getCollageList();
    }

    /**
     * 内部切换选中项
     *
     * @param position
     * @param item
     */
    private void onItemCheck(int position, CollageInfo item) {
        //切换选中项
        mImageHandlerListener.getForeground().exitEditMode();
        mImageHandlerListener.getForeground().reEdit(item, mImageHandlerListener.enablePipDeleteMenu(), true); //再次编辑
    }

    /**
     * 外部删除单个item时|切换选中项
     */
    public void refresh() {
        List<CollageInfo> list = initData();
        if (list.size() > 1) {
            mMixLayer.setVisibility(View.VISIBLE);
            mMixLayer.setOnClickListener(v -> {
                mICallback.onMerge();
            });
        } else {
            mMixLayer.setVisibility(View.GONE);
        }


        int index = list.indexOf(mImageHandlerListener.getForeground().getCurrentCollageInfo());

        Log.e(TAG, "refresh: " + index + " >" + mImageHandlerListener.getForeground().getCurrentCollageInfo());
        adapter.addAll(list, index);
        ViewGroup.LayoutParams lp = rvPipList.getLayoutParams();
        int tmp = mActivity.getResources().getDimensionPixelSize(R.dimen.dp_55) * list.size();

        int maxHeight = CoreUtils.getMetrics().heightPixels - mActivity.getResources().getDimensionPixelSize(R.dimen.pesdk_fragment_main_menu_height) - 2 * mActivity.getResources().getDimensionPixelSize(R.dimen.dp_45);
        if (tmp < maxHeight) {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            lp.height = maxHeight;
        }
    }


    private AnimUtil animUtil;
    private float bgAlpha = 1f;
    private boolean bright = false;

    private static final long DURATION = 500;
    private static final float START_ALPHA = 0.8f;
    private static final float END_ALPHA = 1f;


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

    public void reset() {
        mIvAdd.setImageResource(R.drawable.pesdk_layer_add);
    }

    /**
     * 仅用户手动关闭时才允许关闭
     */
    public void dismiss() {
        popupWindow.dismiss();
        backgroundAlpha(1f);
    }

    /**
     * 切换到二级界面主动隐藏
     */
    public void hide() {
        if (isShowing()) {
            popupWindow.dismiss();
            backgroundAlpha(1f);
        }
    }

    /**
     * 切换主界面时恢复UI
     */
    public void restore() {
        if (!isShowing() && !mActivity.isFinishing()) {
            popupWindow.showAtLocation(mTargetView, Gravity.LEFT | Gravity.TOP, CoreUtils.dpToPixel(10), topY);
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }


    public static interface ICallback {
        void onAddLayer();

        void onMerge();

    }
}
