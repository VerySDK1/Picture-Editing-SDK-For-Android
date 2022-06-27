package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.fragment.callback.IFragmentMenuCallBack;
import com.pesdk.uisdk.widget.SysAlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
public abstract class BaseFragment extends AbsBaseFragment {


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IFragmentMenuCallBack) {
            mMenuCallBack = (IFragmentMenuCallBack) context;
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View view1 = $(R.id.ivCancel);
        if (null != view1) {
            view1.setOnClickListener(v -> onCancelClick());
        }
        view1 = $(R.id.ivSure);
        if (null != view1) {
            view1.setOnClickListener(v -> onSureClick());
        }
    }


    public abstract void onCancelClick();

    public abstract void onSureClick();

    protected IFragmentMenuCallBack mMenuCallBack;

    /**
     * 取消时，弹窗警告
     */
    protected final void showAlert(AlertCallback callback) {
        SysAlertDialog.createAlertDialog(getContext(),
                mContext.getString(R.string.pesdk_dialog_tips),
                mContext.getString(R.string.pesdk_cancel_all_changed),
                mContext.getString(R.string.pesdk_cancel),
                (dialog, which) -> {
                    callback.cancel();
                }, mContext.getString(R.string.pesdk_sure),
                (dialog, which) -> {
                    callback.sure();
                }, false, null).show();
    }

    public static interface AlertCallback {

        void cancel();

        void sure();
    }

}
