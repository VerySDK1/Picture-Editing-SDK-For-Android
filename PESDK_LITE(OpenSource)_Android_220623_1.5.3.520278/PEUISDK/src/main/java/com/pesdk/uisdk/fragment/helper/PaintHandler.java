package com.pesdk.uisdk.fragment.helper;

import android.widget.SeekBar;

/**
 * 描边
 */
public class PaintHandler {
    private final int MIN = 10, MAX = 80;
    private SeekBar mBar;
    private Callback mCallback;

    public PaintHandler(SeekBar bar, Callback callback) {
        mBar = bar;
        mCallback = callback;
        mCallback.onProgressChanged(getValue(mBar.getProgress(), mBar.getMax()));
    }


    public void init() {
        mBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCallback.onProgressChanged(getValue(seekBar.getProgress(), seekBar.getMax()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mCallback.onStartTrackingTouch();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCallback.onStopTrackingTouch(getValue(seekBar.getProgress(), seekBar.getMax()));
            }
        });

    }

    private int getValue(int progress, int max) {
        return MIN + ((MAX - MIN) * progress / max);
    }


    public static interface Callback {

        void onStartTrackingTouch();

        void onProgressChanged(float value);

        void onStopTrackingTouch(float value);

    }
}
