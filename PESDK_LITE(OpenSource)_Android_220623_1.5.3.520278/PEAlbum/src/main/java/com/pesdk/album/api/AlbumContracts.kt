package com.pesdk.album.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.uisdk.ui.activity.AlbumActivity

/**
 * 相册
 */
open class AlbumContracts : ActivityResultContract<Void, ArrayList<String>>() {

    companion object {

        /**
         * 返回选中路径
         */
        const val INTENT_MEDIA_LIST = "media_list"

    }

    override fun createIntent(context: Context, input: Void?): Intent {
        return AlbumActivity.newInstance(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<String>? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getStringArrayListExtra(INTENT_MEDIA_LIST)
        } else {
            null
        }
    }

}