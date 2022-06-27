package com.vesdk.camera.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vecore.recorder.api.RecorderCore
import com.vesdk.camera.R
import com.vesdk.camera.entry.CameraConfig
import com.vesdk.camera.entry.CameraConfig.Companion.CAMERA_SUPPORT_ALL
import com.vesdk.camera.entry.CameraConfig.Companion.CAMERA_SUPPORT_PHOTO
import com.vesdk.camera.entry.CameraConfig.Companion.CAMERA_SUPPORT_RECORDER
import com.vesdk.camera.entry.CameraConfig.Companion.FIRST_VIDEO
import com.vesdk.camera.entry.CameraSdkInit
import com.vesdk.camera.listener.OnRecorderMenuListener
import com.vesdk.camera.ui.contract.ConfigResultContract
import com.vesdk.camera.ui.adapter.RecorderVideoAdapter
import com.vesdk.camera.viewmodel.CameraViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.helper.init
import kotlinx.android.synthetic.main.camera_fragment_menu.*
import kotlin.math.abs

/**
 * 拍摄界面
 */
class MenuFragment : BaseFragment() {

    companion object {

        @JvmStatic
        fun newInstance() = MenuFragment()

        /**
         * 速度
         */
        private val SPEED_DEFINE = doubleArrayOf(1.0 / 3, 1.0 / 2, 1.0, 2.0, 3.0)

        /**
         * 拍摄、录制
         */
        private const val MENU_PHOTO = 0
        private const val MENU_VIDEO = 1

    }

    /**
     * 菜单
     */
    private lateinit var mMenuListener: OnRecorderMenuListener

    /**
     * 配置
     */
    private lateinit var mConfigResult: ActivityResultLauncher<Boolean>

    /**
     * ViewModel
     */
    private val mViewModel by lazy { ViewModelProvider(requireActivity()).get(CameraViewModel::class.java) }

    /**
     * 配置
     */
    private lateinit var mCameraConfig: CameraConfig

    /**
     * 已经录制文件
     */
    private lateinit var mRecorderVideoAdapter: RecorderVideoAdapter

    /**
     * 0拍照  1录像
     */
    private var mMenuCurrent = MENU_PHOTO

    /**
     * 倒计时时间
     */
    private var mDelayTime = 0

