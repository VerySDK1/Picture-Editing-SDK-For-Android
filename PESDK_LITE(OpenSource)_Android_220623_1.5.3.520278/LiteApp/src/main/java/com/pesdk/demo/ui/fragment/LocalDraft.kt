package com.pesdk.demo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.pesdk.api.ActivityResultContract.EditDraftResultContract
import com.pesdk.api.IVirtualImageInfo
import com.pesdk.api.SdkEntry
import com.pesdk.demo.R
import com.pesdk.demo.ui.OnDraftListener
import com.pesdk.demo.ui.SelectDialog
import com.pesdk.demo.ui.adapter.LocalDraftAdapter
import com.pesdk.demo.ui.viewmodel.LocalDraftViewModel
import com.pesdk.uisdk.base.BaseFragment
import com.vecore.VirtualImage
import com.vecore.listener.ExportListener
import com.vecore.utils.UriUtils
import kotlinx.android.synthetic.main.app_stub_draft_none.*
import kotlinx.android.synthetic.main.fragment_local_draft_layout.*

/**
 *本地草稿
 */
class LocalDraft : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): LocalDraft {
            return LocalDraft()
        }

    }

    private lateinit var mLocalDraftAdapter: LocalDraftAdapter

    /**
     * 没有媒体
     */
    private var mNotMedia: View? = null
    private var mNullPrompt: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnDraftListener

    }

    override fun initView(view: View?) {

    }

    private val mLocalViewModel by lazy {
        ViewModelProvider(this, AndroidViewModelFactory(requireActivity().application)).get(
            LocalDraftViewModel::class.java
        )
    }

    lateinit var mDraftActivityResult: ActivityResultLauncher<IVirtualImageInfo>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDraftActivityResult = registerForActivityResult(EditDraftResultContract()) {
            it?.let {
                onToastExport(it)
            }
        }

        mLocalViewModel.liveData.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    draftNull()
                }
                mLocalDraftAdapter.update(it)
            } ?: kotlin.run {
                draftNull()
                mLocalDraftAdapter.update(arrayListOf())
            }
        }
        rvDarft.layoutManager = GridLayoutManager(context, 2)
        mLocalDraftAdapter = LocalDraftAdapter(Glide.with(this))
        mLocalDraftAdapter.setOnItemClickListener { position, item ->
            mLocalViewModel.setCurrent(item as IVirtualImageInfo)
            showMenuDialog()
        }
        rvDarft.adapter = mLocalDraftAdapter
        mNullPrompt = getString(R.string.app_draft_none)
        mLocalViewModel.load()
    }


    /**
     * 草稿为null
     */
    private fun draftNull() {
        if (mNotMedia == null) {
            mNotMedia = draftNone.inflate()
        } else {
            mNotMedia?.visibility = VISIBLE
        }
        tvPrompt?.text = mNullPrompt
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_local_draft_layout
    }

    /**
     * listener
     */
    private var mListener: OnDraftListener? = null

    /**
     * 菜单选择
     */
    private var mSelectDialog: SelectDialog? = null

    /**
     * 弹窗
     */
    private fun showMenuDialog() {
        val content = requireContext()
        if (mSelectDialog?.isShowing == true) {
            return
        }
        //构建返回选项
        val optionList: java.util.ArrayList<SelectDialog.SelectOption> =
            java.util.ArrayList<SelectDialog.SelectOption>()

        //导出、编辑、重命名、删除
        val export = ContextCompat.getDrawable(content, R.drawable.flow_ic_home_export)
        export?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            optionList.add(SelectDialog.SelectOption(getString(R.string.flow_menu_export), this))
        }
        val edit = ContextCompat.getDrawable(content, R.drawable.flow_ic_home_go)
        edit?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            optionList.add(SelectDialog.SelectOption(getString(R.string.flow_menu_edit), this))
        }
        val delete = ContextCompat.getDrawable(content, R.drawable.flow_ic_home_delete)
        delete?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            optionList.add(SelectDialog.SelectOption(getString(R.string.flow_menu_delete), this))
        }

        //菜单
        mSelectDialog = SelectDialog.Builder(content)
            .setOption(optionList)
            .setCancelable(true)
            .setCanceledOnTouchOutside(true)
            .setListener(object : SelectDialog.OnClickSelectListener {
                override fun onSelect(index: Int) {
                    mSelectDialog?.dismiss()
                    when (index) {
                        0 -> { //导出
                            mLocalViewModel.currentDraft.value?.let { it ->
                                mPath =
                                    SdkEntry.onExportDraft(context, it, object : ExportListener {
                                        override fun onExportStart() {
                                            showLoading()
                                        }

                                        override fun onExporting(progress: Int, max: Int): Boolean {
                                            return true
                                        }

                                        override fun onExportEnd(
                                            result: Int,
                                            extra: Int,
                                            info: String?
                                        ) {
                                            dialog?.dismiss()
                                            if (result >= VirtualImage.RESULT_SUCCESS) {
                                                mPath?.let {
                                                    onToastExport(it)
                                                }
                                            }
                                        }
                                    })
                            }
                        }
                        1 -> {
                            //编辑
                            mLocalViewModel.currentDraft.value?.let {
                                mDraftActivityResult.launch(it)
                            }
                        }
                        2 -> {  //删除
                            mLocalViewModel.currentDraft.value?.let {
                                mLocalViewModel.deleteDraft(it)
                                mLocalViewModel.setCurrent(null)
                            }
                        }
                    }
                }
            })
            .create()
        mSelectDialog?.show()
    }

    var mPath: String? = null

    fun onToastExport(string: String) {
        val path = UriUtils.getAbsolutePath(context, string)
        Toast.makeText(requireContext(), "保存到:$path", Toast.LENGTH_SHORT)
    }

    fun showLoading() {
        dialog = context?.let { getLoadingDialog(it, getString(R.string.pesdk_saving_image)) }
        dialog!!.setCancelable(false)
        dialog!!.show()
    }


    private var dialog: android.app.ProgressDialog? = null

    private fun getLoadingDialog(context: Context, title: String): android.app.ProgressDialog? {
        val dialog = android.app.ProgressDialog(context)
        dialog.setCancelable(true)
        dialog.setMessage(title)
        return dialog
    }


}