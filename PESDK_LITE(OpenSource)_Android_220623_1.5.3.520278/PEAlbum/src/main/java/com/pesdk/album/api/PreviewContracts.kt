package com.pesdk.album.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.api.bean.PreviewInfo
import com.pesdk.album.uisdk.ui.activity.PreviewActivity

/**
 * 预览单个文件
 */
open class PreviewContracts : ActivityResultContract<PreviewInfo, PreviewInfo>() {

    companion object {
        const val INTENT_PREVIEW = "preview"
    }

    override fun createIntent(context: Context, input: PreviewInfo): Intent {
        return PreviewActivity.newInstance(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PreviewInfo? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getParcelableExtra(INTENT_PREVIEW)
        } else null
    }

}