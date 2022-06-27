package com.pesdk.uisdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 区域正方形
 */
public class SquareFrameLayout extends FrameLayout {

    public SquareFrameLayout(Context context) {
        this(context, null, 0);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY));
    }

}
