package com.pesdk.album.uisdk.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager.*
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pesdk.album.R
import com.pesdk.album.api.AlbumConfig
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_DEFAULT
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_IMAGE_ONLY
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_VIDEO_ONLY
import com.pesdk.album.api.AlbumContracts.Companion.INTENT_MEDIA_LIST
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.album.api.AlbumTemplateContract.Companion.INTENT_IMAGE_LIST
import com.pesdk.album.api.PreviewContracts
import com.pesdk.album.api.bean.MediaInfo
import com.pesdk.album.api.bean.MediaType
import com.pesdk.album.api.bean.PreviewInfo
import com.pesdk.album.uisdk.bean.MediaDirectory
import com.pesdk.album.uisdk.helper.RecyclerItemTouchHelper
import com.pesdk.album.uisdk.helper.SdkHelper
import com.pesdk.album.uisdk.listener.OnAlbumListener
import com.pesdk.album.uisdk.listener.OnRecyclerMoveListener
import com.pesdk.album.uisdk.ui.adapter.DirectoryAdapter
import com.pesdk.album.uisdk.ui.adapter.MediaCheckedTemplateAdapter
import com.pesdk.album.uisdk.ui.adapter.SelectedAdapter
import com.pesdk.album.uisdk.ui.fragment.GalleryFragment
import com.pesdk.album.uisdk.ui.fragment.MaterialFragment
import com.pesdk.album.uisdk.viewmodel.AlbumViewModel
import com.pesdk.album.uisdk.widget.RecyclerViewCornerRadius
import com.pesdk.bean.template.RReplaceMedia
import com.vecore.base.lib.utils.CoreUtils
import com.vecore.base.lib.utils.StatusBarUtil
import com.vecore.models.PEImageObject
import com.vesdk.common.base.BaseActivity
import com.vesdk.common.bean.Permission
import com.vesdk.common.listener.PopupDialogListener
import com.vesdk.common.ui.dialog.OptionsDialog
import com.vesdk.common.utils.CommonUtils
import kotlinx.android.synthetic.main.album_activity_album.*

/**
 * 相册
 */
class AlbumActivity : BaseActivity(), OnAlbumListener {

    companion object {

        /**
         * 不支持的视频格式
         */
        val UN_SUPPORT_VIDEO = mutableListOf(".wmv")

        /**
         * 只扫描 >1.5秒的视频
         */
        const val MIN_GALLERY_VIDEO_DURATION = 1500

        /**
         * 替换列表
         */
        const val PARAM_REPLACE_LIST = "_replaceList"


        /**
         * 普通
         */
        @JvmStatic
        fun newInstance(context: Context): Intent {
            return Intent(context, AlbumActivity::class.java)
        }

        /**
         * 剪同款选择文件
         */
        @JvmStatic
        fun createAlbumIntent(context: Context, list: ArrayList<RReplaceMedia>?): Intent {
            val intent = Intent(context, AlbumActivity::class.java)
            intent.putExtra(PARAM_REPLACE_LIST, list)
            return intent
        }

    }

    /**
     * 预览
     */
    private lateinit var mPreviewContract: ActivityResultLauncher<PreviewInfo>

    /**
     * 相机
     */
    private var mCameraContracts: ActivityResultLauncher<Void>? = null


    /**
     * ViewModel
     */
    private val mViewModel by lazy { ViewModelProvider(this).get(AlbumViewModel::class.java) }

    /**
     * 配置
     */
    private val mAlbumConfig = AlbumSdkInit.getAlbumConfig()


    /**
     * 当前所在的界面类型
     */
    private var mCurrentFragmentType = ALBUM_SUPPORT_DEFAULT

    /**
     * 素材库
     */
    private var mMaterialFragment: MaterialFragment? = null

    /**
     * 相册
     */
    private lateinit var mGalleryFragment: GalleryFragment

    /**
     * 目录名称
     */
    private val mDirectoryName = mutableListOf<String?>()

    /**
     * 剪同款
     */
    private var mTemplateMediaInfoList: MutableList<RReplaceMedia>? = null


