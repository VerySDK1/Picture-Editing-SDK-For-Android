package com.pesdk.uisdk.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;

import java.math.BigDecimal;

public class HorizontalProgressDialog extends Dialog {

    private TextView mTvMessage;
    private ProgressBar mPwProgress;
    private ImageView mIvCancel;
    private TextView mTvProgress;
    private String mStrMessage;
    private boolean mIndeterminate;
    private int mMax = 100, mProgress = 0;
    private onCancelClickListener mCancelListener = null;

    public HorizontalProgressDialog(Context context) {
        super(context, R.style.pesdk_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.pesdk_horizontal_progress_dialog,
                null);
        mTvMessage = (TextView) view.findViewById(R.id.tvMessage);
        mPwProgress = (ProgressBar) view.findViewById(R.id.horiProgress);
        mTvProgress = (TextView) view.findViewById(R.id.tvExportProgress);
        mIvCancel = (ImageView) view.findViewById(R.id.ivCancelExport);
        mIvCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mCancelListener != null) {
                    mCancelListener.onCancel();
                }
            }
        });
        setMessage(mStrMessage);
        setContentView(view);
        setIndeterminate(mIndeterminate);
        if (!mIndeterminate) {
            setProgress(mProgress);
        }
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        this.onWindowAttributesChanged(lp);
    }

    public interface onCancelClickListener {
        void onCancel();
    }

    public void setOnCancelClickListener(onCancelClickListener listener) {
        mCancelListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mCancelListener != null) {
            mCancelListener.onCancel();
            return;
        }
        super.onBackPressed();
    }

    public void setMessage(String strMessage) {
        mStrMessage = strMessage;
        if (null != mTvMessage) {
            mTvMessage.setText(strMessage);
            mTvMessage.setVisibility(TextUtils.isEmpty(strMessage) ? View.GONE
                    : View.VISIBLE);
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        mIndeterminate = indeterminate;
        if (mPwProgress != null) {
            mPwProgress.setIndeterminate(indeterminate);
        }
    }

    public void setMax(int max) {
        mMax = max;
        setProgress(mProgress);
    }

    public int getMax() {
        return mMax;
    }

    public void setProgress(int nProgress) {
        nProgress = Math.min(mMax, nProgress);
        nProgress = Math.max(0, nProgress);
        mProgress = nProgress;
        if (mPwProgress != null) {
            mPwProgress.setMax(mMax);
            mPwProgress.setProgress(mProgress);
            BigDecimal b = new BigDecimal((double) nProgress / mMax * 100);
            double progress = b.setScale(1, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            mTvProgress.setText(progress + "%");
        }
    }
}
