package com.vesdk.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.pesdk.R

/**
 * 颜色面板
 */
class ColorPickerView : View {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * 间距
     */
    private val PADDING = 40f
    private val PADDING_TOP = 5f
    private val PADDING_LR = 20f
    private val DROP_HEIGHT = 70f

    /**
     * hue的宽度
     */
    private val HUE_WIDTH = 10f

    /**
     * 笔
     */
    private val mSatValPaint = Paint()
    private val mSVPaint = Paint()
    private val mHuePaint = Paint()
    private val mBorderPaint = Paint()

    /**
     * 选中颜色
     */
    private val mXfermodePaint = Paint()
    private var mDropBitmap: Bitmap? = null
    private var mDrop2Bitmap: Bitmap? = null
    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    /**
     * 颜色缓存
     */
    private var mSatValBackgroundCache: BitmapCache? = null
    private var mHueBackgroundCache: BitmapCache? = null

    /**
     * 渲染
     */
    private var mValShader: Shader? = null
    private var mSatShader: Shader? = null

    /**
     * 宽高
     */
    private val mSatValRectF = RectF()
    private val mHueRectF = RectF()
    private val mDropRectF = RectF()

    /**
     * 颜色
     */
    private var mAlpha = 0xff
    private var mHue = 180f
    private var mSat = 0.5f
    private var mVal = 0.5f

    /**
     * 回调
     */
    var listener: OnColorChangedListener? = null

    /**
     * 颜色
     */
    fun setColor(color: Int) {
        mAlpha = Color.alpha(color)
        val red = Color.red(color)
        val blue = Color.blue(color)
        val green = Color.green(color)
        val hsv = FloatArray(3)
        Color.RGBToHSV(red, green, blue, hsv)

        mAlpha = alpha.toInt()
        mHue = hsv[0]
        mSat = hsv[1]
        mVal = hsv[2]

        invalidate()
    }