    /**
     * 普通选中
     */
    private var mSelectedAdapter: SelectedAdapter? = null

    /**
     * 剪同款选中
     */
    private var mMediaCheckedTemplateAdapter: MediaCheckedTemplateAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.setImmersiveStatusBar(this, true)
        super.onCreate(savedInstanceState)
    }

    /**
     * 跳转
     */
    override fun initRegisterContract() {
        super.initRegisterContract()
        //预览
        mPreviewContract = registerForActivityResult(PreviewContracts()) {
            it?.let {
                if (it.oldSelected && !it.selected) {
                    mViewModel.deleteSelectMedia(it.mediaInfo)
                } else if (!it.oldSelected && it.selected) {
                    mViewModel.addSelectMedia(it.mediaInfo)
                }
            }
        }

        //相机
        SdkHelper.getCameraContracts()?.apply {
            mCameraContracts = registerForActivityResult(this) { it ->
                it?.let { pathList ->
                    if (pathList.size > 0) {
                        mViewModel.selectedDir.value?.let {
                            mGalleryFragment.refresh(it.type, it.id)
                        } ?: let {
                            mGalleryFragment.refresh(ALBUM_SUPPORT_IMAGE_ONLY)
                        }
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
        //修改/删除SD卡中的内容
        CommonUtils.getPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)?.let {
            it.icon = R.drawable.common_ic_permission_storage
            permissions.add(it)
        }
        CommonUtils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)?.let {
            it.icon = R.drawable.common_ic_permission_storage
            permissions.add(it)
        }
        return permissions
    }

    /**
     * 初始化
     */
    override fun init() {
        //模板
        mTemplateMediaInfoList = intent.getParcelableArrayListExtra(PARAM_REPLACE_LIST)

        //控件
        initView()

        //目录列表
        initDirectory()

        //fragment
        initFragment()

        //选中
        initSelectMedia()

        //监听
        initViewModel()
    }

    /**
     * 初始化控件
     */
    private fun initView() {
        // 返回
        btnClose.setOnClickListener { onBackPressed() }

        // 下一步
        btnNext.setOnClickListener { onNextStep() }

        // 相机
        btnCamera.setOnClickListener { onCamera() }

        // 菜单
        //目录
        btnDirectory.setOnClickListener { directoryUI() }
        ivArrow.setOnClickListener { directoryUI() }
        rlDirectory.setOnClickListener { directoryUI() }

        //素材库
        btnMaterialLibrary.setOnClickListener { onMaterialLibrary() }

        //配置控制显示与隐藏
        btnCamera.visibility = if (mAlbumConfig.hideCamera) INVISIBLE else VISIBLE
        llMaterial.visibility = if (mAlbumConfig.hideMaterial) GONE else VISIBLE

        rvCheckedMedia.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    /**
     * 初始化
     */
    private fun initFragment() {
        //素材库
        if (!mAlbumConfig.hideMaterial) {
            mMaterialFragment = MaterialFragment.newInstance()
            mMaterialFragment?.setListener(this)
        }

        //列表
        mGalleryFragment = GalleryFragment.newInstance()
        //菜单布局
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentAlbum, mGalleryFragment)
        ft.commitAllowingStateLoss()

        //优先选中
        if (mAlbumConfig.firstShow == AlbumConfig.FIRST_MATERIAL) {
            onMaterialLibrary()
        }
    }

    /**
     * 选中媒体
     */
    private fun initSelectMedia() {
        if (mTemplateMediaInfoList == null || mTemplateMediaInfoList.isNullOrEmpty()) {
            //只选中一张时不显示下一步
            rlAlbumBottomBar.visibility = if (mAlbumConfig.limitMaxNum == 1
                    || (mAlbumConfig.limitMediaNum + mAlbumConfig.limitVideoNum + mAlbumConfig.limitImageNum) == 1
            ) GONE else VISIBLE
            //选中
            initSelected()
            mViewModel.setTemplate(false)
        } else {
            mViewModel.setTemplate()
            tvMediaHint.visibility = GONE
            rlAlbumBottomBar.visibility = VISIBLE
            mMediaCheckedTemplateAdapter = MediaCheckedTemplateAdapter(mTemplateMediaInfoList!!)
            rvCheckedMedia.adapter = mMediaCheckedTemplateAdapter
            mMediaCheckedTemplateAdapter?.let { adapter ->
                adapter.addChildClickViewIds(R.id.iv_delete)
                adapter.setOnItemChildClickListener { _, _, position ->
                    adapter.update(null, position)
                    mViewModel.deleteSelectMedia(null)
                }
            }
        }
    }

    /**
     * 目录列表
     */
    private fun initDirectory() {
        mDirectoryName.add(getString(R.string.album_camera_roll))
        mDirectoryName.add(getString(R.string.album_all_video))
        mDirectoryName.add(getString(R.string.album_all_photo))
        //列表
        rvDirectory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val directoryAdapter = DirectoryAdapter(mViewModel.getDirectoryList())
        rvDirectory.adapter = directoryAdapter
        val radiusItemDecoration = RecyclerViewCornerRadius(rvDirectory)
        radiusItemDecoration.setCornerRadius(
                0, 0,
                CoreUtils.dip2px(this, 15f),
                CoreUtils.dip2px(this, 15f)
        )
        rvDirectory.addItemDecoration(radiusItemDecoration)
        directoryAdapter.setOnItemClickListener { _, _, position ->
            //关闭
            directoryUI()
            //显示名字
            val item = directoryAdapter.getItem(position)
            btnDirectory.text = item.name
            when (mCurrentFragmentType) {
                ALBUM_SUPPORT_DEFAULT -> mDirectoryName[0] = item.name
                ALBUM_SUPPORT_VIDEO_ONLY -> mDirectoryName[1] = item.name
                ALBUM_SUPPORT_IMAGE_ONLY -> mDirectoryName[2] = item.name
                else -> mDirectoryName[0] = item.name
            }
            //刷新目录
            mViewModel.freshSelectedDirectory(MediaDirectory(mCurrentFragmentType, item.id))
        }

        //监听变化
        mViewModel.directoryListData.observe(this) { result ->
            for (info in result) {
                if (info.id == "0") {
                    when (mCurrentFragmentType) {
                        ALBUM_SUPPORT_DEFAULT -> info.name = getString(R.string.album_camera_roll)
                        ALBUM_SUPPORT_VIDEO_ONLY -> info.name = getString(R.string.album_all_video)
                        ALBUM_SUPPORT_IMAGE_ONLY -> info.name = getString(R.string.album_all_video)
                        else -> info.name = getString(R.string.album_camera_roll)
                    }
                    break
                }
            }
            mViewModel.setDirectoryList(result)
            val name = when (mCurrentFragmentType) {
                ALBUM_SUPPORT_DEFAULT -> mDirectoryName[0]
                ALBUM_SUPPORT_VIDEO_ONLY -> mDirectoryName[1]
                ALBUM_SUPPORT_IMAGE_ONLY -> mDirectoryName[2]
                else -> mDirectoryName[0]
            }
            directoryAdapter.setCheck(name)

            ivArrow.setImageResource(R.drawable.album_ic_arrow_up)
            rlDirectory.visibility = View.VISIBLE
            cameraUI(false)
            val aniSlideIn = AnimationUtils.loadAnimation(this, R.anim.album_top_in)
            rvDirectory.startAnimation(aniSlideIn)

        }
    }

    /**
     * 选中媒体
     */
    private fun initSelected() {
        mSelectedAdapter = SelectedAdapter(mViewModel.getSelectedList()).apply {
            rvCheckedMedia.adapter = this
            //点击
            addChildClickViewIds(R.id.ivDelete)
            setOnItemClickListener { _, _, position ->
                //预览
                onEdit(getItem(position), true)
            }
            setOnItemChildClickListener { _, _, position ->
                //删除
                mViewModel.deleteSelectMedia(getItem(position))
            }
            val helper = ItemTouchHelper(
                    RecyclerItemTouchHelper(
                            false,
                            object : OnRecyclerMoveListener {

                                //拖拽
                                private var drag = false

                                override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
                                    mViewModel.swapSelectedList(fromPosition, toPosition)
                                    mSelectedAdapter?.notifyItemMoved(fromPosition, toPosition)
                                    return true
                                }

                                override fun onItemRemove(position: Int): Boolean {
                                    return mSelectedAdapter?.let {
                                        mViewModel.deleteSelectMedia(it.getItem(position))
                                        true
                                    } ?: kotlin.run { false }
                                }

                                override val isLongPressDragEnabled = true

                                override val isItemViewSwipeEnabled = true

                                @SuppressLint("NotifyDataSetChanged")
                                override fun clearView(viewHolder: RecyclerView.ViewHolder?) {
                                    viewHolder?.let {
                                        mSelectedAdapter?.setDrag(viewHolder as BaseViewHolder, false)
                                    }
                                    if (drag) {
                                        //拖拽 刷新
                                        mSelectedAdapter?.notifyDataSetChanged()
                                        drag = false
                                    }
                                }

                                override fun onSelectedChanged(
                                        viewHolder: RecyclerView.ViewHolder?,
                                        actionState: Int
                                ) {
                                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                                        drag = true
                                        viewHolder?.let {
                                            mSelectedAdapter?.setDrag(viewHolder as BaseViewHolder, true)
                                        }
                                    }
                                }

                            })
            )
            helper.attachToRecyclerView(rvCheckedMedia)
        }
    }

    /**
     *
     */
    private fun initViewModel() {
        //监听
        mViewModel.selectedLiveData.observe(this) {
            mMediaCheckedTemplateAdapter?.let { adapter ->
                it?.let { path ->
                    if ("" != path) {
                        if (adapter.getNum() < mTemplateMediaInfoList?.size ?: 0) {
                            val mediaObject = PEImageObject(path)
                            val update = adapter.update(mediaObject)
                            if (update >= 0) {
                                rvCheckedMedia.scrollToPosition(update)
                            }
                        }
                    }
                    btnNext.isEnabled = adapter.getNum() >= mTemplateMediaInfoList?.size ?: 0
                }
            } ?: kotlin.run {
                changeMedia(it)
            }
        }
    }

    /**
     * 初始化权限成功
     */
    override fun permissionsSuccess() {
        init()
    }


    /**
     * 照相机显示、隐藏
     */
    private fun cameraUI(show: Boolean) {
        if (!show) {
            btnCamera.visibility = View.INVISIBLE
        } else {
            btnCamera.visibility = View.VISIBLE
        }
    }

    /**
     * 目录菜单
     */
    private fun directoryUI() {
        if (fragmentMaterial.visibility == View.VISIBLE) {
            fragmentMaterial.visibility = View.GONE
            return
        }
        val show = rlDirectory.visibility == View.GONE
        if (show) {
            //获取数据
            mViewModel.freshDirectory(mCurrentFragmentType)
        } else {
            val aniSlideOut = AnimationUtils.loadAnimation(this, R.anim.album_top_out)
            aniSlideOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    rlDirectory.visibility = View.GONE
                    if (!mAlbumConfig.hideCamera) {
                        cameraUI(true)
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            rvDirectory.startAnimation(aniSlideOut)
            ivArrow.setImageResource(R.drawable.album_ic_arrow_down)
        }
    }

    /**
     * 媒体数量改变
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun changeMedia(path: String?) {
        val list = mViewModel.getSelectedList()
        //只选中一张
        if (list.size == 1
                && (mAlbumConfig.limitMaxNum == 1
                        || (mAlbumConfig.limitMediaNum + mAlbumConfig.limitVideoNum + mAlbumConfig.limitImageNum) == 1)
        ) {
            //下一步
            onNextStep()
        } else {
            btnNext.isEnabled = list.size > 0
            tvMediaHint.visibility = if (btnNext.isEnabled) GONE else VISIBLE
            //刷新
            mSelectedAdapter?.notifyDataSetChanged()
            //移动到最后一个位置
            if ("" != path) {
                rvCheckedMedia.smoothScrollToPosition(list.size - 1)
            }
            //数量
            var text = 0
            var image = 0
            var video = 0
            for (info in list) {
                when (info.type) {
                    MediaType.TYPE_WORD -> {
                        text++
                    }
                    MediaType.TYPE_VIDEO -> {
                        video++
                    }
                    else -> {
                        image++
                    }
                }
            }

            if (mAlbumConfig.limitMaxNum > 0 || mAlbumConfig.limitMinNum > 0) {
                val totalColor =
                        if (list.size >= mAlbumConfig.limitMinNum && mAlbumConfig.limitMaxNum >= list.size) "#000000" else "#ff0000"
                val html =
                        "${getString(R.string.album_video)}:${video}&nbsp; " +
                                "${getString(R.string.album_photo)}:${image}&nbsp; " +
                                "${getString(R.string.album_all)}:<font color='${totalColor}'>${list.size}</font>/" +
                                "(${mAlbumConfig.limitMinNum}-${mAlbumConfig.limitMaxNum})"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvImportNum.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    tvImportNum.text = Html.fromHtml(html)
                }
            } else if (mAlbumConfig.limitMediaNum > 0 || mAlbumConfig.limitVideoNum > 0 || mAlbumConfig.limitImageNum > 0) {
                val totalNum =
                        mAlbumConfig.limitMediaNum + mAlbumConfig.limitVideoNum + mAlbumConfig.limitImageNum
                val videoColor =
                        if (video >= mAlbumConfig.limitVideoNum) "#000000" else "#ff0000"
                val imageColor =
                        if (image >= mAlbumConfig.limitImageNum) "#000000" else "#ff0000"
                val totalColor =
                        if (totalNum == list.size) "#000000" else "#ff0000"
                val html =
                        "${getString(R.string.album_video)}:<font color='${videoColor}'>${video}</font>/${mAlbumConfig.limitVideoNum}&nbsp; " +
                                "${getString(R.string.album_photo)}:<font color='${imageColor}'>${image}</font>/${mAlbumConfig.limitImageNum}&nbsp; " +
                                "${getString(R.string.album_all)}:<font color='${totalColor}'>${list.size}</font>/${totalNum}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvImportNum.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    tvImportNum.text = Html.fromHtml(html)
                }
            } else {
                val buffer = StringBuffer()
                if (video > 0) {
                    buffer.append(getString(R.string.album_video))
                    buffer.append(":")
                    buffer.append(video)
                }
                if (image > 0) {
                    buffer.append("  ")
                    buffer.append(getString(R.string.album_photo))
                    buffer.append(":")
                    buffer.append(image)
                }
                if (text > 0) {
                    buffer.append("  ")
                    buffer.append(getString(R.string.album_text))
                    buffer.append(":")
                    buffer.append(text)
                }
                tvImportNum.text = buffer.toString()
            }
        }
    }


    /**
     * 素材库
     */
    private fun onMaterialLibrary() {
        if (fragmentMaterial.visibility == View.VISIBLE) {
            return
        }
        mMaterialFragment?.let { fragment ->
            //显示素材库
            fragmentMaterial.visibility = View.VISIBLE
            val manager = supportFragmentManager
            if (!fragment.isAdded) {
                manager.beginTransaction().remove(fragment).commitAllowingStateLoss()
                manager.beginTransaction()
                        .add(R.id.fragmentMaterial, fragment)
                        .show(fragment)
                        .commitAllowingStateLoss()
            } else {
                manager.beginTransaction().show(fragment).commitAllowingStateLoss()
            }
        }
    }

    /**
     * 相机
     */
    private fun onCamera() {
        mCameraContracts?.launch(null)
    }


    /**
     * 模板下一步
     */
    private fun onTemplateNext() {
        mMediaCheckedTemplateAdapter?.let { adapter ->
            mTemplateMediaInfoList?.let { list ->
                if (list.size <= adapter.getNum()) {
                    val mediaList = adapter.data
                    val srcList = arrayListOf<PEImageObject>()
                    for (info in mediaList) {
                        srcList.add(info.mediaObject)
                    }
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    intent.putParcelableArrayListExtra(INTENT_IMAGE_LIST, srcList)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    /**
     * 下一步
     */
    private fun onNextStep() {
        if (null != mMediaCheckedTemplateAdapter) {
            onTemplateNext()
            return
        }

        val selectedList = mViewModel.getSelectedList()
        val selectedSize = selectedList.size
        if (selectedSize <= 0) {
            return
        }
        val result = if (mAlbumConfig.limitMaxNum > 0 || mAlbumConfig.limitMinNum > 0) {
            if (selectedSize >= mAlbumConfig.limitMinNum && selectedSize <= mAlbumConfig.limitMaxNum) {
                val pathList = arrayListOf<String>()
                for (info in selectedList) {
                    pathList.add(info.path)
                }
                pathList
            } else {
                null
            }
        } else if (mAlbumConfig.limitMediaNum > 0 || mAlbumConfig.limitVideoNum > 0 || mAlbumConfig.limitImageNum > 0) {
            //数量
            var image = 0
            var video = 0
            for (info in selectedList) {
                when (info.type) {
                    MediaType.TYPE_VIDEO -> {
                        video++
                    }
                    else -> {
                        image++
                    }
                }
            }
            if (mAlbumConfig.limitVideoNum > video || mAlbumConfig.limitImageNum > image) {
                null
            } else if ((mAlbumConfig.limitMediaNum + mAlbumConfig.limitVideoNum + mAlbumConfig.limitImageNum) != (video + image)) {
                null
            } else {
                val pathList = arrayListOf<String>()
                for (info in selectedList) {
                    pathList.add(info.path)
                }
                pathList
            }
        } else {
            val pathList = arrayListOf<String>()
            for (info in selectedList) {
                pathList.add(info.path)
            }
            pathList
        }

        //返回
        if (result != null) {
            val intent = Intent().apply {
                putStringArrayListExtra(INTENT_MEDIA_LIST, result)
            }
            setResult(RESULT_OK, intent)
            finish()
        } else {
            onToast(R.string.album_error_media_num)
        }
    }


    /**
     * 返回弹窗
     */
    private fun onQuitAlert() {
        OptionsDialog
                .create(this, getString(R.string.album_exit), getString(R.string.album_give_up),
                        cancelable = true, cancelTouch = true, listener = object : PopupDialogListener {

                    override fun onDialogSure() {
                        finish()
                    }

                    override fun onDialogCancel() {

                    }

                })
                .show()
    }


    /**
     * 预览
     */
    override fun onEdit(mediaInfo: MediaInfo, selected: Boolean) {
        if (!mAlbumConfig.hideEdit) {
            mPreviewContract.launch(PreviewInfo(mediaInfo, selected, selected))
        }
    }

    /**
     * 目录显示
     */
    override fun selectMenu(type: Int) {
        mCurrentFragmentType = type
        when (mCurrentFragmentType) {
            ALBUM_SUPPORT_DEFAULT -> {
                btnDirectory.text = mDirectoryName[0]
            }
            ALBUM_SUPPORT_VIDEO_ONLY -> {
                btnDirectory.text = mDirectoryName[1]
            }
            ALBUM_SUPPORT_IMAGE_ONLY -> {
                btnDirectory.text = mDirectoryName[2]
            }
        }
        //隐藏素材库
        fragmentMaterial.visibility = View.GONE
    }

    /**
     * 布局
     */
    override fun getLayoutId(): Int {
        return R.layout.album_activity_album
    }

    /**
     * 返回
     */
    override fun onBackPressed() {
        //目录打开 关闭
        if (rlDirectory.visibility == View.VISIBLE) {
            directoryUI()
            return
        }

        //素材库
        if (fragmentMaterial.visibility == View.VISIBLE) {
            fragmentMaterial.visibility = View.GONE
            return
        }

        //选中是否放弃
        if (mViewModel.getSelectedList().size > 0) {
            onQuitAlert()
            return
        }

        //关闭
        finish()
    }

}