package com.vesdk.camera.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.vesdk.camera.R

class OutsideFrameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var mBorderPaint: Paint = Paint()

    /**
     * 边框宽度
     */
    private var mBorderWidth = 0

    /**
     * 边框圆弧
     */
    private var mBorderRoundRadius = 0

    /**
     * 边框颜色
     */
    private var mBorderColor = 0

    /**
     * 选中颜色
     */
    private var mBorderSelectColor = 0

    /**
     * 区域
     */
    private val mRectF: RectF = RectF()

    init {
        mBorderPaint.isAntiAlias = true
        mBorderPaint.style = Paint.Style.STROKE

        mBorderWidth = 2
        mBorderRoundRadius = 6
        mBorderColor = ContextCompat.getColor(context, R.color.camera_border)
        mBorderSelectColor = ContextCompat.getColor(context, R.color.white)
    }

    fun setBorder(
        width: Int = 2,
        radius: Int = 6,
        color: Int = Color.TRANSPARENT,
        selectColor: Int = ContextCompat.getColor(context, R.color.white)
    ) {
        mBorderWidth = width
        mBorderRoundRadius = radius
        mBorderColor = color
        mBorderSelectColor = selectColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mBorderPaint.color = if (isSelected) mBorderSelectColor else mBorderColor
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()
        //绘制区域
        val widthHalf = mBorderWidth / 2f
        mRectF.set(widthHalf, widthHalf, width.toFloat() - widthHalf, height.toFloat() - widthHalf)
        canvas.drawRoundRect(
            mRectF,
            mBorderRoundRadius.toFloat(),
            mBorderRoundRadius.toFloat(),
            mBorderPaint
        )
    }

}