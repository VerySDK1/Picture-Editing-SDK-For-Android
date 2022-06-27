package com.pesdk.widget.loading;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pesdk.R;
import com.pesdk.widget.loading.render.LoadingRenderer;
import com.pesdk.widget.loading.render.LoadingRendererFactory;
import com.pesdk.widget.text.JumpingBeans;
import com.vecore.base.lib.utils.CoreUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomLoadingView extends FrameLayout {

    /**
     * 最多重试3次
     */
    public static final int MAX_NUM = 3;

    /**
     * 根
     */
    private RelativeLayout mRlRoot;
    /**
     * 加载
     */
    private LoadingView mLoadingView;
    /**
     * 错误
     */
    private RelativeLayout mRlError;
    private TextView mTvError;
    private JumpingBeans mJumpingBeans;
    /**
     * 返回
     */
    private ImageView mBtnCancel;
    /**
     * 加载错误
     */
    private String mLoadError;
    /**
     * 显示动画
     */
    private LoadingRenderer mLoadingRenderer;
    /**
     * 背景颜色和圆弧
     */
    private int mBgColor = Color.TRANSPARENT;
    private int mRound;
    /**
     * 重新加载的数量
     */
    private int mLoadingNum = 1;
    /**
     * 隐藏返回按钮
     */
    private boolean mIsHideCancel = false;
    /**
     * 重新加载
     */
    private boolean mIsReload = false;
    /**
     * 整个背景颜色
     */
    private int mAllBgColor = Color.TRANSPARENT;
    private int mTvColor = Color.BLACK;

    public CustomLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //上下文
        mLoadError = context.getString(R.string.common_pe_loading_error);
        mRound = CoreUtils.dip2px(context, 5);
        if (attrs != null) {
            try {
                @SuppressLint("CustomViewStyleable")
                TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.pecom_LoadingView);
                int loadingRendererId = ta.getInt(R.styleable.pecom_LoadingView_pecom_loading_renderer, 7);
                mBgColor = ta.getColor(R.styleable.pecom_LoadingView_pecom_lv_bg_color, Color.TRANSPARENT);
                mAllBgColor = ta.getColor(R.styleable.pecom_LoadingView_pecom_bg_color, Color.TRANSPARENT);
                mTvColor = ta.getColor(R.styleable.pecom_LoadingView_pecom_tv_color, Color.BLACK);
                mRound = ta.getColor(R.styleable.pecom_LoadingView_pecom_lv_round, CoreUtils.dip2px(context, 5));
                mLoadingRenderer = LoadingRendererFactory.createLoadingRenderer(context, loadingRendererId);
                ta.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 加载中
     */
    public void loadIng() {
        mLoadingNum++;
        if (mIsReload && mLoadingView != null) {
            mLoadingView.setVisibility(VISIBLE);
            mRlError.setVisibility(GONE);
            if (mJumpingBeans != null) {
                mJumpingBeans.stopJumping();
                mJumpingBeans = null;
            }
        }
    }

    /**
     * 加载失败
     */
    public void loadError() {
        loadError(null);
    }

    /**
     * 加载失败, 可以回调
     */
    public void loadError(String msg) {
        loadError(msg, true);
    }

    /**
     * 加载失败
     */
    public void loadError(String msg, boolean enableReload) {
        mIsReload = false;
        if (mLoadingView != null) {
            mLoadingView.setVisibility(GONE);
            mRlError.setVisibility(VISIBLE);
            if (!TextUtils.isEmpty(msg)) {
                mTvError.setText(msg);
            } else {
                mTvError.setText(mLoadError);
            }
            errorJump();
        }
        if (!enableReload) {
            mLoadingNum = MAX_NUM;
        }
    }

    /**
     * 加载成功
     */
    public void loadSuccess() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(GONE);
            mRlError.setVisibility(GONE);
        }
    }

    /**
     * 隐藏
     */
    public void setHideCancel(boolean hideCancel) {
        mIsHideCancel = hideCancel;
        if (mBtnCancel != null) {
            mBtnCancel.setVisibility(hideCancel ? GONE : VISIBLE);
        }
    }

    /**
     * 设置Renderer
     */
    public void setLoadingRenderer(LoadingRenderer loadingRenderer) {
        if (mLoadingView != null) {
            mLoadingView.setLoadingRenderer(loadingRenderer);
            mLoadingView.setRound(mRound);
            mLoadingView.setColor(mBgColor);
        }
    }

    /**
     * 设置背景
     */
    public void setBackground(int color) {
        mAllBgColor = color;
        if (mRlRoot != null) {
            mRlRoot.setBackgroundColor(color);
        }
        if (mRlError != null) {
            mRlError.setBackgroundColor(color);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.common_loading_custom, null);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        mRlRoot = view.findViewById(R.id.rl_loading);
        mLoadingView = view.findViewById(R.id.loading);
        mRlError = view.findViewById(R.id.rl_error);
        mTvError = view.findViewById(R.id.tv_error);

        view.findViewById(R.id.iv_loading).setOnClickListener(v -> clickReload());
        mTvError.setOnClickListener(v -> clickReload());

        mBtnCancel = view.findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }
        });
        mBtnCancel.setVisibility(mIsHideCancel ? GONE : VISIBLE);

        setLoadingRenderer(mLoadingRenderer);
        setBackground(mAllBgColor);
        mTvError.setTextColor(mTvColor);
        addView(view, layoutParams);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        boolean gone = visibility == GONE;
        if (gone) {
            if (mJumpingBeans != null) {
                mJumpingBeans.stopJumping();
                mJumpingBeans = null;
            }
        } else if (mRlError.getVisibility() == VISIBLE) {
            errorJump();
        }
    }

    /**
     * 加载失败跳动
     */
    private void errorJump() {
        if (mJumpingBeans != null) {
            mJumpingBeans.stopJumping();
            mJumpingBeans = null;
        }
        CharSequence text = mTvError.getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mJumpingBeans = JumpingBeans.with(mTvError)
                .makeTextJump(0, text.length())
                .setIsWave(true)
                .setAnimatedDutyCycle(0.25f)
                // ms
                .setLoopDuration(2000)
                .build();
    }

    /**
     * 重新加载
     */
    private void clickReload() {
        mIsReload = true;
        if (mLoadingNum <= MAX_NUM && mListener != null && mListener.reloadLoading()) {
            loadIng();
        }
    }

    private OnCustomLoadingListener mListener;

    public void setListener(OnCustomLoadingListener listener) {
        mListener = listener;
    }

    /**
     * 重新加载
     */
    public interface OnCustomLoadingListener {

        /**
         * 重新加载
         *
         * @return 重新加载
         */
        boolean reloadLoading();

        /**
         * 返回
         */
        void onCancel();

    }

}
