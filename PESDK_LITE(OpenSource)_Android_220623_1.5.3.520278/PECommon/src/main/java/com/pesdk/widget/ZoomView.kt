package com.pesdk.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.vecore.base.lib.utils.CoreUtils
import kotlin.math.abs
import kotlin.math.sqrt

class ZoomView : View {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
            context,
            attrs,
            defStyle
    )

    /**
     * 判断手指移动距离，是否画了有效图形的区域
     */
    private val mValidRange = CoreUtils.dpToPixel(3f).toFloat()

    /**
     * listener
     */
    private var mListener: OnFlowTouchListener? = null

    /**
     * 设置listener
     */
    fun setListener(listener: OnFlowTouchListener) {
        mListener = listener
    }

    /**
     * 点击时的位置
     */
    private var mLastMoveX = 0f
    private var mLastMoveY = 0f


    /**
     * 双指缩放开始之间的距离
     */
    private var mStartLen = 0.0
    private var mCenter = PointF()


    private val TAG = "ZoomView"

    /**
     * 单指还是双指
     */
    private var mIsSingleFinger = false;

    /**
     * 双指按住时平移
     */
    private fun onDoublePointMove(event: MotionEvent) {
        var center = getCenter(event) //双指的中心点
        if (!mIsSingleFinger) { //双指切换成单指时
            downPoint(center.x, center.y)
        }
        onMoveImp(center.x, center.y)
    }

    private fun onMoveImp(mMoveX: Float, mMoveY: Float) {
        var x = (mMoveX - mLastMoveX) / width
        var y = (mMoveY - mLastMoveY) / height
        if (x == 0f && y == 0f) {
            return
        }
        mListener?.onMove(x, y)
        mLastMoveX = mMoveX
        mLastMoveY = mMoveY
    }


    private var nLastScale = 1f


    /**
     * 1.双指缩放、双指拖动平移
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (event.pointerCount == 2) {
//            Log.e(TAG, "onTouchEvent:" + action + " >" + event.pointerCount + " >" + ((action and MotionEvent.ACTION_MASK)) + " mStartLen:" + mStartLen + " " + mLastMoveX + "*" + mLastMoveY + " mIsSingleFinger:" + mIsSingleFinger)
            if ((action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                enableSinglePoint = false
                mIsSingleFinger = false
                mLastMoveX = event.x
                mLastMoveY = event.y
                mCenter.set(getCenter(event))
                mStartLen = getDistance(event)
                nLastScale = 1f
            } else if (action == MotionEvent.ACTION_MOVE) { //同时处理缩放和平移
                //移动
                val endLen = getDistance(event)
                if (endLen > mValidRange && mStartLen > 0) {  //支持双指按住同时缩放 平移
                    var newDis = (endLen / mStartLen).toFloat()
                    mListener?.onZoom(newDis, mCenter.x / width, mCenter.y / height) //1.缩放
                    mStartLen = endLen
                    nLastScale = newDis
                    onDoublePointMove(event) //2. 响应双指按住平移
                } else { //双指按住平移
                    onDoublePointMove(event)
                }
            } else {
                if ((action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP) { //取消双指(仍保留一个手指)
                    enableSinglePoint = true
//                    Log.e(TAG, "xxxxxxxxxxxxxxxxxxxxxx  single--->" + event.getX(a) + "*" + event.getY(a) + "  b：" + event.getX(b) + "*" + event.getY(b) + " >" + event.x + "*" + event.y)
                    mIsSingleFinger = false//双指取消手势后应重新设置LastPoint
                }

            }
        } else if (event.pointerCount == 1) { //单指平移
//            Log.e(TAG, "single--->" + action + " " + enableSinglePoint + " >" + mIsSingleFinger + " >" + event.x + "*" + event.y)
            if (enableSinglePoint) {
                if (action == MotionEvent.ACTION_DOWN) {
                    downPoint(event.x, event.y)
                } else if (mIsSingleFinger && action == MotionEvent.ACTION_MOVE) {
                    onMoveImp(event.x, event.y)
                } else if (!mIsSingleFinger && action == MotionEvent.ACTION_MOVE) { //双指切换成单指时
                    downPoint(event.x, event.y)
                }
            }
        }
        return true
    }

    private var enableSinglePoint = false

    private fun downPoint(x: Float, y: Float) {
        mIsSingleFinger = true;
        mLastMoveX = x
        mLastMoveY = y
    }

    /**
     * 计算两点之间的距离 缩放
     */
    private fun getDistance(event: MotionEvent): Double {
        val pointerCount = event.pointerCount
        val pointerId = event.getPointerId(0)
        val pointerId1 = event.getPointerId(1)
        if (pointerId > pointerCount || pointerId1 > pointerCount) {
            return mStartLen
        }
        val xLen = abs(
                event.getX(pointerId).toInt() - event.getX(pointerId1)
        )
        val yLen = abs(
                event.getY(pointerId).toInt() - event.getY(pointerId1)
        )
        return sqrt((xLen * xLen + yLen * yLen).toDouble())
    }

    /**
     * 计算两点之间的距离 缩放
     */
    private fun getCenter(event: MotionEvent): PointF {
        val pointerCount = event.pointerCount
        val a = event.getPointerId(0)
        val b = event.getPointerId(1)
        if (a > pointerCount || b > pointerCount) {
            return PointF(0f, 0f)
        }
        val centerX = (event.getX(a) + event.getX(b)) / 2
        val centerY = (event.getY(a) + event.getY(b)) / 2
        return PointF(centerX, centerY)
    }

    interface OnFlowTouchListener {

        /**
         * 移动 相对上一次的偏移 0~1
         */
        fun onMove(x: Float, y: Float)

        /**
         * 缩放 0~1
         */
        fun onZoom(zoom: Float, x: Float, y: Float)

    }

}