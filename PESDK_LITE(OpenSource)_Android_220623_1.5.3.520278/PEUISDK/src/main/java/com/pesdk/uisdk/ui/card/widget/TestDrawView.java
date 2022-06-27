package com.pesdk.uisdk.ui.card.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 *
 */
public class TestDrawView extends View {
    public TestDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Callback mCallback;

    public void setCallback1(Callback callback1) {
        mCallback1 = callback1;
    }

    private Callback mCallback1;

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        if (null != mCallback) {
            mCallback.draw(canvas);
        }
        if (null != mCallback1) {
            mCallback1.draw(canvas);
        }
    }


    public static interface Callback {

        void draw(Canvas cv);
    }

}
