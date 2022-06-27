package com.pesdk.album.uisdk.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pesdk.album.uisdk.bean.Sort
import com.pesdk.album.uisdk.listener.OnMaterialAddListener
import com.pesdk.album.uisdk.ui.fragment.MaterialItemFragment

class MaterialPagerAdapter(fm: FragmentManager, sortList: List<Sort>, listener: OnMaterialAddListener) : FragmentPagerAdapter(
    fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    private val fragmentList = mutableListOf<MaterialItemFragment>()

    init {
        for (sort in sortList) {
            val element = MaterialItemFragment.newInstance(sort)
            element.setListener(listener)
            fragmentList.add(element)
        }
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

}