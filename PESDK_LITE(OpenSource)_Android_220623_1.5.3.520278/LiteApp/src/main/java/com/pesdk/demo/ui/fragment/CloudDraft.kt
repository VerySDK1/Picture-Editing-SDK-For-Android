package com.pesdk.demo.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.pesdk.demo.R
import com.pesdk.demo.ui.adapter.LocalDraftAdapter
import com.pesdk.uisdk.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_local_draft_layout.*

/**
 *云草稿
 */
class CloudDraft : BaseFragment() {


    companion object {
        @JvmStatic
        fun newInstance(): CloudDraft {
            return CloudDraft()
        }

    }

    private lateinit var mLocalDraftAdapter: LocalDraftAdapter

    override fun initView(view: View?) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvDarft.layoutManager = GridLayoutManager(context, 2)
        mLocalDraftAdapter = LocalDraftAdapter(Glide.with(this))
        rvDarft.adapter = mLocalDraftAdapter
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_local_draft_layout
    }
}