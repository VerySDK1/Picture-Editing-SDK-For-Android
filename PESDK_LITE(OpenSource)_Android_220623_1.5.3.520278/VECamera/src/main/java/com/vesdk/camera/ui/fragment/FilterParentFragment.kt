package com.vesdk.camera.ui.fragment

import android.content.Context
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vesdk.camera.R
import com.vesdk.camera.listener.OnRecorderMenuLevelTwoListener
import com.vesdk.camera.viewmodel.ToneViewModel
import com.vesdk.common.base.BaseFragment
import kotlinx.android.synthetic.main.camera_filter_parent_layout.*
import kotlinx.android.synthetic.main.camera_filter_parent_layout.ivSure

/**
 *
 */
class FilterParentFragment : BaseFragment() {
    /**
     * listener
     */
    private lateinit var mListener: OnRecorderMenuLevelTwoListener
    private val mToneViewModel by lazy { ViewModelProvider(requireActivity()).get(ToneViewModel::class.java) }

    companion object {
        @JvmStatic
        fun newInstance() = FilterParentFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnRecorderMenuLevelTwoListener
    }

    override fun init() {
        vpager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    FilterFragment.newInstance()
                } else {
                    ToneFragment.newInstance()
                }
            }
        }

        vpager2.isUserInputEnabled = false
        rgFilter.setOnCheckedChangeListener(fun(group: RadioGroup, checkedId: Int) {
            if (checkedId == R.id.rbFilter) {
                vpager2.setCurrentItem(0, false)
                ivReset.visibility = View.INVISIBLE
            } else {
                vpager2.setCurrentItem(1, false)
                ivReset.visibility = View.VISIBLE
            }
        })

        ivReset.setOnClickListener {
            mToneViewModel.resetParam()
        }
        mToneViewModel.getToneResetEnableLiveData().observe(viewLifecycleOwner) {
            ivReset.isEnabled = it
        }

        //确定
        ivSure.setOnClickListener { onBackPressed() }
    }


    override fun onBackPressed(): Int {
        mListener.hide()
        return -1
    }

    override fun getLayoutId(): Int {
        return R.layout.camera_filter_parent_layout
    }
}