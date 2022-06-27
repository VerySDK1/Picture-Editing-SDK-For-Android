package com.pesdk.album.uisdk.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import com.pesdk.album.R
import com.pesdk.album.api.PreviewContracts.Companion.INTENT_PREVIEW
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.api.bean.PreviewInfo
import com.vecore.base.lib.utils.StatusBarUtil
import com.vesdk.common.base.BaseActivity
import com.vesdk.common.helper.bindExtra
import com.vesdk.common.utils.DateTimeUtils
import kotlinx.android.synthetic.main.album_activity_preview.*
import kotlinx.android.synthetic.main.album_stub_preview_image.*
import kotlinx.android.synthetic.main.album_stub_preview_video.*

/**
 * 预览
 */
class PreviewActivity : BaseActivity() {

    companion object {

        /**
         * 地址
         */
        private const val PREVIEW = "preview"

        @JvmStatic
        fun newInstance(context: Context, previewInfo: PreviewInfo): Intent {
            return Intent(context, PreviewActivity::class.java).run {
                putExtra(PREVIEW, previewInfo)
            }
        }
    }

    /**
     * 播放器初始化完成
     */
    private var mIsInitVideo: Boolean = false

    /**
     * 预览信息
     */
    private val mPreviewInfo: PreviewInfo? by bindExtra(PREVIEW)

    /**
     * 最后播放时间
     */
    private var mLastPlayProgress = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.setImmersiveStatusBar(this, true)
        super.onCreate(savedInstanceState)
    }

    override fun init() {
        if (mPreviewInfo == null) {
            finish()
            return
        }

        initView()

    }

    private fun initView() {
        //退出
        btnClose.setOnClickListener { onBackPressed() }

        //选中还是未选中
        cbSelected.isChecked = mPreviewInfo?.selected ?: false

        mPreviewInfo?.let { info ->
            if (info.mediaInfo.type == MediaType.TYPE_VIDEO) {
                initVideo(info)
            } else {
                initImage(info)
            }
        }
    }

    /**
     * 图片
     */
    private fun initImage(info: PreviewInfo) {
        mIsInitVideo = false
        //图片
        vsImage.inflate()
        photo.setImageURI(Uri.parse(info.mediaInfo.path))
    }

    /**
     * 视频
     */
    private fun initVideo(info: PreviewInfo) {
        //视频
        vsVideo.inflate()
        video.setVideoURI(Uri.parse(info.mediaInfo.path))
        video.setOnPreparedListener { mp ->
            if (mLastPlayProgress == -1) {
                tvProgress.text = getTime(0)
                tvTotal.text = getTime(mp.duration)
            }
            playVideo()
            mIsInitVideo = true
        }
        video.setOnErrorListener { _, _, _ ->
            onToast(R.string.album_preview_error)
            mIsInitVideo = false
            false
        }
        video.setOnCompletionListener {
            onSeekTo(0)
            sbProgress.progress = 0
            ivPlayStatue.setImageResource(R.drawable.album_ic_player_play)
            tvProgress.text = getTime(0)
            video.removeCallbacks(mPlayProgressRunnable)
        }

        //播放暂停
        ivPlayStatue.setOnClickListener { if (video.isPlaying) pauseVideo() else playVideo() }

        //进度
        sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            // Seek时是否播放中...
            private var IsPlayingOnSeek = false

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    val p = (progress * 1.0f / sbProgress.max * video.duration).toInt()
                    tvProgress.text = getTime(p)
                    onSeekTo(p)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                IsPlayingOnSeek = if (video.isPlaying) {
                    pauseVideo()
                    true
                } else {
                    false
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (IsPlayingOnSeek) {
                    playVideo()
                }
            }

        })
    }


    /**
     * 时间
     */
    fun getTime(progress: Int): String? {
        return DateTimeUtils.millisecond2String(
            progress.toLong(),
            existsHours = false,
            exitsMin = true
        )
    }

    /**
     * Seek
     */
    private fun onSeekTo(mes: Int) {
        video.removeCallbacks(mPlayProgressRunnable)
        video.seekTo(mes)
    }

    /**
     * 播放
     */
    private fun playVideo() {
        val currentPosition = video.currentPosition
        video.start()
        onSeekTo(currentPosition)
        ivPlayStatue.setImageResource(R.drawable.album_ic_player_pause)
        video.post(mPlayProgressRunnable)
    }

    /**
     * 暂停
     */
    private fun pauseVideo() {
        if (video.isPlaying) {
            video.pause()
        }
        ivPlayStatue.setImageResource(R.drawable.album_ic_player_play)
        video.removeCallbacks(mPlayProgressRunnable)
    }

    /**
     * 处理播放进度runnable
     */
    private val mPlayProgressRunnable: Runnable by lazy {
        object : Runnable {
            override fun run() {
                val position = video.currentPosition
                video.postDelayed(this, 100)
                sbProgress.progress = (position * 1.0f / video.duration * sbProgress.max).toInt()
                tvProgress.text = getTime(position)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.album_activity_preview
    }

    override fun onBackPressed() {
        val previewInfo = mPreviewInfo?.copy()
        previewInfo?.let {
            it.selected = cbSelected.isChecked
            val intent = Intent().run {
                putExtra(INTENT_PREVIEW, it)
            }
            setResult(RESULT_OK, intent)
        }
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (mIsInitVideo && null != video) {
            mLastPlayProgress = video.currentPosition
            pauseVideo()
        }
    }

    override fun onDestroy() {
        if (mIsInitVideo && null != video) {
            video.stopPlayback()
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mIsInitVideo && mLastPlayProgress > 0) {
            if (null != video) {
                onSeekTo(mLastPlayProgress)
                mLastPlayProgress = -1
                playVideo()
            }
        }
    }

}