    /**
     * 变速
     */
    private var mSpeed = 1.0


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMenuListener = context as OnRecorderMenuListener
        mCameraConfig = CameraSdkInit.getCameraConfig()
    }

    override fun initRegisterContract() {
        mConfigResult = registerForActivityResult(ConfigResultContract()) {

        }
    }

    override fun init() {
        //控件
        initView()

        //录制文件
        mViewModel.getRecorderFileLiveData().observe(this) {
            notifyRecorderVideo()
        }

        //音乐
        mViewModel.getMusicLiveData().observe(this) {
            val music = mViewModel.getMusic()
            if (mCameraConfig.hideMusic || music == null) {
                tvMusic.visibility = GONE
                RecorderCore.clearMusic()
            } else {
                RecorderCore.addMusic(music)
                tvMusic.visibility = VISIBLE
                tvMusic.text = mViewModel.getMusicName()
            }
        }

        //时间
        mViewModel.getDurationLiveData().observe(this) {
            rlTime.visibility = VISIBLE
            tvTime.text = mViewModel.getRecorderTime()
            tvFps.text = mViewModel.getRecorderFps()
        }

    }

    private fun initView() {
        //返回
        menuCancel.setOnClickListener { mMenuListener.onCancel() }
        //倒计时
        menuDelay.setOnClickListener {
            mDelayTime += 2
            if (mDelayTime > 7) {
                mDelayTime = 0
            } else if (mDelayTime < 3) {
                mDelayTime = 3
            }
            //更换图标
            when (mDelayTime) {
                3 -> {
                    menuDelay.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.camera_ic_delay_3,
                        0,
                        0
                    )
                }
                5 -> {
                    menuDelay.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.camera_ic_delay_5,
                        0,
                        0
                    )
                }
                7 -> {
                    menuDelay.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.camera_ic_delay_7,
                        0,
                        0
                    )
                }
                else -> {
                    menuDelay.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.camera_ic_delay_n,
                        0,
                        0
                    )
                }
            }
        }

        //设置分辨率
        menuSetting.setOnClickListener {
            mConfigResult.launch(mCameraConfig.cameraSupport != CAMERA_SUPPORT_PHOTO)
        }
        //闪光
        menuFlash.setOnClickListener {
            if (RecorderCore.isFaceFront()) {
                RecorderCore.setFlashMode(false)
            } else {
                RecorderCore.setFlashMode(!RecorderCore.getFlashMode())
            }
            menuFlash?.isEnabled = !RecorderCore.isFaceFront()
        }
        //音乐
        menuMusic.setOnClickListener {
            if (!mCameraConfig.hideMusic) {
                mMenuListener.onMusic()
            }
        }
        //切换摄像头
        menuSwitch.setOnClickListener {
            RecorderCore.switchCamera()
            if (RecorderCore.isFaceFront()) {
                RecorderCore.setFlashMode(false)
            }
            menuFlash?.isEnabled = !RecorderCore.isFaceFront()
        }
        //美颜
        menuBeauty.setOnClickListener { mMenuListener.onBeauty() }
        //滤镜
        menuFilter.setOnClickListener { mMenuListener.onFilter() }
        //速度
        speed.setListener { position ->
            mSpeed = SPEED_DEFINE[position]
            menuSpeed.setImageResource(
                if (abs(mSpeed - 1) < 0.01)
                    R.drawable.camera_ic_speed_off
                else
                    R.drawable.camera_ic_speed_on
            )

        }
        menuSpeed.setOnClickListener {
            if (mCameraConfig.hideSpeed) {
                llSpeed.visibility = GONE
                menuSpeed.visibility = GONE
            } else {
                if (llSpeed.visibility == VISIBLE) {
                    llSpeed.visibility = GONE
                    menuSpeed.visibility = VISIBLE
                } else {
                    llSpeed.visibility = VISIBLE
                    menuSpeed.visibility = GONE
                }
            }
        }
        //音乐
        tvMusic.setOnClickListener {
            mViewModel.clearMusic()
        }
        //确定
        btnSure.setOnClickListener { mMenuListener.onNext() }
        //录制
        btnCamera.setOnClickListener {
            it.isEnabled = false
            mMenuListener.onRecorder(mMenuCurrent == 0)
            it.postDelayed({
                it.isEnabled = true
            }, 500)
        }

        //录制视频
        initRvVideo()

        //加载菜单
        when (mCameraConfig.cameraSupport) {
            CAMERA_SUPPORT_PHOTO -> {
                cusMenu.visibility = GONE
                menuSpeed.visibility = GONE
                mMenuCurrent = MENU_PHOTO
                btnCamera.setImageResource(R.drawable.camera_ic_photo)
            }
            CAMERA_SUPPORT_RECORDER -> {
                cusMenu.visibility = GONE
                mMenuCurrent = MENU_VIDEO
                btnCamera.setImageResource(R.drawable.camera_ic_recorder)
            }
            else -> {
                val menuList = ArrayList<String>()
                menuList.add(getString(R.string.camera_photo))
                menuList.add(getString(R.string.camera_video))
                cusMenu.addMenu(menuList, 0)
                cusMenu.setListener { index: Int ->
                    mMenuCurrent = index
                    llSpeed.visibility = GONE
                    if (mMenuCurrent == MENU_PHOTO) {
                        menuSpeed.visibility = GONE
                        context?.let {
                            val d = arrayOf(
                                ContextCompat.getDrawable(it, R.drawable.camera_ic_recorder),
                                ContextCompat.getDrawable(it, R.drawable.camera_ic_photo)
                            )
                            val transitionDrawable = TransitionDrawable(d)
                            transitionDrawable.startTransition(300)
                            btnCamera.background = transitionDrawable
                        } ?: kotlin.run {
                            btnCamera.setImageResource(R.drawable.camera_ic_photo)
                        }
                    } else {
                        menuSpeed.visibility = if (mCameraConfig.hideSpeed) GONE else VISIBLE
                        context?.let {
                            val d = arrayOf(
                                ContextCompat.getDrawable(it, R.drawable.camera_ic_photo),
                                ContextCompat.getDrawable(it, R.drawable.camera_ic_recorder)
                            )
                            val transitionDrawable = TransitionDrawable(d)
                            transitionDrawable.startTransition(300)
                            btnCamera.background = transitionDrawable
                        } ?: kotlin.run {
                            btnCamera.setImageResource(R.drawable.camera_ic_recorder)
                        }
                    }
                }
                if (mCameraConfig.firstShow == FIRST_VIDEO) {
                    cusMenu.onSwitch(MENU_VIDEO)
                }
            }
        }

        //配置
        if (mCameraConfig.hideBeauty) {
            menuBeauty.visibility = GONE
        }
        if (mCameraConfig.hideFilter) {
            menuFilter.visibility = GONE
        }
        if (mCameraConfig.hideMusic) {
            menuMusic.visibility = GONE
            tvMusic.visibility = GONE
        }
        if (mCameraConfig.hideSpeed) {
            llSpeed.visibility = GONE
            menuSpeed.visibility = GONE
        }
        menuFlash?.isEnabled = !mCameraConfig.isFaceFront


        tvMusic.ellipsize = TextUtils.TruncateAt.MARQUEE
        tvMusic.isSingleLine = true
        tvMusic.isSelected = true
        tvMusic.isFocusable = true
        tvMusic.isFocusableInTouchMode = true
    }

    /**
     * 录制文件
     */
    private fun initRvVideo() {
        mRecorderVideoAdapter =
            RecorderVideoAdapter(mViewModel.getRecorderList())
        rvFile.init(mRecorderVideoAdapter, requireContext(), LinearLayoutManager.HORIZONTAL)
        mRecorderVideoAdapter.addChildClickViewIds(R.id.ivDelete)
        mRecorderVideoAdapter.setOnItemChildClickListener { _, _, position ->
            //删除
            mViewModel.deleteCameraPath(position)
        }
        mRecorderVideoAdapter.setOnItemClickListener { _, _, _ ->
            //预览

        }
    }

    /**
     * 刷新
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun notifyRecorderVideo() {
        val recorderList = mViewModel.getRecorderList()
        //大于最短时间显示下一步
        if (recorderList.size <= 0) {
            rvFile.visibility = GONE
            btnSure.visibility = GONE
            if (mCameraConfig.cameraSupport == CAMERA_SUPPORT_ALL) {
                cusMenu.visibility = VISIBLE
            }
        } else if (mCameraConfig.multiShoot) {
            cusMenu.visibility = GONE
            rvFile.visibility = VISIBLE
            btnSure.visibility = if (isNext()) VISIBLE else GONE
            mRecorderVideoAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 显示下一步
     */
    private fun isNext(): Boolean {
        //当前时间是否达到最小时间
        if (mCameraConfig.limitMinTime > 0
            && mCameraConfig.limitMinTime > mViewModel.getRecorderTotalTime()
        ) {
            return false
        }
        return true
    }


    /**
     * 拍照
     */
    fun isPhoto(): Boolean {
        return mMenuCurrent == MENU_PHOTO
    }

    /**
     * 倒计时时间
     */
    fun getDelayTime(): Int {
        return mDelayTime
    }

    /**
     * 获取速度
     */
    fun getSpeed(): Double {
        return mSpeed
    }

    /**
     * 暂停和录制UI
     */
    fun setRecorderUI(recorder: Boolean) {
        if (recorder) {
            menuTop.visibility = GONE
            menuLeft.visibility = GONE
            menuRight.visibility = GONE
            llSpeed.visibility = GONE
            menuSpeed.visibility = GONE
            //文件
            rvFile.visibility = GONE
            tvMusic.visibility = GONE
            cusMenu.visibility = GONE
            btnCamera.setImageResource(R.drawable.camera_ic_recorder_pause)
        } else {
            menuTop.visibility = VISIBLE
            menuLeft.visibility = VISIBLE
            menuRight.visibility = VISIBLE
            rlTime.visibility = GONE
            if (mRecorderVideoAdapter.itemCount > 0) {
                rvFile.visibility = VISIBLE
            }
            if (!mCameraConfig.hideMusic && tvMusic.text != null) {
                tvMusic.visibility = VISIBLE
            }
            if (mCameraConfig.cameraSupport == CAMERA_SUPPORT_ALL) {
                cusMenu.visibility = VISIBLE
            }
            if (!mCameraConfig.hideSpeed && mMenuCurrent == MENU_VIDEO) {
                menuSpeed.visibility = VISIBLE
            }
            btnCamera.setImageResource(R.drawable.camera_ic_recorder)
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.camera_fragment_menu
    }

    override fun onBackPressed(): Int {
        if (llSpeed.visibility == VISIBLE) {
            llSpeed.visibility = GONE
            if (!mCameraConfig.hideSpeed && mMenuCurrent == MENU_VIDEO) {
                menuSpeed.visibility = VISIBLE
            }
            return 0
        }
        return super.onBackPressed()
    }

}