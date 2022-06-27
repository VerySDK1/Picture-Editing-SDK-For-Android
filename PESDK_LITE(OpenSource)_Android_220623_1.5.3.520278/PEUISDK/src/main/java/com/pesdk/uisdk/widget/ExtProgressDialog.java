package com.pesdk.uisdk.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.pesdk.uisdk.R;

public class ExtProgressDialog extends Dialog {

    private TextView mTvMessage;
    private CircleProgressBar mPwProgress;
    private String mStrMessage;
    private boolean mIndeterminate;
    private int mMax = 100, mProgress = 0;

    public ExtProgressDialog(Context context) {
        super(context, R.style.pesdk_dialog);
    }

    public ExtProgressDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.pesdk_progress_dialog, null);
        mTvMessage = (TextView) view.findViewById(R.id.tvMessage);
        mPwProgress = (CircleProgressBar) view.findViewById(R.id.pbProgress);
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
        }
    }
}
