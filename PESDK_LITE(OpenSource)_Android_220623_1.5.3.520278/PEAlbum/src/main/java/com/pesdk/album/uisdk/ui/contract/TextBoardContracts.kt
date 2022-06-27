package com.pesdk.album.uisdk.ui.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.uisdk.ui.activity.TextBoardActivity

/**
 * 文字
 */
class TextBoardContracts : ActivityResultContract<Void, String>() {

    companion object {

        /**
         * 文字板返回的数据
         */
        const val INTENT_TEXT_BOARD = "text_board"

    }

    override fun createIntent(context: Context, input: Void?): Intent {
        return TextBoardActivity.newInstance(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return intent?.getStringExtra(INTENT_TEXT_BOARD)
    }

}