package com.pesdk.album.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.uisdk.ui.activity.AlbumActivity
import com.pesdk.bean.template.RReplaceMedia
import com.vecore.models.PEImageObject

/**
 * 选择模板需要替换的素材
 */
open class AlbumTemplateContract : ActivityResultContract<ArrayList<RReplaceMedia>, ArrayList<PEImageObject>?>() {

    companion object {

        /**
         * 返回选中路径
         */
        const val INTENT_IMAGE_LIST = "_image_list"

    }

    override fun createIntent(context: Context, input: ArrayList<RReplaceMedia>?): Intent {
        return AlbumActivity.createAlbumIntent(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<PEImageObject>? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getParcelableArrayListExtra(INTENT_IMAGE_LIST)
        } else {
            null
        }

    }

}