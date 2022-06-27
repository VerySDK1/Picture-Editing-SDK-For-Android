package com.vesdk.camera.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vesdk.camera.bean.Sort
import com.vesdk.camera.listener.OnFilterItemListener
import com.vesdk.camera.ui.fragment.FilterItemFragment

class FilterPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    sortList: List<Sort>,
    itemListener: OnFilterItemListener
) : FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = mutableListOf<FilterItemFragment>()

    init {
        for (sort in sortList) {
            val element = FilterItemFragment.newInstance(sort)
            element.setListener(itemListener)
            fragmentList.add(element)
        }
    }

    /**
     * 设置选中
     */
    fun setChecked(sort: Sort, position: Int) {
        for (fragment in fragmentList) {
            fragment.setChecked(sort, position)
        }
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}