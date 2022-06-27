package com.vesdk.camera.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.vecore.BaseVirtual
import com.vecore.base.lib.utils.CoreUtils
import com.vecore.base.lib.utils.InsertToGalleryUtils
import com.vecore.exception.InvalidArgumentException
import com.vecore.listener.ExportListener
import com.vecore.models.MediaObject
import com.vecore.models.VisualFilterConfig
import com.vecore.models.Watermark
import com.vecore.recorder.api.IRecorderCallBackShot
import com.vecore.recorder.api.RecorderConfig
import com.vecore.recorder.api.RecorderCore
import com.vecore.recorder.api.ResultConstants
import com.vecore.utils.ExportUtils
import com.vesdk.camera.R
import com.vesdk.camera.entry.CameraConfig
import com.vesdk.camera.entry.CameraConfig.Companion.CAMERA_SUPPORT_PHOTO
import com.vesdk.camera.entry.CameraContracts.Companion.RECORDER_MEDIA_LIST
import com.vesdk.camera.entry.CameraSdkInit
import com.vesdk.camera.handler.CameraZoomHandler
import com.vesdk.camera.helper.SdkHelper
import com.vesdk.camera.listener.OnCameraViewListener
import com.vesdk.camera.listener.OnRecorderMenuLevelTwoListener
import com.vesdk.camera.listener.OnRecorderMenuListener
import com.vesdk.camera.ui.fragment.BeautyFragment
import com.vesdk.camera.ui.fragment.FilterFragment
import com.vesdk.camera.ui.fragment.FilterParentFragment
import com.vesdk.camera.ui.fragment.MenuFragment
import com.vesdk.camera.utils.CameraConfiguration
import com.vesdk.camera.utils.CameraPathUtils
import com.vesdk.camera.utils.CameraUtils
import com.vesdk.camera.utils.CheckSDSizeUtils
import com.vesdk.camera.viewmodel.CameraViewModel
import com.vesdk.camera.viewmodel.ToneViewModel
import com.vesdk.common.base.BaseActivity
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.bean.Permission
import com.vesdk.common.download.DownloadManager
import com.vesdk.common.listener.PopupDialogListener
import com.vesdk.common.ui.dialog.OptionsDialog
import com.vesdk.common.utils.CommonUtils
import com.vesdk.engine.EngineManager
import com.vesdk.engine.listener.CameraExtraListener
import kotlinx.android.synthetic.main.camera_activity_recorder.*
import kotlinx.android.synthetic.main.camera_layout_gate.*

/**
 * 录制界面
 */
class RecorderActivity : BaseActivity(), OnRecorderMenuListener, OnRecorderMenuLevelTwoListener {

    companion object {

        @JvmStatic
        fun newInstance(context: Context): Intent {
            return Intent(context, RecorderActivity::class.java)
        }

    }

    /**
     * 配置
     */
    private lateinit var mCameraConfig: CameraConfig

    /**
     * 音乐
     */
    private lateinit var mMusicContracts: ActivityResultLauncher<Array<String>>
    private var mMusicContracts2: ActivityResultLauncher<Void>? = null

    /**
     * viewModel
     */
    private val mViewModel by lazy { ViewModelProvider(this).get(CameraViewModel::class.java) }

    /**
     * 调色
     */
    private val mToneViewModel by lazy { ViewModelProvider(this).get(ToneViewModel::class.java) }

    /**
     * 对焦
     */
    private lateinit var mCameraZoom: CameraZoomHandler

    /**
     * 菜单
     */
    private val mMenuFragment = MenuFragment.newInstance()

    /**
     * 菜单
     */
    private var mCurrentFragment: BaseFragment? = null

    /**
     * 美颜
     */
    private val mBeautyFragment = BeautyFragment.newInstance()

    /**
     * 滤镜
     */
    private val mFilterFragment = FilterParentFragment.newInstance()

    /**
     * 录制回调
     */
    private var mRecorderCallBack: RecorderCallBack? = null

    /**
     * 存储地址
     */
    private var mSaveFilePath: String? = null

    /**
     * 退出
     */
    private var mFinish = false

    /**
     * 释放相机
     */
    private var mRecycleCamera = false

    /**
     * 相机准备
     */
    private var mCameraPrepared = false

    /**
     * 权限
     */
    private var mPermissionGranted = false

    /**
     * 录制准备
     */
    private var mRecordPrepared = false

    /**
     * 倒计时
     */
    private var mCountDownTimer: CountDownTimer? = null

