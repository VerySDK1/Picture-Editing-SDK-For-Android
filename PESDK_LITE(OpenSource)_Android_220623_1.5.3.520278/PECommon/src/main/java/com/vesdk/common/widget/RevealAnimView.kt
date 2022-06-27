package com.vesdk.common.widget

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.vesdk.common.utils.CommonUtils
import kotlinx.coroutines.*
import kotlin.math.hypot
import kotlin.math.min

/**
 * 揭露动画
 */
class RevealAnimView : View, Animator.AnimatorListener, LifecycleObserver {

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
    }

    /**
     * 上下文
     */
    private var mContext: Context

    /**
     * 协程
     */
    private val mScope = CoroutineScope(Job())

    /**
     * 动画
     */
    private var mAnim: Animator? = null

    /**
     * 回调
     */
    private var mListener: Animator.AnimatorListener? = null

    /**
     * 开始动画
     */
    fun startAnim(centerX: Int, centerY: Int, startRadius: Float, endRadius: Float,
                  listener: Animator.AnimatorListener?,
                  duration: Long = 600L, color: Int = Color.TRANSPARENT) {
        if (color != Color.TRANSPARENT) {
            setBackgroundColor(color)
        }
        mListener = listener
        //停止
        stopAnim()
        mAnim = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, startRadius, endRadius)
        mAnim?.let {
            it.duration = duration
            it.addListener(this)
            visibility = VISIBLE
            it.start()
        }
    }

    /**
     * 开始动画
     */
    fun startAnim(view: View, open: Boolean, listener: Animator.AnimatorListener?,
                  duration: Long = 600L, color: Int = Color.TRANSPARENT) {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val centerX: Int = location[0] + view.width / 2
        val centerY: Int = location[1] + view.height / 2
        val minRadius = min(view.width / 2.0f, view.height / 2.0f)
        var maxRadius = hypot(width.toDouble(), height.toDouble()).toFloat()
        if (maxRadius < 0.001f) {
            maxRadius = hypot(CommonUtils.getWidth(mContext).toDouble(), CommonUtils.getHeight(mContext).toDouble()).toFloat()
        }
        if (open) {
            startAnim(centerX, centerY, minRadius, maxRadius, listener, duration, color)
        } else {
            startAnim(centerX, centerY, maxRadius, minRadius, listener, duration, color)
        }
    }

    /**
     * 结束动画
     */
    fun stopAnim() {
        mAnim?.end()
        mAnim?.removeListener(this)
        mAnim = null
        visibility = GONE
    }

    override fun onAnimationStart(animation: Animator?) {
        mListener?.onAnimationStart(animation)
    }

    override fun onAnimationEnd(animation: Animator?) {
        mListener?.onAnimationEnd(animation)
        mScope.launch {
            delay(300)
            withContext(Dispatchers.Main) {
                stopAnim()
            }
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
        mListener?.onAnimationCancel(animation)
    }

    override fun onAnimationRepeat(animation: Animator?) {
        mListener?.onAnimationRepeat(animation)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mScope.cancel()
    }

    init {
        setBackgroundColor(Color.YELLOW)
        visibility = GONE
    }

}