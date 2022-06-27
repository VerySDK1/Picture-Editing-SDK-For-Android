package com.pesdk.uisdk.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.widget.ExtSeekBar2;
import com.pesdk.uisdk.widget.SysAlertDialog;

import androidx.annotation.Nullable;

/**
 * 透明度
 */
public class AlphaFragment extends BaseFragment {

    public static AlphaFragment newInstance() {
        Bundle args = new Bundle();
        AlphaFragment fragment = new AlphaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private float mOldAlpha, mNewAlpha;
    private ExtSeekBar2 mSbAlpha;
    private boolean mHideCbAll = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_alpha, container, false);
        mSbAlpha = $(R.id.sb_alpha);
        mSbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mNewAlpha = progress / 100.0f;
                if (fromUser && mListener != null) {
                    mListener.onChange(mNewAlpha);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_subtitle_alpha);
        return mRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSbAlpha.setProgress((int) (mOldAlpha * 100));
    }

    public void setOldAlpha(float alpha) {
        this.mOldAlpha = alpha;
        if (mSbAlpha != null) {
            mSbAlpha.setProgress((int) (alpha * mSbAlpha.getMax()));
        }
    }

    /**
     * 提示是否放弃保存
     */
    public void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.pesdk_dialog_tips),
                mContext.getString(R.string.pesdk_cancel_all_changed),
                mContext.getString(R.string.pesdk_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.pesdk_sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mNewAlpha = 0;
                        onBackPressed();
                    }
                }, false, null).show();
    }

    @Override
    public int onBackPressed() {
        if (mNewAlpha != 0 && mOldAlpha != mNewAlpha) {
            onShowAlert();
        } else {
            if (mListener != null) {
                mListener.onBack(mOldAlpha);
            }
        }
        return super.onBackPressed();
    }

    public void setHideCbAll(boolean hideCbAll) {
        mHideCbAll = hideCbAll;
    }

    private OnAlphaListener mListener;

    public void setListener(OnAlphaListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCancelClick() {
        onBackPressed();
    }

    @Override
    public void onSureClick() {
        if (mListener != null) {
            mListener.onSure(mNewAlpha, false);
        }
    }

    public interface OnAlphaListener {

        /**
         * 确认
         */
        void onSure(float alpha, boolean all);

        /**
         * 返回 取消
         */
        void onBack(float old);

        /**
         * 改变
         */
        void onChange(float change);

    }

}
