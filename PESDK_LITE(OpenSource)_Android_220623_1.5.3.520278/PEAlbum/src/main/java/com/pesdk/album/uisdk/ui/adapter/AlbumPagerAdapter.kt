package com.pesdk.album.uisdk.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_DEFAULT
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_IMAGE_ONLY
import com.pesdk.album.api.AlbumConfig.Companion.ALBUM_SUPPORT_VIDEO_ONLY
import com.pesdk.album.uisdk.listener.OnGalleryListener
import com.pesdk.album.uisdk.ui.fragment.GalleryItemFragment

class AlbumPagerAdapter(fm: FragmentManager, type: Int, listener: OnGalleryListener) :
    FragmentPagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

    private val fragmentList = mutableListOf<GalleryItemFragment>()

    init {
        when (type) {
            ALBUM_SUPPORT_DEFAULT -> {
                val allItemFragment: GalleryItemFragment =
                    GalleryItemFragment.newInstance(ALBUM_SUPPORT_DEFAULT)
                val videoItemFragment: GalleryItemFragment =
                    GalleryItemFragment.newInstance(ALBUM_SUPPORT_VIDEO_ONLY)
                val photoItemFragment: GalleryItemFragment =
                    GalleryItemFragment.newInstance(ALBUM_SUPPORT_IMAGE_ONLY)
                allItemFragment.setListener(listener)
                videoItemFragment.setListener(listener)
                photoItemFragment.setListener(listener)
                fragmentList.add(allItemFragment)
                fragmentList.add(videoItemFragment)
                fragmentList.add(photoItemFragment)
            }
            ALBUM_SUPPORT_VIDEO_ONLY -> {
                val videoItemFragment: GalleryItemFragment =
                    GalleryItemFragment.newInstance(ALBUM_SUPPORT_VIDEO_ONLY)
                videoItemFragment.setListener(listener)
                fragmentList.add(videoItemFragment)
            }
            ALBUM_SUPPORT_IMAGE_ONLY -> {
                val photoItemFragment: GalleryItemFragment =
                    GalleryItemFragment.newInstance(ALBUM_SUPPORT_IMAGE_ONLY)
                photoItemFragment.setListener(listener)
                fragmentList.add(photoItemFragment)
            }
        }
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    /**
     * 刷新
     */
    fun freshMedia(type: Int, id: String = "0") {
        when (type) {
            ALBUM_SUPPORT_DEFAULT -> {
                fragmentList[0].freshMedia(id)
            }
            ALBUM_SUPPORT_VIDEO_ONLY -> {
                fragmentList[if (count > 1) 1 else 0].freshMedia(id)
            }
            ALBUM_SUPPORT_IMAGE_ONLY -> {
                fragmentList[if (count > 1) 2 else 0].freshMedia(id)
            }
        }
    }

}