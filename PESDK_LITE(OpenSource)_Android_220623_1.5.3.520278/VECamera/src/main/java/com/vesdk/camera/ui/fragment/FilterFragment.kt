package com.vesdk.camera.ui.fragment

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.vecore.base.lib.utils.FileUtils
import com.vecore.models.VisualFilterConfig.Pixelate
import com.vesdk.camera.R
import com.vesdk.camera.bean.FilterInfo
import com.vesdk.camera.bean.Sort
import com.vesdk.camera.entry.CameraSdkInit
import com.vesdk.camera.listener.OnFilterItemListener
import com.vesdk.camera.listener.OnRecorderMenuLevelTwoListener
import com.vesdk.camera.ui.adapter.FilterPagerAdapter
import com.vesdk.camera.ui.adapter.TitleAdapter
import com.vesdk.camera.utils.CameraPathUtils
import com.vesdk.camera.viewmodel.CameraViewModel
import com.vesdk.camera.viewmodel.FilterViewModel
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.helper.init
import kotlinx.android.synthetic.main.camera_fragment_filter.*
import org.json.JSONObject
import java.io.File
import java.lang.RuntimeException

/**
 * 拍摄界面
 */
class FilterFragment : BaseFragment(), OnFilterItemListener {

    companion object {
        @JvmStatic
        fun newInstance() = FilterFragment()
    }

    private val mViewModel by lazy { ViewModelProvider(requireActivity()).get(FilterViewModel::class.java) }

    /**
     * listener
     */
    private lateinit var mListener: OnRecorderMenuLevelTwoListener

    /**
     * ViewModel
     */
    private val mCameraViewModel by lazy { ViewModelProvider(requireActivity()).get(CameraViewModel::class.java) }

    /**
     * 标题
     */
    private lateinit var mTitleAdapter: TitleAdapter

    /**
     * 数据
     */
    private var mPagerAdapter: FilterPagerAdapter? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnRecorderMenuLevelTwoListener
    }

    override fun init() {

        initView()

        initSort()

        //viewModel
        mViewModel.sortLiveData.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { elements ->
                //获取
                val list = mutableListOf<Sort>()
                list.addAll(elements)
                mViewModel.sortList.clear()
                mViewModel.sortList.addAll(list)
                //pager
                initPager(list)
                customLoading.visibility = View.GONE
            } ?: kotlin.run {
                if (TextUtils.isEmpty(CameraSdkInit.getCameraConfig().baseUrl)) {
                    customLoading.loadError("Url is null")
                } else {
                    customLoading.loadError(result.exceptionOrNull().toString())
                }
            }
        }

        //选中滤镜
        mViewModel.configLiveData.observe(viewLifecycleOwner) {
            if (mViewModel.isExitFilter()) {
                mCameraViewModel.setFilter(mViewModel.config)
                ivNone.setImageResource(R.drawable.camera_ic_none_n)
                ivNone.isEnabled = true
                seekbar.isEnabled = true
            } else {
                mCameraViewModel.setFilter(null)
                ivNone.setImageResource(R.drawable.camera_ic_none_p)
                ivNone.isEnabled = false
                seekbar.isEnabled = false
                mPagerAdapter?.setChecked(Sort(), -1)
//                mTitleAdapter.setCheck(-1)
            }
        }

        context?.let {
            var color = ContextCompat.getColor(it, R.color.white)
            seekbar.setBgColor(ContextCompat.getColor(it, R.color.transparent_white50))
            seekbar.setProgressColor(color)
            seekbar.setShadowColor(ContextCompat.getColor(it, R.color.transparent_black50))
            seekbar.setThumb(R.drawable.pesdk_config_sbar_thumb_n)
            seekbar.setTextColor(color)
            seekbar.postInvalidate()
        }

        //刷新
        mViewModel.freshFilterSort()
    }

    private fun initView() {
        customLoading.setHideCancel(true)
        customLoading.setListener(object :
                com.pesdk.widget.loading.CustomLoadingView.OnCustomLoadingListener {

            override fun reloadLoading(): Boolean {
                //刷新
                mViewModel.freshFilterSort()
                return true
            }

            override fun onCancel() {

            }

        })

        //轴
        mViewModel.setDefaultValue(1f)
        seekbar.visibility = View.VISIBLE
        seekbar.progress = 100
        seekbar.isEnabled = false
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mViewModel.setDefaultValue(progress / (seekBar.max + 0.0f))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    /**
     * 初始化列表
     */
    private fun initSort() {
        mTitleAdapter = TitleAdapter(mViewModel.sortList)
        rvSort.init(mTitleAdapter, requireContext(), LinearLayoutManager.HORIZONTAL)
        mTitleAdapter.setOnItemClickListener { _, _, position ->
            mTitleAdapter.setCheck(position)
            viewpager.setCurrentItem(position, true)
        }
        //无
        ivNone.setOnClickListener {
            mViewModel.setFilterPath("")
        }
    }

    /**
     * 分类
     */
    private fun initPager(sortList: List<Sort>) {
        mPagerAdapter = FilterPagerAdapter(childFragmentManager, lifecycle, sortList, this)
        viewpager.adapter = mPagerAdapter
        viewpager.offscreenPageLimit = sortList.size
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                mTitleAdapter.setCheck(position)
                rvSort.scrollToPosition(position)
            }

        })
    }

    override fun getLayoutId(): Int {
        return R.layout.camera_fragment_filter
    }

    override fun onBackPressed(): Int {
        mListener.hide()
        return -1
    }

    override fun onFilter(sort: Sort, filterInfo: FilterInfo, position: Int) {
        if (filterInfo.url.contains("zip")) { //config.json
            var target: String = CameraPathUtils.getFilterDirPath(filterInfo.url)
            target = com.vecore.base.lib.utils.FileUtils.unzip(filterInfo.localPath, target)
            applyZipFilter(target)
        } else {
            mViewModel.setFilterPath(filterInfo.localPath)
            //设置选中
            mPagerAdapter?.setChecked(sort, position)
        }
    }

    /**
     * zip格式的滤镜
     */
    private fun applyZipFilter(dir: String) {
        try {
            //读取 config.json
            var content: String = FileUtils.readTxtFile(File(dir, "config.json"))
            if (TextUtils.isEmpty(content)) {
                content = FileUtils.readTxtFile(File(dir, ".json"))
            }
            if (TextUtils.isEmpty(content)) {
                return
            }
            val jsonObject = JSONObject(content)
            if ("MosaicPixel" == jsonObject.optString("builtIn")) {
                val strip = jsonObject.optBoolean("strip")
                var tmpLookup = Pixelate(strip)
                tmpLookup.filterFilePath = dir
                mViewModel.setFilter(tmpLookup);
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }
}
