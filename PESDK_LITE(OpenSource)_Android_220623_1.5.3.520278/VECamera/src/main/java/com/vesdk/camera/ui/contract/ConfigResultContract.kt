package com.vesdk.camera.ui.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.vesdk.camera.ui.activity.RecorderConfigActivity

/**
 * 相机输出配置
 */
class ConfigResultContract : ActivityResultContract<Boolean, Int>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return resultCode
    }

    override fun createIntent(context: Context, input: Boolean): Intent {
        return RecorderConfigActivity.newInstance(context, input)
    }

}