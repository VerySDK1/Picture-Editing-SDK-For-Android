package com.pesdk.uisdk.base;

import com.pesdk.ui.dialog.LoadingDialog;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.exception.InvalidStateException;

/**
 * 统一管理渲染流程
 */
public abstract class BasePlayerActivity extends BaseActivity {
    protected VirtualImageView mVirtualImageView;
    protected VirtualImage mVirtualImage;

    /**
     * 加载数据
     */
    public abstract void reload(VirtualImage virtualImage);

    /**
     * 重新加载数据并渲染
     */
    public void build() {
        reload(mVirtualImage);
        try {
            mVirtualImageView.enableViewBGHolder(true);
            mVirtualImage.build(mVirtualImageView);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVirtualImageView != null) {
            mVirtualImageView.cleanUp();
            mVirtualImageView = null;
        }
        if (null != mVirtualImage) {
            mVirtualImage.release();
            mVirtualImage = null;
        }
    }

    /**
     * 进度
     */
    private LoadingDialog mLoadingDialog;

    /**
     * 显示loading
     */
    public void showLoading(String progress) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog.Builder(this)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .create();
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
        mLoadingDialog.setMessage(progress);
    }

    /**
     * 隐藏loading
     */
    public void hideLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
