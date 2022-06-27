package com.vesdk.engine.bean

import android.graphics.RectF
import androidx.annotation.Keep

@Keep
data class DetectedObject(
    val box: RectF,
    val id: Int,
    val labels: MutableList<DetectedLabel>
)

@Keep
data class DetectedLabel(
    val description: String,
    val index: Int,
    val confidence: Float,
)
