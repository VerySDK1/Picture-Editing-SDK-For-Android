package com.pesdk.album.uisdk.widget.photoview

import android.annotation.TargetApi
import android.os.Build
import android.view.View

internal object Compat {

    private const val SIXTY_FPS_INTERVAL = 1000 / 60

    @JvmStatic
    fun postOnAnimation(view: View, runnable: Runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postOnAnimationJellyBean(view, runnable)
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL.toLong())
        }
    }

    @TargetApi(16)
    private fun postOnAnimationJellyBean(view: View, runnable: Runnable) {
        view.postOnAnimation(runnable)
    }

}