package com.pesdk.widget.loading;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.pesdk.R;
import com.pesdk.widget.loading.render.LoadingDrawable;
import com.pesdk.widget.loading.render.LoadingRenderer;
import com.pesdk.widget.loading.render.LoadingRendererFactory;
import com.vecore.base.lib.utils.CoreUtils;

@SuppressLint("AppCompatCustomView")
public class LoadingView extends ImageView {

    private LoadingDrawable mLoadingDrawable;

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        try {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.pecom_LoadingView);
            int loadingRendererId = ta.getInt(R.styleable.pecom_LoadingView_pecom_loading_renderer, 0);
            int mBgColor = ta.getColor(R.styleable.pecom_LoadingView_pecom_lv_bg_color, Color.TRANSPARENT);
            float mRound = ta.getFloat(R.styleable.pecom_LoadingView_pecom_lv_round, CoreUtils.dip2px(context, 5));
            LoadingRenderer loadingRenderer = LoadingRendererFactory.createLoadingRenderer(context, loadingRendererId);
            setLoadingRenderer(loadingRenderer);
            setColor(mBgColor);
            setRound(mRound);
            ta.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoadingRenderer(LoadingRenderer loadingRenderer) {
        if (mLoadingDrawable != null) {
            int color = mLoadingDrawable.getBackgroundColor();
            float round = mLoadingDrawable.getRound();
            mLoadingDrawable = new LoadingDrawable(loadingRenderer);
            setImageDrawable(mLoadingDrawable);
            setColor(color);
            setRound(round);
        } else {
            mLoadingDrawable = new LoadingDrawable(loadingRenderer);
            setImageDrawable(mLoadingDrawable);
        }
    }

    public void setColor(int backgroundColor) {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.setColor(backgroundColor);
        }
    }

    public void setRound(float round) {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.setRound(round);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        final boolean visible = visibility == VISIBLE && getVisibility() == VISIBLE;
        if (visible) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    private void startAnimation() {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.start();
        }
    }

    private void stopAnimation() {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.stop();
        }
    }
}
