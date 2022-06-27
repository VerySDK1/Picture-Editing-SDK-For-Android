package com.vesdk.camera.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class SpecialItemFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(
    context, attrs, defStyle
) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val previewWidth = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY)
        )
    }
}