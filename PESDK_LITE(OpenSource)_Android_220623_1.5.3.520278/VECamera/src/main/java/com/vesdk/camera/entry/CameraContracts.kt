package com.vesdk.camera.entry

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.vesdk.camera.ui.activity.RecorderActivity

/**
 * 相册
 */
class CameraContracts : ActivityResultContract<Void, ArrayList<String>>() {

    companion object {

        /**
         * 返回选中路径
         */
        const val RECORDER_MEDIA_LIST = "media_list"

    }

    override fun createIntent(context: Context, input: Void?): Intent {
        return RecorderActivity.newInstance(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<String>? {
        return intent?.getStringArrayListExtra(RECORDER_MEDIA_LIST)
    }

}