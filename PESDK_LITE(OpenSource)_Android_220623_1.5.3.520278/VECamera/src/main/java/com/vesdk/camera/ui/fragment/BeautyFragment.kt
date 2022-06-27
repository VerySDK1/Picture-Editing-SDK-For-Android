package com.vesdk.camera.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.vesdk.camera.R
import com.vesdk.camera.listener.OnRecorderMenuLevelTwoListener
import com.vesdk.camera.viewmodel.CameraViewModel
import com.vesdk.common.base.BaseFragment
import kotlinx.android.synthetic.main.camera_fragment_beauty.*
import kotlinx.android.synthetic.main.camera_layout_top_menu.*

/**
 * 拍摄美颜
 */
class BeautyFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = BeautyFragment()
    }

    private enum class BeautyStatue {
        STATUES_BEAUTY,
        STATUES_WHITENING,
        STATUES_RUDDY,
        STATUES_BIG_EYES,
        STATUES_BIG_FACE_LIFT,
    }

    /**
     * listener
     */
    private lateinit var mListener: OnRecorderMenuLevelTwoListener

    /**
     * ViewModel
     */
    private val mCameraViewModel by lazy { ViewModelProvider(requireActivity()).get(CameraViewModel::class.java) }

    /**
     * 当前状态
     */
    private var mStatus = BeautyStatue.STATUES_BEAUTY

    /**
     * 显示消息
     */
    private val mMessagePrefix = MutableLiveData<String>()
    private val mMessage = MutableLiveData<Float>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnRecorderMenuLevelTwoListener
    }

    override fun init() {
        //初始化
        mCameraViewModel.resetBeauty()

        //显示
        mMessage.observe(this) {
            setMessage()
        }

        initView()
    }

    private fun initView() {
        //确定
        ivSure.setOnClickListener { onBackPressed() }
        //重置
        ivReset.visibility = VISIBLE
        ivReset.setOnClickListener {
            mCameraViewModel.resetBeauty()
            seekbar.progress = 0
            mMessage.value = 0f
            //设置
            freshBeauty()
        }

        seekbar.visibility = VISIBLE
        seekbar.progress = (mCameraViewModel.getBeauty().defaultValue * seekbar.max).toInt()
        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val value = progress / (seekBar.max + 0.0f)
                    when (mStatus) {
                        BeautyStatue.STATUES_BEAUTY -> {
                            mCameraViewModel.getBeauty().beauty = value
                        }
                        BeautyStatue.STATUES_WHITENING -> {
                            mCameraViewModel.getBeauty().whitening = value
                        }
                        BeautyStatue.STATUES_RUDDY -> {
                            mCameraViewModel.getBeauty().ruddy = value
                        }
                        BeautyStatue.STATUES_BIG_EYES -> {
                            mCameraViewModel.getFace().bigEyes = value
                        }
                        BeautyStatue.STATUES_BIG_FACE_LIFT -> {
                            mCameraViewModel.getFace().faceLift = value
                        }
                    }
                    mMessage.value = value
                    freshBeauty()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        mMessagePrefix.value = getString(R.string.camera_beauty)
        mMessage.value = mCameraViewModel.getBeauty().defaultValue

        btnBeauty.setOnClickListener {
            mStatus = BeautyStatue.STATUES_BEAUTY
            var defaultValue = mCameraViewModel.getBeauty().defaultValue
            if (defaultValue.isNaN()) {
                defaultValue = 0f
            }
            seekbar.progress = (defaultValue * seekbar.max).toInt()
            mMessagePrefix.value = getString(R.string.camera_beauty)
            mMessage.value = mCameraViewModel.getBeauty().defaultValue
        }
        btnWhitening.setOnClickListener {
            mStatus = BeautyStatue.STATUES_WHITENING
            var whitening = mCameraViewModel.getBeauty().whitening
            if (whitening.isNaN()) {
                whitening = 0f
            }
            seekbar.progress = (whitening * seekbar.max).toInt()
            mMessagePrefix.value = getString(R.string.camera_whitening)
            mMessage.value = mCameraViewModel.getBeauty().whitening
        }
        btnRuddy.setOnClickListener {
            mStatus = BeautyStatue.STATUES_RUDDY
            var ruddy = mCameraViewModel.getBeauty().ruddy
            if (ruddy.isNaN()) {
                ruddy = 0f
            }
            seekbar.progress = (ruddy * seekbar.max).toInt()
            mMessagePrefix.value = getString(R.string.camera_ruddy)
            mMessage.value = mCameraViewModel.getBeauty().ruddy
        }
        btnBigEye.setOnClickListener {
            mStatus = BeautyStatue.STATUES_BIG_EYES
            seekbar.progress = (mCameraViewModel.getFace().bigEyes * seekbar.max).toInt()
            mMessagePrefix.value = getString(R.string.camera_bigeye)
            mMessage.value = mCameraViewModel.getFace().bigEyes
        }
        btnFaceLift.setOnClickListener {
            mStatus = BeautyStatue.STATUES_BIG_FACE_LIFT
            seekbar.progress = (mCameraViewModel.getFace().faceLift * seekbar.max).toInt()
            mMessagePrefix.value = getString(R.string.camera_facelift)
            mMessage.value = mCameraViewModel.getFace().faceLift
        }
    }

    /**
     * 显示
     */
    @SuppressLint("SetTextI18n")
    private fun setMessage() {
        val value = mMessage.value
        tvMessage.text = "${mMessagePrefix.value}：${
            String.format(
                "%.1f",
                if (value == null || value.isNaN()) 0f else value
            )
        }"
    }

    /**
     * 刷新
     */
    private fun freshBeauty() {
        mCameraViewModel.freshBeauty()
    }

    override fun getLayoutId(): Int {
        return R.layout.camera_fragment_beauty
    }

    override fun onBackPressed(): Int {
        mListener.hide()
        return -1
    }

}