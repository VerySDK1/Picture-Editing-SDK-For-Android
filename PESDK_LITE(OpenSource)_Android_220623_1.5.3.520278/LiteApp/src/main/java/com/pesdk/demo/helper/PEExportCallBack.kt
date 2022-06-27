package com.pesdk.demo.helper

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.api.SdkEntry
import com.pesdk.api.callback.IExportCallBack
import com.pesdk.bean.template.RReplaceMedia
import com.pesdk.demo.config.Configuration
import com.pesdk.demo.dialog.SizeDialog
import com.vecore.models.PEImageObject

/**
 *图片编辑sdk,导出前的回调
 */
class PEExportCallBack : IExportCallBack {

    /**
     * 配置
     */
    private var mConfiguration: Configuration = Configuration()

    /**
     * 广告
     */
    private var mExportSize: SizeDialog? = null

    /**
     * 相册
     */
    override fun onActionAlbum(): ActivityResultContract<Void, ArrayList<String>>? {
        AlbumSdkInit.setAlbumConfig(mConfiguration.initAlbumConfig())
        return AlbumSdkInit.getAlbumContracts()
    }

    /**
     * 准备生成模板
     */
    override fun onActionMakeTemplate(): ActivityResultContract<Void, Boolean>? {
        return null
    }

    /**
     * 模板
     */
    override fun onActionTemplateAlbum(): ActivityResultContract<ArrayList<RReplaceMedia>, ArrayList<PEImageObject>?>? {
        return null
    }

    /**
     * 导出
     */
    override fun onExport(context: Context?, type: Int) {
        context?.let {
            mExportSize = SizeDialog.Builder(it)
                    .setListener(object : SizeDialog.OnClickSizeListener {

                        override fun onMinSide(side: Int) {
                            mExportSize?.dismiss()
                            SdkEntry.onContinueExport(context, true, side)
                        }

                        override fun onCancel() {
                            mExportSize?.dismiss()
                        }
                    })
                    .create()
            mExportSize?.show()
        }
    }

}