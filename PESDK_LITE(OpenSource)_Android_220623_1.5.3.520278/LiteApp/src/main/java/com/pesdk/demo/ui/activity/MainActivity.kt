package com.pesdk.demo.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_DEFAULT
import com.pesdk.album.api.AlbumContracts
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.api.ActivityResultContract.ImageEditResultContract
import com.pesdk.api.ChangeLanguageHelper
import com.pesdk.api.SdkEntry
import com.pesdk.demo.R
import com.pesdk.demo.config.Configuration
import com.pesdk.demo.config.MyGlideModule
import com.pesdk.demo.dialog.AlertListViewDialog
import com.vecore.base.lib.utils.StatusBarUtil
import com.vecore.utils.UriUtils
import com.vesdk.camera.entry.CameraConfig.Companion.CAMERA_SUPPORT_PHOTO
import com.vesdk.camera.entry.CameraConfig.Companion.CAMERA_SUPPORT_RECORDER
import com.vesdk.camera.entry.CameraSdkInit
import com.vesdk.common.base.BaseActivity
import com.vesdk.common.bean.Permission
import com.vesdk.common.helper.log
import com.vesdk.common.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_demo_layout.*
import kotlinx.android.synthetic.main.layout_menu_camera.*
import kotlinx.android.synthetic.main.layout_menu_edit.*
import kotlinx.android.synthetic.main.layout_menu_other.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {


    /**
     * 相机
     */
    private var mCameraContracts: ActivityResultLauncher<Void>? = null


    /**
     * 相册
     */
    private var mAlbumContracts: ActivityResultLauncher<Void>? = null


    /**
     * 选择相册 编辑
     */
    private lateinit var mAlbumActivityResultForEdit: ActivityResultLauncher<Void>


    /**
     * 图片编辑
     */
    private lateinit var mEditResultContract: ActivityResultLauncher<String>


    /**
     * 配置
     */
    private var mConfiguration: Configuration = Configuration()

    /**
     * 最后点击id
     */
    private var mLastClickViewId = 0

    /**
     * 跳转
     */
    override fun initRegisterContract() {
        super.initRegisterContract()
        //相机
        CameraSdkInit.getCameraContracts()?.apply {
            mCameraContracts = registerForActivityResult(this) {
                it?.let { pathList ->
                    Log.e(TAG, "pathList:$pathList")
                    if (pathList.size > 0) {
                        mEditResultContract.launch(pathList[0])//进入图片编辑
                    }
                }
            }
        }
        //相册
        AlbumSdkInit.getAlbumContracts()?.apply {
            mAlbumContracts = registerForActivityResult(this) {
                it?.let { pathList ->
                    onToast(pathList.size.toString())
                }
            }
        }

        //相册 编辑
        mAlbumActivityResultForEdit = registerForActivityResult(AlbumContracts()) {
            it?.let {
                if (it.size > 0) {
                    mEditResultContract.launch(it[0])//进入图片编辑
                }
            }
        }
        //图片编辑
        mEditResultContract = registerForActivityResult(ImageEditResultContract()) {
            it?.let { //图片编辑导出成功
                onToastExport(it)
            }
        }
    }

    /**
     * 初始化
     */
    override fun init() {
        StatusBarUtil.setImmersiveStatusBar(this, true)

        //初始化控件
        initView()

        //图片编辑配置
        SdkEntry.getSdkService().initConfiguration(
                mConfiguration.initUIConfiguration(),
                mConfiguration.initExportConfiguration()
        )
    }

    /**
     * 控件
     */
    private fun initView() {
        //语言
        btnLanguage.setOnClickListener {
            AlertListViewDialog(
                    this,
                    resources.getStringArray(R.array.language),
                    { _, which -> onSelected(which) }).show()
        }


        //编辑
        btnImageEdit.setOnClickListener { checkPermission(it.id) }
        //图片编辑草稿箱
        btnDraftEdit.setOnClickListener {
            startActivity(Intent(this@MainActivity, DraftActivity::class.java))
        }


        //拍照
        btnCameraShot.setOnClickListener {
            //相机配置
            val config = mConfiguration.initCameraConfig()
            config.cameraSupport = CAMERA_SUPPORT_PHOTO
            config.hideMusic = true
            CameraSdkInit.setCameraConfig(config)
            mCameraContracts?.launch(null)
        }
        //录像
        btnCameraRecord.setOnClickListener {
            val config = mConfiguration.initCameraConfig()
            config.cameraSupport = CAMERA_SUPPORT_RECORDER
            config.hideMusic = false
            CameraSdkInit.setCameraConfig(config)
            mCameraContracts?.launch(null)
        }


        //相册
        btnAlbum.setOnClickListener {
            val config = mConfiguration.initAlbumConfig()
            config.hideCamera = false
            config.hideMaterial = false
            config.hideCameraSwitch = false
            config.albumSupport = ALBUM_SUPPORT_DEFAULT
            config.limitMinNum = 0
            config.limitMaxNum = 0
            AlbumSdkInit.setAlbumConfig(config)
            mAlbumContracts?.launch(null)
        }
        //缓存
        btnCache.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val size = tvCacheSize.text.toString()
                Glide.get(this@MainActivity).clearDiskCache()
                //提示
                withContext(Dispatchers.Main) {
                    onToast("${getString(R.string.flow_clear_success)}: $size")
                    showCache()
                }
            }
        }
    }

    /**
     * 点击
     */
    private fun onClick(viewId: Int) {
        mLastClickViewId = 0
        AlbumSdkInit.setAlbumConfig(mConfiguration.initAlbumConfig())
        when (viewId) {
            //编辑
            R.id.btnImageEdit -> mAlbumActivityResultForEdit.launch(null)
        }
    }

    /**
     * 显示语言
     */
    private fun showLanguage() {
        val mLanguages = resources.getStringArray(R.array.language)
        when (ChangeLanguageHelper.getAppLanguage(this)) {
            ChangeLanguageHelper.LANGUAGE_CHINESE -> {
                btnLanguage.text = mLanguages[1]
            }
            ChangeLanguageHelper.LANGUAGE_ENGLISH -> {
                btnLanguage.text = mLanguages[2]
            }
            else -> {
                btnLanguage.text = mLanguages[0]
            }
        }
    }

    /**
     * 显示缓存大小
     */
    private fun showCache() {
        //显示了 临时目录、下载目录、模板目录
        lifecycleScope.launch(Dispatchers.IO) {
            val size = MyGlideModule.getGlideCacheSize(this@MainActivity) / 1000000f
            withContext(Dispatchers.Main) {
                tvCacheSize.text = String.format("%.2fM", size)
            }
        }
    }

    /**
     * 导出路径提示
     */
    private fun onToastExport(string: String) {
        val path = UriUtils.getAbsolutePath(this@MainActivity, string)
        onToast(getString(R.string.app_save_to, path))
    }


    /**
     * 检查权限
     */
    private fun checkPermission(viewId: Int) {
        mLastClickViewId = viewId
        lifecycleScope.launch {
            val permissions = mutableListOf<Permission>()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                //29+ 不再申请Write ,适配分区存储
                CommonUtils.getPermission(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )?.let {
                    it.icon = R.drawable.common_ic_permission_storage
                    permissions.add(it)
                }
            }
            //读取
            CommonUtils.getPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.let {
                        it.icon = R.drawable.common_ic_permission_storage
                        permissions.add(it)
                    }
            checkPermission(permissions)
        }
    }

    /**
     * 权限成功
     */
    override fun permissionsSuccess() {
        onClick(mLastClickViewId)
    }

    /**
     * 权限取消
     */
    override fun permissionsCancel() {
        //权限取消
        onToast("授权取消，取消操作！")
    }


    /**
     * 切换语言
     */
    private fun onChangeAppLanguage(newLanguage: Int) {
        val re = ChangeLanguageHelper.getCurrentLanguage()
        if (newLanguage != re) {
            ChangeLanguageHelper.changeAppLanguage(this, newLanguage)
            recreate()
        }
    }

    /**
     * 选中语言
     */
    private fun onSelected(position: Int) {
        when (position) {
            1 -> {
                onChangeAppLanguage(ChangeLanguageHelper.LANGUAGE_CHINESE)
            }
            2 -> {
                onChangeAppLanguage(ChangeLanguageHelper.LANGUAGE_ENGLISH)
            }
            else -> {
                onChangeAppLanguage(ChangeLanguageHelper.LANGUAGE_SYSTEM)
            }
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_demo_layout
    }

    override fun onResume() {
        super.onResume()
        showCache()
        showLanguage()
    }

    override fun onDestroy() {
        super.onDestroy()
        MyGlideModule.getGlideCacheSize(this).log("onDestroy")
        SdkEntry.onExitApp(this)
    }

}