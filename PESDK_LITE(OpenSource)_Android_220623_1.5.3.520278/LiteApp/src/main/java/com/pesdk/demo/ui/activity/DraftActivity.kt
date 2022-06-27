package com.pesdk.demo.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.pesdk.demo.R
import com.pesdk.demo.ui.OnDraftListener
import com.pesdk.demo.ui.fragment.CloudDraft
import com.pesdk.demo.ui.fragment.LocalDraft
import com.pesdk.uisdk.base.BaseActivity
import com.pesdk.uisdk.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_draft_layout.*
import kotlinx.android.synthetic.main.activity_draft_layout.btnBack

/**
 * 图片编辑草稿
 */
class DraftActivity : BaseActivity(), OnDraftListener {

    private val list = arrayOf("本地草稿")

    //    private val list = arrayOf("本地草稿", "云草稿")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_layout)
        btnBack.setOnClickListener {
            onBackPressed()
        }

        vpData.adapter = Adapter(this, list)
        TabLayoutMediator(tabLayout, vpData, false, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.text = list[position]
        }).attach()
    }

    inner class Adapter(activity: FragmentActivity, private var arr: Array<String>) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return arr.size
        }

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                LocalDraft.newInstance()
            } else {
                CloudDraft.newInstance()
            }
        }

    }

    override fun onCancel(fragment: BaseFragment) {
        TODO("Not yet implemented")
    }

    override fun onClickFlow(id: Long) {
        TODO("Not yet implemented")
    }

    override fun onClickBackup() {
        TODO("Not yet implemented")
    }

    override fun onClickExport() {
        TODO("Not yet implemented")
    }


}