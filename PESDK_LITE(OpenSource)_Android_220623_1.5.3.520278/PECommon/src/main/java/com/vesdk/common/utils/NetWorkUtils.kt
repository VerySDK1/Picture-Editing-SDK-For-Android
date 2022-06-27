package com.vesdk.common.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * 联网判断
 */
object NetWorkUtils {

    /**
     * 判断网络状态，有网络返回true
     */
    @JvmStatic
    fun isConnected(context: Context?): Boolean {
        context?.let {
            return isNetworkConnected(context) || isWifiConnected(context)
        }
        return true
    }

    /**
     * 判断手机是否有网络连接
     */
    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            network?.let {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                    || networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                ) {
                    return true
                }
            }
        } else {
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            if (networkInfo != null) {
                return networkInfo.isAvailable
            }
        }
        return false
    }

    /**
     * 判断wifi网络是否可用
     */
    @JvmStatic
    fun isWifiConnected(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            network?.let {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    return true
                }
            }
        } else {
            val mConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (networkInfo != null) {
                return networkInfo.isAvailable
            }
        }
        return false
    }

}