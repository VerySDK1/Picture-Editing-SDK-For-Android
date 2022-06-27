package com.pesdk.album.uisdk.widget

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class RecyclerViewCornerRadius(recyclerView: RecyclerView) : ItemDecoration() {

    private var rectF: RectF? = null
    private var path: Path? = null
    private var topLeftRadius = 0
    private var topRightRadius = 0
    private var bottomLeftRadius = 0
    private var bottomRightRadius = 0

    fun setCornerRadius(radius: Int) {
        topLeftRadius = radius
        topRightRadius = radius
        bottomLeftRadius = radius
        bottomRightRadius = radius
    }

    fun setCornerRadius(
        topLeftRadius: Int,
        topRightRadius: Int,
        bottomLeftRadius: Int,
        bottomRightRadius: Int
    ) {
        this.topLeftRadius = topLeftRadius
        this.topRightRadius = topRightRadius
        this.bottomLeftRadius = bottomLeftRadius
        this.bottomRightRadius = bottomRightRadius
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (rectF!!.width() == 0f) {
            rectF!![0f, 0f, parent.width.toFloat()] = parent.height.toFloat()
            path!!.reset()
            path!!.addRoundRect(
                rectF!!, floatArrayOf(
                    topLeftRadius.toFloat(), topLeftRadius.toFloat(),
                    topRightRadius.toFloat(), topRightRadius.toFloat(),
                    bottomLeftRadius.toFloat(), bottomLeftRadius.toFloat(),
                    bottomRightRadius.toFloat(), bottomRightRadius
                        .toFloat()
                ), Path.Direction.CCW
            )
        }
        c.drawFilter = PaintFlagsDrawFilter(
            0, Paint.ANTI_ALIAS_FLAG
                    or Paint.FILTER_BITMAP_FLAG
        )
        c.clipRect(rectF!!)
        c.clipPath(path!!)
    }

    init {
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            rectF = RectF(
                0f, 0f, recyclerView.measuredWidth.toFloat(),
                recyclerView.measuredHeight.toFloat()
            )
            path = Path()
            path?.run {
                reset()
                addRoundRect(
                    rectF!!, floatArrayOf(
                        topLeftRadius.toFloat(), topLeftRadius.toFloat(),
                        topRightRadius.toFloat(), topRightRadius.toFloat(),
                        bottomLeftRadius.toFloat(), bottomLeftRadius.toFloat(),
                        bottomRightRadius.toFloat(), bottomRightRadius
                            .toFloat()
                    ), Path.Direction.CCW
                )
            }
            //recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}