    /**
     * 初始化
     */
    private fun init() {
        with(mSVPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
            color = Color.WHITE
        }
        with(mHuePaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        with(mBorderPaint) {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = ContextCompat.getColor(context, R.color.transparent_black20)
        }
        with(mXfermodePaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        mDropBitmap = BitmapFactory.decodeResource(resources, R.drawable.common_ic_drop)
        mDrop2Bitmap = BitmapFactory.decodeResource(resources, R.drawable.common_ic_drop2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSatValPanel(canvas)
        drawHuePanel(canvas)
    }

    private fun drawSatValPanel(canvas: Canvas) {
        //宽高
        if (mSatValRectF.isEmpty) {
            mSatValRectF.set(
                PADDING_LR,
                PADDING_TOP + DROP_HEIGHT,
                width - PADDING - HUE_WIDTH - PADDING_LR,
                height.toFloat()
            )
        }
        //黑色渐变
        if (mValShader == null) {
            mValShader = LinearGradient(
                mSatValRectF.left, mSatValRectF.top, mSatValRectF.left, mSatValRectF.bottom,
                -0x1, -0x1000000, Shader.TileMode.CLAMP
            )
        }
        //Bitmap
        if (mSatValBackgroundCache?.value != mHue) {
            if (mSatValBackgroundCache == null) {
                mSatValBackgroundCache = BitmapCache()
            }
            mSatValBackgroundCache?.let { cache ->
                if (cache.bitmap == null) {
                    cache.bitmap = Bitmap
                        .createBitmap(
                            mSatValRectF.width().toInt(), mSatValRectF.height().toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                }
                cache.bitmap?.let { bitmap ->
                    if (cache.canvas == null) {
                        cache.canvas = Canvas(bitmap)
                    }
                    val rgb = Color.HSVToColor(floatArrayOf(mHue, 1f, 1f))
                    mSatShader = LinearGradient(
                        mSatValRectF.left, mSatValRectF.top, mSatValRectF.right, mSatValRectF.top,
                        -0x1, rgb, Shader.TileMode.CLAMP
                    )
                    if (mValShader != null && mSatShader != null) {
                        val mShader = ComposeShader(
                            mValShader!!, mSatShader!!, PorterDuff.Mode.MULTIPLY
                        )
                        mSatValPaint.shader = mShader
                    }
                    cache.canvas?.drawRect(
                        0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), mSatValPaint
                    )
                    cache.value = mHue
                }
            }
        }
        //绘制
        mSatValBackgroundCache?.bitmap?.let {
            canvas.drawBitmap(it, null, mSatValRectF, null)
            canvas.drawRoundRect(mSatValRectF, 2f, 2f, mBorderPaint)
        }
        //当前位置
        val p = satValToPoint(mSat, mVal)
        val value = 16f
        val value2 = 6f
        canvas.drawLine(p.x - value, p.y, p.x - value2, p.y, mSVPaint)
        canvas.drawLine(p.x + value2, p.y, p.x + value, p.y, mSVPaint)
        canvas.drawLine(p.x, p.y - value, p.x, p.y - value2, mSVPaint)
        canvas.drawLine(p.x, p.y + value2, p.x, p.y + value, mSVPaint)
        mDropRectF.set(
            p.x - DROP_HEIGHT / 2,
            p.y - DROP_HEIGHT - value - value2,
            p.x + DROP_HEIGHT / 2,
            p.y - value - value2
        )
        //离屏绘制
        mDropBitmap?.let {
            mXfermodePaint.color = Color.HSVToColor(mAlpha, floatArrayOf(mHue, mSat, mVal))
            val layerId =
                canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), mXfermodePaint)
            canvas.drawRect(mDropRectF, mXfermodePaint)
            mXfermodePaint.xfermode = mXfermode
            canvas.drawBitmap(it, null, mDropRectF, mXfermodePaint)
            mXfermodePaint.xfermode = null
            mDrop2Bitmap?.let { drop ->
                canvas.drawBitmap(drop, null, mDropRectF, mXfermodePaint)
            }
            canvas.restoreToCount(layerId)
        }
    }

    private fun drawHuePanel(canvas: Canvas) {
        //宽高
        if (mHueRectF.isEmpty) {
            mHueRectF.set(
                width - HUE_WIDTH - PADDING_LR,
                PADDING_TOP + DROP_HEIGHT,
                width - PADDING_LR,
                height.toFloat()
            )
        }
        //bitmap
        if (mHueBackgroundCache == null) {
            mHueBackgroundCache = BitmapCache()
            mHueBackgroundCache?.let { cache ->
                val bitmap = Bitmap.createBitmap(
                    mHueRectF.width().toInt(), mHueRectF.height().toInt(),
                    Bitmap.Config.ARGB_8888
                )
                val bitmapCanvas = Canvas(bitmap)
                cache.bitmap = bitmap
                cache.canvas = bitmapCanvas
                val hueColors = IntArray((mHueRectF.height() + 0.5f).toInt())
                var h = 360f
                for (i in hueColors.indices) {
                    hueColors[i] = Color.HSVToColor(floatArrayOf(h, 1f, 1f))
                    h -= 360f / hueColors.size
                }
                val linePaint = Paint()
                linePaint.strokeWidth = 0f
                for (i in hueColors.indices) {
                    linePaint.color = hueColors[i]
                    bitmapCanvas.drawLine(
                        0f,
                        i.toFloat(),
                        bitmap.height.toFloat(),
                        i.toFloat(),
                        linePaint
                    )
                }
            }
        }
        //绘制
        mHueBackgroundCache?.bitmap?.let {
            canvas.drawBitmap(it, null, mHueRectF, null)
        }
        //当前位置
        val p = hueToPoint(mHue)
        mHuePaint.color = Color.HSVToColor(mAlpha, floatArrayOf(mHue, 1f, 1f))
        canvas.drawCircle(p.x, p.y, HUE_WIDTH * 2, mHuePaint)
    }

    /**
     * 位置
     */
    private fun hueToPoint(hue: Float): PointF {
        val h = mHueRectF.height()
        val p = PointF()
        p.y = h - hue * h / 360f + mHueRectF.top
        p.x = mHueRectF.centerX()
        return p
    }

    /**
     * sat Val 位置
     */
    private fun satValToPoint(s: Float, v: Float): PointF {
        val height = mSatValRectF.height()
        val width = mSatValRectF.width()
        val p = PointF()
        p.x = s * width + mSatValRectF.left
        p.y = (1f - v) * height + mSatValRectF.top
        return p
    }

    private fun pointToSatVal(x: Float, y: Float): FloatArray {
        val result = FloatArray(2)
        val width = mSatValRectF.width()
        val height = mSatValRectF.height()
        val valueX = if (x < mSatValRectF.left) {
            0f
        } else if (x > mSatValRectF.right) {
            width
        } else {
            x - mSatValRectF.left
        }
        val valueY = if (y < mSatValRectF.top) {
            0f
        } else if (y > mSatValRectF.bottom) {
            height
        } else {
            y - mSatValRectF.top
        }
        result[0] = 1f / width * valueX
        result[1] = 1f - 1f / height * valueY
        return result
    }

    private fun pointToHue(y: Float): Float {
        val height = mHueRectF.height()
        val value = if (y < mHueRectF.top) {
            0f
        } else if (y > mHueRectF.bottom) {
            height
        } else {
            y - mHueRectF.top
        }
        return 360f - value * 360f / height
    }

    private val mDownPoint = PointF()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val update = when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownPoint.set(event.x, event.y)
                moveTrackers(event)
            }
            MotionEvent.ACTION_MOVE -> {
                moveTrackers(event)
            }
            MotionEvent.ACTION_UP -> {
                moveTrackers(event)
            }
            MotionEvent.ACTION_CANCEL -> {
                moveTrackers(event)
            }
            else -> false
        }
        if (update) {
            listener?.onColorChange(Color.HSVToColor(mAlpha, floatArrayOf(mHue, mSat, mVal)))
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun moveTrackers(event: MotionEvent): Boolean {
        var update = false
        val startX = mDownPoint.x
        val startY = mDownPoint.y
        if (startX >= mHueRectF.left - 30 && startX <= mHueRectF.right + 30
            && startY >= mHueRectF.top && startY <= mHueRectF.right
        ) {
            mHue = pointToHue(event.y)
            update = true
        } else if (mSatValRectF.contains(startX, startY)) {
            val result = pointToSatVal(event.x, event.y)
            mSat = result[0]
            mVal = result[1]
            update = true
        }
        return update
    }

    /**
     * 颜色
     */
    interface OnColorChangedListener {

        /**
         * 颜色
         * @param color 颜色
         */
        fun onColorChange(color: Int)

    }

    private class BitmapCache {
        var canvas: Canvas? = null
        var bitmap: Bitmap? = null
        var value = 0f
    }
}