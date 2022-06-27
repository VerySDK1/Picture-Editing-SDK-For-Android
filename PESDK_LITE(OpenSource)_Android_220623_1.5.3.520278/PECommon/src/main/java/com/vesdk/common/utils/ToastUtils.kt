package com.vesdk.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.pesdk.R

/**
 * toast
 */
object ToastUtils {

    /**
     * Toast对象
     */
    private var mToast: Toast? = null

    /**
     * 显示
     */
    fun show(context: Context, message: String, time: Int = Toast.LENGTH_SHORT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(context, message, time).show()
        } else {
            if (mToast == null) {
                mToast = Toast(context.applicationContext)
            }
            @SuppressLint("InflateParams")
            val v = LayoutInflater.from(context.applicationContext).inflate(R.layout.common_toast, null)
            val textView = v.findViewById<TextView>(R.id.tv_toast)
            textView.text = message
            mToast?.run {
                view = v
                // 设置Toast要显示的位置，水平居中并在底部，X轴偏移0个单位，Y轴偏移200个单位，
                setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 100)
                duration = time
                show()
            }
        }
    }

}