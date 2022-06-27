package com.pesdk.uisdk.fragment.helper;

import android.widget.SeekBar;

import com.pesdk.uisdk.widget.BaseSizeView;

/**
 * 描边
 */
public class StrokeHandler {
    private SeekBar mBar;
    private BaseSizeView mDoodleView;
    private PaintHandler paintHandler;

    public StrokeHandler(SeekBar bar, BaseSizeView doodleView) {
        mBar = bar;
        mDoodleView = doodleView;

        paintHandler = new PaintHandler(mBar, new PaintHandler.Callback() {
            @Override
            public void onStartTrackingTouch() {
                mDoodleView.beginPaintSizeMode();
            }

            @Override
            public void onProgressChanged(float value) {
                mDoodleView.setPaintWidth(value);
            }

            @Override
            public void onStopTrackingTouch(float value) {
                mDoodleView.setPaintWidth(value);
                mDoodleView.endPaintSizeMode();
            }
        });
    }


    public void init() {
        paintHandler.init();
    }


}
