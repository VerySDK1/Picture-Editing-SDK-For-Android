package com.pesdk.uisdk.fragment.helper;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.Utils;

import androidx.annotation.FloatRange;

/**
 * 景深程度
 */
public class DepthBarHandler {

    private SeekBar mSBar;
    private TextView tvValue;
    private Callback mCallback;
    private static final String TAG = "DepthBarHandler";

    public DepthBarHandler(View root, Callback callback) {
        mSBar = Utils.$(root, R.id.sbar);
        tvValue = Utils.$(root, R.id.tvValue);
        mCallback = callback;
        mSBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCallback.progress(progress / (0.0f + mSBar.getMax()));
                    tvValue.setText(progress + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setValue();
                mCallback.onStopTrackingTouch(seekBar.getProgress() / (0.0f + mSBar.getMax()));
            }
        });
    }

    /**
     * 设置滤镜程度
     */
    public void setFactor(@FloatRange(from = 0f, to = 1f) float progress) {
        mSBar.setProgress((int) (progress * mSBar.getMax()));
        setValue();
    }


    public void enableSeek(boolean enable) {
        mSBar.setEnabled(enable);
    }

    public int getMax() {
        return mSBar.getMax();
    }

    @FloatRange(from = 0f, to = 1f)
    public float getValue() {
        if (null == mSBar) {
            return 0;
        }
        return (mSBar.getProgress() + 0.0f) / mSBar.getMax();
    }


    private void setValue() {
        tvValue.setText(mSBar.getProgress() + "");
    }

    public static interface Callback {

        /**
         * 调节程度
         */
        void progress(@FloatRange(from = 0f, to = 1f) float factor);

        /**
         * 结束手势
         */
        void onStopTrackingTouch(@FloatRange(from = 0f, to = 1f) float factor);

    }

}
