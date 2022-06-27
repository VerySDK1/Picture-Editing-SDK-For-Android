package com.pesdk.demo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.bumptech.glide.Glide
import com.pesdk.album.api.AlbumSdkInit
import com.pesdk.api.ChangeLanguageHelper
import com.pesdk.api.SdkEntry
import com.pesdk.demo.helper.AlbumCustomizeCallBack
import com.pesdk.demo.helper.CameraCustomizeCallBack
import com.pesdk.demo.helper.PEExportCallBack
import com.vesdk.camera.entry.CameraSdkInit

class AppImp : Application() {

    companion object {
        private const val APP_KEY = "cf8f5c4b1f3f045d"
        private const val APP_SECRET = "6f0a8aabf09210fe9369549ede760b9dvJ98dUclBlBWLd2cayyuurgXlQLp0j1kYnrtacjWQlSUYUyUYVgPHdf6cOPUWHnYXX9WkoBURwrZ6kW+Fzfct3i6BtRA11/vWOK/hCT5y8mHXKJnTyM/dQNX7zDzZX3IZDTuNzY5Axevjtn6vBqoWL3YQwQlTWwwXSC3Ooh9FBXIYbfJRUdgPeXjxEKYkHSvmaPbJFdNTlFooe/jX3N4JTtpJDUgxOMg1omeEjQpoCoi3nxRxU1GbfZU1tCHWFAZJ2PD7zWl9ZLTX3RFxxMcBSk4pCo8P7BFH3DA4dSxPMZf6u06Xaddr12RaE+QVcv+W7kqEFoheCQJ2PLX/kYe5O22hma5wy3cDXuCz3d+OLztHTsBXyI+9CmTPOBFtTplYeGRN8ALV4EqI5/Vabq5P0Fw8iPbsqxuc+aUuYy6haAcUJ/kOuD7n6anKhsZQ3c/r7K/HoHCn5O0J5/7BLk0EhV5PHWqQkS1XzvJkwdGpec="
        const val SPACE_NAME = "pe"
    }

    override fun onCreate() {
        super.onCreate()
        initializeSdk()
    }

    override fun attachBaseContext(base: Context) {
        //7.0 以上，处理初始化时切换语言环境
        super.attachBaseContext(ChangeLanguageHelper.attachBaseContext(base, ChangeLanguageHelper.getAppLanguage(base)))
        MultiDex.install(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory()
        }
        Glide.get(this).trimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }

    /**
     * 初始化
     */
    private fun initializeSdk() {
        //图库
        AlbumSdkInit.init(this, APP_KEY, APP_SECRET, AlbumCustomizeCallBack())
        //相机
        CameraSdkInit.init(this, APP_KEY, APP_SECRET, "", CameraCustomizeCallBack())
        try {//PESDK
            SdkEntry.initialize(this, APP_KEY, APP_SECRET, null, PEExportCallBack())
        } catch (ex: IllegalAccessError) {
            ex.printStackTrace()
        } catch (ex: Exception) {
        }

    }

}