    /**
     * AI回调
     */
    private lateinit var mExtraDrawListener: CameraExtraListener

    /**
     * 注册
     */
    override fun initRegisterContract() {
        super.initRegisterContract()
        //音乐
        mMusicContracts = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                CameraUtils.getAbsolutePath(this, it)?.let { path ->
                    mViewModel.addMusicPath(path)
                }
            }
        }
        SdkHelper.getMusicContracts()?.let {
            mMusicContracts2 = registerForActivityResult(it) { pathArray ->
                pathArray?.let { path ->
                    if (path.size > 0) {
                        mViewModel.addMusicPath(path[0])
                    }
                }
            }
        }
    }

    /**
     * 权限
     */
    override suspend fun initPermissions(): MutableList<Permission> {
        val permissions = mutableListOf<Permission>()
        if (mCameraConfig.cameraSupport != CAMERA_SUPPORT_PHOTO) {//允许录视频时
            //录音
            CommonUtils.getPermission(this, Manifest.permission.RECORD_AUDIO)?.let {
                it.icon = R.drawable.common_ic_permission_audio
                permissions.add(it)
            }
        }
        //相机
        CommonUtils.getPermission(this, Manifest.permission.CAMERA)?.let {
            it.icon = R.drawable.common_ic_permission_camera
            permissions.add(it)
        }
        return permissions
    }

    /**
     * 初始化权限成功
     */
    override fun permissionsSuccess() {
        init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CoreUtils.hideVirtualBar(this)
        //常量
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //配置
        mCameraConfig = CameraSdkInit.getCameraConfig()
        super.onCreate(savedInstanceState)
    }

    /**
     * 初始化
     */
    override fun init() {
        mPermissionGranted = true

        //控件
        initView()

        //菜单布局
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.menuFragment, mMenuFragment)
        ft.commitAllowingStateLoss()

        //AI
        initAI()

        //相册配置信息
        initCamera()

        //监听
        mViewModel.getBeautyLiveData().observe(this) {
            RecorderCore.enableBeauty(mViewModel.getBeauty())
            if (mViewModel.isFace()) {
                mExtraDrawListener.setFace(true)
                RecorderCore.enableFaceAdjustment(mViewModel.getFace())
            } else {
                mExtraDrawListener.setFace(false)
                RecorderCore.enableFaceAdjustment(null)
            }
        }
        mViewModel.getFilterLiveData().observe(this) {
            setFilter()
        }

        mToneViewModel.getToneLiveData().observe(this) {
            setToneList(it)
        }
    }


    private fun setToneList(list: List<VisualFilterConfig>) {
        RecorderCore.setToneList(list)
    }

    private fun setFilter() {
        val filter = mViewModel.getFilter()
        filter?.let {
            RecorderCore.setFilter(it)
            RecorderCore.setFilterValue(it.defaultValue)
        } ?: kotlin.run {
            RecorderCore.setLookupFilter(null)
        }
    }

    /**
     * 初始化控件
     */
    private fun initView() {
        //全面屏
        val cameraParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        cameraParams.setMargins(0, 0, 0, 0)
        cameraPreview.layoutParams = cameraParams
        cameraPreview.setAspectRatio(0.0)

        // 处理相机变焦
        glTouch.setViewHandler(object : OnCameraViewListener {
            override fun onSwitchFilterToLeft() {
            }

            override fun onSwitchFilterToRight() {
            }

            override fun onSingleTapUp(e: MotionEvent?) {
            }

            override fun onDoubleTap(e: MotionEvent?) {
            }

            override fun onFilterChangeStart(leftToRight: Boolean, filterProportion: Double) {
            }

            override fun onFilterChanging(leftToRight: Boolean, mFilterProportion: Double) {
            }

            override fun onFilterChangeEnd() {
            }

            override fun onFilterCanceling(leftToRight: Boolean, mFilterProportion: Double) {
            }

            override fun onFilterChangeCanceled() {
            }

        })
        mCameraZoom = CameraZoomHandler(this, null)
        glTouch.setZoomHandler(mCameraZoom)

    }

    /**
     * 初始录制
     */
    private fun initCamera() {
        //录制参数
        val size: BaseVirtual.Size = CameraConfiguration.getRecorderSize()
        val config = RecorderConfig().setVideoSize(size.getWidth(), size.getHeight())
                .setVideoFrameRate(24)
                .setVideoBitrate(CameraConfiguration.recorderBitrate * 1000)
                .setEnableFront(mCameraConfig.isFaceFront)
                .setEnableBeautify(!mCameraConfig.hideBeauty)
                .setKeyFrameTime(mCameraConfig.keyFrameTime)
                .setEnableAutoFocus(true)
                .setEnableAutoFocusRecording(false)
        RecorderCore.setRecorderConfig(config)
        RecorderCore.setMicFactor(CameraConfiguration.recordMicFactor) //设置麦克风音量
        if (!mRecordPrepared) {
            //清理，防止之前已经初始过
            mRecordPrepared = true
            //录制回调
            mRecorderCallBack = RecorderCallBack()
            //准备录制界面
            RecorderCore.prepare(cameraParent, mRecorderCallBack)
            //美颜
            RecorderCore.enableBeauty(!mCameraConfig.hideBeauty)
            //是否静音
            RecorderCore.setMute(false)

            //水印
            if (mCameraConfig.enabledWatermark && mCameraConfig.watermarkPath != "") {
                val watermark = Watermark(mCameraConfig.watermarkPath)
                watermark.gravity = mCameraConfig.watermarkGravity
                watermark.xAdj = mCameraConfig.watermarkAdjX
                watermark.yAdj = mCameraConfig.watermarkAdjY
                RecorderCore.setWatermark(watermark)
            } else {
                RecorderCore.setWatermark(null)
            }
        }
    }

    /**
     * AI引擎
     */
    private fun initAI() {
        mExtraDrawListener = EngineManager.getCameraExtraListener(object :
                com.vesdk.engine.listener.OnEngineFaceListener {

            override fun onSuccess(
                    facePointF: Array<PointF?>?,
                    fivePointF: Array<PointF?>?,
                    asp: Float
            ) {
                facePointF?.let { faceList ->
                    if (faceList.isNotEmpty()) {
                        for (point in faceList) {
                            point?.let { p ->
                                p.set(1 - p.x, p.y)
                            }
                        }
                    }
                }
                mViewModel.setFacePoint(facePointF)
            }

            override fun onFail(code: Int, msg: String) {

            }

        })
        RecorderCore.setCameraExtraDrawListener(mExtraDrawListener, false)
    }

    /**
     * 开始录制
     */
    private fun startRecorder() {
        RecorderCore.setOrientation(0)
        tvWaiting.visibility = GONE
        if (mMenuFragment.isPhoto()) {
            //截取
            val size: BaseVirtual.Size = CameraConfiguration.getRecorderSize()
            mSaveFilePath = CameraPathUtils.getImagePath(
                    com.pesdk.utils.PathUtils.getDisplayName(
                            "camera",
                            "jpg"
                    )
            )
            RecorderCore.shotPicture(true, mSaveFilePath, size.width, size.height, 100)
        } else {
            mSaveFilePath = CameraPathUtils.getVideoPath(
                    com.pesdk.utils.PathUtils.getDisplayName(
                            "camera",
                            "mp4"
                    )
            )
            RecorderCore.startRecord(mSaveFilePath, null, mMenuFragment.getSpeed())
        }
    }

    /**
     * 停止录制
     */
    private fun stopRecorder() {
        if (RecorderCore.isRecording()) {
            RecorderCore.stopRecord()
        }
    }

    /**
     * 倒计时
     */
    private fun countdown() {
        //空间
        val rootPath = CameraPathUtils.getRdVideo()
        if (rootPath == null) {
            onToast(getString(R.string.camera_error_path))
            return
        }
        if (!CheckSDSizeUtils.isThanCurrentSize(rootPath)) {
            onToast(getString(R.string.camera_sd_not_enough_record))
        }
        if (mCameraConfig.limitMaxTime != CameraConfig.DEFAULT_TIME
                && mViewModel.getRecorderTotalTime() >= mCameraConfig.limitMaxTime
        ) {
            onToast(getString(R.string.camera_duration_max))
            return
        }

        if (mMenuFragment.getDelayTime() <= 0) {
            startRecorder()
        } else {
            //开始倒计时
            tvWaiting.visibility = View.VISIBLE
            val total = mMenuFragment.getDelayTime() * 1000
            mCountDownTimer?.cancel()
            mCountDownTimer = object : CountDownTimer(total.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    tvWaiting.text = CameraUtils.ms2s(millisUntilFinished).toInt().toString()
                }

                override fun onFinish() {
                    startRecorder()
                }

            }.start()

        }
    }

    /**
     * 闸门动画
     */
    private fun gateAnim(open: Boolean) {
        if (open) {
            //动画
            if (mCameraPrepared) {
                return
            }
            mCameraPrepared = true
            val animForTop = AnimationUtils.loadAnimation(this, R.anim.camera_slide_to_top)
            animForTop.fillAfter = true
            val animForBottom = AnimationUtils.loadAnimation(this, R.anim.camera_slide_to_down)
            animForBottom.fillAfter = true
            ivGateTop.startAnimation(animForTop)
            ivGateBottom.startAnimation(animForBottom)
        } else {
            if (mCameraPrepared) {
                mFinish = true
                val animForTop = AnimationUtils.loadAnimation(this, R.anim.camera_slide_from_top)
                animForTop.fillAfter = true
                val animForBottom =
                        AnimationUtils.loadAnimation(this, R.anim.camera_slide_from_down)
                animForBottom.fillAfter = true
                ivGateTop.startAnimation(animForTop)
                ivGateBottom.startAnimation(animForBottom)
                animForBottom.setAnimationListener(object : Animation.AnimationListener {

                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        finish()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                })
            } else {
                finish()
            }
        }
    }


    /**
     * 切换fragment
     */
    private fun changeFragment(fragment: BaseFragment) {
        //隐藏
        mCurrentFragment?.let {
            supportFragmentManager
                    .beginTransaction()
                    .hide(it)
                    .commitAllowingStateLoss()
        }
        if (!fragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment, fragment)
                    .show(fragment)
                    .commitAllowingStateLoss()
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .show(fragment)
                    .commitAllowingStateLoss()
        }
        //显示出来
        menuLevelTwo.isClickable = true
        menuLevelTwo.visibility = View.VISIBLE
        //动画
        val aniSlideIn = AnimationUtils.loadAnimation(this, R.anim.camera_slide_in)
        menuLevelTwo.startAnimation(aniSlideIn)
        mCurrentFragment = fragment
        //菜单
        menuFragment.visibility = GONE
    }

    /**
     * 隐藏菜单
     */
    private fun hideFragment() {
        mCurrentFragment?.let {
            supportFragmentManager
                    .beginTransaction()
                    .hide(it)
                    .commitAllowingStateLoss()
        }
        mCurrentFragment = null
        menuLevelTwo.visibility = GONE
        menuFragment.visibility = View.VISIBLE
    }

    /**
     * 退出弹窗
     */
    private fun onGiveUpAlert() {
        OptionsDialog
                .create(this,
                        getString(R.string.camera_exit),
                        getString(R.string.camera_dialog_message_give_up),
                        cancelable = true,
                        cancelTouch = true,
                        listener = object : PopupDialogListener {

                            override fun onDialogSure() {
                                //删除文件
                                mViewModel.clearAll()
                                onBackPressed()
                            }

                            override fun onDialogCancel() {

                            }

                        })
                .show()
    }

    /**
     * 权限弹窗弹窗
     */
    private fun onPermissionsAlert() {
        OptionsDialog
                .create(
                        this, getString(R.string.camera_dialog_title),
                        getString(R.string.camera_dialog_message_permission),
                        getString(R.string.camera_authorization), getString(R.string.camera_exit),
                        cancelable = false, cancelTouch = false, listener = object : PopupDialogListener {

                    override fun onDialogSure() {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                        finish()
                    }

                    override fun onDialogCancel() {
                        finish()
                    }

                }, onDismissListener = null
                )
                .show()
    }


    override fun getLayoutId(): Int {
        return R.layout.camera_activity_recorder
    }

    override fun onBackPressed() {
        if (mFinish) {
            return
        }

        //菜单
        if (menuLevelTwo.visibility == View.VISIBLE) {
            if (mCurrentFragment != null && mCurrentFragment!!.onBackPressed() != -1) {
                return
            }
        }
        //倒计时
        if (tvWaiting.visibility == View.VISIBLE) {
            tvWaiting.visibility = GONE
            mCountDownTimer?.cancel()
            return
        }

        //最终
        if (mMenuFragment.onBackPressed() != -1) {
            return
        }

        //是否已经拍摄文件 删除
        if (mViewModel.getRecorderList().size > 0) {
            onGiveUpAlert()
            return
        }

        //关闭相机
        finishCamera()
    }

    override fun onStart() {
        super.onStart()
        mRecordPrepared = false
    }

    override fun onResume() {
        super.onResume()
        if (mRecycleCamera && mPermissionGranted) {
            initCamera()
        }
        mRecycleCamera = false
    }

    override fun onPause() {
        super.onPause()
        if (RecorderCore.isRecording()) {
            stopRecorder()
        }
    }

    override fun onStop() {
        super.onStop()
        mRecycleCamera = true
        RecorderCore.resetPrepared()
    }

    override fun onDestroy() {
        super.onDestroy()
        glTouch.setViewHandler(null)
        glTouch.setZoomHandler(null)
        if (this::mExtraDrawListener.isInitialized) {
            mExtraDrawListener.release()
        }
        mRecorderCallBack = null
        RecorderCore.onDestory()
        DownloadManager.closeAll()
    }


    /**
     * 播放系统拍照声音
     */
    private fun shootSound() {
        try {
            val manager = getSystemService(AUDIO_SERVICE) as AudioManager
            val volume = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            if (volume != 0) {
                val shootMP = MediaPlayer.create(
                        this,
                        Uri.parse("file:///system/media/audio/ui/camera_click.ogg")
                )
                shootMP?.start()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 录制回调
     */
    inner class RecorderCallBack : IRecorderCallBackShot {
        private var mIsFailed = false

        /**
         * 响应摄像头信息
         *
         * @param resultInfo 具体返回消息
         */
        override fun onCamera(result: Int, resultInfo: String) {
            if (result == ResultConstants.ERROR_CAMERA_OPEN_FAILED) {
                mCameraPrepared = false
                onPermissionsAlert()
            }
        }

        override fun onPrepared(result: Int, resultInfo: String) {
            if (result == ResultConstants.SUCCESS) {
                gateAnim(true)

                //恢复滤镜、调色
                setFilter()
                mToneViewModel.getToneLiveData().value?.let {
                    setToneList(it)
                }
            }
        }

        override fun onPermissionFailed(result: Int, resultInfo: String) {
            //权限失败
            onPermissionsAlert()
        }

        override fun onGetRecordStatus(position: Int, recordFPS: Int, delayed: Int) {
            //响应获取已录制时间
            val currentTotalTime = CameraUtils.s2ms(mViewModel.getRecorderTotalTime()) + position
            val limitMaxTime = CameraUtils.s2ms(mCameraConfig.limitMaxTime)
            if (limitMaxTime in 1 until currentTotalTime) {
                stopRecorder()
            }
            //全屏录制进度
            mViewModel.screenDuration(
                    CameraUtils.stringForTime(currentTotalTime.toLong()),
                    recordFPS
            )
        }

        override fun onRecordBegin(result: Int, resultInfo: String) {
            if (mIsFailed || result != ResultConstants.SUCCESS) {
                mIsFailed = false
                return
            }
            tvWaiting.visibility = GONE
            hideFragment()
            mMenuFragment.setRecorderUI(RecorderCore.isRecording())
        }

        override fun onRecordFailed(result: Int, resultInfo: String) {
            mIsFailed = true
            if (result == ResultConstants.ERROR_AUDIO_RECORD_START) {
                onToast(getString(R.string.camera_error_permission_audio))
            } else if (result == ResultConstants.ERROR_ENCODE_AUDIO) {
                onToast("${getString(R.string.camera_error_code)}${result}")
            }

        }

        override fun onRecordEnd(result: Int, resultInfo: String) {
            if (mRecycleCamera) {
                mRecycleCamera = false
                RecorderCore.resetPrepared()
            }
            mMenuFragment.setRecorderUI(RecorderCore.isRecording())
            if (result >= ResultConstants.SUCCESS) {
                mSaveFilePath?.let { mViewModel.addCameraPath(it) }
            }
        }

        override fun onScreenShot(result: Int, shotMessageOrPath: String) {
            //拍照
            shootSound()
            //记录地址
            mViewModel.addCameraPath(shotMessageOrPath)

            //多次拍摄
            if (!mCameraConfig.multiShoot) {
                onNext()
            }
        }

    }

    /**
     * 关闭相机
     */
    private fun finishCamera() {
        if (mCameraPrepared) {
            gateAnim(false)
        } else {
            finish()
        }
    }

    /**
     * 返回
     */
    private fun onResult(resultList: ArrayList<String>) {
        val intent = Intent()
        intent.putStringArrayListExtra(RECORDER_MEDIA_LIST, resultList)
        setResult(RESULT_OK, intent)
        finishCamera()
    }

    /**
     * 相对路径
     */
    private fun relativePath(): String {
        return mCameraConfig.relativePath ?: let {
            Environment.DIRECTORY_DCIM + "/Camera"
        }
    }

    /**
     * 描述
     */
    private fun artist(): String {
        return mCameraConfig.artists ?: getString(R.string.app_name)
    }

    /**
     * 插入图库
     */
    private fun insertImage2Gallery(): ArrayList<String> {
        val recorderList = mViewModel.getRecorderList()
        val list = arrayListOf<String>()
        val artist = artist()
        val relativePath = relativePath()
        for (path in recorderList) {
            val tmp = InsertToGalleryUtils.insertImage2Gallery(this, path, artist, relativePath)
            list.add(tmp)
        }
        return list
    }

    /**
     * 插入图库
     */
    private fun insertVideo2Gallery(): ArrayList<String> {
        val recorderList = mViewModel.getRecorderList()
        val list = arrayListOf<String>()
        val artist = artist()
        val relativePath = relativePath()
        for (path in recorderList) {
            val tmp = InsertToGalleryUtils.insertVideo2Gallery(this, path, artist, relativePath)
            list.add(tmp)
        }
        return list
    }

    /**
     * 合并视频
     */
    private fun mergeMedia(listener: ExportListener) {
        val recorderList = mViewModel.getRecorderList()
        if (recorderList.size > 1) {
            val videoList = ArrayList<MediaObject>()
            for (s in recorderList) {
                try {
                    videoList.add(MediaObject(s))
                } catch (e: InvalidArgumentException) {
                    e.printStackTrace()
                }
            }
            mSaveFilePath = CameraPathUtils.getVideoPath(
                    com.pesdk.utils.PathUtils.getDisplayName(
                            "camera",
                            "mp4"
                    )
            )
            ExportUtils.fastExport(
                    this,
                    videoList,
                    null,
                    mSaveFilePath,
                    object : ExportListener {

                        override fun onExportStart() {
                            showLoading()
                        }

                        override fun onExporting(progress: Int, max: Int): Boolean {
                            return true
                        }

                        override fun onExportEnd(result: Int, extra: Int, info: String) {
                            if (result >= BaseVirtual.RESULT_SUCCESS) {
                                // 合并成功删除临时文件
                                mViewModel.clearAll()
                                mSaveFilePath?.let { mViewModel.addCameraPath(it) }
                            }
                            listener.onExportEnd(result, 0, "")
                        }
                    })
        } else {
            listener.onExportEnd(BaseVirtual.RESULT_SUCCESS, 0, "")
        }
    }

    /**
     * 视频
     */
    private fun onVideoList() {
        if (mCameraConfig.insertGallery) {
            onResult(insertVideo2Gallery())
        } else {
            onResultList()
        }
    }

    /**
     * 返回
     */
    private fun onResultList() {
        val recorderList = mViewModel.getRecorderList()
        val resultList = ArrayList<String>()
        recorderList.let {
            resultList.addAll(it)
        }
        onResult(resultList)
    }

    override fun hide() {
        hideFragment()
    }

    override fun onNext() {
        val recorderList = mViewModel.getRecorderList()
        if (recorderList.size <= 0) {
            finishCamera()
            return
        }
        //图片 插入相册
        if (mMenuFragment.isPhoto()) {
            if (mCameraConfig.insertGallery) {
                val list = insertImage2Gallery()
                onResult(list)
            } else {
                onResultList()
            }
        } else if (mCameraConfig.mergeMedia) {  //合并视频
            mergeMedia(object : ExportListener {
                override fun onExportStart() {
                }

                override fun onExporting(progress: Int, max: Int): Boolean {
                    return true
                }

                override fun onExportEnd(result: Int, extra: Int, info: String?) {
                    onVideoList()
                }
            })
        } else {
            onVideoList()
        }
    }

    override fun onCancel() {
        onBackPressed()
    }

    override fun onRecorder(photo: Boolean) {
        if (RecorderCore.isRecording()) {
            stopRecorder()
        } else {
            countdown()
        }
    }

    override fun onMusic() {
        mMusicContracts2?.launch(null)
                ?: kotlin.run { mMusicContracts.launch(arrayOf("audio/*")) }
    }

    override fun onFilter() {
        changeFragment(mFilterFragment)
    }

    override fun onBeauty() {
        changeFragment(mBeautyFragment)
    }

}